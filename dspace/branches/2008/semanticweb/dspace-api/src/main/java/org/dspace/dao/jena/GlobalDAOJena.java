package org.dspace.dao.jena;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import java.sql.SQLException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sdb.assembler.SDBModelAssembler;
import com.hp.hpl.jena.util.FileManager;
import de.fuberlin.wiwiss.d2rq.assembler.D2RQAssembler;
import java.util.Arrays;
import java.util.UUID;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.proxy.ItemProxy;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.dao.postgres.GlobalDAOPostgres;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

public class GlobalDAOJena extends GlobalDAOPostgres
{

    protected Model tripleStore, d2rqStore, assemblerSpec;
    private DSpaceObjectAssembler asm;

    // FIXME: This should be a GlobalDAOException (pending Interface change)
    public GlobalDAOJena() throws SQLException
    {
        assemblerSpec = FileManager.get().loadModel( 
                ConfigurationManager.getProperty( 
                    "org.dspace.dao.jena.configuration" ) );
        Assembler.general.implementWith( assemblerSpec.createResource( 
                    assemblerSpec.expandPrefix( "d2rq:D2RQModel" ) ),
                new D2RQAssembler() )
                .implementWith( assemblerSpec.createResource( 
                    assemblerSpec.expandPrefix( "sdb:Model" ) ),
                new SDBModelAssembler() );
        d2rqStore = assembleModel( DSPACE.d2rqStore.getURI() );
        tripleStore = assembleModel( DSPACE.tripleStore.getURI() );
        tripleStore.setNsPrefixes( d2rqStore );
        // TODO: Is there value + a way to get the context in without manually
        //         creating the assembler?
        asm = new DSpaceObjectAssembler( Assembler.general );
    }
    
    public DSpaceObject assembleDSO( String uri, Context c )
    {
        return assembleDSO( tripleStore.createResource( uri ), c );
    }
    
    public DSpaceObject assembleDSO( Resource r, Context c )
    {
        asm.setContext( c );
        return (DSpaceObject)asm.open( r );
    }
    
    public Model assembleModel( String uri )
    {
        return assembleModel( assemblerSpec.createResource( uri ) );
    }
    
    public Model assembleModel( Resource r )
    {
        return Assembler.general.openModel( r, Mode.ANY );
    }
    
    public Object assemble( String uri )
    {
        return assemble( assemblerSpec.createResource( uri ) );
    }
    
    public Object assemble( Resource r )
    {
        return Assembler.general.open( r );
    }
    
    public Model getTripleStore() {
        return tripleStore;
    }
    
    public Model getD2RQStore() {
        return d2rqStore;
    }
    
    public String getResourceBase() {
        return tripleStore.expandPrefix( "res:" );
    }
    
    public Resource getResource( DSpaceObject obj ) {
        if ( obj.getIdentifier() == null )
            throw new UnsupportedOperationException( 
                    "Cannot get Resource for un-identified object!" );
        return getResource( obj.getClass(), obj.getIdentifier().getUUID() );
    }
    
    public Resource getResource( Class c, UUID uuid ) {
        String type = "resource/";
        if ( c.equals( EPerson.class ) )
            type = "eperson/";
        else if ( c.equals( Bitstream.class ) )
            type = "bistream/";
        else if ( c.equals( BitstreamFormat.class ) )
            type = "bitstreamformat/";
        else if ( c.equals( Community.class ) )
            type = "community/";
        else if ( c.equals( Collection.class ) )
            type = "collection/";
        else if ( c.equals( Bundle.class ) )
            type = "bundle/";
        else if ( c.equals( Group.class ) )
            type = "group/";
        else if ( c.equals( Item.class ) || c.equals( ItemProxy.class ) )
            type = "item/";
        else
            log.warn( "Couldn't work out resource class for " + c );
        return getResource( type + uuid.toString() );
    }
    
    public Resource getResource( String local ) {
        return tripleStore.getResource( getResourceBase() + local );
    }

}
