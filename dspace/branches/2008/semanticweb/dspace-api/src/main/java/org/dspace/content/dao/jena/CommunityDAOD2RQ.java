package org.dspace.content.dao.jena;

import java.util.UUID;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Community;
import org.dspace.content.dao.CommunityDAO;
import org.dspace.core.Context;
import org.dspace.dao.jena.D2RQConnectorDAO;

public class CommunityDAOD2RQ extends CommunityDAO
{

    private D2RQConnectorDAO<CommunityDAO, Community> connector;

    public CommunityDAOD2RQ()
    {// no-op
    }

    public CommunityDAOD2RQ( Context c )
    {
        super( c );
        connector = new D2RQConnectorDAO<CommunityDAO, Community>( c );
    }

    @Override
    public void setChild( CommunityDAO dao )
    {
        childDAO = dao;
        connector.setChild( dao );
    }

    @Override
    public CommunityDAO getChild()
    {
        return connector.getChild();
    }

    @Override
    public Community retrieve( int id )
    {
        return connector.retrieve( id );
    }

    @Override
    public Community retrieve( UUID uuid )
    {
        return connector.retrieve( uuid );
    }

    @Override
    public Community create() throws AuthorizeException
    {
        return connector.create();
    }

    @Override
    public void update( Community t ) throws AuthorizeException
    {
        connector.update( t );
    }

    @Override
    public void delete( int id ) throws AuthorizeException
    {
        connector.delete( id );
    }

}
