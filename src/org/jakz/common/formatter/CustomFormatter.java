package org.jakz.common.formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jakz.common.JSONArray;
import org.jakz.common.JSONObject;
import org.jakz.common.ApplicationException;
import org.jakz.common.DataCache;
import org.jakz.common.IndexedMap;
import org.jakz.common.DataEntry;
import org.jakz.common.Util;


public class CustomFormatter
{
	
	public static enum IOType {DATACACHE,EXCEL,CSV,TSV};
	public static CSVFormat csvFormatCSVSTD = CSVFormat.DEFAULT.withAllowMissingColumnNames(false);
	public static CSVFormat csvFormatCSVIMPROVED = CSVFormat.DEFAULT.withDelimiter(';').withAllowMissingColumnNames(false);
	public static CSVFormat csvFormatTSV = CSVFormat.DEFAULT.withDelimiter('\t').withAllowMissingColumnNames(false);
	
	
	//private OutputStream output;
	//private InputStream input;
	private String settingPath;
	private File settingInputFile, settingOutputFile;
	private DataCache dataCache;
	private IndexedMap<String,XSSFCellStyle> excelStyle;
	private boolean settingFirstRowVariableNames;
	private boolean settingOverwriteExistingTables;
	private boolean settingExcelAppend, settingNiceExcelColumns;
	private boolean settingOutputSkipEmptyColumns;
	private int settingTypeGuessingRows;
	
	private IOType inputType,outputType;
	
	public CustomFormatter() 
	{
		settingPath=null;
		settingInputFile=null; settingOutputFile=null;
		inputType=IOType.EXCEL; outputType=IOType.DATACACHE;
		settingFirstRowVariableNames=false;
		settingOverwriteExistingTables=false;
		settingExcelAppend=true;
		settingNiceExcelColumns=false;
		settingOutputSkipEmptyColumns=false;
		excelStyle=new IndexedMap<String, XSSFCellStyle>();
		settingTypeGuessingRows=10;
	}
	
	public CustomFormatter setInputType(IOType nInputType)
	{
		inputType=nInputType;
		return this;
	}
	
	public CustomFormatter setOutputType(IOType nOutputType)
	{
		outputType=nOutputType;
		return this;
	}
	
	public CustomFormatter setInputFile(File nFile)
	{
		/*
		if(inputType!=InputOutputType.EXCEL)
			throw new ApplicationException("Wrong input for the configured input type. The input type is "+inputType.toString()+" and an attempt was made to set an InputFile.");
			*/
		settingInputFile = nFile;
		return this;
	}
	
	public CustomFormatter setOutputFile(File nFile)
	{
		/*
		if(outputType!=InputOutputType.EXCEL)
			throw new ApplicationException("Wrong output for the configured output type. The output type is "+outputType.toString()+" and an attempt was made to set an OutputFile.");
			*/
		settingOutputFile = nFile;
		return this;
	}
	
	public CustomFormatter setDataCache(DataCache nDataCache)
	{
		dataCache = nDataCache;
		return this;
	}
	
	
	public CustomFormatter setExcelStyle(IndexedMap<String,XSSFCellStyle> nExcelStyle)
	{
		excelStyle = nExcelStyle;
		return this;
	}
	
	
	public CustomFormatter setOverwriteExistingTables(boolean overwriteExistingTables)
	{
		settingOverwriteExistingTables=overwriteExistingTables;
		return this;
	}
	
	public CustomFormatter setFirstRowVariableNames(boolean firstRowVariableNames)
	{
		settingFirstRowVariableNames=firstRowVariableNames;
		return this;
	}
	
	public CustomFormatter setExcelAppend(boolean excelAppend)
	{
		settingExcelAppend=excelAppend;
		return this;
	}
	
	public CustomFormatter setNiceExcelColumns(boolean niceExcelColumns)
	{
		settingNiceExcelColumns=niceExcelColumns;
		return this;
	}
	
	public CustomFormatter setOutputSkipEmptyColumns(boolean outputSkipEmptyColumns)
	{
		settingOutputSkipEmptyColumns=outputSkipEmptyColumns;
		return this;
	}
	
	public CustomFormatter setPath(String nPath)
	{
		settingPath=nPath;
		return this;
	}
	
	public CustomFormatter read(DataEntry nEntry) throws InvalidFormatException, IOException, ApplicationException, SQLException 
	{
		if(nEntry==null)
			nEntry=dataCache.newEntry(settingPath);
		else if(nEntry.path==null)
			nEntry.path=settingPath;
		
		
		if(inputType==IOType.EXCEL)
		{
			readExcel(nEntry);
			return this;
		}
		else if (inputType==IOType.CSV||inputType==IOType.TSV)
		{
			CSVFormat formatToUse;
			if(inputType==IOType.TSV)
				formatToUse=csvFormatTSV;
			else
				formatToUse=csvFormatCSVIMPROVED;
				
			readCSV(nEntry,formatToUse);
			return this;
		}
		else if(inputType==IOType.DATACACHE)
		{
			readDatacacheRows(nEntry);
			return this;
		}
		
		throw new ApplicationException("Input read format is unsupported by the formatter");
	}
	
