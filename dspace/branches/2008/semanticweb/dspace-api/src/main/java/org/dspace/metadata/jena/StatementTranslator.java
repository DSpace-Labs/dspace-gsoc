package org.dspace.metadata.jena;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.dao.jena.DSPACE;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.LiteralValue;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.Predicate;
import org.dspace.metadata.URIResource;

public class StatementTranslator 
{
    
    private Logger log = Logger.getLogger( StatementTranslator.class );
    private GlobalDAOJena dao;
    private Context context;
    
    public StatementTranslator( Context c )
    {
        try
        {
            dao = new GlobalDAOJena();
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
    }
    
    public StatementTranslator( GlobalDAOJena dao, Context c )
    {
        context = c;
        this.dao = dao;
    }
    
    public Iterator<MetadataItem> translate( final Iterator<Statement> it ) {
        final StatementTranslator tran = this;
        return new Iterator<MetadataItem>() 
        {

            public boolean hasNext()
            {
                return it.hasNext();
            }

            public MetadataItem next()
            {
                return tran.translate( it.next() );
            }

            public void remove()
            {
                it.remove();
            }
            
        };
    }
    
    public Collection<MetadataItem> translate( Collection<Statement> coll )
    {
        Collection<MetadataItem> out = new HashSet<MetadataItem>();
        for ( Statement s : coll )
            out.add( translate( s ) );
        return out;
    }
    
    public MetadataItem[] translate( Statement[] coll )
    {
        List<MetadataItem> out = new ArrayList<MetadataItem>();
        for ( Statement s : coll )
            out.add( translate( s ) );
        return out.toArray( new MetadataItem[ coll.length ]);
    }
    
    public MetadataItem translate( Statement s )
    {
        URIResource subject;
        if ( s.getSubject().hasProperty( RDF.type ) && 
                    s.getSubject().getRequiredProperty( RDF.type ).
                        getResource().getNameSpace().startsWith( 
                        dao.getResourceBase() ) )
            subject = dao.assembleDSO( s.getSubject(), context );
        else
            subject = MetadataFactory.createURIResource( s.getSubject() );
        
        Predicate p = MetadataFactory.createPredicate( context, s.getPredicate().getURI() );
        if ( s.getObject().isLiteral() )
        {
            return MetadataFactory.createItem( subject, p, 
                    MetadataFactory.createLiteral( s.getLiteral() ) );
        } else
        {
            // Is this a DSO?
            if ( s.getResource().hasProperty( RDF.type ) && 
                    s.getResource().getRequiredProperty( RDF.type ).
                        getResource().getNameSpace().startsWith( 
                        dao.getResourceBase() ) )
                return MetadataFactory.createItem( subject, p, 
                        dao.assembleDSO( s.getResource(), context ) );
            
            return MetadataFactory.createItem( subject, p, 
                    MetadataFactory.createURIResource( s.getResource() ) );
        }
    }
    
    public Iterator<Statement> translateItems( final Iterator<MetadataItem> it ) {
        final StatementTranslator tran = this;
        return new Iterator<Statement>() 
        {

            public boolean hasNext()
            {
                return it.hasNext();
            }

            public Statement next()
            {
                return tran.translateItem( it.next() );
            }

            public void remove()
            {
                it.remove();
            }
            
        };
    }
    
    public List<Statement> translateItems( Collection<MetadataItem> coll )
    {
        List<Statement> out = new ArrayList<Statement>();
        for ( MetadataItem m : coll )
            out.add( translateItem( m ) );
        return out;
    }
    
    public Statement[] translateItems( MetadataItem[] coll )
    {
        List<Statement> out = new ArrayList<Statement>();
        for ( MetadataItem m : coll )
            out.add( translateItem( m ) );
        return out.toArray( new Statement[ coll.length ]);
    }
    
    public Statement translateItem( MetadataItem m )
    {
        return m.isDSpaceObject() ? 
                ResourceFactory.createStatement( 
                        ResourceFactory.createResource( m.getSubject().getURI() ), 
                        (Property)m.getPredicate(), 
                        ResourceFactory.createResource( m.getDSpaceObject().getURI() ) ) : 
               (m.isLiteral() ?
                    ResourceFactory.createStatement( 
                            ResourceFactory.createResource( m.getSubject().getURI() ), 
                            (Property)m.getPredicate(), 
                            (Literal)m.getLiteralValue() ) :
                    ResourceFactory.createStatement( 
                            ResourceFactory.createResource( m.getSubject().getURI() ), 
                            (Property)m.getPredicate(), 
                            (Resource)m.getURIResource() ));
    }
    
    public Resource translate( URIResource o )
    {
        return o == null ? null : dao.getTripleStore().getResource( o.getURI() );
    }
    
    public Resource translate( LiteralValue v )
    {
        return v == null ? null : (Resource)v;
    }
    
    public Property translate( Property p )
    {
        return p == null ? null : (Property)p;
    }
    
}