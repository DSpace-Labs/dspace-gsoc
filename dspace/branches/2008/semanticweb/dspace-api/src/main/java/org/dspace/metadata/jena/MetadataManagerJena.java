package org.dspace.metadata.jena;

import com.hp.hpl.jena.rdf.model.Statement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
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

    public MetadataManagerJena()
    {
        try
        {
            dao = new GlobalDAOJena();
            tran = new StatementTranslator( dao );
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
    }

    public MetadataCollection getMetadata( DSpaceObject o )
    {
        return getMetadata( o, 1 );
    }

    public MetadataCollection getMetadata( final DSpaceObject o, int depth )
    {
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
            c.add( m );
            if ( m.isDSpaceObject() && depth > 1 )
            {
                final DSpaceObject newSub = m.getDSpaceObject();
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
            }
        }
        return new MetadataCollectionJena( c );
    }

    public void addMetadata( MetadataItem... is )
    {
        try
        {
            beginTransaction();
            dao.getTripleStore().add( tran.translate( is ) );
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

    public void removeMetadata( Selector s )
    {
        if ( s.isValueMatcher() ) 
        {
            Statement st = tran.translate( s );
            dao.getTripleStore().removeAll( st.getSubject(), st.getPredicate(),
                                            st.getObject() );
        } else 
        {
            dao.getTripleStore().remove( dao.getTripleStore().listStatements( 
                    SelectorCore.toJenaSelector( s, tran ) ) );
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

}
