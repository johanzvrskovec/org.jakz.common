package org.jakz.common;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.server.TcpServer;
import org.h2.tools.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * NOT THREAD SAFE! SHOULD BE USED PER THREAD
 * @author johkal
 *
 */
public class DataCache 
{
	
	
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
		
		private DataEntry(String npath, ResultSet ndbResult) throws SQLException, ApplicationException
		{
			path=npath;
			namemap = new IndexedMap<String,JSONObject>();
			rows = new JSONArray();
			memory=false;
			temporary=false;
			local=false;
			dbResult=ndbResult;
			hasMoreDbBuffer=true;
			dbInit();
			
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
		
		public DataEntry copy()
		{
			DataEntry r = new DataEntry(path);
			r.namemap=namemap.copy();
			r.rows=Util.copyJSONArrayInto(rows, new JSONArray());
			r.memory=memory;
			r.temporary=temporary;
			r.local=local;
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
	
	public enum ReservedKeyword {CROSS, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP, DISTINCT, EXCEPT, EXISTS, FALSE, FETCH, FOR, FROM, FULL, GROUP, HAVING, INNER, INTERSECT, IS, JOIN, LIKE, LIMIT, MINUS, NATURAL, NOT, NULL, OFFSET, ON, ORDER, PRIMARY, ROWNUM, SELECT, SYSDATE, SYSTIME, SYSTIMESTAMP, TODAY, TRUE, UNION, UNIQUE, WHERE };
	private static int standardDBCacheSizeKB = 1000000;
	private HashSet<String> reservedKeywordsSet;
	
	private String cacheDBURL;
	private Connection con;
	//private boolean settingOverwriteExistingTables;
	private boolean settingInsertIntoExistingTables;
	private long settingConnectionTimeoutMilliseconds;
	
	
	//entry variables
	private String tableName, schemaName;
	//private JSONObject elementMeta;
	private IndexedMap<String,JSONObject> elementNameMap;

	//private JSONArray elementNameArray;
	private JSONObject rowDataNameMap;
	private String variableNameListSQL;
	private String variableDeclarationListSQL;
	
	public DataCache(String path) 
	{
		reservedKeywordsSet=new HashSet<String>();
		fillReservedKeywordSet();
		cacheDBURL = "jdbc:h2:"+path;
		//settingOverwriteExistingTables=false;
		settingInsertIntoExistingTables=true;
		settingConnectionTimeoutMilliseconds=60000;
	}
	
	public DataEntry newEntry(String npath)
	{
		return new DataEntry(npath);
	}
	
	public DataEntry newEntry(JSONObject srcObj)
	{
		return new DataEntry(srcObj);
	}
	
	private void fillReservedKeywordSet()
	{
		ReservedKeyword[] values =ReservedKeyword.values();
		for(int i=0; i<values.length; i++)
		{
			reservedKeywordsSet.add(values[i].toString());
		}
		
	}
	
	public boolean isReservedKeyword(String toTest)
	{
		return reservedKeywordsSet.contains(toTest.toUpperCase());
	}
	
	
	//public DataCache setOverwriteExistingTables(boolean nSettingOverwriteExistingTables){settingOverwriteExistingTables=nSettingOverwriteExistingTables; return this;}
	//public boolean getOverwriteExistingTables(){return settingOverwriteExistingTables;}
	
	public DataCache setInsertIntoExistingTables(boolean nSettingInsertIntoTables){settingInsertIntoExistingTables=nSettingInsertIntoTables; return this;}
	public boolean getInsertIntoExistingTables(){return settingInsertIntoExistingTables;}
	
	public DataCache setConnectionTimeoutMilliseconds(long nSettingConnectionTimeoutMilliseconds){settingConnectionTimeoutMilliseconds=nSettingConnectionTimeoutMilliseconds; return this;}
	public long getConnectionTimeoutMilliseconds(){return settingConnectionTimeoutMilliseconds;}


	public DataCache createCacheConnectionEmbedded() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, ApplicationException, InterruptedException
    {
		DataSource dSource;
		//Class.forName("org.h2.Driver").newInstance();
        //Get a connection
        //con = DriverManager.getConnection(cacheDBURL);
		
		for(long t0 = System.currentTimeMillis(); System.currentTimeMillis()-t0<settingConnectionTimeoutMilliseconds;)
		{
			try
			{
				dSource = JdbcConnectionPool.create(cacheDBURL, "user", "password");
				con=dSource.getConnection();
				break;
			}
			catch(Exception e)
			{
				Thread.sleep(2000);
			}
		}
		
		if(con==null)
			throw new ApplicationException("Database access timeout. The database is probably in use.");
		
        //Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        //Get a connection
        //con = DriverManager.getConnection(cacheDBURL+";create=true;");
		
        con.setAutoCommit(false);
        setDBCacheSizeKB(standardDBCacheSizeKB);
        rebuildCommonArchitecture();
        con.commit();
        return this;
    }
	
	public DataCache createCacheConnectionServer() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, ApplicationException, InterruptedException
    {
		
		DataSource dSource;
		//Class.forName("org.h2.Driver").newInstance();
        //Get a connection
        //con = DriverManager.getConnection(cacheDBURL);
		
		for(long t0 = System.currentTimeMillis(); System.currentTimeMillis()-t0<settingConnectionTimeoutMilliseconds;)
		{
			try
			{
				dSource = JdbcConnectionPool.create(cacheDBURL, "user", "password");
				con=dSource.getConnection();
				break;
			}
			catch(Exception e)
			{
				Thread.sleep(2000);
			}
		}
		
		if(con==null)
			throw new ApplicationException("Database access timeout. The database is probably in use.");
		
        //Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        //Get a connection
        //con = DriverManager.getConnection(cacheDBURL+";create=true;");
		
        con.setAutoCommit(false);
        setDBCacheSizeKB(standardDBCacheSizeKB);
        rebuildCommonArchitecture();
        con.commit();
        return this;
    }
	
	private TcpServer createConfiguratedServer()
	{
		//Server.createTcpServer(args);
		TcpServer s = new TcpServer();
		s.init("-tcpPort", "9123", "-tcpAllowOthers");
		return s;
	}
	
	public DataCache startServer() throws SQLException
	{
		TcpServer s = createConfiguratedServer();
		s.start();
		return this;
	}
	
	public DataCache stopServer() throws SQLException
	{
		TcpServer s = createConfiguratedServer();
		s.stop();
		return this;
	}
	
	public DataCache shutdownCacheConnection() throws SQLException
    {
        if (con != null&&!con.isClosed())
        {
            con.close();
        }
        return this;
    }
	
	public DataCache commit() throws SQLException
	{
		con.commit();
		return this;
	}
	
	private static void assertDBString(String dbString) throws ApplicationException
	{
		String forbiddencharsregex = "^[^\\s;\"\']+$";
		if(!dbString.matches(forbiddencharsregex))
			throw new ApplicationException("Database string error. Database string matches forbiddden characters REGEX: "+forbiddencharsregex);
	}
	
	private void assertDBStringReserved(String dbString) throws ApplicationException
	{
		if(isReservedKeyword(dbString))
			throw new ApplicationException("Database string \""+dbString+"\" matches reserved keyword");
	}
	
	private void assertMeta() throws ApplicationException
	{
		assertDBString(tableName);
		assertDBStringReserved(tableName);
		assertDBString(schemaName);
		assertDBStringReserved(schemaName);
		ArrayList<String> names = elementNameMap.keys();

		for(int iVal=0; iVal<names.size(); iVal++)
		{
			assertDBString(names.get(iVal));
			assertDBStringReserved(names.get(iVal));
		}
	}
	
	private void assertRowMeta() throws ApplicationException
	{
		//if(elementNameMap.length()!=rowDataNameMap.length())
			//throw new ApplicationException("Incoherent element count"); //this can be handled now
		
		JSONArray names = rowDataNameMap.names();
		for(int iVal=0; iVal<names.length(); iVal++)
		{
			assertDBString(names.getString(iVal));
		}
	}
	
	private void constructVariableNameListSQL() throws ApplicationException
	{
		StringBuilder q = new StringBuilder();
		ArrayList<String> names = elementNameMap.keys();
		if(0<names.size())
		{
			q.append(names.get(0));
		}
		for(int iVal=1; iVal<names.size(); iVal++)
		{
			q.append(","+names.get(iVal));
		}
		variableNameListSQL=q.toString();
	}
	
	private static String getVariableTypeDeclaration(int datatype) throws ApplicationException
	{
		if(datatype==java.sql.Types.BOOLEAN)
			return "BOOLEAN";
		else if(datatype==java.sql.Types.TIMESTAMP)
			return "TIMESTAMP";
		else if(datatype==java.sql.Types.BIGINT)
			return "BIGINT";
		else if(datatype==java.sql.Types.INTEGER)
			return "INTEGER";
		else if(datatype==java.sql.Types.DOUBLE)
			return "DOUBLE";
		else if(datatype==java.sql.Types.VARCHAR)
			return "VARCHAR(32672)";
		else throw new ApplicationException("Wrong datatype");
	}
	
	private void constructVariableDeclarationListSQL() throws JSONException, ApplicationException
	{
		StringBuilder q = new StringBuilder();
		int vartype;
		ArrayList<String> names = elementNameMap.keys();
		ArrayList<JSONObject> elements = elementNameMap.values();
		if(0<elements.size())
		{
			JSONObject e = elements.get(0);
			vartype=e.getInt("type");
			q.append(names.get(0));
			q.append(" "+getVariableTypeDeclaration(vartype));
		}
		
		for(int iVal=1; iVal<elements.size(); iVal++)
		{
			JSONObject e = elements.get(iVal);
			vartype=e.getInt("type");
			q.append(","+names.get(iVal));
			q.append(" "+getVariableTypeDeclaration(vartype));
		}
		variableDeclarationListSQL = q.toString();
	}
	
	private static String constructCreateStatement(String tableName, String variableDeclarationList, String asQuery, boolean memory, boolean temporary, boolean local)
	{
		
		String memoryAddition = " CACHED"; //" CACHED";
		if(memory)
			memoryAddition=" MEMORY";
		
		String temporaryAddition = "";
		if(temporary)
			temporaryAddition=" TEMPORARY";
		
		String localAddition = ""; //" GLOBAL";
		if(temporary)
		{
			if(local)
				localAddition=" LOCAL";
			else
				localAddition = " GLOBAL";
		}
		
		String variableDeclarationListAddition="";
		if(variableDeclarationList!=null)
			variableDeclarationListAddition="("+variableDeclarationList+")";
		
		String asQueryAddition="";
		if(asQuery!=null)
			asQueryAddition=" AS "+asQuery;
		
		String toReturn = "CREATE"+memoryAddition+localAddition+temporaryAddition+" TABLE "+tableName+variableDeclarationListAddition+asQueryAddition;
		return toReturn;
	}
	
	//private String constructCreateStatement() throws ApplicationException
	//{
		//return "CREATE TABLE "+tableName+"("+variableDeclarationListSQL+")";
	//}
	
	private static String constructDropStatement(String tableName, boolean cascade) throws ApplicationException
	{
		String cascadeAdd = "";
		if(cascade)
			cascadeAdd=" CASCADE";
		
		return "DROP TABLE "+tableName.toUpperCase()+cascadeAdd;
	}
	
	private String constructInsertStatement() throws ApplicationException
	{
		ArrayList<JSONObject> elements = elementNameMap.values();
		return "INSERT INTO "+tableName+"("+variableNameListSQL+") VALUES ("+StringUtils.repeat("?,", elements.size()-1)+StringUtils.repeat("?",Math.max(Math.min(1,elements.size()),1))+")";
	}
	
	public boolean getHasTable(String name) throws ApplicationException, SQLException
	{
		assertDBString(name);
		ResultSet rs = con.getMetaData().getTables(null, null, name.toUpperCase(), new String[]{"TABLE"});
		boolean result =  rs.next();
		rs.close();
		return result;
	}
	
	/**
	 * @param name
	 * @return
	 * @throws ApplicationException
	 * @throws SQLException
	 */
	@Deprecated
	public boolean getHasFunction(String name) throws ApplicationException, SQLException
	{
		assertDBString(name);
		//ResultSet rs = con.getMetaData().getFunctions(null, null, name.toUpperCase());
		//boolean result =  rs.next();
		//rs.close();
		
		//return result;
		return false;
	}
	
	public void setDBCacheSizeKB(int nDBCacheSize) throws ApplicationException, SQLException
	{
		PreparedStatement s;
		s=con.prepareStatement("SET CACHE_SIZE "+nDBCacheSize);
		s.execute();
	}
	
	private void rebuildCommonArchitecture() throws ApplicationException, SQLException
	{
		PreparedStatement s;
		s=con.prepareStatement("CREATE ALIAS IF NOT EXISTS stringSeparateFixedSpacingRight FOR \"org.jakz.common.Util.stringSeparateFixedSpacingRight\"");
		s.execute();
		s=con.prepareStatement("CREATE ALIAS IF NOT EXISTS stringSeparateFixedSpacingLeft FOR \"org.jakz.common.Util.stringSeparateFixedSpacingLeft\"");
		s.execute();
		s=con.prepareStatement("CREATE ALIAS IF NOT EXISTS NUM_MAX_DOUBLE FOR \"org.jakz.common.Util.numMaxDouble\"");
		s.execute();
		s=con.prepareStatement("CREATE ALIAS IF NOT EXISTS NUM_MIN_DOUBLE FOR \"org.jakz.common.Util.numMinDouble\"");
		s.execute();
		s=con.prepareStatement("CREATE ALIAS IF NOT EXISTS NUM_MAX_INTEGER FOR \"org.jakz.common.Util.numMaxInteger\"");
		s.execute();
		s=con.prepareStatement("CREATE ALIAS IF NOT EXISTS NUM_MIN_INTEGER FOR \"org.jakz.common.Util.numMinInteger\"");
		s.execute();
	}
	
	
	
	
	
	public DataCache dropTable(String tableName) throws SQLException, ApplicationException
	{
		PreparedStatement ps;
		boolean tableExists = getHasTable(tableName);
		if(tableExists)
		{
			ps=con.prepareStatement(constructDropStatement(tableName,true));
			ps.execute();
		}
		return this;
	}
	
	
	
	public DataCache enter(DataEntry entry) throws SQLException, ApplicationException
	{
		
		boolean tmpAutocommit = con.getAutoCommit();
		
		con.setAutoCommit(false);
		
		JSONObject row;
		JSONArray rows = entry.getRows();
		if(rows.length()<=0)
			return this;
		
		elementNameMap = entry.namemap;
		
		//construct required meta from rows
		for(int iRow=0; iRow<rows.length(); iRow++ )
		{
			row=rows.getJSONObject(iRow);
			JSONObject rowData = row.getJSONObject("data");
			JSONArray names = rowData.names();
			JSONArray elements = rowData.toJSONArray(names);
			/*
			if(!entry.has("path"))
				entry.putOnce("path",row.getString("path"));
			*/
			
			//shared variable init
			tableName=entry.getPath();
			int dotIndex = tableName.lastIndexOf('.');
			if(dotIndex>=0)
			{
				schemaName=tableName.substring(0, dotIndex);
				tableName=tableName.substring(dotIndex+1);
			}
			else
			{
				schemaName="PUBLIC";
			}
			
			
			for(int ie=0; ie<elements.length(); ie++)
			{
				JSONObject re = elements.getJSONObject(ie);
				JSONObject e = new JSONObject();
				
				if(!elementNameMap.containsKey(names.getString(ie)))
				{
					
					if(re.has("type"))
					{
						e.put("type", re.getInt("type"));
					}
					
					elementNameMap.put(names.getString(ie),e);
					
					/*
					if(re.has("index"))
					{
						if(re.getInt("index")<=elementNameMap.size())
						{
							e.put("index", re.getString("index"));
							elementNameMap.put(names.getString(ie),e,re.getInt("index"));
						}
					}
					else
					{
						elementNameMap.put(names.getString(ie),e);
					}
					*/
				}
			}
			
			if(elementNameMap.size()>=elements.length())
				break;
			
		}
			
		assertMeta();
		
		constructVariableNameListSQL();
		constructVariableDeclarationListSQL();
		
		PreparedStatement ps;
		boolean tableExists = getHasTable(tableName);
		
		if(tableExists&&!settingInsertIntoExistingTables)
		{
			return this;
		}
		
		if(!tableExists)
		{
			ps = con.prepareStatement(constructCreateStatement(tableName,variableDeclarationListSQL,null,entry.getMemory(),entry.getTemporary(),entry.getLocal()));
			ps.execute();
		}
		
		ps = con.prepareStatement(constructInsertStatement());
		
		for(int iRow=0; iRow<rows.length(); iRow++)
		{
			ps.clearParameters();
			row=rows.getJSONObject(iRow);
			rowDataNameMap = row.getJSONObject("data");
			assertRowMeta();
			
			ArrayList<String> names = elementNameMap.keys();
			ArrayList<JSONObject> elementMeta = elementNameMap.values();
			for(int iVar=0; iVar<elementMeta.size(); iVar++)
			{
				JSONObject e = elementMeta.get(iVar);
				if(rowDataNameMap.has(names.get(iVar))&&rowDataNameMap.getJSONObject(names.get(iVar)).has("value")&&!rowDataNameMap.getJSONObject(names.get(iVar)).isNull("value"))
				{
					JSONObject re = rowDataNameMap.getJSONObject(names.get(iVar));
					if(e.getInt("type")==java.sql.Types.BOOLEAN&&re.getInt("type")==java.sql.Types.BOOLEAN)
						ps.setBoolean(iVar+1, re.getBoolean("value"));
					else if(e.getInt("type")==java.sql.Types.INTEGER && re.getInt("type")==java.sql.Types.INTEGER)
						ps.setInt(iVar+1, re.getInt("value"));
					else if(e.getInt("type")==java.sql.Types.BIGINT && re.getInt("type")==java.sql.Types.BIGINT)
						ps.setInt(iVar+1, re.getInt("value"));
					else if(e.getInt("type")==java.sql.Types.DOUBLE && re.getInt("type")==java.sql.Types.DOUBLE)
						ps.setDouble(iVar+1, re.getDouble("value"));
					else if(e.getInt("type")==java.sql.Types.TIMESTAMP && re.getInt("type")==java.sql.Types.TIMESTAMP)
						ps.setDate(iVar+1, new Date(re.getLong("value")));
					else if(e.getInt("type")==java.sql.Types.VARCHAR && re.getInt("type")==java.sql.Types.VARCHAR)
						ps.setString(iVar+1, re.getString("value"));
					else throw new ApplicationException("Datatype coherencey error.");
				}
				else
				{
					if(e.getInt("type")==java.sql.Types.BOOLEAN)
						ps.setNull(iVar+1, java.sql.Types.BOOLEAN);
					else if(e.getInt("type")==java.sql.Types.BIGINT)
						ps.setNull(iVar+1, java.sql.Types.BIGINT);
					else if(e.getInt("type")==java.sql.Types.INTEGER)
						ps.setNull(iVar+1, java.sql.Types.INTEGER);
					else if(e.getInt("type")==java.sql.Types.DOUBLE)
						ps.setNull(iVar+1, java.sql.Types.DOUBLE);
					else if(e.getInt("type")==java.sql.Types.TIMESTAMP)
						ps.setNull(iVar+1, java.sql.Types.TIMESTAMP);
					else if(e.getInt("type")==java.sql.Types.VARCHAR)
						ps.setNull(iVar+1, java.sql.Types.LONGVARCHAR);
					else throw new ApplicationException("Datatype coherencey error.");
				}
			}
			ps.addBatch();
		}
		
		int[] batchRes = ps.executeBatch();
		if(tmpAutocommit)
			con.commit();
		con.setAutoCommit(tmpAutocommit);
		return this;
	}
	
	public DataCache enter(JSONObject entry) throws SQLException, ApplicationException
	{
		return enter(new DataEntry(entry));
	}
	
	public DataEntry get(String path) throws SQLException, ApplicationException
	{
		HashSet<String> emptyColumns = discernFilledColumns(path, false);
		PreparedStatement ps = con.prepareStatement("SELECT * FROM "+path);
		ResultSet rs = ps.executeQuery();
		DataEntry entry = new DataEntry(path,rs);
		
		for(int i=0; i<entry.getNamemapSize(); i++)
		{
			if(emptyColumns.contains(entry.namemap.getKeyAt(i)))
			{
				entry.namemap.getValueAt(i).put("empty", true);
			}
		}
		
		return entry;
	}
	
	private HashSet<String> discernFilledColumns(String path, boolean filled) throws SQLException, ApplicationException
	{
		PreparedStatement ps = con.prepareStatement("SELECT * FROM "+path);
		ResultSet rs = ps.executeQuery();
		ResultSetMetaData rsm = rs.getMetaData();
		int columnCount = rsm.getColumnCount();
		ArrayList<String> columnName = new ArrayList<String>();
		for(int i=1; i<=columnCount; i++)
		{
			columnName.add(rsm.getColumnName(i));
		}
		rs.close();
		
		HashSet<String> toReturn = new HashSet<String>();
		for(int i=0; i<columnCount;i++)
		{
			String name = columnName.get(i);
			ps = con.prepareStatement("SELECT _path."+name+" FROM "+path+" _path WHERE _path."+name+" IS NOT NULL LIMIT 1");
			rs = ps.executeQuery();
			boolean hasData = rs.next();
			rs.close();
			if(hasData&&filled)
				toReturn.add(name);
			else if (!hasData&&!filled)
				toReturn.add(name);
		}
		
		
		return toReturn;
		
	}
	
	public DataCache table(String name, String query) throws ApplicationException, SQLException
	{
		return table(name, query, false, false, false);
	}
	
	public DataCache table(String name, String query, boolean memory, boolean temporary, boolean local) throws ApplicationException, SQLException
	{
		assertDBString(name);
		assertDBStringReserved(name);
		
		if(getHasTable(name))
		{
			dropTable(name);
		}

		PreparedStatement ps = con.prepareStatement(constructCreateStatement(name.toUpperCase(), null, query, memory, temporary, local));
		ps.execute();
		return this;
	}
	
	public DataCache view(String name, String query) throws ApplicationException, SQLException
	{
		assertDBString(name);
		assertDBStringReserved(name);
		PreparedStatement ps = con.prepareStatement("CREATE OR REPLACE VIEW "+name.toUpperCase()+" AS "+query);
		ps.execute();
		return this;
	}
	
	public DataCache index(String tablename, String columnname) throws SQLException, ApplicationException
	{
		return index(tablename,columnname, tablename+"_"+columnname);
	}
	
	public DataCache index(String tablename, String columnname, String indexname) throws SQLException, ApplicationException
	{
		assertDBString(indexname);
		assertDBStringReserved(indexname);
		PreparedStatement ps = con.prepareStatement("CREATE INDEX IF NOT EXISTS "+indexname.toUpperCase()+" ON "+tablename.toUpperCase()+"("+columnname.toUpperCase()+")");
		ps.execute();
		return this;
	}
	
	public ArrayList<String> listDatasets() throws SQLException
	{
		ArrayList<String> toreturn = new ArrayList<String>();
		
		PreparedStatement ps = con.prepareStatement("SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='TABLE' OR TABLE_TYPE='VIEW'");
		ResultSet rs = ps.executeQuery();
		
		while(rs.next())
		{
			String tname = rs.getString("TABLE_NAME");
			toreturn.add(tname);
		}
		
		return toreturn;
	}
	
	public String scriptDoubleToVarchar(String columnName)
	{
		//return "TRIM(CAST(CAST(CAST(\""+columnName+"\" AS DECIMAL(25,0)) AS CHAR(38)) AS VARCHAR(32672)))";
		return " TRIM(CAST(CAST("+columnName.toUpperCase()+" AS DECIMAL(25,0)) AS VARCHAR(32672)))";
	}
	
	public String scriptSeparateFixedSpacingRight(String expression, String separator, int spacing)
	{
		return "STRINGSEPARATEFIXEDSPACINGRIGHT("+expression+",'"+separator+"',"+spacing+")";
	}
	
	public String scriptTwoSegmentOverlapCondition(String a0, String a1, String b0, String b1)
	{
		return "(("+a0+"<="+b0+" AND "+b0+"<="+a1+") OR ("+a0+"<="+b1+" AND "+b1+"<="+a1+") OR ("+b0+"<="+a0+" AND "+a0+"<="+b1+") OR ("+b0+"<="+a1+" AND "+a1+"<="+b1+"))";
	}
	
}
