package org.dspace.metadata;

import org.dspace.metadata.jena.MetadataManagerJena;

public class MetadataManagerFactory
{

    public static MetadataManager get()
    {
        return new MetadataManagerJena();
    }
    
}
