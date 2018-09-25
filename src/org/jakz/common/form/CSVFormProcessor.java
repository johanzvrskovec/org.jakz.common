package org.jakz.common.form;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jakz.common.form.Form.FieldType;

public class CSVFormProcessor 
{
	
	public static CSVFormat csvFormatCSVSTD = CSVFormat.DEFAULT.withAllowMissingColumnNames(false);
	public static CSVFormat csvFormatCSVIMPROVED = CSVFormat.DEFAULT.withDelimiter(';').withAllowMissingColumnNames(false);
	public static CSVFormat csvFormatTSV = CSVFormat.DEFAULT.withDelimiter('\t').withAllowMissingColumnNames(false);
	
	public boolean settingSkipEmptyRows = true;
	public boolean settingSkipBlankRows = true;
	public boolean settingBlankCharactersNull = false;
	public boolean settingSkipVariableOnError = true;
	public boolean settingSkipRowOnError = true;
	public boolean settingSkipUnmappedColumns = true;
	
	
	protected InputStream sourceIS;
	protected Form targetForm, templateQuery;
	protected CSVFormat formatToUse;
	
	
	public CSVFormProcessor() 
	{
		formatToUse=csvFormatCSVSTD;
	}
	
	public CSVFormProcessor setSource(InputStream nSource)
	{
		sourceIS=nSource;
		return this;
	}
	
	public CSVFormProcessor setTargetForm(Form nTarget)
	{
		targetForm=nTarget;
		return this;
	}
	
	public CSVFormProcessor setTemplateQuery(Form nTemplate)
	{
		templateQuery=nTemplate;
		return this;
	}
	
	public CSVFormProcessor setFormat(CSVFormat nFormat)
	{
		formatToUse=nFormat;
		return this;
	}
	
	public CSVFormProcessor populateFormDataFromFile(boolean firstRowNames, Charset charset, ArrayList<String> messageList) throws IOException, FormException
	{
		boolean hasPopulationErrors = false;
		
		if(targetForm==null)
			targetForm=new Form("CSVForm");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(sourceIS,charset));
		CSVParser parser = formatToUse.parse(reader);
		Iterator<CSVRecord> rowIt = parser.iterator();
		Iterator<String> cellIt;
		
		ArrayList<String> fileColumns = new ArrayList<String>();
		
		CSVRecord currentRow=null;
		
		boolean rowIsEmpty;
		boolean rowIsBlank;
		
		//names
		if(firstRowNames && rowIt.hasNext())
		{
			currentRow = rowIt.next();
			cellIt = currentRow.iterator();
			
			while(cellIt.hasNext())
			{
				String fileColumnName = cellIt.next().trim();
				fileColumns.add(fileColumnName);
			}
		}
		
		Form errVar = null;
		for(int iRow=0; rowIt.hasNext(); iRow++)
		{
			Form q = null;
			if(templateQuery!=null)
			{
				q=templateQuery.createNewDcopy();
				q.id=""+iRow;
			}
			else
				q = new Form(""+iRow,FieldType.QRY);
			
			currentRow = rowIt.next();
			cellIt = currentRow.iterator();
			rowIsEmpty=true;
			rowIsBlank=true;

			for(int iCell=0; cellIt.hasNext(); iCell++)
			{
				
				String fileColumnName = fileColumns.get(iCell);
				String cellContent = cellIt.next();
				
				//alternative columns pre work
				String altColumnMaster = null;
				String alternativeString = null;
				boolean isOther = false;
				if(fileColumnName.indexOf("_other")>=0)
				{
					isOther=true;
					String alt = fileColumnName.substring(0,fileColumnName.lastIndexOf("_other"));
					altColumnMaster = alt.substring(0,alt.lastIndexOf('_'));
					alternativeString = fileColumnName.substring(alt.lastIndexOf('_')+1,fileColumnName.lastIndexOf("_other")).trim();
				}
				else if(fileColumnName.indexOf('_')>=0)
				{
					altColumnMaster = fileColumnName.substring(0,fileColumnName.lastIndexOf('_'));
					alternativeString = fileColumnName.substring(fileColumnName.lastIndexOf('_')+1).trim();
				}
				
				boolean isAlternative = altColumnMaster!=null &&templateQuery!=null && templateQuery.content.containsKey(altColumnMaster);
				
				if(
						settingSkipUnmappedColumns&&templateQuery!=null&&!templateQuery.content.containsKey(fileColumnName)
							&& !isAlternative //alternative variable
					)
					continue;
				
				if(cellContent.length()>0)
					rowIsEmpty=false;
				
				if(cellContent.trim().length()>0)
					rowIsBlank=false;
				
				
				try
				{
					
					if(q.content.containsKey(fileColumnName))
					{
						Form newVar =q.content.getValue(fileColumnName);
						errVar=newVar;
						parseFormValue(newVar,cellContent);
					}
					else if(templateQuery!=null&&templateQuery.content.containsKey(fileColumnName))
					{
						Form newVar = templateQuery.content.getValue(fileColumnName).createNewDcopy();
						errVar=newVar;
						parseFormValue(newVar,cellContent);
						q.add(newVar);
					}
					else if(isAlternative && (q.content.containsKey(altColumnMaster) || (templateQuery!=null && templateQuery.content.containsKey(altColumnMaster))))
					{
						Form masterVar = null;
						
						//create master if not present
						if(!q.content.containsKey(altColumnMaster)&&templateQuery.content.containsKey(altColumnMaster))
						{
							masterVar = templateQuery.content.getValue(altColumnMaster).createNewDcopy();
							q.add(masterVar);
						}
						else
						{
							masterVar = q.content.getValue(altColumnMaster);
						}
						
						
						Form newAlt =null;
						if(masterVar.getHasContent()&&masterVar.content.containsKey(alternativeString))
						{
							newAlt = masterVar.content.getValue(alternativeString);
							errVar=newAlt;
							
							if(isOther&&newAlt.alternativeHasOtherField)
								newAlt.setOtherValue(cellContent);
							else
								parseFormValue(newAlt,cellContent);
						}
						else throw new FormException("Unknown alternative detected.");
						
					}
					else if(!settingSkipUnmappedColumns)
					{
						Form newVar = new Form(fileColumnName,FieldType.VAR);
						newVar.setValue(Types.NVARCHAR);
						errVar=newVar;
						parseFormValue(newVar,cellContent);
						q.add(newVar);
					}
				}
				catch (Exception e)
				{
					hasPopulationErrors=true;
					errVar.errorFlag=true;
					String s = "Syntax error for variable "+fileColumnName+" ("+ iCell+","+iRow+"). Cell content ["+cellContent+"].";
					if(errVar.getValue()!=null&&errVar.getValue().getType()!=null)
						s=s+" Data type="+errVar.getValue().getTypeString()+", Nullable="+errVar.nullable+", Length="+errVar.getValue().getSizeLimit();
					
					messageList.add(s);
					errVar.errorMessage=s;
				}
			}
			
			if(
					(rowIsEmpty&&(settingSkipEmptyRows||settingSkipBlankRows))
					|| 
					(rowIsBlank&&settingSkipBlankRows)
				)
			{
				messageList.add("Skipping row index "+iRow+" (blank or empty)");
				continue;
			}
			
			targetForm.add(q);
		}
		
