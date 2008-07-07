package org.dspace.eperson.dao.jena;

import java.util.UUID;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.dao.jena.D2RQConnectorDAO;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.dao.EPersonDAO;

public class EPersonDAOD2RQ extends EPersonDAO {
    
    private D2RQConnectorDAO<EPersonDAO, EPerson> connector;
    
    public EPersonDAOD2RQ() {
        connector = new D2RQConnectorDAO<EPersonDAO, EPerson>();
    }
    
    public EPersonDAOD2RQ( Context c ) {
        connector = new D2RQConnectorDAO<EPersonDAO, EPerson>( c );
    }
    
    @Override
    public void setChild( EPersonDAO dao ) {
        childDAO = dao;
        connector.setChild( dao );
    }
    
    @Override
    public EPersonDAO getChild() {
        return connector.getChild();
    }
    
    @Override
    public EPerson retrieve( int id )
    {
        return connector.retrieve( id );
    }

    @Override
    public EPerson retrieve( UUID uuid )
    {
        return connector.retrieve( uuid );
    }

    @Override
    public EPerson create() throws AuthorizeException
    {
        return connector.create();
    }

    @Override
    public void update( EPerson t ) throws AuthorizeException
    {
        connector.update( t );
    }

    @Override
    public void delete( int id ) throws AuthorizeException
    {
        connector.delete( id );
    }
    
}