	public CustomFormatter read() throws InvalidFormatException, IOException, ApplicationException, SQLException 
	{
		return read(null);
	}
	
	
	public CustomFormatter write(DataEntry nEntry) throws InvalidFormatException, IOException, ApplicationException, SQLException
	{	
		
		if(nEntry==null)
			nEntry=dataCache.newEntry(settingPath);
		else if(nEntry.path==null)
			nEntry.path=settingPath;
		
		if(outputType==IOType.EXCEL)
		{
			writeExcel(settingExcelAppend,nEntry);
			return this;
		}
		else if(outputType==IOType.CSV||outputType==IOType.TSV)
		{
			CSVFormat formatToUse;
			if(inputType==IOType.TSV)
				formatToUse=csvFormatTSV;
			else
				formatToUse=csvFormatCSVIMPROVED;
			
			writeCSV(formatToUse,nEntry);
			return this;
		}
		else if(outputType==IOType.DATACACHE)
		{
			writeDatacache(nEntry);
			return this;
		}
		else throw new ApplicationException("Output write format ("+outputType+") is unsupported by the formatter");
	}
	
	private void readExcel(DataEntry entryTemplate) throws InvalidFormatException, IOException, ApplicationException, SQLException
	{
		int rowBufferSize = 100000;
		@SuppressWarnings("resource")
		XSSFWorkbook currentWorkbook = new XSSFWorkbook(settingInputFile);
		XSSFSheet currentSheet;
		Row currentRow;
		Cell currentCell;
		
		for(int iSheet = 0; iSheet<currentWorkbook.getNumberOfSheets(); iSheet++)
		{
			
			Iterator<Row> rowIt;
			Iterator<Cell> cellIt;
			JSONObject rowToAdd;
			JSONObject variableValues;
			currentSheet = currentWorkbook.getSheetAt(iSheet);
			
			
			HashSet<String> typeset = new HashSet<String>();
			JSONArray elementNameArray = new JSONArray();
			if(entryTemplate.getPath()==null)
				entryTemplate.path=currentSheet.getSheetName().replace('.', '_');
			
			
			if(dataCache.getHasTable(entryTemplate.path))
			{
				if(settingOverwriteExistingTables)
				{
					dataCache.dropTable(entryTemplate.path);
				}
			}
			
			JSONArray rowBuffer=new JSONArray();
			//columnIndexVariableNameMap = new HashMap<Integer, String>();
			//columnIndexVariableTypeMap = new HashMap<Integer, Integer>();

			rowIt = currentSheet.rowIterator();
			//set variable names and number of variables
			if(settingFirstRowVariableNames && rowIt.hasNext())
			{
				currentRow = rowIt.next();
				cellIt = currentRow.cellIterator();
				while(cellIt.hasNext())
				{
					currentCell = cellIt.next();
					if(currentCell.getCellType()!=Cell.CELL_TYPE_STRING)
						throw new ApplicationException("Excel error at row number "+currentRow.getRowNum()+" and column index "+currentCell.getColumnIndex());
					
					
					if(!entryTemplate.namemap.containsKey(currentCell.getStringCellValue().toUpperCase()))  //upper case names
					{
						JSONObject element = new JSONObject();
						//element.put("name", currentCell.getStringCellValue());
						//element.put("index", currentCell.getColumnIndex());
						entryTemplate.namemap.put(currentCell.getStringCellValue(),element,currentCell.getColumnIndex());
						elementNameArray.put(currentCell.getStringCellValue().toUpperCase());
					}
				}
			}
			
			//set variable type from the first element that contains data
			while(rowIt.hasNext())
			{
				currentRow = rowIt.next();
				cellIt = currentRow.cellIterator();
				while(cellIt.hasNext())
				{
					currentCell = cellIt.next();
					if(currentCell.getCellType()!=Cell.CELL_TYPE_ERROR && currentCell.getCellType()!=Cell.CELL_TYPE_BLANK)
					{
						String name = entryTemplate.namemap.getKeyAt(currentCell.getColumnIndex());
						JSONObject element = entryTemplate.namemap.getValue(name);
						short typeToPut;
						
						if(currentCell.getCellType()==Cell.CELL_TYPE_BOOLEAN || (currentCell.getCellType()==Cell.CELL_TYPE_FORMULA && currentCell.getCachedFormulaResultType()==Cell.CELL_TYPE_BOOLEAN))
						{
							typeToPut= java.sql.Types.BOOLEAN;
						}
						else if(currentCell.getCellType()==Cell.CELL_TYPE_NUMERIC || (currentCell.getCellType()==Cell.CELL_TYPE_FORMULA && currentCell.getCachedFormulaResultType()==Cell.CELL_TYPE_NUMERIC))
						{
							DataFormatter formatter = new DataFormatter();
							if (DateUtil.isCellDateFormatted(currentCell))
								typeToPut= java.sql.Types.TIMESTAMP;
							else if(!formatter.formatCellValue(currentCell).contains(".")&&!formatter.formatCellValue(currentCell).contains(","))
								typeToPut= java.sql.Types.INTEGER;
							else
								typeToPut= java.sql.Types.DOUBLE;
						}
						else if(currentCell.getCellType()==Cell.CELL_TYPE_STRING || (currentCell.getCellType()==Cell.CELL_TYPE_FORMULA && currentCell.getCachedFormulaResultType()==Cell.CELL_TYPE_STRING))
						{
							typeToPut= java.sql.Types.VARCHAR;
						}
						else throw new ApplicationException("Column error - the cell is of an incompatible type. At row number "+currentRow.getRowNum()+" and column index "+currentCell.getColumnIndex());
						
						if(!element.has("type"))
							element.put("type", typeToPut);
						typeset.add(name);
					}
				}
				
				if(typeset.size()>=entryTemplate.namemap.size())
					break;
			}
			
			
			//add input order column
			if(!entryTemplate.namemap.containsKey("INPUTID"))
			{
				JSONObject nelement = new JSONObject();
				nelement.put("name", "INPUTID"); nelement.put("type", java.sql.Types.INTEGER);
				entryTemplate.namemap.put("INPUTID", nelement,0);
			}
			
			
			//restart
			rowIt = currentSheet.rowIterator();
			if(settingFirstRowVariableNames)
				currentRow = rowIt.next();
			
			
			//convert the rest of the data
			for(int iRow=0; rowIt.hasNext(); iRow++)
			{
				currentRow = rowIt.next();
				rowToAdd=new JSONObject();
				//rowToAdd.put("path", inputFile.getName()+"_"+currentSheet.getSheetName());
				rowToAdd.put("path", entryTemplate.path);
				variableValues = new JSONObject();
				
				//adding input order
				JSONObject varValueIO = new JSONObject();
				varValueIO.put("value", iRow);
				varValueIO.put("type", java.sql.Types.INTEGER);
				variableValues.put("INPUTID", varValueIO);
				
				
				cellIt = currentRow.cellIterator();
				while(cellIt.hasNext())
				{
					currentCell = cellIt.next();
					int currentCellType = currentCell.getCellType();
					
					//if(!(currentCellType==Cell.CELL_TYPE_BOOLEAN||currentCellType==Cell.CELL_TYPE_NUMERIC||currentCellType==Cell.CELL_TYPE_STRING))
					if(currentCellType==Cell.CELL_TYPE_ERROR)
						throw new ApplicationException("Excel error at row number "+currentRow.getRowNum()+" and column index "+currentCell.getColumnIndex()+". The cell contains an error or is of an incompatible type.");
					
					String name = entryTemplate.namemap .getKeyAt(currentCell.getColumnIndex());
					JSONObject element =  entryTemplate.namemap.getValue(name);
					
					JSONObject variableValueToAdd = new JSONObject();
					
					int type = element.getInt("type");
					
					//variableValueToAdd.put("name", name);
					
					if(type==java.sql.Types.BOOLEAN)
					{
						variableValueToAdd.put("value", currentCell.getBooleanCellValue());
					}
					else if(type==java.sql.Types.TIMESTAMP)
					{
						variableValueToAdd.put("value", currentCell.getDateCellValue().getTime());
					}
					else if(type==java.sql.Types.BIGINT)
					{
						variableValueToAdd.put("value", (long)currentCell.getNumericCellValue());
					}
					else if(type==java.sql.Types.INTEGER)
					{
						variableValueToAdd.put("value", (int)currentCell.getNumericCellValue());
					}
					else if(type==java.sql.Types.DOUBLE)
					{
						variableValueToAdd.put("value", currentCell.getNumericCellValue());
					}
					else if(type==java.sql.Types.VARCHAR)
					{
						variableValueToAdd.put("value", currentCell.getStringCellValue());
					}
					else throw new ApplicationException("Column error - the cell is of an incompatible type. At row number "+currentRow.getRowNum()+" and column index "+currentCell.getColumnIndex());
					
					variableValueToAdd.put("type", type);
					variableValues.put(name,variableValueToAdd);
				}
				
				rowToAdd.put("data", variableValues);
				if(rowBuffer.length()<rowBufferSize)
				{
					rowBuffer.put(rowToAdd);
				}
				else
				{
					entryTemplate.rows = rowBuffer;
					if(outputType==IOType.DATACACHE)
					{
						dataCache.enter(entryTemplate);
					}
					rowBuffer=new JSONArray();
				}
			}
			
			if(rowBuffer.length()>0)
			{
				entryTemplate.rows = rowBuffer;
				dataCache.enter(entryTemplate);
			}
		}
		currentWorkbook.close();
	}
	
	
	private void readCSV(DataEntry entryTemplate, CSVFormat format) throws IOException, ApplicationException, SQLException
	{
		int rowBufferSize = 100000;
		BufferedReader reader = new BufferedReader(new FileReader(settingInputFile));
		
		CSVParser parser = format.parse(reader);
		Iterator<CSVRecord> rowIt = parser.iterator();
		Iterator<String> cellIt;
		JSONObject rowToAdd;
		JSONObject variableValues;
		
		ArrayList<String> fileColumns = new ArrayList<String>();
		HashSet<String> typeset = new HashSet<String>();

		if(entryTemplate.path==null)
		{
			entryTemplate.path=settingInputFile.getName();
			int dotIndex = entryTemplate.path.lastIndexOf('.');
			if(dotIndex>=0)
				entryTemplate.path=entryTemplate.path.substring(0,dotIndex).replace('.', '_');
		}
		
		if(dataCache.getHasTable(entryTemplate.path))
		{
			if(settingOverwriteExistingTables)
			{
				dataCache.dropTable(entryTemplate.path);
			}
		}
		
		JSONArray rowBuffer=new JSONArray();
		
		CSVRecord currentRow=null;
		String currentCell;
		
		//set variable names, types and number of variables
		boolean isNameRow =true;
		boolean firstRowPrePass=false;
		int typeguessingrows =0;
		
		while(rowIt.hasNext())
		{	
			if(!firstRowPrePass)
			{
				currentRow = rowIt.next();
			}
			firstRowPrePass=false;
				
			
			cellIt = currentRow.iterator();
			boolean isComment=false;
			for(int iCell=0; cellIt.hasNext(); iCell++)
			{
				currentCell = cellIt.next().toUpperCase();
				
				if(iCell==0&&currentCell.trim().substring(0, 2).contains("##")) //is comment
				{
					isComment=true;
					break;
				}
				
				if(isNameRow)
				{
					if(settingFirstRowVariableNames||(iCell==0&&currentCell.trim().substring(0, 1).contains("#")))
					{
						//is name cell
						if(iCell==0&&currentCell.trim().substring(0, 1).contains("#"))
						{
							currentCell=currentCell.replaceFirst("#", "");
						}
						
						fileColumns.add(currentCell);
						if(!entryTemplate.namemap.containsKey(currentCell))
						{
							JSONObject element = new JSONObject();
							entryTemplate.namemap.put(currentCell,element);
						}
						continue; //cell contains name
					}
					else //if(!settingFirstRowVariableNames)
					{
						//is not name cell
						firstRowPrePass=true;
						if(iCell>=entryTemplate.getNamemapSize())
						{
							JSONObject element = new JSONObject();
							entryTemplate.namemap.put("custom"+iCell,element);
							fileColumns.add("custom"+iCell);
						}
						else
							fileColumns.add(entryTemplate.namemap.getKeyAt(iCell));
					}
				}
				else if(
							currentCell.length()>0&&iCell<fileColumns.size() //is data cell
							&&!(currentCell.contains(".")&&currentCell.length()==1) //special null encoding
						)
				{
					short typeToPut = -1;
					short typesuggestion =-1;
					String name = fileColumns.get(iCell);
					JSONObject element = entryTemplate.namemap.getValue(name);
					if(!element.has("type"))
					{
						if(element.has("typesuggestion"))
							typesuggestion = (short) element.getInt("typesuggestion");
						
						//Can't handle date yet!
						if(
								NumberUtils.isNumber(currentCell)
								&&!(typesuggestion==java.sql.Types.VARCHAR||typesuggestion==java.sql.Types.BOOLEAN)
								)
						{
							if(!currentCell.contains(".")&&!currentCell.toUpperCase().contains("E"))
								typeToPut= java.sql.Types.INTEGER;
							else
								typeToPut= java.sql.Types.DOUBLE;
						}
						else if(
								(currentCell.toUpperCase().equals("TRUE") || currentCell.toUpperCase().equals("FALSE")) 
								&& !(typesuggestion==java.sql.Types.VARCHAR||typesuggestion==java.sql.Types.INTEGER||typesuggestion==java.sql.Types.DOUBLE)
								)
						{
							typeToPut= java.sql.Types.BOOLEAN;
						}
						else
						{
							typeToPut= java.sql.Types.VARCHAR;
						}
						
						element.put("typesuggestion", typeToPut);
						
						if(typeguessingrows>=settingTypeGuessingRows||!rowIt.hasNext())
							element.put("type", typeToPut);
					}
					
					if(element.has("type"))
						typeset.add(name);
				}
				
			}
			
			if(!isComment)
			{
				isNameRow=false;
				if(!isNameRow)
					typeguessingrows++;
			}
			
			if(!isComment&&fileColumns.size()>=currentRow.size()&&typeset.size()>=fileColumns.size())
				break;
		}
		
		//fallback for non specified columns - needed? template columns should contain type
		for(int i=0; typeset.size()<entryTemplate.namemap.size()&&i<entryTemplate.namemap.size(); i++)
		{
			String name = entryTemplate.namemap.getKeyAt(i);
			JSONObject element = entryTemplate.namemap.getValue(name);
			if(!typeset.contains(name)&&!element.has("type"))
			{
				element.put("type", java.sql.Types.VARCHAR);
				typeset.add(name);
			}
		}
		
		//add input order column
		if(!entryTemplate.namemap.containsKey("INPUTID"))
		{
			JSONObject nelement = new JSONObject();
			nelement.put("name", "INPUTID"); nelement.put("type", java.sql.Types.INTEGER);
			entryTemplate.namemap.put("INPUTID", nelement,0);
		}
		
		
		//restart
		parser.close();
		reader = new BufferedReader(new FileReader(settingInputFile));
		parser = format.parse(reader);
		rowIt = parser.iterator();
		
		//convert the rest of the data
		boolean isFirstRow =true;
		for(int iRow=0; rowIt.hasNext(); iRow++)
		{
			currentRow = rowIt.next();
			boolean isComment=false;
			rowToAdd=new JSONObject();
			rowToAdd.put("path", entryTemplate.path);
			variableValues = new JSONObject();
			
			//adding input order
			JSONObject varValueIO = new JSONObject();
			varValueIO.put("value", iRow);
			varValueIO.put("type", java.sql.Types.INTEGER);
			variableValues.put("INPUTID", varValueIO);
				
			cellIt = currentRow.iterator();
			for(int iCell=0; cellIt.hasNext(); iCell++)
			{
				currentCell = cellIt.next();
				
				if(iCell==0&&currentCell.contains("#")) //is comment
				{
					isComment=true;
					break;
				}
				
				if(settingFirstRowVariableNames&&isFirstRow)
					break;
				
				if(currentCell.length()>0
						&&!(currentCell.contains(".")&&currentCell.length()==1)  //special null encoding
						)
				{
					String name = fileColumns.get(iCell);
					JSONObject element =  entryTemplate.namemap.getValue(name);
					
					JSONObject variableValueToAdd = new JSONObject();
					
					int type = element.getInt("type");
					
					if(type==java.sql.Types.INTEGER)
					{
						variableValueToAdd.put("value", Integer.parseInt(currentCell));
					}
					else if(type==java.sql.Types.BIGINT)
					{
						variableValueToAdd.put("value", Long.parseLong(currentCell));
					}
					else if(type==java.sql.Types.DOUBLE)
					{
						variableValueToAdd.put("value", Double.parseDouble(currentCell));
					}
					else if(type==java.sql.Types.BOOLEAN)
					{
						variableValueToAdd.put("value", Boolean.parseBoolean(currentCell));
					}
					else if(type==java.sql.Types.VARCHAR)
					{
						variableValueToAdd.put("value", currentCell);
					}
					else throw new ApplicationException("Column error - the cell is of an incompatible type. Type:"+type+"\n At row number "+(iRow+1)+" and column index "+iCell);
					
					variableValueToAdd.put("type", type);
					variableValues.put(name,variableValueToAdd);
				}
			}
			
			if(!isComment&&isFirstRow&&settingFirstRowVariableNames)
			{
				isFirstRow=false;
			}
			else if(!isComment)
			{
				rowToAdd.put("data", variableValues);
				if(rowBuffer.length()<rowBufferSize)
				{
					rowBuffer.put(rowToAdd);
				}
				else
				{
					entryTemplate.rows=rowBuffer;
					dataCache.enter(entryTemplate);
					rowBuffer=new JSONArray();
				}
			}
		}
		
		if(rowBuffer.length()>0)
		{
			entryTemplate.rows=rowBuffer;
			dataCache.enter(entryTemplate);
		}
	}
	
