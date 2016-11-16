package org.jakz.common;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class DataEntry
{
	public String path;
	public IndexedMap<String,JSONObject> namemap;
	public JSONArray rows;
	public boolean memory,temporary,local;
	
	private ResultSet dbResult;
	private boolean hasMoreDbBuffer;
	
	public DataEntry(String npath)
	{
		path=npath;
		namemap = new IndexedMap<String,JSONObject>();
		rows = new JSONArray();
		memory=false;
		temporary=false;
		local=false;
		hasMoreDbBuffer=true;
	}
	
	public DataEntry(String npath, DataEntry template)
	{
		namemap = new IndexedMap<String,JSONObject>();
		rows = new JSONArray();
		memory=false;
		temporary=false;
		local=false;
		hasMoreDbBuffer=true;
		
		copy(template,this);
		
		path=npath;
	}
	
	public DataEntry(String npath, ResultSet ndbResult) throws SQLException, ApplicationException
	{
		path=npath;
		namemap = new IndexedMap<String,JSONObject>();
		rows = new JSONArray();
		memory=false;
		temporary=false;
		local=false;
		hasMoreDbBuffer=true;
		connectResultSet(ndbResult);
	}
	
	public DataEntry(JSONObject json)
	{
		namemap = new IndexedMap<String,JSONObject>();
		fromJSON(json);
	}
	
	private void dbInit() throws SQLException, ApplicationException
	{
		ResultSetMetaData m = dbResult.getMetaData();
		int numColumns = m.getColumnCount();
		for(int i=0; i<numColumns; i++)
		{
			JSONObject element = new JSONObject();
			element.put("name",m.getColumnName(i+1));
			int ctype = m.getColumnType(i+1);
			element.put("type", ctype);
			namemap.put(m.getColumnName(i+1),element);
		}
	}
	
	public String getPath(){return path;}
	public JSONObject getNamemap(){return namemap.toJSON();}
	public JSONArray getRows(){return rows;}
	public boolean getMemory(){return memory;}
	public boolean getTemporary(){return temporary;}
	public boolean getLocal(){return local;}
	public int getNamemapSize(){return namemap.size();}
	public JSONObject getNamemapMeta(String name){return namemap.getValue(name);}
	public JSONObject getNamemapMetaAt(int index){return namemap.getValueAt(index);}
	public JSONObject toJSON(){return new JSONObject(this);}
	
	public static void copy(DataEntry source, DataEntry target)
	{
		target.path=source.path;
		target.namemap=source.namemap.copy();
		target.rows=Util.copyJSONArrayInto(source.rows, new JSONArray());
		target.memory=source.memory;
		target.temporary=source.temporary;
		target.local=source.local;
	}
	
	public DataEntry copy()
	{
		DataEntry r = new DataEntry(path);
		copy(this,r);
		return r;
	}
	
	public DataEntry fromJSON(JSONObject json)
	{

		path=json.getString("path");

		namemap.fromJSON(json.optJSONObject("namemap"));
		
		rows=json.optJSONArray("rows");
		if(rows==null)
			rows=new JSONArray();
		memory=json.getBoolean("memory");
		temporary=json.getBoolean("temporary");
		local=json.getBoolean("local");
		return this;
	}
	
	public DataEntry connectResultSet(ResultSet ndbResult) throws SQLException, ApplicationException
	{
		dbResult=ndbResult;
		dbInit();
		return this;
	}
	
	public boolean next(int rowbuffer, boolean append) throws SQLException, ApplicationException
	{
		boolean oldHasMore = hasMoreDbBuffer;
		if(dbResult!=null && !dbResult.isClosed())
		{
			boolean hasRows = dbResult.next();
			if(hasRows)
			{
				if(!append)
					rows=new JSONArray();
				//ResultSetMetaData m = dbResult.getMetaData();
				
				for(int iRowbuf=0; iRowbuf<rowbuffer&&hasRows; iRowbuf++)
				{
					JSONObject rowtoadd = new JSONObject();
					for(int i=0; i<namemap.size(); i++)
					{
						String variableName = namemap.getKeyAt(i);
						JSONObject variableMeta = namemap.getValueAt(i);
						JSONObject elementToPut = new JSONObject();
						
						elementToPut.put("name", variableName);
						elementToPut.put("type", variableMeta.getInt("type"));
						
						if(variableMeta.getInt("type")==java.sql.Types.BOOLEAN)
						{
							elementToPut.put("value", dbResult.getBoolean(variableName));
						}
						else if(variableMeta.getInt("type")==java.sql.Types.TIMESTAMP)
						{
							elementToPut.put("value", dbResult.getTimestamp(variableName).getTime());
						}
						else if(variableMeta.getInt("type")==java.sql.Types.BIGINT)
						{
							elementToPut.put("value", dbResult.getLong(variableName));
						}
						else if(variableMeta.getInt("type")==java.sql.Types.INTEGER||variableMeta.getInt("type")==java.sql.Types.SMALLINT)
						{
							elementToPut.put("value", dbResult.getInt(variableName));
						}
						else if(variableMeta.getInt("type")==java.sql.Types.DOUBLE||variableMeta.getInt("type")==java.sql.Types.DECIMAL)
						{
							elementToPut.put("value", dbResult.getDouble(variableName));
						}
						else if(variableMeta.getInt("type")==java.sql.Types.VARCHAR)
						{
							elementToPut.put("value", dbResult.getString(variableName));
						}
						else throw new ApplicationException("Could not parse DataCache value of dataset "+path+", variable "+variableName+" at relative line index "+iRowbuf+" with type "+variableMeta.getInt("type"));
						
						if(dbResult.wasNull())
						{
							elementToPut.remove("value");
						}
						
						rowtoadd.put(variableName, elementToPut);
					}
					rows.put(rowtoadd);
					
					if(iRowbuf<rowbuffer-1)
					{
						hasRows = dbResult.next();
					}
					
				}
				
				if(hasRows)
				{
					hasMoreDbBuffer=true;
				}
				else 
				{
					hasMoreDbBuffer=false;
				}
			}
			else 
			{
				hasMoreDbBuffer=false;
			}
			
		}
		else 
		{
			hasMoreDbBuffer=false;
		}
		
		return oldHasMore;
	}
	
	public DataEntry close() throws SQLException
	{
		if(dbResult!=null)
			dbResult.close();
		return this;
	}
	/*
	public JSONObject toJSON()
	{
		JSONObject toreturn = new JSONObject();
		
		toreturn.put("path", path);
		toreturn.put("elementmeta", elementmeta);
		toreturn.put("namemap", namemap.toJSON());
		
		return toreturn;
	}
	*/
}