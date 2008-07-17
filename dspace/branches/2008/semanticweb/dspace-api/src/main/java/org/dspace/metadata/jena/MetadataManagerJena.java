package org.dspace.metadata.jena;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.MetadataCollection;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.MetadataManager;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.Selector;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

public class MetadataManagerJena implements MetadataManager
{

    private Logger log = Logger.getLogger( MetadataManagerJena.class );
    private GlobalDAOJena dao;
    private StatementTranslator tran;
    private Context context;
    private Map<DSpaceObject,Boolean> read, write, remove;

    public MetadataManagerJena( Context c )
    {
        context = c;
        read = new HashMap<DSpaceObject,Boolean>();
        write = new HashMap<DSpaceObject,Boolean>();
        remove = new HashMap<DSpaceObject,Boolean>();
        try
        {
            if ( c.getGlobalDAO() instanceof GlobalDAOJena )
                dao = (GlobalDAOJena)c.getGlobalDAO();
            else
                dao = new GlobalDAOJena();
            tran = new StatementTranslator( dao, context );
        } catch ( SQLException ex )
        {
            log.error( "SQL Exception creating new DAO", ex );
        }
    }

    public MetadataCollection getMetadata( URIResource o ) throws AuthorizeException
    {
        auth( o, Constants.READ );
        return getMetadata( o, 1 );
    }

    public MetadataCollection getMetadata( final URIResource o, int depth ) throws AuthorizeException
    {
        auth( o, Constants.READ );
        return getMetadata( (Selector) new SelectorCore()
        {

            @Override
            public URIResource getSubject()
            {
                return o;
            }

        }, depth );
    }

    public MetadataCollection getMetadata( Selector s )
    {
        return getMetadata( s, 1 );
    }

    public MetadataCollection getMetadata( Selector s, int depth )
    {
        Collection<MetadataItem> c = new HashSet<MetadataItem>();
        Iterator<MetadataItem> it = tran.translate(
                (Iterator<Statement>) dao.getTripleStore().listStatements(
                SelectorCore.toJenaSelector( s, tran ) ) );
        while ( it.hasNext() )
        {
            MetadataItem m = it.next();
            try {
                auth( m.getSubject(), Constants.READ );
                auth( m.getPredicate(), Constants.READ );
                c.add( m );
                if ( m.isDSpaceObject() && depth > 1 )
                {
                    final DSpaceObject newSub = m.getDSpaceObject();
                    try {
                        auth( newSub, Constants.READ );
                        Iterator<MetadataItem> cit = getMetadata(
                                (Selector) new SelectorCore( s )
                                {

                                    @Override
                                    public DSpaceObject getSubject()
                                    {
                                        return newSub;
                                    }

                                },
                                depth - 1 ).getMetadata();
                        while ( cit.hasNext() ) // already auth'd
                            c.add( cit.next() );
                    } catch ( AuthorizeException e ) { }
                }
            } catch ( AuthorizeException e ) { }
        }
        return new MetadataCollectionJena( c );
    }

    public void addMetadata( MetadataItem... is )
    {
        try
        {
            beginTransaction();
            for ( MetadataItem m : is )
            {
                try {
                    auth( m.getSubject(), Constants.WRITE );
                    auth( m.getPredicate(), Constants.WRITE );
                    dao.getTripleStore().add( tran.translateItem( m ) );
                } catch ( AuthorizeException e ) { }
            }
            commitTransaction();
        } catch ( Exception e )
        {
            log.error( "Unexpected error adding metadata, transaction aborted",
                       e );
            abortTransaction();
        }
    }

    public void addMetadata( MetadataCollection coll )
    {
        Iterator<MetadataItem> it = coll.getMetadata();
        while ( it.hasNext() )
            addMetadata( it.next() );
    }

    public void addMetadata( URIResource o, Predicate p, Value v )
    {
        addMetadata( MetadataFactory.createItem( o, p, v ) );
    }

    public void addMetadata( URIResource o, Predicate p, DSpaceObject v )
    {
        addMetadata( MetadataFactory.createItem( o, p, v ) );
    }

    public void removeMetadata( Selector s ) throws AuthorizeException
    {
        if ( s.isValueMatcher() && s.getSubject() != null && s.getPredicate() != null ) 
        {
            auth( s.getSubject(), Constants.REMOVE );
            auth( s.getPredicate(), Constants.REMOVE );
            Statement st = tran.translateItem( s );
            dao.getTripleStore().removeAll( st.getSubject(), 
                        st.getPredicate(), st.getObject() );
        } else 
        {
            Iterator<Statement> it = dao.getTripleStore().listStatements( 
                    SelectorCore.toJenaSelector( s, tran ) );
            while ( it.hasNext() )
            {
                Statement curr = it.next();
                try {
                    MetadataItem m = tran.translate( curr );
                    auth( m.getSubject(), Constants.REMOVE );
                    auth( m.getPredicate(), Constants.REMOVE );
                    dao.getTripleStore().remove( curr );
                } catch ( AuthorizeException e ) { }
            }
        }
    }

    public void removeAllMetadata( URIResource o )
    {
        dao.getTripleStore().removeAll( 
                ResourceFactory.createResource( o.getURI() ), null, 
                (RDFNode)null );
    }

    public void beginTransaction()
    {
        if ( dao.getTripleStore().supportsTransactions() )
            dao.getTripleStore().begin();
    }

    public void abortTransaction()
    {
        if ( dao.getTripleStore().supportsTransactions() )
            dao.getTripleStore().abort();
    }

    public void commitTransaction()
    {
        if ( dao.getTripleStore().supportsTransactions() )
            dao.getTripleStore().commit();
    }
    
    private Boolean auth( URIResource subject, int action ) throws AuthorizeException
    {
        if ( subject instanceof DSpaceObject )
            return auth( (DSpaceObject)subject, action );
        return true;
    }

    private Boolean auth( DSpaceObject subject, int action ) throws AuthorizeException
    {
        if ( subject == null )
            throw new AuthorizeException( "Cannot auth null" );
        switch( action )
        {
            case Constants.READ : 
                    if ( read.containsKey( subject )  )
                    {
                        if ( read.get( subject ) )
                            return true;
                        else
                            throw new AuthorizeException( 
                                    "Cached auth failed for " 
                                    + subject.getName() );
                    }
            case Constants.WRITE : 
                    if ( write.containsKey( subject ) )
                    {
                        if ( write.get( subject ) )
                            return true;
                        else
                            throw new AuthorizeException( 
                                    "Cached auth failed for " 
                                    + subject.getName() );
                    }
            case Constants.REMOVE : 
                    if ( remove.containsKey( subject ) )
                    {
                        if ( remove.get( subject ) )
                            return true;
                        else
                            throw new AuthorizeException( 
                                    "Cached auth failed for " 
                                    + subject.getName() );
                    }
            default : break;
        }
        boolean res = true;
        try 
        {
            AuthorizeManager.authorizeAction( context, subject, action );
        } catch ( AuthorizeException e )
        {
            res = false;
        }
        switch( action )
        {
            case Constants.READ : return read.put( subject, res );
            case Constants.WRITE : return write.put( subject, res );
            case Constants.REMOVE : return remove.put( subject, res );
            default : return res;
        }
    }

}
