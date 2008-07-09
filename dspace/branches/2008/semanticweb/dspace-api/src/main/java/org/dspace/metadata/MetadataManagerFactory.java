package org.dspace.metadata;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.metadata.jena.MetadataManagerJena;

public class MetadataManagerFactory
{
    
    private static Logger log = Logger.getLogger( MetadataManagerFactory.class );

    public static MetadataManager get( Context c )
    {
        return new MetadataManagerJena( c );
    }

    public static MetadataManager get()
    {
        try
        {
            return get( new Context() );
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
        return null;
    }
    
}
