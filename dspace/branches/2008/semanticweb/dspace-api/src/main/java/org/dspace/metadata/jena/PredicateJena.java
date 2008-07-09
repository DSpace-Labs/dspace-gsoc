package org.dspace.metadata.jena;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.metadata.Predicate;
import org.dspace.uri.ExternalIdentifier;
import org.dspace.uri.ObjectIdentifier;
import org.dspace.uri.SimpleIdentifier;
import org.dspace.uri.UnsupportedIdentifierException;

public class PredicateJena extends ResourceImpl implements Predicate, DSpaceObject
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

    private static Logger log = Logger.getLogger( PredicateJena.class );
    private StringBuffer eventDetails = null;
    
    protected Context context;
    protected ObjectIdentifier oid;
    protected List<ExternalIdentifier> identifiers;

    protected void clearDetails()
    {
        eventDetails = null;
    }
    
    protected void addDetails( String detail )
    {
        if ( eventDetails == null )
        {
            eventDetails = new StringBuffer( detail );
        } else
        {
            eventDetails.append( ", " ).append( detail );
        }
    }
    
    protected String getDetails()
    {
        return ( eventDetails == null ? null : eventDetails.toString() );
    }
    
    public int getType()
    {
        return Constants.METADATAPREDICATE;
    }
    
    public String getName()
    {
        return "Predicate <" + getURI() + ">";
    }
    
    public int getID()
    {
        return -1;
    }

    public SimpleIdentifier getSimpleIdentifier()
    {
        return oid;
    }

    public void setSimpleIdentifier( SimpleIdentifier sid )
            throws UnsupportedIdentifierException
    {
        if ( sid instanceof ObjectIdentifier )
        {
            this.setIdentifier( (ObjectIdentifier) sid );
        } else
        {
            throw new UnsupportedIdentifierException( "DSpaceObjects must use ObjectIdentifiers, not SimpleIdentifiers" );
        }
    }

    public ObjectIdentifier getIdentifier()
    {
        return oid;
    }

    public void setIdentifier( ObjectIdentifier oid )
    {
        // ensure that the identifier is configured for the item
        this.oid = oid;
    }
    
    @Deprecated
    public ExternalIdentifier getExternalIdentifier()
    {
        if ( ( identifiers != null ) && ( identifiers.size() > 0 ) )
        {
            return identifiers.get( 0 );
        } else
        {
            log.warn( "no external identifiers found. type=" + getType() +
                      ", id=" + getID() );
            return null;
        }
    }

    public List<ExternalIdentifier> getExternalIdentifiers()
    {
        if ( identifiers == null )
        {
            identifiers = new ArrayList<ExternalIdentifier>();
        }

        return identifiers;
    }

    public void addExternalIdentifier( ExternalIdentifier identifier )
            throws UnsupportedIdentifierException
    {
        identifier.setObjectIdentifier( this.getIdentifier() );
        this.identifiers.add( identifier );
    }

    public void setExternalIdentifiers( List<ExternalIdentifier> identifiers )
            throws UnsupportedIdentifierException
    {
        for ( ExternalIdentifier eid : identifiers )
        {
            eid.setObjectIdentifier( this.getIdentifier() );
        }
        this.identifiers = identifiers;
    }

    ////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////
    public boolean equals( DSpaceObject other )
    {
        if ( other instanceof Predicate )
            return compareTo( (Predicate)other ) == 0;
        if ( getType() == other.getType() && getID() == other.getID() )
            return true;
        return false;
    }

    public boolean contains( List<? extends DSpaceObject> dsos,
                              DSpaceObject dso )
    {
        for ( DSpaceObject obj : dsos )
            if ( obj.equals( dso ) )
                return true;
        return false;
    }
    
}