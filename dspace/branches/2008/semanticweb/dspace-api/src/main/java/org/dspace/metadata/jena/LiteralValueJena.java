package org.dspace.metadata.jena;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import org.dspace.metadata.LiteralValue;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

public class LiteralValueJena extends LiteralImpl implements LiteralValue
{
    
    public LiteralValueJena( Node n, ModelCom m) {
        super( n, m );
    }
    
    public LiteralValueJena( Node n, EnhGraph m ) {
        super( n, m );
    }
    
    public LiteralValueJena( String s ) {
        super( s );
    }

    public URIResource getDatatypeURIResource()
    {
        return MetadataFactory.createURIResource( getDatatypeURI() );
    }

    public int compareTo( Value o )
    {
        return o instanceof LiteralValue ? 
            ((LiteralValue)o).getLexicalForm().compareTo( getLexicalForm() ) : 1;
    }

    public boolean isLiteralValue()
    {
        return true;
    }
    
}