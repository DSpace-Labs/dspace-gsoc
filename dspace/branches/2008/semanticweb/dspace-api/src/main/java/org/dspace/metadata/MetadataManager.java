package org.dspace.metadata;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;

public interface MetadataManager
{
    
    public MetadataCollection getMetadata( DSpaceObject o ) throws AuthorizeException;
    
    public MetadataCollection getMetadata( DSpaceObject o, int depth ) throws AuthorizeException;
    
    public MetadataCollection getMetadata( Selector s ) throws AuthorizeException;
    
    public MetadataCollection getMetadata( Selector s, int depth ) throws AuthorizeException;
    
    public void addMetadata( MetadataItem ... is ) throws AuthorizeException;
    
    public void addMetadata( MetadataCollection coll ) throws AuthorizeException;
    
    public void addMetadata( DSpaceObject o, Predicate p, Value v ) throws AuthorizeException;
    
    public void addMetadata( DSpaceObject o, Predicate p, DSpaceObject v ) throws AuthorizeException;
    
    public void removeMetadata( Selector s ) throws AuthorizeException;
    
    public void removeAllMetadata( DSpaceObject o ) throws AuthorizeException;
    
    public void beginTransaction();
    
    public void abortTransaction();
    
    public void commitTransaction();
    
}