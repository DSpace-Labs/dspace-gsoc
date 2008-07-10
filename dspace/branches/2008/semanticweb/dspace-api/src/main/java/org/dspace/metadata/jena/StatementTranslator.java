package org.dspace.metadata.jena;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.MetadataItem;
import org.dspace.metadata.Predicate;

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
        DSpaceObject subject = dao.assembleDSO( s.getSubject(), context );
        Predicate p = MetadataFactory.createPredicate( s.getPredicate().getURI() );
        return s.getResource().isLiteral() ? 
                MetadataFactory.createItem( subject, p, 
                        MetadataFactory.createLiteral( s.getLiteral() ) )
                : MetadataFactory.createItem( subject, p, 
                        dao.assembleDSO( s.getResource(), context ) );
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
                        dao.getResource( m.getSubject() ), 
                        (Property)m.getPredicate(), 
                        dao.getResource( m.getDSpaceObject() ) ) : 
                ResourceFactory.createStatement( 
                        dao.getResource( m.getSubject() ), 
                        (Property)m.getPredicate(), 
                        (Resource)m.getLiteralValue() );
    }
    
}