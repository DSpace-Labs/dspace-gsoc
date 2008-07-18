package org.dspace.metadata.jena;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.metadata.LiteralValue;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.MetadataManager;
import org.dspace.metadata.MetadataManagerFactory;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

public class MetadataItemJena implements MetadataItem
{
    
    private URIResource subj;
    private Predicate pred;
    private Value val;
    private DSpaceObject obj;
    private MetadataManager manager;

    public MetadataItemJena( MetadataManager m, URIResource o, Predicate p, Value v )
    {
        this( o, p, v );
        manager = m;
    }

    public MetadataItemJena( MetadataManager m, URIResource o, Predicate p, 
            DSpaceObject v )
    {
        this( o, p, v );
        manager = m;
    }

    public MetadataItemJena( URIResource o, Predicate p, Value v )
    {
        subj = o;
        pred = p;
        val = v;
    }

    public MetadataItemJena( URIResource o, Predicate p, DSpaceObject v )
    {
        subj = o;
        pred = p;
        obj = v;
    }

    public MetadataItemJena( MetadataItem m )
    {
        subj = m.getSubject();
        pred = m.getPredicate();
        obj = m.getDSpaceObject();
        val = m.getValue();
    }
    
    public void setManager( MetadataManager m )
    {
        this.manager = m;
    }

    public URIResource getSubject()
    {
        return subj;
    }

    public Predicate getPredicate()
    {
        return pred;
    }

    public Value getValue()
    {
        return val;
    }

    public boolean isLiteral()
    {
        return val != null && val instanceof LiteralValue;
    }

    public LiteralValue getLiteralValue()
    {
        return isLiteral() ? (LiteralValue)val : null;
    }

    public boolean isURI()
    {
        return val != null && val instanceof URIResource;
    }

    public URIResource getURIResource()
    {
        return isLiteral() ? null : (URIResource)val;
    }

    public boolean isDSpaceObject()
    {
        return obj != null;
    }

    public DSpaceObject getDSpaceObject()
    {
        return obj;
    }

    public void remove() throws AuthorizeException
    {
        getManager().removeMetadata( new SelectorCore( this ) );
    }

    public int compareTo( MetadataItem o )
    {
        int v = o.getSubject().compareTo( subj );
        if ( v != 0 )
            return v;
        v = o.getPredicate().compareTo( pred );
        return v != 0 ? v : 
            (val == null ? 
                obj.getIdentifier().getUUID().compareTo( 
                        o.getDSpaceObject().getIdentifier().getUUID() ) 
                : o.getLiteralValue().compareTo( val ) );
    }
    
    @Override
    public String toString()
    {
        return "[" + (subj == null ? "null" : subj.getURI()) + ", " + pred + ", " 
                + (isDSpaceObject() ? "(DSO) " + (obj == null ? "null" : obj.getURI()) : "(Value) " + val) + "]";
    }
    
    private MetadataManager getManager() {
            if ( manager == null )
                manager = MetadataManagerFactory.get();
        return manager;
    }
    
}