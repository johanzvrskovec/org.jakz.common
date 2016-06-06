package org.jakz.common;

public class ParallelWorkerException extends Exception 
{
    private static final long serialVersionUID = 0;
    private Throwable cause;

    public ParallelWorkerException(String message) 
    {
        super(message);
    }

    public ParallelWorkerException(Throwable cause) 
    {
        super(cause.getMessage());
        this.cause = cause;
    }
    
    public ParallelWorkerException(String message, Throwable cause) 
    {
        super(message);
        this.cause = cause;
    }

    public Throwable getCause() 
    {
        return this.cause;
    }
}