package org.dspace.app.webui.servlet;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase;
import com.hp.hpl.jena.sparql.procedure.ProcLib;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.Map;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.dao.jena.DSPACE;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.jena.MetadataFactory;

public class AuthorizeFunction extends PropertyFunctionBase
{
    private Logger log = Logger.getLogger( AuthorizeFunction.class );
    public static final Symbol cache = Symbol.create( DSPACE.getURI() + "AuthCache" );
    public static final Symbol dao = Symbol.create( DSPACE.getURI() + "DAO" );
    public static final Symbol context = Symbol.create( DSPACE.getURI() + "Context" );
    
    @Override
    public QueryIterator exec( Binding binding, PropFuncArg argSubject,
                               Node predicate, PropFuncArg argObject,
                               ExecutionContext execCxt )
    {
        if ( argSubject == null )
            return ProcLib.result( binding, execCxt );
        
        Map<Node,Boolean> c = (Map<Node,Boolean>)execCxt.getContext().get( cache );
        GlobalDAOJena d = (GlobalDAOJena)execCxt.getContext().get( dao );
        Context ctx = (Context)execCxt.getContext().get( context );
        
        Node subj = argSubject.evalIfExists( binding ).getArg();
        if ( !c.containsKey( subj ) ) {
            boolean result = true;
            // Create a predicate or a plain dso?
            DSpaceObject o = argObject.evalIfExists( binding ).getArg().equals( RDF.predicate.asNode() ) ?
                MetadataFactory.createPredicate( ctx, subj.getURI() ) :
                d.assembleDSO( subj.getURI(), ctx );
            try
            {
                AuthorizeManager.authorizeAction( ctx, o, Constants.READ );
            } catch ( AuthorizeException ex )
            {
                result = false;
                log.warn( ex );
            }
            c.put( subj, result );
        }
        
        return c.get( subj ) ?
            ProcLib.result( binding, execCxt ) : ProcLib.noResults( execCxt );
    }
    
}