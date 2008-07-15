package org.dspace.content.dao.jena;

import java.util.UUID;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.dao.CollectionDAO;
import org.dspace.core.Context;
import org.dspace.dao.jena.D2RQConnectorDAO;

public class CollectionDAOD2RQ extends CollectionDAO
{

    private D2RQConnectorDAO<CollectionDAO, Collection> connector;

    public CollectionDAOD2RQ()
    {// no-op
    }

    public CollectionDAOD2RQ( Context c )
    {
        super( c );
        connector = new D2RQConnectorDAO<CollectionDAO, Collection>( c );
    }

    @Override
    public void setChild( CollectionDAO dao )
    {
        childDAO = dao;
        connector.setChild( dao );
    }

    @Override
    public CollectionDAO getChild()
    {
        return connector.getChild();
    }

    @Override
    public Collection retrieve( int id )
    {
        return connector.retrieve( id );
    }

    @Override
    public Collection retrieve( UUID uuid )
    {
        return connector.retrieve( uuid );
    }

    @Override
    public Collection create() throws AuthorizeException
    {
        return connector.create();
    }

    @Override
    public void update( Collection t ) throws AuthorizeException
    {
        connector.update( t );
    }

    @Override
    public void delete( int id ) throws AuthorizeException
    {
        connector.delete( id );
    }

}
