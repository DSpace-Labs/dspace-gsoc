/*
 * SparqlServlet.java
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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerFactory;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementUnsaid;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.syntax.TemplateVisitor;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.IOException;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.dao.jena.DSPACE;
import org.dspace.dao.jena.GlobalDAOJena;

/**
 * Servlet for executing SPARQL queries
 * 
 * @author Peter Coetzee
 */
public class SparqlServlet extends DSpaceServlet
{
    
    /** log4j category */
    private static Logger log = Logger.getLogger( SparqlServlet.class  );
    public static final Symbol cache = Symbol.create( DSPACE.getURI() + "AuthCache" );
    public static final Symbol dao = Symbol.create( DSPACE.getURI() + "DAO" );
    public static final Symbol context = Symbol.create( DSPACE.getURI() + "Context" );
    
    public SparqlServlet()
    {
        DescribeHandlerRegistry reg = DescribeHandlerRegistry.get();
        reg.clear();
        reg.add( new DescribeHandlerFactory() {

            public DescribeHandler create()
            {
                return new AuthorizeDescribeHandler();
            }
            
        } );
    }
    
    protected void doDSPost( Context context, HttpServletRequest request,
                                HttpServletResponse response ) 
            throws ServletException, IOException,
                   SQLException, AuthorizeException
    {
        doDSGet( context, request, response );
    }

    protected void doDSGet( Context context, HttpServletRequest request,
                             HttpServletResponse response )
            throws ServletException, IOException,
                   SQLException, AuthorizeException
    {
        log.info( LogManager.getHeader( context, "sparql " 
                + request.getQueryString(), "" ) );
        
        if ( request.getParameter( "query" ) == null || 
                    request.getParameter( "query" ).length() < 1 )
            throw new ServletException( "Please specify a query variable" );
        Query query = prepare( request.getParameter( "query" ) );
        String output = request.getParameter( "output" );
        if ( output == null )
            output = "";
        
        GlobalDAOJena dao = context.getGlobalDAO() instanceof GlobalDAOJena ? 
            (GlobalDAOJena)context.getGlobalDAO() : new GlobalDAOJena();
        
        QueryExecution exec = QueryExecutionFactory.create( query, dao.getTripleStore() );
        exec.getContext().put( SparqlServlet.cache, new HashMap<Node,Boolean>() );
        exec.getContext().put( SparqlServlet.dao, dao );
        exec.getContext().put( SparqlServlet.context, context );
        long no = System.currentTimeMillis();
        log.info( "Executing query #" + no + "\n" + query );
        
        if ( query.isAskType() )
        {
            boolean res = exec.execAsk();
            /*if ( output.equalsIgnoreCase( "json" ) ) // FIXME: dependency??
            {
                response.setHeader( "Content-Type", "application/json" );
                ResultSetFormatter.outputAsJSON( response.getOutputStream(), res );
            } else */if ( output.equalsIgnoreCase( "rdf/xml" ) )
            {
                response.setHeader( "Content-Type", "application/rdf+xml" );
                ResultSetFormatter.outputAsRDF( response.getOutputStream(), "RDF/XML", res );
            } else if ( output.equalsIgnoreCase( "n3" ) )
            {
                response.setHeader( "Content-Type", "text/rdf+n3" );
                ResultSetFormatter.outputAsRDF( response.getOutputStream(), "N3", res );
            } else if ( output.equalsIgnoreCase( "nt" ) || output.equalsIgnoreCase( "n-triples" ) || output.equalsIgnoreCase( "ntriples" ) )
            {
                response.setHeader( "Content-Type", "text/plain" );
                ResultSetFormatter.outputAsRDF( response.getOutputStream(), "N-TRIPLES", res );
            } else
            {
                response.setHeader( "Content-Type", "application/sparql-results+xml" );
                ResultSetFormatter.outputAsXML( response.getOutputStream(), res );
            }
        } else if ( query.isConstructType() )
        {
            Model m = exec.execConstruct();
            if ( output.equalsIgnoreCase( "n3" ) )
            {
                response.setHeader( "Content-Type", "text/rdf+n3" );
                m.write( response.getOutputStream(), "N3" );
            } else if ( output.equalsIgnoreCase( "nt" ) || output.equalsIgnoreCase( "n-triples" ) || output.equalsIgnoreCase( "ntriples" ) )
            {
                response.setHeader( "Content-Type", "text/plain" );
                m.write( response.getOutputStream(), "N-TRIPLES" );
            } else
            {
                response.setHeader( "Content-Type", "application/rdf+xml" );
                m.write( response.getOutputStream(), "RDF/XML" );
            }
        } else if ( query.isDescribeType() )
        {
            Model m = exec.execDescribe();
            if ( output.equalsIgnoreCase( "n3" ) )
            {
                response.setHeader( "Content-Type", "text/rdf+n3" );
                m.write( response.getOutputStream(), "N3" );
            } else if ( output.equalsIgnoreCase( "nt" ) || output.equalsIgnoreCase( "n-triples" ) || output.equalsIgnoreCase( "ntriples" ) )
            {
                response.setHeader( "Content-Type", "text/plain" );
                m.write( response.getOutputStream(), "N-TRIPLES" );
            } else
            {
                response.setHeader( "Content-Type", "application/rdf+xml" );
                m.write( response.getOutputStream(), "RDF/XML" );
            }
        } else if ( query.isSelectType() )
        {
            ResultSet res = exec.execSelect();
            /*if ( output.equalsIgnoreCase( "json" ) ) // FIXME: dependency??
            {
                response.setHeader( "Content-Type", "application/json" );
                ResultSetFormatter.outputAsJSON( response.getOutputStream(), res );
            } else */if ( output.equalsIgnoreCase( "rdf/xml" ) )
            {
                response.setHeader( "Content-Type", "application/rdf+xml" );
                ResultSetFormatter.outputAsRDF( response.getOutputStream(), "RDF/XML", res );
            } else if ( output.equalsIgnoreCase( "n3" ) )
            {
                response.setHeader( "Content-Type", "text/rdf+n3" );
                ResultSetFormatter.outputAsRDF( response.getOutputStream(), "N3", res );
            } else if ( output.equalsIgnoreCase( "nt" ) || output.equalsIgnoreCase( "n-triples" ) || output.equalsIgnoreCase( "ntriples" ) )
            {
                response.setHeader( "Content-Type", "text/plain" );
                ResultSetFormatter.outputAsRDF( response.getOutputStream(), "N-TRIPLES", res );
            } else
            {
                response.setHeader( "Content-Type", "application/sparql-results+xml" );
                ResultSetFormatter.outputAsXML( response.getOutputStream(), res );
            }
        } else
        {
            throw new ServletException( "Unrecognised query format!" );
        }
        
        response.getOutputStream().flush();
        log.info( "Finished executing query #" + no );
    }
    
