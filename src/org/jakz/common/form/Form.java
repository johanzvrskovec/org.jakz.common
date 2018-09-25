package org.jakz.common.form;

import java.util.HashSet;
import org.jakz.common.IndexedMap;
import org.jakz.common.JSONObject;
import org.jakz.common.JSONObjectReadAspect;
import org.jakz.common.JSONObjectWriteAspect;
import org.jakz.common.TypedValue;
/**
 * Class to deal with more or less formalized datasets. Contains the definition of the dataset. Can also contain data or a link to data. The Form is either a complete form or a nested element in a form.
 * @author johan
 *
 */

//TODO use modified DataEntry? Merge DataEntry and Form
public class Form implements JSONObjectReadAspect, JSONObjectWriteAspect
{
	protected static String valueNotNullableExceptionString ="Value is not nullable.";
	
	public static enum FieldType 
	{
		/**
		 * A top level form containing queries/rows
		 */
		FRM,
		/**
		 * A query corresponding to a row
		 */
		QRY,
		/**
		 * A question/variable corresponding to a column
		 */
		VAR,
		/**
		 * A single or multi question/variable alternative
		 */
		ALT
	};
	
	/**
	 * Codeable identifier for the form object
	 */
	protected String id;
	
	
	/*
	 * Data source mapping
	 */
	
	/**
	 * Identifier of the data source for mapping purposes. Can be used on forms and queries;
	 */
	public String dataSourceId;
	
	/**
	 * Mapped path in the data source. Set equal to the id of variables.
	 * For forms and queries: Corresponding to table name. Can include schema name.
	 * For variables: Corresponding to column names.
	 * Form variables mapped against a foreign table: Key to match against foreign key. The table from the query or form is considered as the local table.
	 */
	public String dataSourcePath;
	
	/**
	 * Foreign table to in which to look for the foreign key.
	 */
	public String varMapForeignTable;
	
	/**
	 * Value column to fetch valule from in foreign table.
	 */
	public String varMapForeignLabel;
	
	/**
	 * Key in foreign table to be matched against the local key.
	 */
	public String varMapForeignKey;
	
	/*
	 * ******************************************************************
	 * */
	
	
	/**
	 * The type of Form object. To distinguish between different Form objects.
	 */
	public FieldType type;
	
	/**
	 * Human readable name text
	 */
	public String name;
	/**
	 * Human readable descriptive text
	 */
	public String text;
	
	/**
	 * Human readable descriptive instruction for the administrator
	 */
	public String instruction;
	
	/**
	 * Definitions of type for the question/variable. Can contain question/variable data.
	 */
	protected TypedValue value;
	
	/**
	 * Other data value (string).
	 */
	protected String otherValue;
	
	/**
	 * Parent Form object in content hierarchy.
	 */
	protected Form parent;
	
	/**
	 * Form content. Different Form types are expected to have different content.
	 */
	public IndexedMap<String,Form> content;
	 
	/**
	 * If the element is nullable or not required.
	 */
	public boolean nullable;
	
	/**
	 * If the element is writeable.
	 */
	public boolean writeable;
	
	/**
	 * If the question/column is a key for the corresponding table.
	 */
	public boolean tablekey;
	
	/**
	 * Other parameters not covered by standardized settings or variables.
	 */
	public IndexedMap<String,String> parameter;
	
	/**
	 * Flag to indicate variable error.
	 */
	public boolean errorFlag;
	
	
	/**
	 * Indicates if "other-data" can be or is entered instead of an alternative.
	 */
	public boolean alternativeHasOtherField;
	
	/**
	 * Indicates if an alternative is exclusive to the other alternatives.
	 */
	public boolean alternativeExclusive;
	
	/**
	 * Parse format to use when parsing date values for example.
	 */
	public String valueParseFormat;
	
	/**
	 * String message corresponding to a positive error flag.
	 */
	public String errorMessage;
	
	/*
	 * Temporary session settings
	 */
	
