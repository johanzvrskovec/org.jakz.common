package org.jakz.common;

public class MynionException extends Exception 
{
    private static final long serialVersionUID = 0;
    private Throwable cause;

    public MynionException(String message) 
    {
        super(message);
    }

    public MynionException(Throwable cause) 
    {
        super(cause.getMessage());
        this.cause = cause;
    }
    
    public MynionException(String message, Throwable cause) 
    {
        super(message);
        this.cause = cause;
    }

    public Throwable getCause() 
    {
        return this.cause;
    }
}