package org.dspace.metadata;

public interface Predicate extends Comparable<Predicate>
{

    public String getURI();

    public String getNameSpace();

    public String getLocalName();
    
}