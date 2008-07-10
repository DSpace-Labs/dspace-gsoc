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
    private Context context;
    private Logger log = Logger.getLogger( DSpaceObjectAssembler.class );

    public DSpaceObjectAssembler( Context c )
    {
        context = c;
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
    }

    @Override
    public Object open( Assembler a, Resource r, Mode m )
    {
        Resource type = null;
        try
        {
            type = r.getRequiredProperty( RDF.type ).getResource();

            if ( !daos.containsKey( type ) )
            {
                log.warn( "No CRUD available for classes of type " + type 
                        + ". Chaining." );
                return a.open( r );
            }

            CRUD c = getCrud( daos.get( type ) );
            return c.retrieve( UUID.fromString( r.getLocalName() ) );
        } catch ( PropertyNotFoundException e )
        {
            log.warn( "Unable to get type of " + r + ". Chaining." );
            return a.open( r );
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
                log.warn( "Couldn't find property. Chaining." );
            }
            return a.open( r );
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
