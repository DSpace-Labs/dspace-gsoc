package org.dspace.administer.update1516;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.util.UUID;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.jena.MetadataFactory;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class MigrateMetadata
{
    public static void main(String[] args)
            throws Exception
    {
        MigrateMetadata migrate = new MigrateMetadata();
        migrate.migrate();
    }
    
    public void migrate() throws Exception
    {
        GlobalDAOJena dao = new GlobalDAOJena();
        try
        {
            // Setup our context
            Context c = new Context();
            c.setIgnoreAuthorization( true );
            
            // Clear out the triple store
            dao.getTripleStore().removeAll();

            // Pour contents of D2RQStore into TripleStore
            System.out.println( "Loading data from D2RQ..." );
            long start = System.currentTimeMillis();
            dao.getTripleStore().add( dao.getD2RQStore() );
            System.out.println( "Done. Took " + (System.currentTimeMillis() - start) + "ms." );
        
            // Now make sure we get the contents of metadata committed
            TableRowIterator tri = DatabaseManager.query( c, 
                    "SELECT item.uuid, namespace, element, qualifier, text_value, text_lang" +
                    " FROM metadatafieldregistry, metadataschemaregistry, metadatavalue, item" +
                    " WHERE metadatafieldregistry.metadata_schema_id = metadataschemaregistry.metadata_schema_id" + 
                    " AND metadatafieldregistry.metadata_field_id = metadatavalue.metadata_field_id" +
                    " AND item.item_id = metadatavalue.item_id;");
            
            for ( int i = 0; tri.hasNext(); i++ )
            {
                if ( i % 10000 == 0 )
                { // commit periodically
                    if ( i > 0 )
                        dao.getTripleStore().commit();
                    dao.getTripleStore().begin();
                    System.out.println( "Comitting transaction, starting next 10,000 triples... (i = " + i + ")" );
                }
                TableRow curr = tri.next();
                Resource r = dao.getResource( Item.class, UUID.fromString( curr.getStringColumn( "uuid" ) ) );
                String prop = curr.getStringColumn( "namespace" );
                if ( !(prop.endsWith( "/" ) || prop.endsWith( "#" )) )
                    prop += "/";
                prop += curr.getStringColumn( "element" );
                if ( curr.getStringColumn( "qualifier" ) != null )
                    prop += "." + curr.getStringColumn( "qualifier" );
                Property p = ResourceFactory.createProperty( prop );
                
                Literal l = dao.getTripleStore().createLiteral( 
                        curr.getStringColumn( "text_value" ), 
                        curr.getStringColumn( "text_lang" ) );
                dao.getTripleStore().add( r, p, l );
                
                // Auth this predicate, as it's not necessarily in D2RQ
                Predicate pred = MetadataFactory.createPredicate( c, p );
                AuthorizeManager.addPolicy( c, pred, Constants.READ, c.getCurrentUser() );
            }
            c.commit();
            
            dao.getTripleStore().commit();
            
            // Load up the MetadataManager and assign Properties their UUIDs
            // Also, authorize Anonymous user to READ all metadata Properties
            // Don't modify DSO policies, however - so users who can't READ
            // a DSO also can't READ its metadata
            ResultSet r = QueryExecutionFactory.create( 
                    "SELECT DISTINCT ?p WHERE { ?s ?p ?o }", 
                    dao.getD2RQStore() ).execSelect();
            while ( r.hasNext() )
            {
                Predicate p = MetadataFactory.createPredicate( c, 
                  (Property)r.nextSolution().get( "p" ).as( Property.class ) );
                AuthorizeManager.addPolicy( c, p, Constants.READ, c.getCurrentUser() );
            }
            c.complete();
        } catch ( Exception e )
        {
            dao.getTripleStore().abort();
            throw e;
        }
    }
}