	private void readDatacacheRows(DataEntry entryTemplate) throws InvalidFormatException, IOException, SQLException, ApplicationException
	{
		byte[] byteContent = Files.readAllBytes(settingInputFile.toPath());
		Charset cs = Charset.forName("UTF-8");
		JSONObject entryJSON = new JSONObject(new String(byteContent,cs));
		entryTemplate.namemap.fromJSONObject(entryJSON.optJSONObject("namemap"));
		entryTemplate.rows=entryJSON.getJSONArray("rows");
		
		if(dataCache.getHasTable(entryTemplate.path))
		{
			if(settingOverwriteExistingTables)
			{
				dataCache.dropTable(entryTemplate.path);
			}
		}
		
		dataCache.enter(entryTemplate);
	}
	
	@SuppressWarnings("unused")
	private void readDatacache() throws InvalidFormatException, IOException, SQLException, ApplicationException
	{
		byte[] byteContent = Files.readAllBytes(settingInputFile.toPath());
		Charset cs = Charset.forName("UTF-8");
		JSONObject entryJSON = new JSONObject(new String(byteContent,cs));
		DataEntry entry = dataCache.newEntry(entryJSON);
		if(dataCache.getHasTable(entry.path))
		{
			if(settingOverwriteExistingTables)
			{
				dataCache.dropTable(entry.path);
			}
		}
		
		dataCache.enter(entry);
	}
	
