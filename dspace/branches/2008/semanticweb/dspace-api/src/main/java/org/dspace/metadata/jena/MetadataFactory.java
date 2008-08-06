package org.dspace.metadata.jena;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import java.sql.SQLException;
import java.util.Calendar;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.LiteralValue;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

public class MetadataFactory
{    

    public static URIResource createURIResource( String uriref )
    {
        return new URIResourceJena( uriref );
    }

    public static URIResource createURIResource( Resource r )
    {
        if ( !r.isURIResource() )
            throw new UnsupportedOperationException( 
                    "Cannot create non-URI resources" );
        return new URIResourceJena( r.getURI(), (ModelCom)r.getModel() );
    }

    public static LiteralValue createPlainLiteral( String string )
    {
        return new LiteralValueJena( string );
    }

    public static LiteralValue createTypedLiteral( String string, RDFDatatype dType )
    {
        return new LiteralValueJena( Node.createLiteral( string, "", dType ),
                                     null );
    }

    public static LiteralValue createTypedLiteral( String string, String lang, RDFDatatype dType )
    {
        return new LiteralValueJena( Node.createLiteral( string, lang, dType ),
                                     null );
    }

    public static LiteralValue createTypedLiteral( Object value )
    {
        LiteralLabel ll = null;
        if ( value instanceof Calendar )
        {
            Object valuec = new XSDDateTime( (Calendar) value );
            ll = new LiteralLabel( valuec, "", XSDDatatype.XSDdateTime );
        } else
        {
            ll = new LiteralLabel( value );
        }
        return new LiteralValueJena( Node.createLiteral( ll ), null );
    }
    
    public static LiteralValue createLiteral( Literal l )
    {
        return new LiteralValueJena( l.asNode(), null );
    }

    public static Predicate createPredicate( Context c, String uriref )
    {
        return new PredicateJena( c, uriref );
    }

    public static Predicate createPredicate( Context c, Property p )
    {
        return new PredicateJena( c, p.getURI() );
    }

    public static Predicate createPredicate( Context c, String namespace, 
            String localName )
    {
        return new PredicateJena( c, expand( namespace, c ), localName );
    }

    public static Predicate createPredicate( Context c, String schema, 
            String element, String qualifier )
    {
        return new PredicateJena( c, expand( schema + ":" + element + 
                (qualifier==null ? "" : "." + qualifier), c ) );
    }
    
    public static String expand( String r, Context c )
    {
        try
        {
            GlobalDAOJena dao = c.getGlobalDAO() instanceof GlobalDAOJena
                    ? (GlobalDAOJena) c.getGlobalDAO()
                    : new GlobalDAOJena();
            return dao.getTripleStore().expandPrefix( r );
        } catch ( SQLException ex )
        {
            Logger.getLogger( MetadataFactory.class ).error( ex );
        }
        return r;
    }
    
    public static MetadataItem createItem( URIResource o, Predicate p, Value v )
    {
        return new MetadataItemJena( o, p, v );
    }
    
    public static MetadataItem createItem( URIResource o, Predicate p, DSpaceObject v )
    {
        return new MetadataItemJena( o, p, v );
    }

}
