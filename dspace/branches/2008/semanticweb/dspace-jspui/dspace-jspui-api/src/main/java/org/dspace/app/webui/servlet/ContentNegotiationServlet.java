/*
 * ContentNegotiationServlet.java
 *
 * Version: $Revision: 1774 $
 *
 * Date: $Date: 2007-05-23 21:03:03 +0100 (Wed, 23 May 2007) $
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
package org.dspace.app.webui.servlet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

/**
 * Servlet for content negotiating URLs
 * 
 * @author Peter Coetzee
 */
public class ContentNegotiationServlet extends DSpaceServlet
{

    /** log4j category */
    private static Logger log = Logger.getLogger( ContentNegotiationServlet.class  );
    private Map<String, SortedSet<ContentFilterItem>> items;

    public ContentNegotiationServlet()
    {
        items = new HashMap<String, SortedSet<ContentFilterItem>>();
        Model config = FileManager.get().loadModel(
                ConfigurationManager.getProperty( 
                    "org.dspace.dao.jena.configuration" ) );
        items = new HashMap<String, SortedSet<ContentFilterItem>>();
        Property match = config.createProperty( 
                config.expandPrefix( "filter:matchContentType" ) );
        StmtIterator it = config.listStatements( null, match, (Resource) null );
        while ( it.hasNext() )
        {
            Statement curr = it.nextStatement();
            if ( items.get( curr.getString() ) == null )
                items.put( curr.getString(), new TreeSet<ContentFilterItem>() );
            items.get( curr.getString() )
                    .add( new ContentFilterItem( 
                            match.getNameSpace(), curr.getSubject() ) );
        }
    }

    protected void doDSGet( Context context, HttpServletRequest request,
                             HttpServletResponse response )
            throws ServletException, IOException,
                   SQLException, AuthorizeException
    {
        log.info( LogManager.getHeader( context, "get_metadata " 
                + request.getRequestURI(), "" ) );
        if ( !negotiate( request, response ) )
        {
            throw new ServletException( "Unable to content negotiate URI <" 
                    + request.getRequestURI() + ">" );
        }
    }

    private boolean negotiate( HttpServletRequest rq, HttpServletResponse re )
            throws ServletException, IOException
    {
        String toPattern = null;
        int statusNum = 303;
        
        String request = rq.getRequestURI().substring( rq.getContextPath().length() );
        log.info( rq.getRequestURI() + " -> " + request );
        Iterator<AcceptPair> it = calcAccept( rq.getHeader( "accept" ) );
        while ( it.hasNext() )
        {
            SortedSet<ContentFilterItem> s = items.get( it.next().getType() );
            if ( s != null )
            {
                Iterator<ContentFilterItem> patIt = s.iterator();
                while ( patIt.hasNext() )
                {
                    ContentFilterItem curr = patIt.next();
                    Matcher m = Pattern.compile( curr.getFromURI() ).matcher( request );
                    if ( m.find() )
                    {
                        toPattern = curr.getToURI();
                        MatchResult mr = m.toMatchResult();
                        for ( int i = 1; i < 10 && i <= mr.groupCount(); i++ )
                            toPattern = toPattern.replaceAll( "\\$" + i, 
                                  mr.group( i ) == null ? "" : mr.group( i ) );
                        statusNum = curr.getStatusCode();
                        log.info( " Rewrite to " + toPattern 
                                + " with status " + statusNum );
                        break;
                    }
                }
            }
            if ( toPattern != null )
                break;
        }
        if ( toPattern == null )
            // no patterns found, abort rewrite
            return false;
        if ( statusNum == -1 )
        {
            // passthrough
            RequestDispatcher disp = rq.getRequestDispatcher( toPattern );
            disp.forward( rq, re );
            return true;
        } else
        {
            // rewrite
            re.setStatus( statusNum );
            re.setHeader( "Location", re.encodeRedirectURL( rq.getContextPath() + toPattern ) );
            return true;
        }
    }

    private Iterator<AcceptPair> calcAccept( String in )
    {
        SortedSet<AcceptPair> out = new TreeSet<AcceptPair>();
        try
        {
            out.add( new AcceptPair( "*", Integer.MAX_VALUE ) ); // force highest priority
            if ( in == null )
                return out.iterator();
            String[] types = in.split( "," );
            for ( String t : types )
            {
                String[] curr = t.split( ";" );
                if ( curr[0].trim().equals( "*/*" ) )
                    curr[0] = "*";
                out.add( new AcceptPair( curr[0].trim(),
                    curr.length == 1 ? 1.0 : 
                        Double.parseDouble( 
                            curr[1].split( "=" )[1].trim() ) ) );
            }
        } catch ( Exception e )
        {
            e.printStackTrace(); // but keep going
        }
        if ( out.size() == 1 )
            out.add( new AcceptPair( "text/html", 1.0 ) );
        log.info( "Accept: " + Arrays.toString( out.toArray() ) );
        return out.iterator();
    }

    public class ContentFilterItem implements Comparable<ContentFilterItem>
    {

        private Resource r;
        protected String base;
        private Property fromURI;
        private Property toURI;
        private Property matchContentType;
        private Property statusCode;
        private Property priority;

        public ContentFilterItem( String base, Resource r )
        {
            this.r = r;
            this.base = base;
            fromURI = ResourceFactory.createProperty( base + "fromURI" );
            toURI = ResourceFactory.createProperty( base + "toURI" );
            matchContentType = ResourceFactory.createProperty( base + "matchContentType" );
            statusCode = ResourceFactory.createProperty( base + "statusCode" );
            priority = ResourceFactory.createProperty( base + "priority" );
        }

        public String getFromURI()
        {
            return r.getRequiredProperty( fromURI ).getString();
        }

        public String getToURI()
        {
            return r.getRequiredProperty( toURI ).getString();
        }

        public String getContentType()
        {
            return r.getRequiredProperty( matchContentType ).getString();
        }

        public int getStatusCode()
        {
            Statement s = r.getProperty( statusCode );
            if ( s == null )
                return -1;
            return s.getInt();
        }

        public int getPriority()
        {
            Statement s = r.getProperty( priority );
            if ( s == null )
                return Integer.MAX_VALUE;
            return s.getInt();
        }

        /** Note - may answer {-1,1} when are actually equal */
        public int compareTo( ContentFilterItem o )
        {
            if ( getPriority() != o.getPriority() )
                return getPriority() < o.getPriority() ? -1 : 1;
            if ( !getFromURI().equals( o.getFromURI() ) )
                return getFromURI().compareTo( o.getFromURI() );
            if ( !getToURI().equals( o.getToURI() ) )
                return getToURI().compareTo( o.getToURI() );
            return getContentType().equals( o.getContentType() ) ? -1 : 1;
        }

        @Override
        public String toString()
        {
            return "ContentFilterItem( " + getPriority() + ": " + getFromURI() + ", " + getToURI() + ", " + getStatusCode() + ") ";
        }

    }

    private class AcceptPair implements Comparable<AcceptPair>
    {

        private String type;
        private double pref;

        public AcceptPair( String type, double pref )
        {
            this.type = type;
            this.pref = pref;
        }

        public double getPref()
        {
            return pref;
        }

        public String getType()
        {
            return type;
        }

        public int compareTo( AcceptPair o )
        {
            if ( o.getType().equals( getType() ) )
                return 0;
            if ( o.getPref() != getPref() )
                return o.getPref() < getPref() ? -1 : 1;
            return o.getType().equals( "text/html" ) ? 1 : 0;
        }

        public String toString()
        {
            return getType() + "; q=" + getPref();
        }

    }
}
