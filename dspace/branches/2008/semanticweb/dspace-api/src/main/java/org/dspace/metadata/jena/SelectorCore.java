package org.dspace.metadata.jena;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.Selector;
import org.dspace.metadata.Value;


public class SelectorCore extends MetadataItemJena implements Selector
{
    
    public SelectorCore()
    {
        super( null, null, (Value)null );
    }
    
    public SelectorCore( MetadataItem m )
    {
        super( m );
    }

    public boolean matches( MetadataItem m )
    {
        if ( !(getSubject() == null || getSubject().equals( m.getSubject() )) )
            return false;
        if ( !(getPredicate() == null || getPredicate().equals( m.getPredicate() )) )
            return false;
        if ( !(getValue() == null || getValue().equals( m.getValue() )) )
            return false;
        if ( !(getDSpaceObject() == null || getDSpaceObject().equals( m.getDSpaceObject() )) )
            return false;
        return true;
    }

    public boolean isValueMatcher()
    {
        return true;
    }
    
    public static com.hp.hpl.jena.rdf.model.Selector toJenaSelector( 
            final Selector o, final StatementTranslator tran ) {
        final Statement stat = tran.translate( o );
        return new com.hp.hpl.jena.rdf.model.Selector() {

            public boolean test( Statement s )
            {
                return o.matches( tran.translate( s ) );
            }

            public boolean isSimple()
            {
                return o.isValueMatcher();
            }

            public Resource getSubject()
            {
                return stat.getSubject();
            }

            public Property getPredicate()
            {
                return stat.getPredicate();
            }

            public RDFNode getObject()
            {
                return stat.getObject();
            }
            
        };
    }

}
