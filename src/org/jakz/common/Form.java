package org.jakz.common;

import java.util.ArrayList;
import java.util.HashSet;
/**
 * Class to deal with more or less formalized datasets. Contains the definition of the dataset. Can also contain data or a link to data. The Form is either a complete form or a nested element in a form.
 * @author johan
 *
 */

//TODO use modified DataEntry? Merge DataEntry and Form
public class Form implements JSONObjectReadAspect, JSONObjectWriteAspect
{
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
		VAR
	};
	
	/**
	 * Codeable identifier for the form object
	 */
	public String id;
	
	
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
	 * Definitions of types for each value in the question/variable. Can contain question/variable data.
	 */
	public ArrayList<TypedValue> value;
	
	/**
	 * Parent Form object in content hierarchy.
	 */
	protected Form parent;
	
	/**
	 * Form content. Different Form types are expected to have different content.
	 */
	public IndexedMap<String,Form> content;
	 
	/**
	 * If the element is required. Used for queries and questions.
	 */
	public boolean required;
	/**
	 * If the element is nullable.
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
		value=new ArrayList<TypedValue>();
		
		name="";
		text="";
		
		required =false;
		nullable = true;
		writeable = false;
		tablekey = false;
		
		
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
		value=source.value;
		parent=source.parent;
		content=source.content;
		required=source.required;
		nullable=source.nullable;
		writeable=source.writeable;
		tablekey=source.tablekey;
		
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
		
		value=new ArrayList<TypedValue>();
		
		for(int i=0; i<source.value.size(); i++)
		{
			value.add(source.value.get(i).createNewScopy());
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
		id=nid;
		type=ntype;
		if(type==FieldType.VAR)
			dataSourcePath=id;
	}
	
	public Form(String nid) 
	{
		init();
		id=nid;
		type=FieldType.FRM;
		if(type==FieldType.VAR)
			dataSourcePath=id;
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
	
	public Form addVariable(String nId, int nType)
	{
		Form nf = new Form(nId,FieldType.VAR).setValueType(nType);
		add(nf);
		return nf;
	}
	
	public Form addVariable(String nId, int nType, int nSizeLimit)
	{
		Form nf = new Form(nId,FieldType.VAR).setValueType(nType,nSizeLimit);
		add(nf);
		return nf;
	}
	
	public Form setId(String nId)
	{
		id=nId;
		return this;
	}
	
	public Form setName(String nName)
	{
		name=nName;
		return this;
	}
	
	public Form setValue(ArrayList<TypedValue> nValue)
	{
		value=nValue;
		return this;
	}
	
	public Form setValue(TypedValue tv)
	{
		value=new ArrayList<TypedValue>();
		value.add(tv);
		return this;
	}
	
	public Form setValueType(int nType)
	{
		value=new ArrayList<TypedValue>();
		TypedValue tv = new TypedValue(nType);
		value.add(tv);
		return this;
	}
	
	public Form setValueType(int nType, int nSize)
	{
		value=new ArrayList<TypedValue>();
		TypedValue tv = new TypedValue(nType,nSize);
		value.add(tv);
		return this;
	}
	
	public Form addValue(TypedValue tv)
	{
		value.add(tv);
		return this;
	}
	
	public Form addValueType(int type)
	{
		TypedValue tv = new TypedValue(type);
		value.add(tv);
		return this;
	}
	
	public Form setText(String nText)
	{
		text=nText;
		return this;
	}
	
	public Form setRequired(boolean nRequiered)
	{
		required=nRequiered;
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
		if(parent!=null)
			j.put("parent", parent.id);
		else
			j.put("parent", org.jakz.common.JSONObject.NULL);
		
		j.put("content",content.values());
		
		j.put("required",required);
		j.put("nullable",nullable);
		j.put("writeable",writeable);
		j.put("tablekey",tablekey);
		
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
			varMapForeignKey=source.getString("varMapForeignKey");
		}
		
		name=source.getString("name");
		text=source.optString("text");
		type=FieldType.valueOf(source.getString("type").toUpperCase());
		org.jakz.common.JSONArray a = source.getJSONArray("value");
		for(int i=0; i<a.length(); i++)
		{
			org.jakz.common.JSONObject toputJSON= a.getJSONObject(i);
			TypedValue toput = new TypedValue();
			toput.fromJSONObject(toputJSON);
			value.add(toput);
		}
		
		content=new IndexedMap<String,Form>();
		org.jakz.common.JSONArray contentArray = source.getJSONArray("content");
		for(int i=0; i<contentArray.length(); i++)
		{
			Form newForm = new Form("DEFAULT", FieldType.FRM);
			newForm.fromJSONObject(contentArray.getJSONObject(i));
			newForm.parent=this;
			content.put(newForm.id, newForm);
		}
		
		required=source.optBoolean("required");
		nullable=source.optBoolean("nullable");
		writeable=source.optBoolean("writeable");
		tablekey=source.optBoolean("tablekey");
		
	}
	
	public boolean getHasContent()
	{
		return content!=null&&content.size()>0;
	}
	
	/**
	 * Returns parent form in relation. The added form is a child.
	 * @param nContent
	 * @return
	 */
	public Form add(Form nContent)
	{
		content.put(nContent.id, nContent);
		nContent.parent=this;
		return this;
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
	
	public Form mapVariable(String nDataSourcePath, String nForeignTable, String nForeignLabel, String nForeignKey)
	{
		dataSourcePath=nDataSourcePath;
		varMapForeignLabel = nForeignLabel;
		varMapForeignTable = nForeignTable;
		varMapForeignKey = nForeignKey;
		return this;
	}
	
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
	
	public String getEvaluatedDBPath() throws OperationException
	{
		return getEvaluatedDBPath(this);
	}
	
	public static String getEvaluatedDBPath(Form source) throws OperationException
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
		else throw new OperationException("The Form is of unknown type");
	}
	
	public String getEvaluatedDBSchema() throws OperationException
	{
		return getEvaluatedDBSchema(this);
	}
	
	public static String getEvaluatedDBSchema(Form source) throws OperationException
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
	
	public String getEvaluatedDBTable() throws OperationException
	{
		return getEvaluatedDBTable(this);
	}
	
	public static String getEvaluatedDBTable(Form source) throws OperationException
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
	public IndexedMap<String, HashSet<String>> getUniqueMappedDBPaths(int maxDepth) throws OperationException
	{
		return getUniqueMappedDBPaths(this, maxDepth);
	}
	
	@Deprecated
	public static IndexedMap<String, HashSet<String>> getUniqueMappedDBPaths(Form formWithQueries, int maxDepth) throws OperationException
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
				else throw new OperationException("Form variable "+iVar+" has no dataSourcePath.");		
			}
		}
		
		return tablesWithColumnsToReturn;
	}

}
