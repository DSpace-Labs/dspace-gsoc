package org.dspace.metadata;

public interface Selector extends MetadataItem
{

    public boolean matches( MetadataItem m );
    
    public boolean isValueMatcher();

}
