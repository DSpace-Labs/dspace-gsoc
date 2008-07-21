/*
 * DescribeMetadataServlet.java
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

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.IOException;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.MetadataCollection;
import org.dspace.metadata.MetadataManagerFactory;
import org.dspace.metadata.jena.MetadataFactory;
import org.dspace.metadata.jena.StatementTranslator;

/**
 * Servlet for serving linked data RDF metadata
 * 
 * @author Peter Coetzee
 */
public class DescribeMetadataServlet extends DSpaceServlet
{

    /** log4j category */
    private static Logger log = Logger.getLogger( DescribeMetadataServlet.class );

    protected void doDSGet( Context context, HttpServletRequest request,
                             HttpServletResponse response )
            throws ServletException, IOException,
                   SQLException, AuthorizeException
    {
        log.info( LogManager.getHeader( context, "describe_metadata " 
                + request.getQueryString(), "" ) );
        String[] params = request.getQueryString().split( ";" );
        if ( params.length < 2 )
            throw new ServletException( 
                    "Insufficient parameters to describe resource (" 
                    + Arrays.toString( params ) + ")" );
        
        GlobalDAOJena dao = context.getGlobalDAO() instanceof GlobalDAOJena ?
                (GlobalDAOJena)context.getGlobalDAO(): new GlobalDAOJena();
        
        ResultSet r = QueryExecutionFactory.create( 
                    "PREFIX dspace: <http://purl.org/dspace/model#>\n" +
                    "SELECT ?s WHERE\n" +
                    "{  ?s dspace:uuid \"" + params[0] + "\".\n" +
                    "}", dao.getTripleStore() ).execSelect();
        if ( !r.hasNext() )
            throw new ServletException( "No resource found for UUID " + params[0] );
        
        Resource res = r.nextSolution().getResource( "s" );
        log.info( "Describing <" + res + ">" );
        
        MetadataCollection coll = MetadataManagerFactory.get( context )
                .getMetadata( MetadataFactory.createURIResource( res ) );
        StatementTranslator tran = new StatementTranslator( context );
        Model m = ModelFactory.createDefaultModel();
        Iterator<Statement> it = tran.translateItems( coll.getMetadata() );
        while ( it.hasNext() )
            m.add( it.next() );
        
        String ctype = "application/rdf+xml";
        if ( params[1].equals( "N3" ) )
            ctype = "text/rdf+n3";
        else if ( params[1].equals( "N-TRIPLES" ) )
            ctype = "text/plain";
        response.setHeader( "Content-Type", ctype );
        
        m.write( response.getWriter(), params[1] );
        response.getWriter().flush();
    }
}
