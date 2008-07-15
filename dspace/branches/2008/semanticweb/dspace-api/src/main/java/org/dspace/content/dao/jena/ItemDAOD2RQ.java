package org.dspace.content.dao.jena;

import org.dspace.eperson.dao.jena.*;
import java.util.UUID;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.dao.ItemDAO;
import org.dspace.core.Context;
import org.dspace.dao.jena.D2RQConnectorDAO;

public class ItemDAOD2RQ extends ItemDAO
{

    private D2RQConnectorDAO<ItemDAO, Item> connector;

    public ItemDAOD2RQ()
    { // no-op
    }

    public ItemDAOD2RQ( Context c )
    {
        super( c );
        connector = new D2RQConnectorDAO<ItemDAO, Item>( c );
    }

    @Override
    public void setChild( ItemDAO dao )
    {
        childDAO = dao;
        connector.setChild( dao );
    }

    @Override
    public ItemDAO getChild()
    {
        return connector.getChild();
    }

    @Override
    public Item retrieve( int id )
    {
        return connector.retrieve( id );
    }

    @Override
    public Item retrieve( UUID uuid )
    {
        return connector.retrieve( uuid );
    }

    @Override
    public Item create() throws AuthorizeException
    {
        return connector.create();
    }

    @Override
    public void update( Item t ) throws AuthorizeException
    {
        connector.update( t );
    }

    @Override
    public void delete( int id ) throws AuthorizeException
    {
        connector.delete( id );
    }

}
