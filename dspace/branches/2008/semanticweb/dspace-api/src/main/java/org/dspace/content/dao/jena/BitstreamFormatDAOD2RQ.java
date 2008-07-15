package org.dspace.content.dao.jena;

import java.util.UUID;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.dao.BitstreamFormatDAO;
import org.dspace.core.Context;
import org.dspace.dao.jena.D2RQConnectorDAO;

public class BitstreamFormatDAOD2RQ extends BitstreamFormatDAO
{

    private D2RQConnectorDAO<BitstreamFormatDAO, BitstreamFormat> connector;

    public BitstreamFormatDAOD2RQ()
    {// no-op
    }

    public BitstreamFormatDAOD2RQ( Context c )
    {
        super( c );
        connector = new D2RQConnectorDAO<BitstreamFormatDAO, BitstreamFormat>( c );
    }

    @Override
    public void setChild( BitstreamFormatDAO dao )
    {
        childDAO = dao;
        connector.setChild( dao );
    }

    @Override
    public BitstreamFormatDAO getChild()
    {
        return connector.getChild();
    }

    @Override
    public BitstreamFormat retrieve( int id )
    {
        return connector.retrieve( id );
    }

    @Override
    public BitstreamFormat retrieve( UUID uuid )
    {
        return connector.retrieve( uuid );
    }

    @Override
    public BitstreamFormat create() throws AuthorizeException
    {
        return connector.create();
    }

    @Override
    public void update( BitstreamFormat t ) throws AuthorizeException
    {
        connector.update( t );
    }

    @Override
    public void delete( int id ) throws AuthorizeException
    {
        connector.delete( id );
    }

}