	public boolean settingJSONIncludeDataSourceMapping;
	
	
	private void init()
	{
		dataSourceId=null;
		dataSourcePath=null;
		varMapForeignLabel=null;
		varMapForeignTable=null;
		varMapForeignKey=null;
		
		parent=null;
		content=new IndexedMap<String, Form>();
		value=null;
		otherValue=null;
		
		name="";
		text="";
		instruction="";
		
		nullable = true;
		writeable = false;
		tablekey = false;
		
		parameter = new IndexedMap<String, String>();
		
		errorFlag = false;
		errorMessage = null;
		
		alternativeHasOtherField=false;
		alternativeExclusive=false;
		valueParseFormat = null;
		
		
		settingJSONIncludeDataSourceMapping=false;
		
		
	}

	/**
	 * Shallow copy of source into this object
	 * @param source
	 * @return
	 */
	public Form scopy(Form source)
	{
		id=source.id;
		
		dataSourceId=source.dataSourceId;
		dataSourcePath=source.dataSourcePath;
		varMapForeignLabel=source.varMapForeignLabel;
		varMapForeignTable=source.varMapForeignTable;
		varMapForeignKey=source.varMapForeignKey;
		
		type=source.type;
		name=source.name;
		text=source.text;
		instruction=source.instruction;
		value=null;
		if(source.value!=null)
			value=source.value.createNewScopy();
		otherValue=source.otherValue;
		parent=source.parent;
		//content=source.content;
		nullable=source.nullable;
		writeable=source.writeable;
		tablekey=source.tablekey;
		
		//parameter=source.parameter;
		
		errorFlag=source.errorFlag;
		errorMessage=source.errorMessage;
		
		alternativeHasOtherField=source.alternativeHasOtherField;
		alternativeExclusive=source.alternativeExclusive;
		valueParseFormat=source.valueParseFormat;
		
		settingJSONIncludeDataSourceMapping = source.settingJSONIncludeDataSourceMapping;
		
		return this;	
	}
	
	/**
	 * Create new shallow copy from current object.
	 * @return
	 */
	public Form createNewScopy()
	{
		Form toReturn = new Form("");
		return toReturn.scopy(this);
	}
	
	/**
	 * Deep copy of source into this object. Deep copy of the content and value variables.
	 * @param source
	 * @return
	 */
	public Form dcopy(Form source)
	{
		scopy(source);
		
		content=new IndexedMap<String, Form>();
		
		for(int i=0; i<source.content.size(); i++)
		{
			content.put(source.content.getKeyAt(i), source.content.getValueAt(i).createNewDcopy());
		}
		
		parameter=new IndexedMap<String, String>();
		
		for(int i=0; i<source.parameter.size(); i++)
		{
			parameter.put(source.parameter.getKeyAt(i), source.parameter.getValueAt(i));
		}
		
		return this;
	}
	
	/**
	 * Create new deep copy from current object.
	 * @return
	 */
	public Form createNewDcopy()
	{
		Form toReturn = new Form("");
		return toReturn.dcopy(this);
	}
	
	public Form(String nid, FieldType ntype) 
	{
		init();
		type=ntype;
		setId(nid);
	}
	
	public Form(String nid) 
	{
		init();
		type=FieldType.FRM;
		setId(nid);
	}
	
	public String getHTMLGlobalID()
	{
		if(parent!=null)
			return parent.getHTMLGlobalID()+"."+id;
		else
			return id;
	}
	
	public Form addQuery(String nId)
	{
		Form nf = new Form(nId,FieldType.QRY);
		add(nf);
		return nf;
	}
	
	public Form addVariable(String nId)
	{
		Form nf = new Form(nId,FieldType.VAR);
		add(nf);
		return nf;
	}
	
	public Form optVariable(String nId)
	{
		Form nf = new Form(nId,FieldType.VAR);
		opt(nf);
		return content.getValue(nId);
	}
	
	public Form addVariable(String nId, int nType)
	{
		Form nf = new Form(nId,FieldType.VAR).setValueType(nType);
		add(nf);
		return nf;
	}
	
	public Form optVariable(String nId, int nType)
	{
		Form nf = new Form(nId,FieldType.VAR).setValueType(nType);
		opt(nf);
		return content.getValue(nId);
	}
	
