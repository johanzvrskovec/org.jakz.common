package org.jakz.common;

import org.jakz.common.util.ExceptionUtil;

public class OperationException extends Exception implements JSONObjectWriteAspect
{
    private static final long serialVersionUID = 0;
    protected long currentTimeMillis;

    public OperationException(String message) 
    {
        super(message);
        currentTimeMillis=System.currentTimeMillis();
    }

    public OperationException(Throwable cause) 
    {
        super(cause);
        currentTimeMillis=System.currentTimeMillis();
    }
    
    public OperationException(String message, Throwable cause) 
    {
        super(message,cause);
        currentTimeMillis=System.currentTimeMillis();
    }
    
    public OperationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) 
	{
		super(message, cause, enableSuppression, writableStackTrace);
		currentTimeMillis=System.currentTimeMillis();
	}

	@Override
	public JSONObject toJSONObject()
	{
		JSONObject toReturn = new JSONObject();
		toReturn.put("currentTimeMillis", currentTimeMillis);
		toReturn.put("message", getMessage());
		toReturn.put("stackTrace", ExceptionUtil.getStackTraceString(this));
		if(getCause()!=null)
			toReturn.put("cause", getCause().getMessage());
		
		return toReturn;
	}
}