package org.jakz.common;

import java.util.Collection;

/**
 * Experimental class for dealing with data with restrictions 
 * @author johkal
 *
 * @param <T>
 */
public class Enforced<T>
{
	private T value;
	
	private boolean settingExceptionOnFail = true;
	
	//Objects
	private boolean enfNotNull =false;
	
	//String etc
	private Integer enfMaxLength;
	
	//Numerical
	private Long enfMaxNumValInt;
	private Double enfMaxNumValFloat;
	private Long enfMinNumValInt;
	private Double enfMinNumValFloat;
	
	public Enforced(T value)
	{
		this.value=value;
	}
	
	public Enforced<T> set(T value)
	{
		this.value=value;
		enforceAll();
		return this;
	}
	
	public T get() {return value;}
	
	public String toString() {return get().toString();}
	
	public Enforced<T> setExceptionOnFail(boolean settingExceptionOnFail)
	{
		this.settingExceptionOnFail=settingExceptionOnFail;
		return this;
	}
	
	private boolean possiblyThrowErrorOnFailedEnforcementResult(String enforcementLabel, boolean result)
	{
		if(settingExceptionOnFail&&!result)
		{
			throw new NumberFormatException("Enforcement failure, enforcing "+enforcementLabel);
		}
		
		return result;
	}
	
	private boolean enforceAll()
	{
		boolean maxLength =false;
		if(enfMaxLength!=null)
			maxLength=possiblyThrowErrorOnFailedEnforcementResult("max length", enforceMaxLength());
		
		return maxLength;
	}
	
	private boolean enforceMaxLength() throws UnsupportedOperationException
	{
		if(value==null)
			return true;
		
		if(value instanceof String)
		{
			if(((String)value).length()<=enfMaxLength)
				return true;
			else
				return false;
		}
		
		if(value instanceof Object[])
		{
			if(((Object[])value).length<=enfMaxLength)
				return true;
			else
				return false;
		}
		
		if(value instanceof Collection)
		{
			if(((Collection)value).size()<=enfMaxLength)
				return true;
			else
				return false;
		}
		
		throw new UnsupportedOperationException("The datatype does not support this restriction.");
	}
	
	private boolean enforceMaxValue()
	{
		if(value==null)
			return true;
		
		if(value instanceof Number)
		{
			if(enfMaxNumValInt!=null)
			{
				if(((Number) value).longValue()<=enfMaxNumValInt)
					return true;
				else
					return false;
			}
			else if(enfMaxNumValFloat!=null)
			{
				if(((Number) value).doubleValue()<=enfMaxNumValFloat)
					return true;
				else
					return false;
			}
				
		}
		
		throw new UnsupportedOperationException("The datatype does not support this restriction.");
	}
	
	private boolean enforceMinValue()
	{
		if(value==null)
			return true;
		
		if(value instanceof Number)
		{
			if(enfMinNumValInt!=null)
			{
				if(((Number) value).longValue()>=enfMinNumValInt)
					return true;
				else
					return false;
			}
			else if(enfMinNumValFloat!=null)
			{
				if(((Number) value).doubleValue()>=enfMinNumValFloat)
					return true;
				else
					return false;
			}
				
		}
		
		throw new UnsupportedOperationException("The datatype does not support this restriction.");
	}
	
	private boolean enforceNotNullValue()
	{
		if(enfNotNull&&value==null)
			return false;
		else
			return true;	
	}
	
	public Enforced<T> maxLength(Integer enfLength)
	{
		this.enfMaxLength=enfLength;
		
		possiblyThrowErrorOnFailedEnforcementResult("max length", enforceMaxLength());
			
		
		return this;
	}
	
	public Enforced<T> maxNumVal(Long enfMaxNumVal)
	{
		this.enfMaxNumValInt =enfMaxNumVal;
		
		possiblyThrowErrorOnFailedEnforcementResult("max value", enforceMaxValue());
			
		return this;
	}
	
	public Enforced<T> maxNumVal(Integer enfMaxNumVal)
	{
		this.enfMaxNumValInt = enfMaxNumVal.longValue();
		
		possiblyThrowErrorOnFailedEnforcementResult("max value", enforceMaxValue());
			
		return this;
	}
	 
	public Enforced<T> maxNumVal(Double enfMaxNumVal)
	{
		this.enfMaxNumValFloat =enfMaxNumVal;
		
		possiblyThrowErrorOnFailedEnforcementResult("max value", enforceMaxValue());
			
		return this;
	}
	
	public Enforced<T> minNumVal(Long enfMinNumVal)
	{
		this.enfMinNumValInt =enfMinNumVal;
		
		possiblyThrowErrorOnFailedEnforcementResult("min value", enforceMinValue());
			
		return this;
	}
	
	public Enforced<T> minNumVal(Integer enfMinNumVal)
	{
		this.enfMinNumValInt =enfMinNumVal.longValue();
		
		possiblyThrowErrorOnFailedEnforcementResult("min value", enforceMinValue());
			
		return this;
	}
	 
	public Enforced<T> minNumVal(Double enfMinNumVal)
	{
		this.enfMinNumValFloat =enfMinNumVal;
		
		possiblyThrowErrorOnFailedEnforcementResult("min value", enforceMinValue());
			
		return this;
	}
	
	public Enforced<T> notNull(boolean notNull)
	{
		this.enfNotNull=notNull;
		
		possiblyThrowErrorOnFailedEnforcementResult("not null value", enforceNotNullValue());
			
		return this;
	}
	
	public Enforced<T> notNull()
	{
		return notNull(true);
	}
	
}
