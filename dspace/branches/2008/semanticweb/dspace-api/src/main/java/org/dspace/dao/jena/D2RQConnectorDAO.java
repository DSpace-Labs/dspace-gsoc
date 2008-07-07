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

public class D2RQConnectorDAO<DAO, O extends DSpaceObject> extends StackableDAO<DAO>
        implements CRUD<O> {
    
    private DAO child;
    private GlobalDAOJena dao;
    private Context context;
    protected Logger log = Logger.getLogger( D2RQConnectorDAO.class );
    
    public D2RQConnectorDAO() {
        try
        {
            log.info( "Loading D2RQConnectorDAO()" );
            dao = new GlobalDAOJena();
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
    }

    public D2RQConnectorDAO( Context context )
    {
        log.info( "Loading D2RQConnectorDAO(context)" );
        this.context = context;
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
            throw new UnsupportedOperationException( "Cannot perform CRUD operations without child to stack to" );
    }

    public O retrieve( int id )
    {
        assertCrud();
        return (O) ((CRUD)child).retrieve( id );
    }

    public O retrieve( UUID uuid )
    {
        assertCrud();
        return (O) ((CRUD)child).retrieve( uuid );
    }

    public O create() throws AuthorizeException
    {
        assertCrud();
        O o = (O) ((CRUD)child).create();
        commit( o );
        return o;
    }

    public void update( O t ) throws AuthorizeException
    {
        assertCrud();
        ((CRUD)child).update( t );
        commit( t );
    }

    public void delete( int id ) throws AuthorizeException
    {
        dao.getResource( retrieve( id ) ).removeProperties();
        assertCrud();
        ((CRUD)child).delete( id );
    }
    
    public void commit( O o ) {
        Resource r = dao.getResource( o ).removeProperties();
        log.info( "Committing resource " + r );
        r.getModel().add( dao.getD2RQStore().getResource( r.getURI() ).listProperties() );
    }
    
}