    private Query prepare( String q )
    {
        return prepare( QueryFactory.create( q ) );
    }
    
    private Query prepare( Query q )
    {
        // Find the output variables
        final Set<String> vars = new HashSet<String>();
        if ( q.isSelectType() )
        {
            vars.addAll( q.getResultVars() );
            vars.addAll( q.getResultURIs() );
        } else if ( q.isConstructType() )
        {
            Template t = q.getConstructTemplate();
            t.visit( new TemplateVisitor() {

                public void visit( TemplateTriple template )
                {
                    Triple t = template.getTriple();
                    if ( t.getSubject().isVariable() )
                        vars.add( t.getSubject().getName() );
                    if ( t.getPredicate().isVariable() )
                        vars.add( t.getPredicate().getName() );
                    if ( t.getObject().isVariable() )
                        vars.add( t.getObject().getName() );
                }

                public void visit( TemplateGroup template )
                {
                    for( Object t : template.getTemplates() )
                        ((Template)t).visit( this );
                }
            } );
        }
        
        // Now add property function invocation to query tree
        Element el = q.getQueryPattern();
        if ( el == null )
            return q;
        el.visit( new ElementVisitorBase()
          {

              private Node pf = Node.createURI( "java:org.dspace.app.webui.servlet.AuthorizeFunction" );
              private Node pred = RDF.predicate.asNode();
              private Node subj = RDF.subject.asNode();

              @Override
              public void visit( ElementTriplesBlock el )
              {
                  Set<Node> seen = new HashSet<Node>();
                  Iterator<Triple> it = el.triples();
                  List<Triple> toAdd = new ArrayList<Triple>();
                  while ( it.hasNext() )
                  {
                      Triple curr = it.next();
                      if ( !seen.contains( curr.getPredicate() ) )
                      {
                          seen.add( curr.getPredicate() );
                          // Is this predicate output ever?
                          if ( curr.getPredicate().isVariable() && !vars.contains( curr.getPredicate().getName() ) )
                              continue;
                          toAdd.add( new Triple( curr.getPredicate(), pf, pred ) );
                      }
                      if ( !seen.contains( curr.getSubject() ) )
                      {
                          seen.add( curr.getSubject() );
                          // Is this subject output ever?
                          if ( curr.getSubject().isVariable() && !vars.contains( curr.getSubject().getName() ) )
                              continue;
                          toAdd.add( new Triple( curr.getSubject(), pf, subj ) );
                      }
                  }
                  for ( Triple t : toAdd )
                      el.addTriple( t );
              }

              @Override
              public void visit( ElementUnion el )
              {
                  for ( Object object : el.getElements() )
                      ((Element)object).visit( this );
              }

              @Override
              public void visit( ElementOptional el )
              {
                  el.getOptionalElement().visit( this );
              }

              @Override
              public void visit( ElementGroup el )
              {
                  for ( Object object : el.getElements() )
                      ((Element)object).visit( this );
              }

              @Override
              public void visit( ElementUnsaid el )
              {
                  el.getElement().visit( this );
              }

          } );
          return q;
    }
}
