package org.dspace.content.dao.jena;

import java.sql.SQLException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.dao.jena.DSPACE;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.HashMap;
import java.util.Map;
import org.dspace.content.Community;
import org.dspace.content.dao.CommunityDAO;
import org.dspace.core.ConfigurationManager;

public class CommunityDAOJena extends CommunityDAO
{

    private GlobalDAOJena dao;
    private Map<CommunityMetadataField, Property> meta;
    private static String configPrefix = "org.dspace.CommunityDAOJena.";

    public CommunityDAOJena()
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

    public CommunityDAOJena( Context context )
    {
        this.context = context;
        if ( !( context.getGlobalDAO() instanceof GlobalDAOJena ) )
            try
            {
                dao = new GlobalDAOJena();
            } catch ( SQLException ex )
            {
                log.error( ex );
            }
        else
            dao = (GlobalDAOJena) context.getGlobalDAO();
        init();
    }

    private void init()
    {
        meta = new HashMap<CommunityMetadataField, Property>();

        for ( CommunityMetadataField f : CommunityMetadataField.values() )
        {
            String p = ConfigurationManager.getProperty( configPrefix + f );
            if ( p != null )
                meta.put( f, dao.getTripleStore().getProperty( 
                        dao.getTripleStore().expandPrefix( p ) ) );
        }
    }

    @Override
    public Community create() throws AuthorizeException
    {
        if ( getChild() == null )
        {
            log.error( "Cannot create Community without database persistence" );
            return null;
        }
        Community c = getChild().create();
        dao.getResource( c ).addProperty( RDF.type, DSPACE.Collection );
        return c;
    }

    @Override
    public void update( Community c ) throws AuthorizeException
    {
        commit( c );
        log.info( "Committed Community " + c.getName() );
        if ( getChild() != null )
            getChild().update( c );
    }

    @Override
    public void delete( int id ) throws AuthorizeException
    {
        dao.getResource( retrieve( id ) ).removeProperties();
        if ( getChild() != null )
            getChild().delete( id );
    }

    private void commit( Community c )
    {
        Resource r = dao.getResource( c );
        for ( CommunityMetadataField f : CommunityMetadataField.values() )
        {
            Property p = meta.get( f );
            if ( p == null )
                continue;
            if ( r.hasProperty( p ) ) 
                r.removeAll( p );
            String v = c.getMetadata( f.toString() );
            if ( v != null && v.length() > 0 )
                r.addLiteral( p, v );
        }
    }

}
