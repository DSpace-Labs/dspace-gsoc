package org.dspace.app.webui.servlet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.util.Context;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.metadata.MetadataCollection;
import org.dspace.metadata.MetadataManager;
import org.dspace.metadata.MetadataManagerFactory;
import org.dspace.metadata.jena.MetadataFactory;
import org.dspace.metadata.jena.StatementTranslator;

public class AuthorizeDescribeHandler implements DescribeHandler
{
    private Logger log = Logger.getLogger( AuthorizeDescribeHandler.class );

    private Model m;
    private Context c;
    private MetadataManager meta;
    private StatementTranslator tran;
    
    public void start( Model m, Context c )
    {
        this.m = m;
        this.c = c;
        org.dspace.core.Context ctx = 
                (org.dspace.core.Context)c.get( SparqlServlet.context );
        meta = MetadataManagerFactory.get( ctx );
        tran = new StatementTranslator( ctx );
    }

    public void describe( Resource r )
    {
        try
        {
            MetadataCollection coll = 
                    meta.getMetadata( MetadataFactory.createURIResource( r ) );
            Iterator<Statement> it = tran.translateItems( coll.getMetadata() );
            while ( it.hasNext() )
                m.add( it.next() );
        } catch ( AuthorizeException ex )
        {
            log.warn( ex );
        }
    }

    public void finish()
    { 
        meta = null; // release the MetadataManager and StatementTranslator
        tran = null;
    }

}
