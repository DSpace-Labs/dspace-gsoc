/*
 * MetadataTag.java
 *
 * Copyright (c) 2002-2005, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.app.webui.jsptag;

import java.sql.SQLException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.MetadataCollection;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.MetadataManager;
import org.dspace.metadata.MetadataManagerFactory;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.jena.MetadataFactory;

/**
 * Tag for displaying a collection of metadata as RDFa
 * 
 * @author Peter Coetzee
 */
public class MetadataTag extends TagSupport
{

    private Logger log = Logger.getLogger( MetadataTag.class );
    private Collection<URIResource> rs = new ArrayList<URIResource>();
    private boolean display = false;
    private Map<String,String> nsprefix;

    @Override
    public int doStartTag() throws JspException
    {   
        JspWriter out = pageContext.getOut();
        try
        {
            Context c = UIUtil.obtainContext( 
                    (HttpServletRequest)pageContext.getRequest() );
            nsprefix = getNSPrefixMapping( c );
            MetadataManager meta = MetadataManagerFactory.get( c );
            
            
            out.println( "<!-- Begin NS Declarations -->" );
            if ( display )
                out.println( "<div" );
            else
                out.println( "<div style=\"display: none\"" );
            
            for ( Map.Entry<String, String> e : nsprefix.entrySet() )
                out.println( "      xmlns:" + e.getValue() 
                                + "=\"" + e.getKey() + "\"" );
            out.println( "    >" );
            out.println( "<!-- End NS Declarations -->" );
            
            for ( URIResource r : rs )
            {
                log.info( "Describing " + r.getURI() );
                out.println( "<!-- Metadata About " + r.getURI() + " -->" );
                out.println( "    <div about=\"" + shrink( r, true ) + "\">" );
                MetadataCollection coll = meta.getMetadata( r );
                Iterator<MetadataItem> it = coll.getMetadata();
                while ( it.hasNext() )
                    out.println( "        " + rdfa( it.next() ) );
                out.println( "    </div>" );
            }
            
            out.println( "</div>" );
            out.println( "<!-- End Metadata Block -->" );
        } catch ( SQLException ex )
        {
            throw new JspException( ex );
        } catch ( IOException ie )
        {
            throw new JspException( ie );
        } catch ( AuthorizeException e )
        {
            // log + swallow
            log.warn( e );
        } finally 
        {
            rs.clear(); // for future uses
        }
        
        return SKIP_BODY;
    }
    
    private final String shrink( URIResource r, boolean curie )
    {
        return r == null || r.getURI() == null ? "" : (nsprefix.containsKey( r.getNameSpace() ) ?
            // Safe CURIE or simple short-form ?
            (curie ? 
                "["+nsprefix.get( r.getNameSpace() )+":"+r.getLocalName()+"]"
                : nsprefix.get( r.getNameSpace() )+":"+r.getLocalName())
            // Plain old URI
            : r.getURI());
    }
    
    private final String rdfa( MetadataItem m )
    {
        String pred = shrink( m.getPredicate(), false );
        if ( pred.equals( m.getPredicate().getURI() ) )
            return "<!-- No prefix mapping for " + pred 
                    + ", so cannot output as RDFa -->";
        
        if ( m.isLiteral() )
        {
            // Check value validity + do we need CDATA declaration
            if ( m.getLiteralValue().getLexicalForm().trim().length() < 1 )
                return "<!-- Value of " + pred + " was empty -->";
            String val = m.getLiteralValue().getLexicalForm();
            if ( val.contains( "<" ) || val.contains( ">" ) || val.contains( "&" ) )
                val = "<![CDATA[\n" + val + "\n]]>";
            
            URIResource datatype = m.getLiteralValue().getDatatypeURIResource();
            String shrunkDatatype = shrink( datatype, false );
            // Did we manage to shrink? Only a CURIE is valid for datatype.
            if ( datatype != null && (datatype.getURI() == null ||
                    shrunkDatatype.equals( datatype.getURI() )) )
                datatype = null;
            
            return "<span property=\"" + pred + "\""
                    + (datatype == null ? "" 
                        : " datatype=\"" + shrink( datatype, false ) + "\"" )
                    + ">" + val + "</span>";
        } else
        {
            URIResource r = null;
            if ( m.isDSpaceObject() )
                r = m.getDSpaceObject();
            else
                r = m.getURIResource();
            if ( r == null )
                return "<!-- Resource of " + pred + " was empty -->";
            return "<link rel=\"" + pred + "\" resource=\"" 
                + shrink( r, true ) + "\" />";
        }
    }
    
    public final Map<String, String> getNSPrefixMapping( Context c ) 
            throws JspException
    {
        Map<String,String> prefns = getPrefixNSMapping( c );
        Map<String,String> out = new HashMap<String,String>();
        for ( Map.Entry<String, String> e : prefns.entrySet() )
            if ( e.getKey().length() == 0 ) // must deal with default ns
                out.put( e.getValue(), "root" );
            else
                out.put( e.getValue(), "ns_" + e.getKey() );
        return out;
    }
    
    public final Map<String,String> getPrefixNSMapping( Context c ) 
            throws JspException
    {
        try
        {
            GlobalDAOJena dao = c.getGlobalDAO() instanceof GlobalDAOJena ? 
                    (GlobalDAOJena)c.getGlobalDAO() : new GlobalDAOJena();
            return dao.getTripleStore().getNsPrefixMap();
        } catch ( SQLException ex )
        {
            throw new JspException( ex );
        }
    }

    /**
     * Get the resources to list
     * 
     * @return the collections
     */
    public Collection<URIResource> getResource()
    {
        return rs;
    }

    /**
     * Set the collection of resources to describe
     * 
     * @param resources
     *            the resources
     */
    public void setResource( Collection c )
    {
        for ( Object o : c )
        {
            if( o instanceof URIResource )
                setResource( (URIResource)o );
            else if ( o instanceof String )
                setResource( (String)o );
            else
                log.warn( "Got invalid resource: " + o.toString() + " (a " 
                        + o.getClass() + ")" );
        }

    }

    /**
     * Set the array of resources to describe
     * 
     * @param resources
     *            the resources
     */
    public void setResource( Object[] c )
    {
        for ( Object o : c )
        {
            if( o instanceof URIResource )
                setResource( (URIResource)o );
            else if ( o instanceof String )
                setResource( (String)o );
            else
                log.warn( "Got invalid resource: " + o.toString() + " (a " 
                        + o.getClass() + ")" );
        }

    }

    /**
     * Set the resource to describe
     * 
     * @param resource
     *            the URIResource
     */
    public void setResource( URIResource r )
    {
        rs.add( r );
    }

    /**
     * Set the resource to describe
     * 
     * @param uri
     *            the URI of the resource
     */
    public void setResource( String uri )
    {
        setResource( MetadataFactory.createURIResource( uri ) );
    }

    /**
     * Set whether or not to display the block to the user
     * @param display
     */
    public void setDisplay( boolean display )
    {
        this.display = display;
    }

    /**
     *  Return whether or not the block will be displayed
     * @return boolean value
     */
    public boolean getDisplay()
    {
        return display;
    }

    @Override
    public void release()
    {
        rs = new ArrayList<URIResource>(); // release, but remain consistent
        display = false;
    }

}
