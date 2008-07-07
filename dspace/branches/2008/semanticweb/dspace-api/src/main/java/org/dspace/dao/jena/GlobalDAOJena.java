package org.dspace.dao.jena;

import com.hp.hpl.jena.assembler.Assembler;
import java.sql.SQLException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import java.util.Collection;
import java.util.UUID;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Community;
import org.dspace.content.MetadataEnabled;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.dao.postgres.GlobalDAOPostgres;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

public class GlobalDAOJena extends GlobalDAOPostgres
{

    protected Model tripleStore, d2rqStore;

    // FIXME: This should be a GlobalDAOException (pending Interface change)
    public GlobalDAOJena() throws SQLException
    {
        new de.fuberlin.wiwiss.d2rq.assembler.D2RQAssembler();
        Model assemblerSpec = FileManager.get().loadModel( 
                ConfigurationManager.getProperty( 
                    "org.dspace.dao.jena.assemblerspec" ) );
        d2rqStore = Assembler.general.openModel( 
                assemblerSpec.createResource( DSPACE.d2rqStore.getURI() ) );
        tripleStore = Assembler.general.openModel( 
                assemblerSpec.createResource( DSPACE.tripleStore.getURI() ) );
        tripleStore.setNsPrefixes( d2rqStore );
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
    
    public Resource getResource( MetadataEnabled obj ) {
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
        else if ( c.equals( Item.class ) )
            type = "item/";
        return getResource( type + uuid.toString() );
    }
    
    public Resource getResource( String local ) {
        return tripleStore.getResource( getResourceBase() + local );
    }

}