	boolean skipColumnCheck(JSONObject valueMeta, JSONObject entryTemplate)
	{
		//condition for skipping column for various reasons
		return
				(settingOutputSkipEmptyColumns && valueMeta.has("empty") && valueMeta.getBoolean("empty")==true) //empty column together with skip empty column setting
				||
				(entryTemplate!=null && (entryTemplate.has("hide") && entryTemplate.getBoolean("hide")==true)) //column is marked with hide
			;
	}
	
	
	private void writeExcel(boolean append, DataEntry entryTemplate) throws InvalidFormatException, IOException, ApplicationException, SQLException
	{
		
		
		
		if(settingPath==null)
			throw new ApplicationException("Dataset for reading not named");
		int rowBufferSize = 100000;
		
		
		File f = settingOutputFile.getAbsoluteFile();
		
		XSSFWorkbook currentWorkbook;
		FileInputStream fileIn = null;
		if(append&&f.exists())
		{
			fileIn = new FileInputStream(f);
			currentWorkbook = new XSSFWorkbook(fileIn);
		}
		else
		{	
			Util.deleteFileIfExistsOldCompatSafe(f);
			currentWorkbook = new XSSFWorkbook();
		}
		
		//formulas
		FormulaEvaluator formulaEvaluator = currentWorkbook.getCreationHelper().createFormulaEvaluator();
		
		
		//Styling
		
		//Style inventory
		for(int i=0; i<currentWorkbook.getNumCellStyles();i++)
		{
			if(excelStyle.size()<=i)
			{
				String styleCallsign = "inv"+i;
				XSSFCellStyle newCellStyle;
				newCellStyle = currentWorkbook.getCellStyleAt(i);
				excelStyle.put(styleCallsign,newCellStyle);
			}
		}
		
		XSSFCellStyle style_link, style_fp_numeric;
		//System.out.println("num cell styles>"+currentWorkbook.getNumCellStyles());
		if(excelStyle.containsKey("style_link"))
		{
			int stylei = excelStyle.get("style_link").index;
			//System.out.println("Using existing style at index "+stylei);
			style_link=currentWorkbook.getCellStyleAt(stylei);
			stylei = excelStyle.get("style_fp_numeric").index;
			style_fp_numeric=currentWorkbook.getCellStyleAt(stylei);
		}
		else
		{
			/*
			style_link = currentWorkbook.createCellStyle();
			XSSFFont font = currentWorkbook.createFont();
			XSSFColor color = new XSSFColor(Color.BLUE);
			font.setColor(color);
			font.setUnderline(XSSFFont.U_SINGLE);
			style_link.setFont(font);
			*/
			XSSFFont linkfont = currentWorkbook.createFont();
		    linkfont.setUnderline(XSSFFont.U_SINGLE);
		    linkfont.setColor(IndexedColors.BLUE.getIndex());
		    
			style_link = currentWorkbook.createCellStyle();
		    style_link.setFont(linkfont);
		    //StylesTable stylesSource = currentWorkbook.getStylesSource();
		    //System.out.println("Putting link style at index "+excelStyle.size());
		    excelStyle.put("style_link",style_link);
		
			style_fp_numeric = currentWorkbook.createCellStyle();
			style_fp_numeric.setDataFormat(11); //scientific format
		    excelStyle.put("style_fp_numeric",style_fp_numeric);
		}

			
		
		//style.setAlignment(CellStyle.ALIGN_CENTER);
		//XSSFFont font = currentWorkbook.createFont();
		//font.setUnderline(FontUnderline.SINGLE);
		//XSSFColor color = ;
		//font.setColor(color);
		//style.setFont(font);
		//c.setCellStyle(style);
		
		//CellStyle cellStyleTextWrap = currentWorkbook.createCellStyle();
		//cellStyleTextWrap.setWrapText(true);
		//cellStyleTextWrap.setShrinkToFit(true);
		
		
		//CreationHelper createHelper = currentWorkbook.getCreationHelper();
		XSSFSheet currentSheet = currentWorkbook.createSheet(WorkbookUtil.createSafeSheetName(settingPath));
		DataEntry e = dataCache.get(settingPath);
		Row currentRow;
		//Cell currentCell;
		
		int rownum =0;
		
		//first row names
		currentRow = currentSheet.createRow(rownum++);
		int iCellNonSkipped=0;
		for(int iCell=0; iCell<e.getNamemapSize(); iCell++)
		{
			JSONObject valueMeta = e.getNamemapMetaAt(iCell);
			String cellName = valueMeta.getString("name");
			if(skipColumnCheck(valueMeta,entryTemplate.namemap.getValue(cellName)))
				continue;
			
			currentRow.createCell(iCellNonSkipped++, Cell.CELL_TYPE_STRING).setCellValue(cellName);
		}
		
		while(e.next(rowBufferSize, false))
		{
			for(int iRow=0; iRow<e.getRows().length(); iRow++)
			{
				currentRow = currentSheet.createRow(rownum++);
				JSONObject readRow = e.getRows().getJSONObject(iRow);
				iCellNonSkipped=0;
				for(int iCell=0; iCell<e.getNamemapSize(); iCell++)
				{
					JSONObject valueMeta = e.getNamemapMetaAt(iCell);
					String cellName = valueMeta.getString("name");
					if(skipColumnCheck(valueMeta,entryTemplate.namemap.getValue(cellName)))
						continue;
					
					JSONObject readElement = readRow.getJSONObject(cellName);
					//Set<String> keys = readRow.keySet();
					
					if(!readElement.isNull("value"))
					{
						boolean isExcelFormula = false;
						boolean isHyperlink = false;
						if(entryTemplate!=null&&entryTemplate.namemap.containsKey(cellName))
						{
							JSONObject templateValueMeta = entryTemplate.getNamemapMeta(cellName);
							isExcelFormula = templateValueMeta.has("isExcelFormula")&&templateValueMeta.getBoolean("isExcelFormula");
							isHyperlink = templateValueMeta.has("isHyperlink")&&templateValueMeta.getBoolean("isHyperlink");
						}
						
						
						Cell c;
						if(isExcelFormula)
						{
							c = currentRow.createCell(iCellNonSkipped++, Cell.CELL_TYPE_FORMULA);
							c.setCellFormula(readElement.getString("value"));
							CellValue evalvalue = formulaEvaluator.evaluate(c);
							//int evaltype = formulaEvaluator.evaluateFormulaCell(c);
							switch (evalvalue.getCellType())
							{
								case Cell.CELL_TYPE_BOOLEAN:
								{
									c.setCellValue(evalvalue.getBooleanValue());
								}
								case Cell.CELL_TYPE_NUMERIC:
								{
									c.setCellValue(evalvalue.getNumberValue());
								}
								case Cell.CELL_TYPE_STRING:
								{
									c.setCellValue(evalvalue.getStringValue());
								}
								case Cell.CELL_TYPE_ERROR:
								{
									c.setCellValue(evalvalue.getErrorValue());
								}
							}
							
							//styling
							if(isHyperlink)
								c.setCellStyle(style_link);
						}
						else if(valueMeta.getInt("type")==java.sql.Types.BOOLEAN)
						{
							currentRow.createCell(iCellNonSkipped++, Cell.CELL_TYPE_BOOLEAN).setCellValue(readElement.getBoolean("value"));
						}
						else if(valueMeta.getInt("type")==java.sql.Types.TIMESTAMP)
						{
							c = currentRow.createCell(iCellNonSkipped++, Cell.CELL_TYPE_BOOLEAN);
							c.setCellValue(new Date(readElement.getLong("value")));
						}
						else if(valueMeta.getInt("type")==java.sql.Types.DOUBLE||valueMeta.getInt("type")==java.sql.Types.DECIMAL)
						{
							c = currentRow.createCell(iCellNonSkipped++, Cell.CELL_TYPE_NUMERIC);
							c.setCellValue(readElement.getDouble("value"));
							c.setCellStyle(style_fp_numeric);
						}
						else if(valueMeta.getInt("type")==java.sql.Types.BIGINT||valueMeta.getInt("type")==java.sql.Types.INTEGER||valueMeta.getInt("type")==java.sql.Types.SMALLINT)
						{
							currentRow.createCell(iCellNonSkipped++, Cell.CELL_TYPE_NUMERIC).setCellValue(readElement.getLong("value"));
						}
						else if(valueMeta.getInt("type")==java.sql.Types.VARCHAR)
						{
							c = currentRow.createCell(iCellNonSkipped++, Cell.CELL_TYPE_STRING);
							//cc.setCellStyle(cellStyleTextWrap);
							c.setCellValue(readElement.getString("value"));
						}
						else throw new ApplicationException("Wrong datatype for Excel conversion");
					}
					else
					{
						Cell c =currentRow.createCell(iCellNonSkipped++, Cell.CELL_TYPE_BLANK);
					}
				}
			}
		}
		
		if(settingNiceExcelColumns)
		{
			for(int i=0; i<e.getNamemapSize(); i++)
			{
				currentSheet.autoSizeColumn(i);
			}
		}
		
		if(fileIn!=null)
			fileIn.close();
		FileOutputStream fileOut = new FileOutputStream(f);
		currentWorkbook.write(fileOut);
	    fileOut.close();
		currentWorkbook.close();
	}
	
	
	private void writeCSV(CSVFormat format, DataEntry entryTemplate) throws InvalidFormatException, IOException, ApplicationException, SQLException
	{
		if(settingPath==null)
			throw new ApplicationException("Dataset for reading not named");
		int rowBufferSize = 100000;
		settingOutputFile.createNewFile();
		CSVPrinter printer = format.print(new FileWriter(settingOutputFile));
		ArrayList<String> currentRow;
		
		DataEntry e = dataCache.get(settingPath);
		
		@SuppressWarnings("unused")
		int rownum =0;
		
		//first row names
		currentRow = new ArrayList<String>();
		for(int iCell=0; iCell<e.getNamemapSize(); iCell++)
		{
			JSONObject valueMeta = e.getNamemapMetaAt(iCell);
			String cellName = valueMeta.getString("name");
			if(skipColumnCheck(valueMeta,entryTemplate.namemap.getValue(cellName)))
				continue;
			
			currentRow.add(cellName);
		}
		printer.printRecord(currentRow);
		
		while(e.next(rowBufferSize, false))
		{
			for(int iRow=0; iRow<e.getRows().length(); iRow++)
			{
				currentRow = new ArrayList<String>();
				rownum++;
				JSONObject readRow = e.getRows().getJSONObject(iRow);
				for(int iCell=0; iCell<e.getNamemapSize(); iCell++)
				{
					JSONObject valueMeta = e.getNamemapMetaAt(iCell);
					String cellName = valueMeta.getString("name");
					if(skipColumnCheck(valueMeta,entryTemplate.namemap.getValue(cellName)))
						continue;
					
					JSONObject readElement = readRow.getJSONObject(cellName);
					//Set<String> keys = readRow.keySet();
					
					if(!readElement.isNull("value"))
					{
					
						if(valueMeta.getInt("type")==java.sql.Types.BOOLEAN)
						{
							currentRow.add(((Boolean)readElement.getBoolean("value")).toString());
						}
						else if(valueMeta.getInt("type")==java.sql.Types.TIMESTAMP)
						{
							currentRow.add(new Date(readElement.getLong("value")).toString());
						}
						else if(valueMeta.getInt("type")==java.sql.Types.DOUBLE||valueMeta.getInt("type")==java.sql.Types.DECIMAL)
						{
							currentRow.add(((Double)readElement.getDouble("value")).toString());
						}
						else if(valueMeta.getInt("type")==java.sql.Types.BIGINT||valueMeta.getInt("type")==java.sql.Types.INTEGER||valueMeta.getInt("type")==java.sql.Types.SMALLINT)
						{
							currentRow.add(((Long)readElement.getLong("value")).toString());
						}
						else if(valueMeta.getInt("type")==java.sql.Types.VARCHAR)
						{
							currentRow.add(readElement.getString("value"));
						}
						else throw new ApplicationException("Wrong datatype for CSV conversion. Type:"+valueMeta.getInt("type"));
					}
					else
					{
						currentRow.add(null);
					}
					
				}
				
				printer.printRecord(currentRow);
				
			}
		}
		
		printer.flush();
		printer.close();
	}
	
	private void writeDatacache(DataEntry entryTemplate) throws InvalidFormatException, IOException, ApplicationException, SQLException
	{
		if(settingPath==null)
			throw new ApplicationException("Dataset for reading not named");
		int rowBufferSize = 100000;
		settingOutputFile.createNewFile();
		FileWriter writer = new FileWriter(settingOutputFile);
		writer.write("{");
		
		DataEntry e = dataCache.get(settingPath);
		
		//int rownum =0;
		
		//first row names
		writer.write("\"path\":\""+e.getPath()+"\",");
		writer.write("\"namemap\":"+e.getNamemap()+",");
		writer.write("\"rows\":[");
		while(e.next(rowBufferSize, false))
		{
			for(int iRow=0; iRow<e.getRows().length(); iRow++)
			{
				//rownum++;
				JSONObject readRow = e.getRows().getJSONObject(iRow);
				if(iRow!=0)
					writer.write(",");
				writer.write(readRow.toString());
			}
		}
		writer.write("]");
		writer.write("}");
		writer.flush();
		writer.close();
	}
	
	


}
