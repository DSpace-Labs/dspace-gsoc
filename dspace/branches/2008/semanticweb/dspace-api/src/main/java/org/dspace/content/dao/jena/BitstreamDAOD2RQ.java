package org.dspace.content.dao.jena;

import org.dspace.eperson.dao.jena.*;
import java.util.UUID;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.dao.BitstreamDAO;
import org.dspace.core.Context;
import org.dspace.dao.jena.D2RQConnectorDAO;

public class BitstreamDAOD2RQ extends BitstreamDAO
{

    private D2RQConnectorDAO<BitstreamDAO, Bitstream> connector;

    public BitstreamDAOD2RQ()
    {// no-op
    }

    public BitstreamDAOD2RQ( Context c )
    {
        super( c );
        connector = new D2RQConnectorDAO<BitstreamDAO, Bitstream>( c );
    }

    @Override
    public void setChild( BitstreamDAO dao )
    {
        childDAO = dao;
        connector.setChild( dao );
    }

    @Override
    public BitstreamDAO getChild()
    {
        return connector.getChild();
    }

    @Override
    public Bitstream retrieve( int id )
    {
        return connector.retrieve( id );
    }

    @Override
    public Bitstream retrieve( UUID uuid )
    {
        return connector.retrieve( uuid );
    }

    @Override
    public Bitstream create() throws AuthorizeException
    {
        return connector.create();
    }

    @Override
    public void update( Bitstream t ) throws AuthorizeException
    {
        connector.update( t );
    }

    @Override
    public void delete( int id ) throws AuthorizeException
    {
        connector.delete( id );
    }

}
