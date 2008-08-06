package org.dspace.app.webui.servlet;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.LiteralValue;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;
import org.dspace.metadata.jena.MetadataFactory;

public class MetadataSearchServlet extends DSpaceServlet
{
    
    private Logger log = Logger.getLogger( MetadataSearchServlet.class );
    
    @Override
    protected void doDSGet(Context context, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException,
            AuthorizeException
    {
        String pred = (String)request.getParameter( "pred" );
        String ns = (String)request.getParameter( "ns" );
        Predicate p = pred == null ? null : 
            MetadataFactory.createPredicate( context, 
                ns == null || ns.length() == 0 ? "vocab:" : ns, pred );
        
        String value = (String)request.getParameter( "value" );
        Value val = value == null ? null : 
            (Boolean.valueOf( (String)request.getParameter( "uri" ) ) ?
                MetadataFactory.createURIResource( value )
                : MetadataFactory.createPlainLiteral( value ));
        
        if ( p == null && val == null )
            throw new ServletException( "Cannot search without any terms!" );
        
        getBySparql( context, request, response, p, val );
    }
    
    private void getBySparql( Context context, HttpServletRequest request, 
            HttpServletResponse response, final Predicate p, final Value val ) 
            throws AuthorizeException, ServletException, IOException
    {
        String prop = p == null ? "?p" : "<" + p.getURI() + ">";
        String obj = val == null ? "?o" : (val.isLiteralValue() ? 
            "\"\"\"" + ((LiteralValue)val).getLexicalForm() + "\"\"\"" : 
            "<" + ((URIResource)val).getURI() + ">");
        String sparql = 
                "prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT * WHERE {\n" +
                "  ?s " + prop + " " + obj + ".\n" +
                "  OPTIONAL { ?s rdfs:label ?slabel . }\n" +
                "  OPTIONAL { ?s rdf:type ?stype .\n" +
                "    OPTIONAL { ?stype rdfs:label ?stypelabel }\n" +
                "  }\n" +
                "  OPTIONAL { " + prop + " rdfs:label ?plabel . }\n" +
                (val == null || !val.isLiteralValue() ? 
                    "  OPTIONAL { " + obj + " rdf:type ?otype .\n" +
                    "    OPTIONAL { ?otype rdfs:label ?otypelabel . }\n" +
                    "  }\n" +
                    "  OPTIONAL { " + obj + " rdfs:label ?olabel . }\n" 
                : "") +
                "}";
        log.info( "Querying for '" + p + "', '" + val + "':\n" + sparql );
        ResultSet r = SparqlServlet.toExec( sparql, ((GlobalDAOJena)context.getGlobalDAO()), context ).execSelect();
        
        MetadataSearchResultSet solutions = new MetadataSearchResultSet();
        while ( r.hasNext() )
        {
            QuerySolution sol = r.nextSolution();
            
            URIResource s = MetadataFactory.createURIResource( sol.getResource( "s" ) );
            Predicate pred = p == null ? MetadataFactory.createPredicate( context, sol.getResource( "p" ).getURI() ) : p;
            Value v = val;
            if ( v == null )
            {
                RDFNode n = sol.get( "o" );
                if ( n.isLiteral() )
                    v = MetadataFactory.createLiteral( (Literal)n );
                else
                    v = MetadataFactory.createURIResource( (Resource)n );
            }
            
            String pLabel = sol.contains( "plabel" ) ? 
                sol.getLiteral( "plabel" ).getLexicalForm() : null;
            String sLabel = sol.contains( "slabel" ) ? 
                sol.getLiteral( "slabel" ).getLexicalForm() : null;
            String oLabel = sol.contains( "olabel" ) ? 
                sol.getLiteral( "olabel" ).getLexicalForm() : null;
            String sType = sol.contains( "stype" ) ? 
                sol.getResource( "stype" ).getURI() : null;
            String oType = sol.contains( "otype" ) ? 
                sol.getResource( "otype" ).getURI() : null;
            String sTypeLabel = sol.contains( "stypelabel" ) ? 
                sol.getLiteral( "stypelabel" ).getLexicalForm() : null;
            String oTypeLabel = sol.contains( "otypelabel" ) ? 
                sol.getLiteral( "otypelabel" ).getLexicalForm() : null;
            
            solutions.add( s, pred, v, pLabel, sLabel, oLabel, sType, 
                    oType, sTypeLabel, oTypeLabel );
        }
        
        request.setAttribute( "meta.results", solutions );
        request.setAttribute( "meta.pred", p );
        request.setAttribute( "meta.value", val );
        JSPManager.showJSP( request, response, "/metadata.jsp" );
    }
    
}
