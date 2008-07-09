package org.dspace.metadata.jena;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

public class URIResourceJena extends ResourceImpl implements URIResource
{

    public URIResourceJena()
    {
        super();
    }

    public URIResourceJena( ModelCom m )
    {
        super( m );
    }

    public URIResourceJena( Node n, EnhGraph m )
    {
        super( n, m );
    }

    public URIResourceJena( String uri )
    {
        super( uri );
    }

    public URIResourceJena( String nameSpace, String localName )
    {
        super( nameSpace, localName );
    }

    public URIResourceJena( String uri, ModelCom m )
    {
        super( uri, m );
    }

    public URIResourceJena( Resource r, ModelCom m )
    {
        super( r, m );
    }

    public URIResourceJena( String nameSpace, String localName, ModelCom m )
    {
        super( nameSpace, localName, m );
    }

    /**
     * Compare lexical form of URIs, if o a URIResource. Default to ordering
     * URIResource first.
     */
    public int compareTo( Value o )
    {
        return o instanceof URIResource ? 
                ( (URIResource) o ).getURI().compareTo( getURI() )
                : -1;
    }

    public boolean isLiteralValue()
    {
        return false;
    }

}
