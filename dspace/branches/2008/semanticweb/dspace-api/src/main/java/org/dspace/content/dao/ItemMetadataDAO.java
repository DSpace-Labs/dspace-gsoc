package org.dspace.content.dao;

import java.util.ArrayList;
import java.util.List;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.MetadataManager;
import org.dspace.metadata.MetadataManagerFactory;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.jena.MetadataFactory;
import org.dspace.metadata.jena.SelectorCore;

public class ItemMetadataDAO extends ItemDAO
{

    public ItemMetadataDAO()
    { // no-op
    }

    public ItemMetadataDAO( Context c )
    {
        super( c );
    }

    @Override
    public void setChild( ItemDAO dao )
    {
        childDAO = dao;
    }
    
    public void create( Item i ) throws AuthorizeException
    {
        if ( childDAO != null )
            childDAO.update( i );
        commit( i );
    }

    @Override
    public void update( Item i ) throws AuthorizeException
    {
        if ( childDAO != null )
            childDAO.update( i );
        commit( i );
    }
    
    private void commit( Item i ) throws AuthorizeException
    {
        MetadataManager meta = MetadataManagerFactory.get( context );
        boolean failed = true;
        try
        {
            meta.beginTransaction();
            List<MetadataItem> is = new ArrayList<MetadataItem>();
            // Remove old metadata
            for ( DCValue val : i.getMetadata() )
            {
                final MetadataItem m = MetadataFactory.createItem( i, 
                        MetadataFactory.createPredicate( context, val.schema, 
                            val.element, val.qualifier ), 
                        MetadataFactory.createTypedLiteral( val.value, 
                            val.language, null ) );
                meta.removeMetadata( new SelectorCore() {
                    
                    @Override
                    public Predicate getPredicate()
                    {
                        return m.getPredicate();
                    }
                    
                } );
                is.add( m );
            }
            // Set new values
            meta.addMetadata( (MetadataItem[])is.toArray() );
            meta.commitTransaction();
            failed = false;
        } finally
        {
            if ( failed )
                meta.abortTransaction();
        }
    }

}
