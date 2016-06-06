package org.jakz.common;


public class ApplicationException extends Exception 
{
    private static final long serialVersionUID = 0;
    private Throwable cause;

    public ApplicationException(String message) 
    {
        super(message);
    }

    public ApplicationException(Throwable cause) 
    {
        super(cause.getMessage());
        this.cause = cause;
    }
    
    public ApplicationException(String message, Throwable cause) 
    {
        super(message);
        this.cause = cause;
    }
    
    public ApplicationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) 
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

    public Throwable getCause() 
    {
        return this.cause;
    }
}
