package org.dspace.metadata.jena;

import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

public class URIResourceJena extends ResourceImpl implements URIResource
{

    public URIResourceJena( String uri )
    {
        super( uri );
    }

    public URIResourceJena( String uri, ModelCom m )
    {
        super( uri, m );
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
