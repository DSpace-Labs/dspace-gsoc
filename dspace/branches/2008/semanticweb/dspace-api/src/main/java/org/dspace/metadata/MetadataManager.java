package org.dspace.metadata;

import org.dspace.content.DSpaceObject;

public interface MetadataManager
{
    
    public MetadataCollection getMetadata( DSpaceObject o );
    
    public MetadataCollection getMetadata( DSpaceObject o, int depth );
    
    public MetadataCollection getMetadata( Selector s );
    
    public MetadataCollection getMetadata( Selector s, int depth );
    
    public void addMetadata( MetadataItem ... is );
    
    public void addMetadata( MetadataCollection coll );
    
    public void addMetadata( DSpaceObject o, Predicate p, Value v );
    
    public void addMetadata( DSpaceObject o, Predicate p, DSpaceObject v );
    
    public void removeMetadata( Selector s );
    
    public void removeAllMetadata( DSpaceObject o );
    
    public void beginTransaction();
    
    public void abortTransaction();
    
    public void commitTransaction();
    
}