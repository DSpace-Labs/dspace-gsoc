package org.dspace.metadata;

import org.dspace.content.DSpaceObject;

public interface Selector
{

    public boolean matches( MetadataItem m );
    
    public boolean isValueMatcher();
    
    public DSpaceObject getSubject();
    
    public Predicate getPredicate();
    
    public Value getValue();

}
