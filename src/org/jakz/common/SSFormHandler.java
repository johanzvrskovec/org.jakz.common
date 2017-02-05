package org.jakz.common;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jakz.common.Form.FieldType;

public class SSFormHandler
{
	//TODO use modified DataEntry? Merge DataEntry and Form
	public static Form produceFormFromDBTable(String tableName, boolean withData, int dataLimit, Connection c) throws SQLException, ApplicationException
	{
		Form masterForm = new Form(tableName,FieldType.FORM);
		
		Statement s = c.createStatement();
		ResultSet result;
		
		//TODO change for more secure handling of arguments. Maybe use the query building package from TIEFighter?
		if(withData)
			result =  s.executeQuery("SELECT TOP "+dataLimit+" * FROM "+tableName);
		else
			result =  s.executeQuery("SELECT TOP 1 * FROM "+tableName);
		
		ResultSetMetaData resultMeta = result.getMetaData();
		for(int rowi=0; result.next(); rowi++)
		{
			Form rowForm = new Form(tableName+"_"+rowi,Form.FieldType.QUERY);
			rowForm.name=""+rowi;
			int colCount = resultMeta.getColumnCount();
			for(int coli=1; coli<=colCount; coli++)  //index from 1
			{
				String columnName = resultMeta.getColumnName(coli);
				Form columnForm = new Form(columnName, Form.FieldType.QUERY);
				columnForm.name=columnName;
				
				int columnType = resultMeta.getColumnType(coli);
				String columnTypeName = resultMeta.getColumnTypeName(coli);
				int resultSetNullability = resultMeta.isNullable(coli);
				if(ResultSetMetaData.columnNoNulls==resultSetNullability)
					columnForm.nullable=false;
				else if(ResultSetMetaData.columnNullable==resultSetNullability)
					columnForm.nullable=true;
				else if(ResultSetMetaData.columnNullableUnknown==resultSetNullability)
					columnForm.nullable=true;
				else throw new ApplicationException("Unrecognizeable nullability status when parsing Form");
				//int jdbcnvarcharordinal = JDBCType.NVARCHAR.ordinal();
				//int jdbcvarcharordinal = JDBCType.VARCHAR.ordinal();
				
				
				columnForm.writeable=resultMeta.isWritable(coli);
				
				TypedValue tv = new TypedValue(columnType);
				
				if(withData)
				{
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
				}
				
				columnForm.value.add(tv);
				
				rowForm.addContent(columnForm);
			}
			
			masterForm.addContent(rowForm);
		}
		return masterForm;
	}

}
