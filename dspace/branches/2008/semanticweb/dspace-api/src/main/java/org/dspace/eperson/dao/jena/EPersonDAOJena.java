package org.dspace.eperson.dao.jena;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.EPerson.EPersonMetadataField;
import org.dspace.eperson.dao.EPersonDAO;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.dao.jena.DSPACE;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.HashMap;
import java.util.Map;
import org.dspace.core.ConfigurationManager;

public class EPersonDAOJena extends EPersonDAO
{

    private GlobalDAOJena dao;
    private Map<EPersonMetadataField, Property> meta;
    private static String configPrefix = "org.dspace.EPersonDAOJena.";

    public EPersonDAOJena()
    {
        try
        {
            dao = new GlobalDAOJena();
            init();
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
    }

    public EPersonDAOJena( Context context )
    {
        this.context = context;
        if ( !( context.getGlobalDAO() instanceof GlobalDAOJena ) )
            log.error( "Jena DAO requires an instance of GlobalDAOJena to operate" );
        else
            dao = (GlobalDAOJena) context.getGlobalDAO();
        init();
    }

    private void init()
    {
        meta = new HashMap<EPersonMetadataField, Property>();

        for ( EPersonMetadataField f : EPersonMetadataField.values() )
        {
            String p = ConfigurationManager.getProperty( configPrefix + f );
            if ( p != null )
                meta.put( f, dao.getTripleStore().getProperty( 
                        dao.getTripleStore().expandPrefix( p ) ) );
        }
    }

    @Override
    public EPerson create() throws AuthorizeException
    {
        if ( getChild() == null )
        {
            log.error( "Cannot create EPerson without database persistence" );
            return null;
        }
        EPerson e = getChild().create();
        dao.getResource( e ).addProperty( RDF.type, DSPACE.EPerson );
        return e;
    }

    @Override
    public EPerson retrieve( EPersonMetadataField field, String value )
    {
        if ( getChild() == null )
        {
            log.error( "Cannot retrieve EPerson without database" );
            return null;
        }
        EPerson e = getChild().retrieve( field, value );
        if ( e == null )
        { // couldn't find metadata; try in triple store
            Iterator<Resource> it = dao.getTripleStore().
                    listResourcesWithProperty( getProperty( field ),
                                               value );
            if ( !it.hasNext() )
                return null;
            Resource r = it.next();
            e = retrieve( UUID.fromString( r.getLocalName() ) );
        }
        return e;
    }

    @Override
    public void update( EPerson eperson ) throws AuthorizeException
    {
        commit( eperson );
        log.info( "Committed eperson " + eperson.getFullName() );
        if ( getChild() != null )
            getChild().update( eperson );
    }

    /**
     * FIXME We need link() and unlink() for EPerson <--> Group mapping
     */
    @Override
    public void delete( int id ) throws AuthorizeException
    {
        dao.getResource( retrieve( id ) ).removeProperties();
        if ( getChild() != null )
            getChild().delete( id );
    }

    private void commit( EPerson eperson )
    {
        Resource r = dao.getResource( eperson );
        for ( EPersonMetadataField f : EPersonMetadataField.values() )
        {
            Property p = meta.get( f );
            if ( p == null )
                continue;
            if ( r.hasProperty( p ) ) 
                r.removeAll( p );
            String v = eperson.getMetadata( f );
            if ( v != null && v.length() > 0 )
                r.addLiteral( p, v );
        }
    }

    private Property getProperty( EPersonMetadataField f )
    {
        Property out = meta.get( f );
        return out == null ? DSPACE.uuid : out;
    }

}
