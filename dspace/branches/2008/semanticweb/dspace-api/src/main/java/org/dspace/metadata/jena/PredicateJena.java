package org.dspace.metadata.jena;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.dspace.metadata.Predicate;

public class PredicateJena extends ResourceImpl implements Predicate
{

    public PredicateJena()
    {
        super();
    }

    public PredicateJena( ModelCom m )
    {
        super( m );
    }

    public PredicateJena( Node n, EnhGraph m )
    {
        super( n, m );
    }

    public PredicateJena( String uri )
    {
        super( uri );
    }

    public PredicateJena( String nameSpace, String localName )
    {
        super( nameSpace, localName );
    }

    public PredicateJena( String uri, ModelCom m )
    {
        super( uri, m );
    }

    public PredicateJena( Resource r, ModelCom m )
    {
        super( r, m );
    }

    public PredicateJena( String nameSpace, String localName, ModelCom m )
    {
        super( nameSpace, localName, m );
    }

    public int compareTo( Predicate o )
    {
        return o.getURI().compareTo( getURI() );
    }
    
}