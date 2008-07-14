package org.dspace.dao.jena;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.dspace.authorize.dao.ResourcePolicyDAOFactory;
import org.dspace.content.dao.BitstreamDAOFactory;
import org.dspace.content.dao.BitstreamFormatDAOFactory;
import org.dspace.content.dao.BundleDAOFactory;
import org.dspace.content.dao.CollectionDAOFactory;
import org.dspace.content.dao.CommunityDAOFactory;
import org.dspace.content.dao.ItemDAOFactory;
import org.dspace.core.Context;
import org.dspace.dao.CRUD;
import org.dspace.eperson.dao.EPersonDAOFactory;
import org.dspace.eperson.dao.GroupDAOFactory;

public class DSpaceObjectAssembler extends AssemblerBase
{

    private Map<Resource, Class> daos;
    private Map<Resource, Object> cache;
    private Context context;
    private Logger log = Logger.getLogger( DSpaceObjectAssembler.class );
    private Assembler chain;

    public DSpaceObjectAssembler( Assembler chain )
    {
        this.chain = chain;
        daos = new HashMap<Resource, Class>();
        daos.put( DSPACE.Bitstream, BitstreamDAOFactory.class );
        daos.put( DSPACE.BitstreamFormat, BitstreamFormatDAOFactory.class );
        daos.put( DSPACE.Bundle, BundleDAOFactory.class );
        daos.put( DSPACE.Collection, CollectionDAOFactory.class );
        daos.put( DSPACE.Community, CommunityDAOFactory.class );
        daos.put( DSPACE.EPerson, EPersonDAOFactory.class );
        daos.put( DSPACE.Group, GroupDAOFactory.class );
        daos.put( DSPACE.Item, ItemDAOFactory.class );
        daos.put( DSPACE.Policy, ResourcePolicyDAOFactory.class );
        cache = new HashMap<Resource, Object>();
    }
    
    public void setContext( Context c )
    {
        context = c;
    }

    @Override
    public Object open( Assembler a, Resource r, Mode m )
    {
        if ( cache.containsKey( r ) )
            return cache.get( r );
        Object out = get( r );
        cache.put( r, out );
        return out;
    }
    
    private Object get( Resource r )
    {
        Resource type = null;
        try
        {
            type = r.getRequiredProperty( RDF.type ).getResource();
            
            if ( !daos.containsKey( type ) )
            {
                log.warn( "No CRUD available for classes of type " + type 
                        + "." );
                return chain.open( r );
            }

            CRUD c = getCrud( daos.get( type ) );
            String[] parts = r.getURI().split( "/" );
            return c.retrieve( UUID.fromString( parts[parts.length - 1] ) );
        } catch ( PropertyNotFoundException e )
        {
            log.warn( "Unable to get type of " + r + "." );
            e.printStackTrace();
            return chain.open( r );
        } catch ( IllegalArgumentException e )
        {
            log.warn( "Unable to parse UUID " + r.getLocalName() 
                    + ", trying to get from property..." );
            try
            {
                return getCrud( daos.get( type ) ).retrieve( UUID.fromString( 
                        r.getRequiredProperty( DSPACE.uuid ).getString() ) );
            } catch ( PropertyNotFoundException ex )
            {
                log.warn( "Couldn't find property." );
            }
            return chain.open( r );
        }
    }
    
    private CRUD getCrud( Class c )
    {
        try
        {
            Method m = c.getMethod( "getInstance", Context.class );
            return (CRUD)m.invoke( null, context );
        } catch ( IllegalAccessException ex )
        {
            log.error( ex );
        } catch ( IllegalArgumentException ex )
        {
            log.error( ex );
        } catch ( InvocationTargetException ex )
        {
            log.error( ex );
        } catch ( NoSuchMethodException ex )
        {
            log.error( ex );
        } catch ( SecurityException ex )
        {
            log.error( ex );
        }
        return null;
    }

}