	public Form addVariable(String nId, int nType, int nSizeLimit)
	{
		Form nf = new Form(nId,FieldType.VAR).setValueType(nType,nSizeLimit);
		add(nf);
		return nf;
	}
	
	public Form optVariable(String nId, int nType, int nSizeLimit)
	{
		Form nf = new Form(nId,FieldType.VAR).setValueType(nType,nSizeLimit);
		opt(nf);
		return content.getValue(nId);
	}
	
	public Form addAlternative(String nId)
	{
		setValueType(java.sql.Types.VARCHAR);
		Form nf = new Form(nId,FieldType.ALT).setValueType(java.sql.Types.NVARCHAR);
		add(nf);
		return nf;
	}
	
	/*
	public ArrayList<Form> getAlternatives()
	{
		ArrayList<Form> toReturn = new ArrayList<Form>();
		for(int i=0; i<content.size(); i++)
		{
			Form c = content.getValueAt(i);
			if(c.type==FieldType.ALT)
			{
				toReturn.add(c);
			}
		}
		
		return toReturn;
	}
	*/
	
	public Form setId(String nId)
	{
		id=nId;
		if(type==FieldType.VAR)
			dataSourcePath=id;
		return this;
	}
	
	public String getId()
	{
		return id;
	}
	
	public Form setName(String nName)
	{
		name=nName;
		return this;
	}
	
	public Integer getValueType() { return value.getType();}
	
	public TypedValue getValue() {return value;}
	public Integer getValueInteger() {return value.getValueInteger();}
	public Boolean getValueBoolean() {return value.getValueBoolean();}
	public String getValueVarchar() {return value.getValueVarchar();}
	public String getValueNVarchar() {return value.getValueNVarchar();}
	public Long getValueTimestamp() {return value.getValueTimestamp();}
	public Long getValueBigint() {return value.getValueBigint();}
	public Double getValueDouble() {return value.getValueDouble();}
	
	public String getOtherValue(){return otherValue;}
	
	
	public Form setValueInteger(Integer nValue) throws NumberFormatException
	{
		if(nValue==null&&!nullable)
			throw new NumberFormatException(valueNotNullableExceptionString);
		
		value.setInteger(nValue);
		return this;
	}
	
	public Form setValueBoolean(Boolean nValue) throws NumberFormatException
	{
		if(nValue==null&&!nullable)
			throw new NumberFormatException(valueNotNullableExceptionString);
		
		value.setBoolean(nValue);
		return this;
	}
	
	public Form setValueVarchar(String nValue) throws NumberFormatException
	{
		if(nValue==null&&!nullable)
			throw new NumberFormatException(valueNotNullableExceptionString);
		
		value.setVarchar(nValue);
		return this;
	}
	
	public Form setValueNvarchar(String nValue) throws NumberFormatException
	{
		if(nValue==null&&!nullable)
			throw new NumberFormatException(valueNotNullableExceptionString);
		
		value.setNvarchar(nValue);
		return this;
	}
	
	public Form setValueTimestamp(Long nValue) throws NumberFormatException
	{
		if(nValue==null&&!nullable)
			throw new NumberFormatException(valueNotNullableExceptionString);
		
		value.setTimestamp(nValue);
		return this;
	}
	
	public Form setValueBigint(Long nValue) throws NumberFormatException
	{
		if(nValue==null&&!nullable)
			throw new NumberFormatException(valueNotNullableExceptionString);
		
		value.setBigint(nValue);
		return this;
	}
	
	public Form setValueDouble(Double nValue) throws NumberFormatException
	{
		if(nValue==null&&!nullable)
			throw new NumberFormatException(valueNotNullableExceptionString);
		
		value.setDouble(nValue);
		return this;
	}
	
	public Form setValue(TypedValue nValue)
	{
		value=nValue;
		return this;
	}
	
	public Form setValue(int nType)
	{
		value=new TypedValue(nType);
		return this;
	}
	
	public Form setValueType(int nType)
	{
		if(value==null)
			value=new TypedValue();
		value.setType(nType);
		return this;
	}
	
