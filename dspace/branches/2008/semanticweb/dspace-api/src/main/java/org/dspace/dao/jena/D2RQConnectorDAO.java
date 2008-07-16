package org.dspace.dao.jena;

import com.hp.hpl.jena.rdf.model.Resource;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.dao.CRUD;
import org.dspace.dao.StackableDAO;

public class D2RQConnectorDAO<DAO extends CRUD<O>, O extends DSpaceObject> 
        extends StackableDAO<DAO> implements CRUD<O> {
    
    private DAO child;
    private static GlobalDAOJena dao;
    private Context context;
    protected Logger log = Logger.getLogger( D2RQConnectorDAO.class );
    
    public D2RQConnectorDAO() {
        try
        {
            dao = dao == null ? new GlobalDAOJena() : dao;
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
    }

    public D2RQConnectorDAO( Context context )
    {
        this.context = context;
        if ( dao != null )
            return;
        if ( !( context.getGlobalDAO() instanceof GlobalDAOJena ) )
            log.error( "Jena DAO requires an instance of GlobalDAOJena to operate" );
        else
            dao = (GlobalDAOJena) context.getGlobalDAO();
    }

    @Override
    public DAO getChild()
    {
        return child;
    }

    @Override
    public void setChild( DAO t )
    {
        child = t;
    }
    
    private void assertCrud() {
        if ( child == null || !(child instanceof CRUD) )
            throw new UnsupportedOperationException( 
                 "Cannot perform CRUD operations without child to stack to" );
    }

    public O retrieve( int id )
    {
        assertCrud();
        return child.retrieve( id );
    }

    public O retrieve( UUID uuid )
    {
        assertCrud();
        return child.retrieve( uuid );
    }

    public O create() throws AuthorizeException
    {
        assertCrud();
        O o = child.create();
        commit( o );
        return o;
    }

    public void update( O t ) throws AuthorizeException
    {
        assertCrud();
        child.update( t );
        commit( t );
    }

    public void delete( int id ) throws AuthorizeException
    {
        dao.getResource( retrieve( id ) ).removeProperties();
        assertCrud();
        child.delete( id );
    }
    
    public O commit( O o ) {
        Resource r = dao.getResource( o ).removeProperties();
        log.info( "Committing resource " + r );
        r.getModel().add( 
               dao.getD2RQStore().getResource( r.getURI() ).listProperties() );
        return o;
    }
    
}