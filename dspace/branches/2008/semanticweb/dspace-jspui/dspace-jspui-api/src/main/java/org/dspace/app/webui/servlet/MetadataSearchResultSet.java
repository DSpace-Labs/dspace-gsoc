package org.dspace.app.webui.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

public class MetadataSearchResultSet
{
    
    private Map<String,SortedSet<MetadataSearchResult>> results = new HashMap<String,SortedSet<MetadataSearchResult>>();
    private int count = 0;
    
    public void add( URIResource s, Predicate p, Value o, 
            String pLabel, String sLabel, String oLabel, String sType, 
            String oType, String sTypeLabel, String oTypeLabel )
    {
        add( new MetadataSearchResult( s, p, o, pLabel, sLabel, 
                    oLabel, sType, oType, sTypeLabel, oTypeLabel ) );
    }
    
    public int getResultCount()
    {
        return count;
    }
    
    public void add( MetadataSearchResult r )
    {
        String key = r.getSTypeLabel().length() > 0 ? r.getSTypeLabel() : "Miscellaneous";
        if (  !results.containsKey( key ) )
            results.put( key, new TreeSet<MetadataSearchResult>() );
        results.get( key ).add( r );
        count++;
    }
    
    public SortedSet<String> getTypes()
    {
        return new TreeSet<String>( results.keySet() );
    }
    
    public SortedSet<MetadataSearchResult> getResultsForType( String key )
    {
        return results.containsKey( key ) ? results.get( key ) : new TreeSet<MetadataSearchResult>();
    }
    
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        for ( String k : getTypes() )
        {
            b.append( k + "\n" );
            for ( MetadataSearchResult res : getResultsForType( k ) )
                b.append( "\t" + res + "\n" );
        }
        return b.toString();
    }
    
}