	public Form setValueType(int nType, int nSizeLimit)
	{
		if(value==null)
			value=new TypedValue();
		value.setType(nType);
		value.setSizeLimit(nSizeLimit);
		return this;
	}
	
	public Form setOtherValue(String nOtherValue)
	{
		otherValue=nOtherValue;
		return this;
	}
	
	public Form setText(String nText)
	{
		text=nText;
		return this;
	}
	
	public Form setRequired(boolean nRequiered)
	{
		nullable=!nRequiered;
		return this;
	}
	
	public Form setNullable(boolean nNullable)
	{
		nullable=nNullable;
		return this;
	}
	
	public Form setWritable(boolean nWriteable)
	{
		writeable=nWriteable;
		return this;
	}
	
	public Form setTablekey(boolean nTablekey)
	{
		tablekey=nTablekey;
		return this;
	}

	@Override
	public org.jakz.common.JSONObject toJSONObject() 
	{
		org.jakz.common.JSONObject j = new JSONObject();
		j.put("id", id);
		
		if(settingJSONIncludeDataSourceMapping)
		{
			j.put("dataSourceId", dataSourceId);
			j.put("dataSourcePath", dataSourcePath);
			j.put("varMapForeignLabel", varMapForeignLabel);
			j.put("varMapForeignTable", varMapForeignTable);
			j.put("varMapForeignKey", varMapForeignKey);
		}
		
		j.put("name", name);
		j.put("text", text);
		j.put("type", type.name());
		j.put("value",value);
		j.put("otherValue",otherValue);
		if(parent!=null)
			j.put("parent", parent.id);
		else
			j.put("parent", org.jakz.common.JSONObject.NULL);
		
		j.put("content",content.values());
		
		j.put("nullable",nullable);
		j.put("writeable",writeable);
		j.put("tablekey",tablekey);
		
		j.put("parameter",parameter.map());
		
		j.put("errorFlag",errorFlag);
		j.put("errorMessage",errorMessage);
		
		j.put("hasOtherField", alternativeHasOtherField);
		j.put("alternativeExclusive", alternativeExclusive);
		j.put("valueParseFormat", valueParseFormat);
		
		return j;
	}

	@Override
	public void fromJSONObject(org.jakz.common.JSONObject source) 
	{
		init();
		id=source.getString("id");
		
		if(settingJSONIncludeDataSourceMapping)
		{
			dataSourceId=source.optString("dataSourceId");
			dataSourcePath=source.optString("dataSourcePath");
			varMapForeignLabel=source.optString("varMapForeignLabel");
			varMapForeignTable=source.optString("varMapForeignTable");
			varMapForeignKey=source.optString("varMapForeignKey");
		}
		
		name=source.getString("name");
		text=source.optString("text");
		type=FieldType.valueOf(source.getString("type").toUpperCase());
		
		value=new TypedValue();
		value.fromJSONObject(source.getJSONObject("value"));
		otherValue=source.optString("otherValue");
		
		content=new IndexedMap<String,Form>();
		org.jakz.common.JSONArray contentArray = source.getJSONArray("content");
		for(int i=0; i<contentArray.length(); i++)
		{
			Form newForm = new Form("DEFAULT", FieldType.FRM);
			newForm.fromJSONObject(contentArray.getJSONObject(i));
			newForm.parent=this;
			content.put(newForm.id, newForm);
		}
		
		nullable=source.optBoolean("nullable");
		writeable=source.optBoolean("writeable");
		tablekey=source.optBoolean("tablekey");
		
		parameter=new IndexedMap<String,String>();
		JSONObject parametersObject = source.getJSONObject("parameter");
		String[] paramaterNames = parametersObject.getNames();
		for(int i=0; i<paramaterNames.length; i++)
		{
			parameter.put(paramaterNames[i],parametersObject.getString(paramaterNames[i]));
		}
		
		errorFlag=source.optBoolean("errorFlag");
		errorMessage=source.optString("errorMessage");
		
		alternativeHasOtherField=source.optBoolean("hasOtherField");
		alternativeExclusive=source.optBoolean("valternativeExclusive");
		valueParseFormat=source.optString("valueParseFormat");
	}
	
