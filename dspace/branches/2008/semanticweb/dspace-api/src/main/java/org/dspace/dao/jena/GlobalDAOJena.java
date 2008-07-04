package org.dspace.dao.jena;

import java.sql.Connection;
import java.sql.SQLException;
import org.dspace.dao.GlobalDAO;
import org.dspace.storage.rdbms.DatabaseManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.UUID;
import org.dspace.content.DSpaceObject;

public class GlobalDAOJena extends GlobalDAO
{

    protected Model m;
    private boolean transOpen;

    // FIXME: This should be a GlobalDAOException (pending Interface change)
    public GlobalDAOJena() throws SQLException
    {
        m = ModelFactory.createDefaultModel();
        log.info( "Created GlobalDAOJena" );
    }

    public boolean transactionOpen()
    {
        return transOpen;
    }

    public void startTransaction() throws SQLException
    {
        if ( m.supportsTransactions() )
            m.begin();
        transOpen = true;
    }

    public void endTransaction() throws SQLException
    {
        if ( transOpen && m.supportsTransactions() )
            m.commit();
        transOpen = false;
    }

    public void saveTransaction() throws SQLException
    {
        if ( transOpen && m.supportsTransactions() )
            m.commit();
    }

    public void abortTransaction()
    {
        if ( transOpen && m.supportsTransactions() )
            m.abort();
        transOpen = false;
    }
    
    public Model getModel() {
        return m;
    }
    
    public String getResourceBase() {
        return m.expandPrefix( "db:" );
    }
    
    public Resource getResource( DSpaceObject obj ) {
        return getResource( obj.getIdentifier().getUUID().toString() );
    }
    
    public Resource getResource( UUID uuid ) {
        return getResource( uuid.toString() );
    }
    
    public Resource getResource( String local ) {
        return m.getResource( getResourceBase() + local );
    }

    /**
     * This method will only exist until no-one calls the RDBMS-centric
     * Context.getDBConnection() any more.
     */
    @Deprecated
    public Connection getConnection()
    {
        try
        {
            return DatabaseManager.getConnection();
        } catch ( SQLException ex )
        {
            log.error( ex.getMessage() );
        }
        return null;
    }

}
