package org.dspace.metadata;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;

public interface MetadataItem extends Comparable<MetadataItem>
{
    
    public void setManager( MetadataManager m );

    public DSpaceObject getSubject();

    public Predicate getPredicate();

    public Value getValue();

    public boolean isLiteral();

    public LiteralValue getLiteralValue();

    public boolean isURI();

    public URIResource getURIResource();

    public boolean isDSpaceObject();

    public DSpaceObject getDSpaceObject();
    
    public void remove() throws AuthorizeException;

}
