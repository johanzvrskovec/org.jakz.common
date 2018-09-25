package org.jakz.common.form;

public class FormException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Throwable cause;

    public FormException(String message) 
    {
        super(message);
    }

    public FormException(Throwable cause) 
    {
        super(cause.getMessage());
        this.cause = cause;
    }
    
    public FormException(String message, Throwable cause) 
    {
        super(message);
        this.cause = cause;
    }
    
    public FormException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) 
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

    public Throwable getCause() 
    {
        return this.cause;
    }
}