	public boolean getHasContent()
	{
		return content!=null&&content.size()>0;
	}
	
	/**
	 * Returns added child form in relation. The added form is a child.
	 * @param nContent
	 * @return
	 */
	public Form add(Form nContent)
	{
		content.put(nContent.id, nContent);
		nContent.parent=this;
		return nContent;
	}
	
	/**
	 * Returns added or existing child form in relation. The added form is a child.
	 * @param nContent
	 * @return
	 */
	public Form opt(Form nContent)
	{
		content.opt(nContent.id, nContent);
		nContent.parent=this;
		return content.getValue(nContent.id);
	}
	
	protected org.jakz.common.JSONObject getValues(org.jakz.common.JSONObject toReturn)
	{
		toReturn.put(id, value);
		for(int i=0; i<content.size(); i++)
		{
			content.getAt(i).value.getValues(toReturn);
		}
		return toReturn;
	}
	
	public String toString()
	{
		return toJSONObject().toString();
	}
	
	public Form setJSONIncludeDataSourceMapping(boolean nVal)
	{
		settingJSONIncludeDataSourceMapping = nVal;
		return this;
	}
	
	public Form setDataSourceId(String nDataSourceId)
	{
		dataSourceId=nDataSourceId;
		return this;
	}
	
	public Form setDataSourcePath(String nDataSourcePath)
	{
		dataSourcePath=nDataSourcePath;
		return this;
	}
	
	public Form setValueParseFormat(String nValueParseFormat)
	{
		valueParseFormat=nValueParseFormat;
		return this;
	}
	
	public Form mapVariable(String nDataSourcePath, String nForeignTable, String nForeignLabel, String nForeignKey)
	{
		dataSourcePath=nDataSourcePath;
		varMapForeignLabel = nForeignLabel;
		varMapForeignTable = nForeignTable;
		varMapForeignKey = nForeignKey;
		return this;
	}
	
	/**
	 * Gets the first name of the DB path - the right-most name separated by dots.
	 * @param source
	 * @return
	 */
	public static String get1stLevelDBName(String source)
	{
		if(source==null)
			return null;
		String toReturn = source;
		int dotIndex = source.lastIndexOf('.');
		if(dotIndex>-1)
		{
			toReturn=source.substring(dotIndex+1);
		}
		return toReturn;
	}
	
	/**
	 * Gets the second name of the DB path - the second right-most name separated by dots.
	 * @param source
	 * @return
	 */
	public static String get2ndLevelDBName(String source)
	{
		if(source==null)
			return null;
		String toReturn = null;
		int dotIndex = source.lastIndexOf('.');
		if(dotIndex>-1)
		{
			toReturn=source.substring(0,dotIndex);
			
			dotIndex=toReturn.indexOf('.');
			if(dotIndex>-1)
			{
				toReturn=source.substring(dotIndex+1);
			}
		}
		
		return toReturn;
	}
	
	public static String constructDBPath(String sch, String tab, String var)
	{
		String toReturn = null;
		if(var!=null)
			toReturn = var;
		
		if(tab!=null)
		{
			if(toReturn!=null)
				toReturn = tab +"."+toReturn;
			else
				toReturn = tab;
		}
		
		if(sch!=null)
		{
			if(toReturn!=null)
				toReturn = sch +"."+toReturn;
			else
				toReturn = sch;
		}
				
		return toReturn;
	}
	
	public String getEvaluatedDBPath() throws FormException
	{
		return getEvaluatedDBPath(this);
	}
	
	public static String getEvaluatedDBPath(Form source) throws FormException
	{
		if(source.type==FieldType.VAR||source.type==FieldType.FRM)
			return source.dataSourcePath;
		else if(source.type==FieldType.QRY)
		{
			String schemaName = getEvaluatedDBSchema(source);
			String tableName = getEvaluatedDBTable(source);
			
			if(schemaName!=null&&tableName!=null)
				return schemaName+"."+tableName;
			else 
				return tableName;
				
		}
		else throw new FormException("The Form is of unknown type");
	}
	
