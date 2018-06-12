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
	
	public T get()
	{
		return value;
	}
	
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
	
	
	/**
	 * Unclear if working
	 * @return
	 */
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
	
	public Enforced<T> maxLength(Integer enfLength)
	{
		this.enfMaxLength=enfLength;
		
		possiblyThrowErrorOnFailedEnforcementResult("max length", enforceMaxLength());
			
		
		return this;
	}
	
	 
	
	
}
