package org.dspace.metadata.jena;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.metadata.MetadataCollection;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.MetadataManager;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.Selector;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

public class MetadataCollectionJena implements MetadataCollection
{
    
    private Set<MetadataItem> metadata;
    private MetadataManager manager;
    
    public MetadataCollectionJena( Collection<MetadataItem> items ) {
        metadata = new HashSet<MetadataItem>();
        metadata.addAll( items );
    }
    
    public MetadataCollectionJena( Iterator<MetadataItem> items ) {
        metadata = new HashSet<MetadataItem>();
        while( items.hasNext() )
            metadata.add( items.next() );
    }
    
    public MetadataCollectionJena( Collection<MetadataItem> items, MetadataManager m ) {
        this( items );
        manager = m;
    }

    public Iterator<MetadataItem> getMetadata()
    {
        return metadata.iterator();
    }

    public Iterator<MetadataItem> getMetadata( final Selector s )
    {
        final MetadataItem[] meta = metadata.toArray( new MetadataItem[metadata.size()] );
        return new Iterator<MetadataItem>() {
            private int i = 0;
            
            public void seek() {
                while( i < meta.length && !s.matches( meta[i] ) )
                    i++;
            }
            
            public boolean hasNext()
            {
                return i < meta.length;
            }

            public MetadataItem next()
            {
                seek();
                return meta[i];
            }

            public void remove()
            {
                try
                {
                    meta[i].remove();
                } catch ( AuthorizeException ex ) { }
            }
            
        };
    }

    public Iterator<MetadataItem> getMetadata( final Predicate p )
    {
        return getMetadata( new SelectorCore() {

            @Override
            public Predicate getPredicate()
            {
                return p;
            }
            
        } );
    }

    public Iterator<MetadataItem> getAllMetadata( final DSpaceObject o )
    {
        return getMetadata( new SelectorCore() {

            @Override
            public DSpaceObject getDSpaceObject()
            {
                return o;
            }
            
        } );
    }

    public Iterator<MetadataItem> getMetadata( final URIResource r )
    {
        return getMetadata( new SelectorCore() {

            @Override
            public Value getValue()
            {
                return r;
            }

            @Override
            public URIResource getURIResource()
            {
                return r;
            }
            
        } );
    }

    public void addMetadata( MetadataItem... items ) throws AuthorizeException
    {
        manager.addMetadata( items );
        for ( MetadataItem m : items )
            metadata.add( m );
    }

    public void addMetadata( DSpaceObject o, Predicate p, Value v ) throws AuthorizeException
    {
        MetadataItem i = MetadataFactory.createItem( o, p, v );
        manager.addMetadata( i );
        addMetadata( i );
    }

    public void addMetadata( DSpaceObject o, Predicate p, DSpaceObject v ) throws AuthorizeException
    {
        MetadataItem i = MetadataFactory.createItem( o, p, v );
        manager.addMetadata( i );
        addMetadata( i );
    }
    
}