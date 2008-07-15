package org.dspace.content.dao.jena;

import java.util.UUID;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bundle;
import org.dspace.content.dao.BundleDAO;
import org.dspace.core.Context;
import org.dspace.dao.jena.D2RQConnectorDAO;

public class BundleDAOD2RQ extends BundleDAO
{

    private D2RQConnectorDAO<BundleDAO, Bundle> connector;

    public BundleDAOD2RQ()
    {// no-op
    }

    public BundleDAOD2RQ( Context c )
    {
        super( c );
        connector = new D2RQConnectorDAO<BundleDAO, Bundle>( c );
    }

    @Override
    public void setChild( BundleDAO dao )
    {
        childDAO = dao;
        connector.setChild( dao );
    }

    @Override
    public BundleDAO getChild()
    {
        return connector.getChild();
    }

    @Override
    public Bundle retrieve( int id )
    {
        return connector.retrieve( id );
    }

    @Override
    public Bundle retrieve( UUID uuid )
    {
        return connector.retrieve( uuid );
    }

    @Override
    public Bundle create() throws AuthorizeException
    {
        return connector.create();
    }

    @Override
    public void update( Bundle t ) throws AuthorizeException
    {
        connector.update( t );
    }

    @Override
    public void delete( int id ) throws AuthorizeException
    {
        connector.delete( id );
    }

}
