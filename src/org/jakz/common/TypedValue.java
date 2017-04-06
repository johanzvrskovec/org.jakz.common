package org.jakz.common;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Typically used for values that has to be serialized together with its type
 * @author johkal
 *
 */
public class TypedValue implements JSONObjectReadAspect, JSONObjectWriteAspect
{
	/**
	 * Convention: as java.sql.Types
	 */
	private Integer type;
	private String valueString;
	private Integer valueInteger;
	private Double valueDouble;
	private Long valueLong;
	
	private Integer sizeLimit;
	
	public boolean required,nullable;
	
	private static String valueNotNullableExceptionString ="Value is not nullable";
	
	private void init()
	{
		type=null;
		valueString=null;
		valueInteger=null;
		valueDouble=null;
		valueLong=null;
		sizeLimit=null;
		
		required=false;
		nullable=false;
	}
	
	public TypedValue scopy(TypedValue source)
	{
		type=source.type;
		valueString=source.valueString;
		valueInteger=source.valueInteger;
		valueDouble=source.valueDouble;
		valueLong=source.valueLong;
		
		sizeLimit=source.sizeLimit;
		required=source.required;
		nullable=source.nullable;
		
		return this;
	}
	
	public TypedValue createNewScopy()
	{
		TypedValue toReturn = new TypedValue();
		toReturn.scopy(this);
		return toReturn;
	}
	
	public TypedValue() 
	{
		init();
	}
	
	public TypedValue(Integer nType) 
	{
		//init();
		setType(nType);
	}
	
	public TypedValue(Integer nType, Integer nSizeLimit) 
	{
		//init();
		setType(nType);
		setSizeLimit(nSizeLimit);
	}
	
	/**
	 * Also resets any stored values.
	 * @param nType
	 * @return
	 */
	public TypedValue setType(Integer nType)
	{
		init();
		type=nType;
		return this;
	}
	
	public TypedValue setInteger(Integer nInteger)
	{
		if(!nullable&&nInteger==null)
			throw new NumberFormatException(valueNotNullableExceptionString);
		init();
		valueInteger=nInteger;
		type=java.sql.Types.INTEGER;
		return this;
	}
	
	public TypedValue setBoolean(Boolean nBoolean)
	{
		if(!nullable&&nBoolean==null)
			throw new NumberFormatException(valueNotNullableExceptionString);
		init();
		if(nBoolean!=null)
		{
			if(nBoolean)
				valueInteger=1;
			else
				valueInteger=0;
		}
		type=java.sql.Types.BOOLEAN;
		return this;
	}
	
	public TypedValue setVarchar(String nVarchar)
	{
		if(!nullable&&nVarchar==null)
			throw new NumberFormatException(valueNotNullableExceptionString);
		init();
		valueString=nVarchar;
		type=java.sql.Types.VARCHAR;
		return this;
	}
	
	public TypedValue setNvarchar(String nNvarchar)
	{
		if(!nullable&&nNvarchar==null)
			throw new NumberFormatException(valueNotNullableExceptionString);
		init();
		valueString=nNvarchar;
		type=java.sql.Types.NVARCHAR;
		return this;
	}
	
	public TypedValue setTimestamp(Long nTimestamp)
	{
		if(!nullable&&nTimestamp==null)
			throw new NumberFormatException(valueNotNullableExceptionString);
		init();
		valueLong=nTimestamp;
		type=java.sql.Types.TIMESTAMP;
		return this;
	}
	
	public TypedValue setBigint(Long nBigint)
	{
		if(!nullable&&nBigint==null)
			throw new NumberFormatException(valueNotNullableExceptionString);
		init();
		valueLong=nBigint;
		type=java.sql.Types.BIGINT;
		return this;
	}
	
	public TypedValue setDouble(Double nDouble)
	{
		if(!nullable&&nDouble==null)
			throw new NumberFormatException(valueNotNullableExceptionString);
		init();
		valueDouble=nDouble;
		type=java.sql.Types.DOUBLE;
		return this;
	}
	
