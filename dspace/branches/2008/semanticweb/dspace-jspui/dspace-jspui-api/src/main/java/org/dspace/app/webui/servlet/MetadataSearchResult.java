package org.dspace.app.webui.servlet;

import org.dspace.metadata.LiteralValue;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;
import org.dspace.metadata.jena.MetadataFactory;

public class MetadataSearchResult implements Comparable<MetadataSearchResult>
{
    
    private URIResource s;
    private Predicate p;
    private Value o;
    private String pLabel, sLabel, oLabel, sType, oType, sTypeLabel, oTypeLabel;
    
    public MetadataSearchResult( URIResource s, Predicate p, Value o, 
            String pLabel, String sLabel, String oLabel, String sType, 
            String oType, String sTypeLabel, String oTypeLabel )
    {
        this.s = s;
        this.p = p;
        this.o = o;
        this.pLabel = pLabel;
        this.sLabel = sLabel;
        this.oLabel = oLabel;
        this.sType = sType;
        this.oType = oType;
        this.sTypeLabel = sTypeLabel;
        this.oTypeLabel = oTypeLabel;
        
        if ( sTypeLabel == null && sType != null )
            this.sTypeLabel = MetadataFactory.createURIResource( sType ).getLocalName();
        if ( oTypeLabel == null && oType != null )
            this.oTypeLabel = MetadataFactory.createURIResource( oType ).getLocalName();
        
        if ( pLabel == null )
            this.pLabel = p.getLocalName();
        if ( sLabel == null )
            this.sLabel = sType == null ? s.getURI() : this.sTypeLabel;
        
        if ( oLabel == null )
        {
            if ( o.isLiteralValue() )
                this.oLabel = ((LiteralValue)o).getLexicalForm();
            else if ( oType != null )
                this.oLabel = this.oTypeLabel;
            else
                this.oLabel = o.toString();
        }
    }
    
    public URIResource getSubject()
    {
        return s;
    }
    
    public Predicate getPredicate()
    {
        return p;
    }
    
    public Value getValue()
    {
        return o;
    }
    
    public String getSubjectHtml()
    {
        return link( s.getURI(), sLabel );
    }
    
    public String getPredicateHtml()
    {
        return pLabel;
    }
    
    public String getValueHtml()
    {
        if ( o.isLiteralValue() )
            return oLabel;
        else if ( getOTypeLabel().length() > 0 )
            return link( ((URIResource)o).getURI(), oLabel ) + " [" + oTypeLabel + "]";
        else
            return link( ((URIResource)o).getURI(), oLabel );
    }
    
    private final String link( String uri, String value )
    {
        return "<a href=\"" + uri + "\">" + value + "</a>";
    }
    
    @Override
    public String toString()
    {
        return sLabel + ", " + pLabel + ", " + oLabel;
    }

    public int compareTo( MetadataSearchResult o )
    {
        int c = getSLabel().compareTo( o.getSLabel() );
        if ( c != 0 )
            return c;
        c = getPLabel().compareTo( o.getPLabel() );
        if ( c != 0 )
            return c;
        c = getOLabel().compareTo( o.getOLabel() );
        if ( c != 0 )
            return c;
        
        c = getSubject().compareTo( o.getSubject() );
        if ( c != 0 )
            return c;
        c = getPredicate().compareTo( o.getPredicate() );
        if ( c != 0 )
            return c;
        return getValue().compareTo( o.getValue() );
    }
    
    private final String safe( String o )
    {
        return o == null ? "" : o;
    }

    public String getPLabel()
    {
        return safe( pLabel );
    }

    public String getSLabel()
    {
        return safe( sLabel );
    }

    public String getOLabel()
    {
        return safe( oLabel );
    }

    public String getSType()
    {
        return safe( sType );
    }

    public String getOType()
    {
        return safe( oType );
    }

    public String getSTypeLabel()
    {
        return safe( sTypeLabel );
    }

    public String getOTypeLabel()
    {
        return safe( oTypeLabel );
    }
    
    
}