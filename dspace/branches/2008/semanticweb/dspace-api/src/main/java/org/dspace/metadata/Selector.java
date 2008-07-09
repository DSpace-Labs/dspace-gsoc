package org.dspace.metadata;

import org.dspace.content.DSpaceObject;

public interface Selector extends MetadataItem
{

    public boolean matches( MetadataItem m );
    
    public boolean isValueMatcher();
    
    public DSpaceObject getDSpaceObject();

}
