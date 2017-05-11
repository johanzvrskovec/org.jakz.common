package org.jakz.common.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jakz.common.OperationException;
import org.jakz.common.TypedValue;

public class DBUtil 
{
	public static String quotateSSString(Object source)
	{
		if (source==null)
			return ""+null;
		else
			return "'"+source+"'";
	}
	
	public static TypedValue setTypedValueFromSQLResultSet(TypedValue target, ResultSet r, String columnLabel) throws SQLException, OperationException
	{
		if(target.getType()==java.sql.Types.INTEGER)
			target.setInteger(r.getInt(columnLabel));
		else if(target.getType()==java.sql.Types.DOUBLE)
			target.setDouble(r.getDouble(columnLabel));
		else if(target.getType()==java.sql.Types.BOOLEAN)
			target.setBoolean(r.getBoolean(columnLabel));
		else if(target.getType()==java.sql.Types.VARCHAR)
			target.setVarchar(r.getString(columnLabel));
		else if(target.getType()==java.sql.Types.NVARCHAR)
			target.setNvarchar(r.getString(columnLabel));
		else if(target.getType()==java.sql.Types.TIMESTAMP)
			target.setTimestamp(r.getTimestamp(columnLabel).getTime());
		else if(target.getType()==java.sql.Types.BIGINT)
			target.setBigint(r.getLong(columnLabel));
		else
			throw new OperationException("SQL type unknown to TypedValue");
		
		return target;
	}
	
	public static void setPreparedStatementParameterFromTypedValue(PreparedStatement s, int psParameterNumber, TypedValue source, StringBuilder debugQuery) throws SQLException, OperationException
	{	
		String newQuery = null;
		if(source.getType()==java.sql.Types.INTEGER)
		{
			if(source.getValueInteger()==null)
				s.setNull(psParameterNumber, java.sql.Types.INTEGER);
			else
				s.setInt(psParameterNumber, source.getValueInteger());
			if(debugQuery!=null)
				newQuery=debugQuery.toString().replaceFirst("\\?", ""+source.getValueInteger());
		}
		else if(source.getType()==java.sql.Types.DOUBLE)
		{
			if(source.getValueDouble()==null)
				s.setNull(psParameterNumber, java.sql.Types.DOUBLE);
			else
				s.setDouble(psParameterNumber, source.getValueDouble());
			if(debugQuery!=null)
				newQuery=debugQuery.toString().replaceFirst("\\?", ""+source.getValueDouble());
		}
		else if(source.getType()==java.sql.Types.BOOLEAN)
		{
			if(source.getValueBoolean()==null)
				s.setNull(psParameterNumber, java.sql.Types.BOOLEAN);
			else
				s.setBoolean(psParameterNumber, source.getValueBoolean());
			if(debugQuery!=null)
				newQuery=debugQuery.toString().replaceFirst("\\?", quotateSSString(source.getValueBoolean()));
		}
		else if(source.getType()==java.sql.Types.VARCHAR)
		{
			if(source.getValueVarchar()==null)
				s.setNull(psParameterNumber, java.sql.Types.VARCHAR);
			else
				s.setString(psParameterNumber, source.getValueVarchar());
			if(debugQuery!=null)
				newQuery=debugQuery.toString().replaceFirst("\\?", quotateSSString(source.getValueVarchar()));
		}
		else if(source.getType()==java.sql.Types.NVARCHAR)
		{
			if(source.getValueNVarchar()==null)
				s.setNull(psParameterNumber, java.sql.Types.NVARCHAR);
			else
				s.setNString(psParameterNumber, source.getValueNVarchar());
			if(debugQuery!=null)
				newQuery=debugQuery.toString().replaceFirst("\\?", quotateSSString(source.getValueNVarchar()));
		}
		else if(source.getType()==java.sql.Types.TIMESTAMP)
		{
			if(source.getValueTimestamp()==null)
				s.setNull(psParameterNumber, java.sql.Types.TIMESTAMP);
			else
				s.setTimestamp(psParameterNumber, new java.sql.Timestamp(source.getValueTimestamp()));
			if(debugQuery!=null)
				newQuery=debugQuery.toString().replaceFirst("\\?", quotateSSString(new java.sql.Timestamp(source.getValueTimestamp()).toString()));
		}
		else if(source.getType()==java.sql.Types.BIGINT)
		{
			if(source.getValueBigint()==null)
				s.setNull(psParameterNumber, java.sql.Types.BIGINT);
			else
				s.setLong(psParameterNumber, source.getValueBigint());
			if(debugQuery!=null)
				newQuery=debugQuery.toString().replaceFirst("\\?", ""+source.getValueBigint());
		}
		else
			throw new OperationException("SQL type unknown to TypedValue.");
		
		if(debugQuery!=null)
			debugQuery.replace(0, debugQuery.length(), newQuery);
	}
}
