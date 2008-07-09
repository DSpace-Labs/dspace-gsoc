package org.dspace.metadata.jena;

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
            dao = new GlobalDAOJena();
            tran = new StatementTranslator( dao );
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
    }

    public MetadataCollection getMetadata( DSpaceObject o ) throws AuthorizeException
    {
        auth( o, Constants.READ );
        return getMetadata( o, 1 );
    }

    public MetadataCollection getMetadata( final DSpaceObject o, int depth ) throws AuthorizeException
    {
        auth( o, Constants.READ );
        return getMetadata( (Selector) new SelectorCore()
                    {

                        @Override
                        public DSpaceObject getSubject()
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
                        while ( cit.hasNext() )
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
                    dao.getTripleStore().add( tran.translate( m ) );
                } catch ( AuthorizeException e ) { }
            }
            commitTransaction();
        } catch ( Exception e )
        {
            abortTransaction();
            log.error( "Unexpected error adding metadata, transaction aborted",
                       e );
        }
    }

    public void addMetadata( MetadataCollection coll )
    {
        Iterator<MetadataItem> it = coll.getMetadata();
        while ( it.hasNext() )
            addMetadata( it.next() );
    }

    public void addMetadata( DSpaceObject o, Predicate p, Value v )
    {
        addMetadata( MetadataFactory.createItem( o, p, v ) );
    }

    public void addMetadata( DSpaceObject o, Predicate p, DSpaceObject v )
    {
        addMetadata( MetadataFactory.createItem( o, p, v ) );
    }

    public void removeMetadata( Selector s ) throws AuthorizeException
    {
        if ( s.isValueMatcher() && s.getSubject() != null ) 
        {
            auth( s.getSubject(), Constants.REMOVE );
            Statement st = tran.translate( s );
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
                    auth( tran.translate( curr ).getSubject(), Constants.REMOVE );
                    dao.getTripleStore().remove( curr );
                } catch ( AuthorizeException e ) { }
            }
        }
    }

    public void removeAllMetadata( DSpaceObject o )
    {
        dao.getResource( o ).removeProperties();
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

    private Boolean auth( DSpaceObject subject, int action ) throws AuthorizeException
    {
        switch( action )
        {
            case Constants.READ : 
                    if ( read.containsKey( subject ) && read.get( subject ) )
                        return true;
                    else
                        throw new AuthorizeException();
            case Constants.WRITE : 
                    if ( write.containsKey( subject ) && write.get( subject ) )
                        return true;
                    else
                        throw new AuthorizeException();
            case Constants.REMOVE : 
                    if ( remove.containsKey( subject ) && remove.get( subject ) )
                        return true;
                    else
                        throw new AuthorizeException();
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