		if(hasPopulationErrors)
			throw new FormException("There were errors when populationg the form - see the error list");
		
		return this;
	}
	
	public Form parseFormValue(Form targetAndTemplate, String stringToParse) throws FormException
	{
		
		if(targetAndTemplate.getValueType()==java.sql.Types.INTEGER)
		{
			if(stringToParse.trim().length()==0)
				targetAndTemplate.setValueInteger(null);
			else
				targetAndTemplate.setValueInteger(Integer.parseInt(stringToParse.trim()));
		}
		else if(targetAndTemplate.getValueType()==java.sql.Types.DOUBLE)
		{
			if(stringToParse.trim().length()==0)
				targetAndTemplate.setValueDouble(null);
			else
				targetAndTemplate.setValueDouble(Double.parseDouble(stringToParse.trim().replace(',', '.'))); //works with either commas or dots.
		}
		else if(targetAndTemplate.getValueType()==java.sql.Types.BOOLEAN)
		{
			if(stringToParse.trim().length()==0)
				targetAndTemplate.setValueBoolean(null);
			else
				targetAndTemplate.setValueBoolean(Boolean.parseBoolean(stringToParse.trim()));
		}
		else if(targetAndTemplate.getValueType()==java.sql.Types.VARCHAR)
		{
			if(settingBlankCharactersNull && stringToParse.trim().length()==0)
				targetAndTemplate.setValueVarchar(null);
			else
				targetAndTemplate.setValueVarchar(stringToParse);
		}
		else if(targetAndTemplate.getValueType()==java.sql.Types.NVARCHAR)
		{
			if(settingBlankCharactersNull && stringToParse.trim().length()==0)
				targetAndTemplate.setValueNvarchar(null);
			else
				targetAndTemplate.setValueNvarchar(stringToParse);
		}
		else if(targetAndTemplate.getValueType()==java.sql.Types.TIMESTAMP)
		{	
			if(stringToParse.trim().length()==0)
				targetAndTemplate.setValueTimestamp(null);
			else if(targetAndTemplate.valueParseFormat!=null)
			{
				SimpleDateFormat sdf = new SimpleDateFormat(targetAndTemplate.valueParseFormat);
				java.util.Date parsedDate=null;
				try 
				{
					parsedDate = sdf.parse(stringToParse.trim());
				} catch (ParseException e) 
				{
					throw new FormException("Error when parsing timestamp "+stringToParse+" from specified format "+targetAndTemplate.valueParseFormat);
				}
				
				targetAndTemplate.setValueTimestamp(parsedDate.getTime());
			}
			else
			{
			
				targetAndTemplate.setValueTimestamp(Long.parseLong(stringToParse.trim()));
			}
		}
		else if(targetAndTemplate.getValueType()==java.sql.Types.BIGINT)
		{
			if(stringToParse.trim().length()==0)
				targetAndTemplate.setValueBigint(null);
			else
				targetAndTemplate.setValueBigint(Long.parseLong(stringToParse.trim()));
		}
		else
			throw new FormException("SQL type unknown");
		
		return targetAndTemplate;
	}
}
