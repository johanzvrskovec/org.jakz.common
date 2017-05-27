package org.jakz.common;

public class TestFailedException extends Exception 
{
    private static final long serialVersionUID = 0;
    private Throwable cause;

    public TestFailedException(String message) 
    {
        super(message);
    }

    public TestFailedException(Throwable cause) 
    {
        super(cause.getMessage());
        this.cause = cause;
    }
    
    public TestFailedException(String message, Throwable cause) 
    {
        super(message);
        this.cause = cause;
    }
    
    public TestFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) 
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

    public Throwable getCause() 
    {
        return this.cause;
    }
}