	public String getEvaluatedDBSchema()
	{
		return getEvaluatedDBSchema(this);
	}
	
	public static String getEvaluatedDBSchema(Form source)
	{
		if(source.type==FieldType.VAR)
		{	
			if(source.parent!=null)
				return getEvaluatedDBSchema(source.parent);
		}
		else if(source.type==FieldType.FRM)
		{
			return get2ndLevelDBName(source.dataSourcePath);
		}
		else if(source.type==FieldType.QRY)
		{
			String schemaName = get2ndLevelDBName(source.dataSourcePath);
			
			if(schemaName==null&&source.parent!=null)
			{
				return getEvaluatedDBSchema(source.parent);
			}
				
		}
		
		return null;
	}
	
	public String getEvaluatedDBTable()
	{
		return getEvaluatedDBTable(this);
	}
	
	public static String getEvaluatedDBTable(Form source)
	{
		if(source.type==FieldType.VAR)
		{
			if(source.parent!=null)
				return getEvaluatedDBTable(source.parent);
		}
		else if(source.type==FieldType.FRM)
		{
			return get1stLevelDBName(source.dataSourcePath);
		}
		else if(source.type==FieldType.QRY)
		{
			String tableName = get1stLevelDBName(source.dataSourcePath);
			
			if(tableName==null&&source.parent!=null)
			{
				return getEvaluatedDBTable(source.parent);
			}
				
		}
		
		return null;
	}
	
	@Deprecated
	public IndexedMap<String, HashSet<String>> getUniqueMappedDBPaths(int maxDepth) throws FormException
	{
		return getUniqueMappedDBPaths(this, maxDepth);
	}
	
	@Deprecated
	public static IndexedMap<String, HashSet<String>> getUniqueMappedDBPaths(Form formWithQueries, int maxDepth) throws FormException
	{
		IndexedMap<String, HashSet<String>> tablesWithColumnsToReturn = new IndexedMap<String, HashSet<String>>();
		if(formWithQueries.dataSourcePath!=null)
		{
			tablesWithColumnsToReturn.put(formWithQueries.getEvaluatedDBPath(), new HashSet<String>());
		}
		
		for(int iQry=0; iQry<maxDepth; iQry++)
		{
			Form query = formWithQueries.content.getValueAt(iQry);
			String qTableNameEntry = query.getEvaluatedDBPath();
			HashSet<String> varSet =null;
			if(qTableNameEntry!=null && !tablesWithColumnsToReturn.containsKey(qTableNameEntry))
			{
				tablesWithColumnsToReturn.put(qTableNameEntry, new HashSet<String>());
			}
			
			for(int iVar=0; iVar<query.content.size(); iVar++)
			{
				Form var = query.content.getValueAt(iVar);
				
				if(var.varMapForeignKey!=null)
				{
					String foreignTableName =  get1stLevelDBName(var.varMapForeignTable);
					String foreignSchemaName = get2ndLevelDBName(var.varMapForeignTable);
					
					String fTableNameEntry = constructDBPath(foreignSchemaName, foreignTableName, null);
					
					if(!tablesWithColumnsToReturn.containsKey(fTableNameEntry))
					{
						tablesWithColumnsToReturn.put(fTableNameEntry, new HashSet<String>());
					}
					
					//local variables
					varSet=tablesWithColumnsToReturn.getValue(qTableNameEntry);
					varSet.add(var.dataSourcePath);
					
					//foreign variables
					varSet=tablesWithColumnsToReturn.getValue(fTableNameEntry);
					varSet.add(var.varMapForeignLabel);
					varSet.add(var.varMapForeignKey);
				}
				else if(var.dataSourcePath!=null)
				{
					varSet=tablesWithColumnsToReturn.getValue(qTableNameEntry);
					varSet.add(var.dataSourcePath);
				}
				else throw new FormException("Form variable "+iVar+" has no dataSourcePath.");		
			}
		}
		
		return tablesWithColumnsToReturn;
	}

}
