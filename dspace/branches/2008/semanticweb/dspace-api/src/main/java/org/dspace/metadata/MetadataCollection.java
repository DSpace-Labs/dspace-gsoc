package org.dspace.metadata;

import java.util.Iterator;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;

public interface MetadataCollection
{

    public Iterator<MetadataItem> getMetadata();

    public Iterator<MetadataItem> getMetadata( Selector s );

    public Iterator<MetadataItem> getMetadata( Predicate p );

    public Iterator<MetadataItem> getAllMetadata( DSpaceObject o );

    public Iterator<MetadataItem> getMetadata( URIResource r );
    
    public void addMetadata( MetadataItem ... items ) throws AuthorizeException;
    
    public void addMetadata( DSpaceObject o, Predicate p, Value v ) throws AuthorizeException;
    
    public void addMetadata( DSpaceObject o, Predicate p, DSpaceObject v ) throws AuthorizeException;

}