	public TypedValue setSizeLimit(Integer nSizeLimit)
	{
		sizeLimit=nSizeLimit;
		return this;
	}
	
	public TypedValue setRequired(boolean nRequired)
	{
		required=nRequired;
		return this;
	}
	
	public TypedValue setNullable(boolean nNullable)
	{
		nullable=nNullable;
		return this;
	}
	
	public Integer getType() {return type;}
	public Integer getValueInteger() {return valueInteger;}
	public Boolean getValueBoolean() {return valueInteger!=0;}
	public String getValueVarchar() {return valueString;}
	public String getValueNVarchar() {return valueString;}
	public Long getValueTimestamp() {return valueLong;}
	public Long getValueBigint() {return valueLong;}
	public Double getValueDouble() {return valueDouble;}
	
	public Integer getSizeLimit() {return sizeLimit;}
	
	public TypedValue setValueFromSQLResultSet(ResultSet r, String columnLabel) throws SQLException, OperationException
	{
		if(type==java.sql.Types.INTEGER)
			setInteger(r.getInt(columnLabel));
		else if(type==java.sql.Types.DOUBLE)
			setDouble(r.getDouble(columnLabel));
		else if(type==java.sql.Types.BOOLEAN)
			setBoolean(r.getBoolean(columnLabel));
		else if(type==java.sql.Types.VARCHAR)
			setVarchar(r.getString(columnLabel));
		else if(type==java.sql.Types.NVARCHAR)
			setNvarchar(r.getString(columnLabel));
		else if(type==java.sql.Types.TIMESTAMP)
			setTimestamp(r.getTimestamp(columnLabel).getTime());
		else if(type==java.sql.Types.BIGINT)
			setBigint(r.getLong(columnLabel));
		else
			throw new OperationException("SQL type unknown to TypedValue");
		
		return this;
	}

	@Override
	public JSONObject toJSONObject() 
	{
		JSONObject toreturn = new JSONObject();
		toreturn.put("type", type);
		if(type==java.sql.Types.INTEGER)
			toreturn.put("value", valueInteger);
		else if(type==java.sql.Types.DOUBLE)
			toreturn.put("value", valueDouble);
		else if(type==java.sql.Types.BOOLEAN)
			toreturn.put("value", valueInteger!=0);
		else if(type==java.sql.Types.VARCHAR||type==java.sql.Types.NVARCHAR)
			toreturn.put("value", valueString);
		else if(type==java.sql.Types.TIMESTAMP)
			toreturn.put("value", valueLong);
		else if(type==java.sql.Types.BIGINT)
			toreturn.put("value", valueLong);
		
		toreturn.put("sizeLimit", sizeLimit);
		
		toreturn.put("required", required);
		toreturn.put("nullable", nullable);
		
		return toreturn;
	}

	@Override
	public void fromJSONObject(JSONObject source) 
	{
		init();
		type=source.getInt("type");
		if(source.has("value"))
		{
			if(type==java.sql.Types.INTEGER)
				setInteger(source.getInt("value"));
			else if(type==java.sql.Types.DOUBLE)
				setDouble(source.getDouble("value"));
			else if(type==java.sql.Types.BOOLEAN)
				setBoolean(source.getBoolean("value"));
			else if(type==java.sql.Types.VARCHAR||type==java.sql.Types.NVARCHAR)
				setVarchar(source.getString("value"));
			else if(type==java.sql.Types.TIMESTAMP)
				setTimestamp(source.getLong("value"));
			else if(type==java.sql.Types.BIGINT)
				setBigint(source.getLong("value"));
			
			if(source.has("sizeLimit"))
				sizeLimit=source.getInt("sizeLimit");
			required=source.optBoolean("required");
			nullable=source.optBoolean("nullable");
		}	
	}
	
}
