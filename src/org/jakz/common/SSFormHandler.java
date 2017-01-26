package org.jakz.common;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SSFormHandler
{
	
	//TODO use modified DataEntry? Merge DataEntry and Form
	public static Form[] produceFormFromDBTable(String tableName, boolean withData, int dataLimit, Connection c) throws SQLException, ApplicationException
	{
		ArrayList<Form> rowForms = new ArrayList<Form>();
		
		Statement s = c.createStatement();
		ResultSet result; 
		if(withData)
			result =  s.executeQuery("SELECT TOP "+dataLimit+" * FROM "+tableName);
		else
			result =  s.executeQuery("SELECT TOP 1 * FROM "+tableName);
		
		ResultSetMetaData resultMeta = result.getMetaData();
		for(int rowi=0; result.next(); rowi++)
		{
			Form rowForm = new Form(""+rowi,Form.FieldType.FORM);
			rowForm.name=""+rowi;
			int colCount = resultMeta.getColumnCount();
			System.out.println("col count = "+colCount);
			for(int coli=1; coli<=colCount; coli++)  //index from 1
			{
				String columnName = resultMeta.getColumnName(coli);
				int columnType = resultMeta.getColumnType(coli);
				String columnTypeName = resultMeta.getColumnTypeName(coli);
				int columnNullable = resultMeta.isNullable(coli);
				int jdbcnvarcharordinal = JDBCType.NVARCHAR.ordinal();
				int jdbcvarcharordinal = JDBCType.VARCHAR.ordinal();
				Form columnForm = new Form(tableName+"."+columnName, Form.FieldType.QUERY);
				TypedValue tv = new TypedValue(columnType);
				
				//dummy
				result.getObject(coli);
				if(!result.wasNull())
				{	
					if(columnType==java.sql.Types.BOOLEAN)
					{
						tv.setBoolean(result.getBoolean(coli));
					}
					else if(columnType==java.sql.Types.TIMESTAMP)
					{
						tv.setTimestamp(result.getTimestamp(coli).getTime());
					}
					else if(columnType==java.sql.Types.BIGINT)
					{
						tv.setBigint(result.getLong(coli));
					}
					else if(columnType==java.sql.Types.INTEGER||columnType==java.sql.Types.SMALLINT)
					{
						tv.setInteger(result.getInt(coli));
					}
					else if(columnType==java.sql.Types.DOUBLE||columnType==java.sql.Types.DECIMAL)
					{
						tv.setDouble(result.getDouble(coli));
					}
					else if(columnType==java.sql.Types.VARCHAR)
					{
						tv.setVarchar(result.getString(coli));
					}
					else if(columnType==java.sql.Types.NVARCHAR||columnType==-16) //TODO why does SQL server give -16 for nvarchar(max)?
					{
						tv.setNvarchar(result.getString(coli));
					}
					else throw new ApplicationException("Could not parse Form value of table "+tableName+", column "+columnName+" with type "+columnType+" at relative line index "+rowi);
				}
				
				columnForm.value.add(tv);
				rowForm.addContent(columnForm);
			}
			
			rowForms.add(rowForm);
		}
		return rowForms.toArray(new Form[rowForms.size()]);
	}

}
