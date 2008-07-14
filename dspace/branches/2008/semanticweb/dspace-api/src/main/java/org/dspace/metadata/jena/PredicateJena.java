package org.dspace.metadata.jena;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.dao.jena.DSPACE;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.Predicate;
import org.dspace.uri.ExternalIdentifier;
import org.dspace.uri.ObjectIdentifier;
import org.dspace.uri.ObjectIdentifierService;
import org.dspace.uri.SimpleIdentifier;
import org.dspace.uri.UnsupportedIdentifierException;
import org.dspace.uri.dao.ObjectIdentifierDAOFactory;
import org.dspace.uri.dao.ObjectIdentifierStorageException;

public class PredicateJena extends PropertyImpl 
        implements Predicate, DSpaceObject
{

    public PredicateJena( Context c, Node n, EnhGraph m )
    {
        super( n, m );
        context = c;
        checkUUID();
    }

    public PredicateJena( Context c, String uri )
    {
        super( uri );
        context = c;
        checkUUID();
    }

    public PredicateJena( Context c, String nameSpace, String localName )
    {
        super( nameSpace, localName );
        context = c;
        checkUUID();
    }

    public PredicateJena( Context c, String uri, ModelCom m )
    {
        super( uri, m );
        context = c;
        checkUUID();
    }

    public PredicateJena( Context c, Resource r, ModelCom m )
    {
        super( r.asNode(), m );
        context = c;
        checkUUID();
    }

    public PredicateJena( Context c, String nameSpace, String localName, ModelCom m )
    {
        super( nameSpace, localName, m );
        context = c;
        checkUUID();
    }
    
    private void checkUUID()
    {
        try
        {
            Model m = getModel();
            if ( m == null )
                m = (context.getGlobalDAO() instanceof GlobalDAOJena ? 
                    (GlobalDAOJena)context.getGlobalDAO() : new GlobalDAOJena())
                        .getTripleStore();
            Iterator<Statement> it = m.listStatements( this, DSPACE.uuid, 
                                            (Resource)null );
            if ( it.hasNext() )
            {
                String uuid = it.next().getLiteral().getLexicalForm();
                setIdentifier( new ObjectIdentifier( uuid ) );
            } else
            {
                ObjectIdentifierService.mint( context, this );
                ObjectIdentifierDAOFactory.getInstance( context ).create( oid );
                String uuid = oid.getUUID().toString();
                m.add( this, DSPACE.uuid, uuid );
            }
        } catch ( ObjectIdentifierStorageException ex )
        {
            log.error( ex );
        } catch ( SQLException ex )
        {
            log.error( "Unable to check for UUID on property <" + getURI() 
                                            + ">!", ex );
        }
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
            throw new UnsupportedIdentifierException( 
           "DSpaceObjects must use ObjectIdentifiers, not SimpleIdentifiers" );
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