package org.dspace.administer.update1516;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.uri.ObjectIdentifier;
import org.dspace.uri.dao.ObjectIdentifierDAO;
import org.dspace.uri.dao.ObjectIdentifierDAOFactory;
import org.dspace.uri.dao.ObjectIdentifierStorageException;

import java.sql.SQLException;
import java.util.UUID;
import org.dspace.core.Constants;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class MigrateUUID
{
    public static void main(String[] args)
            throws Exception
    {
        MigrateUUID migrate = new MigrateUUID();
        migrate.migrate();
    }
    
    public void migrate()
            throws SQLException, AuthorizeException, ObjectIdentifierStorageException
    {
        Context context = new Context();
        context.setIgnoreAuthorization( true );
        ObjectIdentifierDAO oidDAO = ObjectIdentifierDAOFactory.getInstance(context);
        
        // Comunities
        migrate( context, oidDAO, "community", "community_id", Constants.COMMUNITY );
        // Collections
        migrate( context, oidDAO, "collection", "collection_id", Constants.COLLECTION );
        // Items
        migrate( context, oidDAO, "item", "item_id", Constants.ITEM );
        // Bundles
        migrate( context, oidDAO, "bundle", "bundle_id", Constants.BUNDLE );
        // Bitstreams
        migrate( context, oidDAO, "bitstream", "bitstream_id", Constants.BITSTREAM );
        // ResourcePolicies
        migrate( context, oidDAO, "resourcepolicy", "policy_id", -1 );
        // Groups
        migrate( context, oidDAO, "epersongroup", "eperson_group_id", Constants.GROUP );
        // EPeople
        migrate( context, oidDAO, "eperson", "eperson_id", Constants.EPERSON );
        
        context.complete();
    }
    
    private void migrate( Context context, ObjectIdentifierDAO oidDAO, 
            String table, String idcol, int type )
            throws SQLException, AuthorizeException, ObjectIdentifierStorageException
    {
        System.out.println( ">> Migrating " + table );
        TableRowIterator tri = DatabaseManager.queryTable( context, table, 
                "SELECT " + idcol + " FROM " + table );
        while ( tri.hasNext() )
        {
            TableRow curr = tri.next();
            int id = curr.getIntColumn( idcol );
            UUID uuid = UUID.randomUUID();
            if ( type > 0 )
            {
                ObjectIdentifier oid = new ObjectIdentifier( uuid, type, id );
                oidDAO.create( oid );
            }
            curr.setColumn( "uuid", uuid.toString() );
            DatabaseManager.update( context, curr );
        }
    }
}
