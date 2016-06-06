package org.jakz.common;

public class WorkableFailureException extends Exception 
{
    private static final long serialVersionUID = 0;
    private Throwable cause;

    public WorkableFailureException(String message) 
    {
        super(message);
    }

    public WorkableFailureException(Throwable cause) 
    {
        super(cause.getMessage());
        this.cause = cause;
    }
    
    public WorkableFailureException(String message, Throwable cause) 
    {
        super(message);
        this.cause = cause;
    }

    public Throwable getCause() 
    {
        return this.cause;
    }
}
