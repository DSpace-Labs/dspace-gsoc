package org.dspace.statistics.dao;

public class SearchItemException extends Exception{
	public SearchItemException()
    {
            super();
    }

    public SearchItemException(String message)
    {
            super(message);
    }

    public SearchItemException(String message, Throwable cause)
    {
            super(message, cause);
    }

    public SearchItemException(Throwable cause)
    {
            super(cause);
    }
}
