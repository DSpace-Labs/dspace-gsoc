package org.dspace.metadata;

import java.util.Iterator;
import org.dspace.content.DSpaceObject;

public interface MetadataCollection extends Comparable<MetadataCollection>
{

    public Iterator<MetadataItem> getMetadata();

    public Iterator<MetadataItem> getMetadata( Selector s );

    public Iterator<MetadataItem> getMetadata( Predicate p );

    public Iterator<MetadataItem> getMetadata( DSpaceObject o );

    public Iterator<MetadataItem> getMetadata( Value v );
    
    public void addMetadata( MetadataItem ... items );
    
    public void addMetadata( DSpaceObject o, Predicate p, Value v );

}
