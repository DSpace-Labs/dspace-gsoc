package org.dspace.statistics.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class SearchItemDAOImpl implements SearchItemDAO{

	private static Logger log = Logger.getLogger(SearchItemDAOImpl.class);

	private Context context;
	private String find = "SELECT * FROM search_stats WHERE search_stats_id = ?";
	private String findAll = "SELECT * FROM search_stats";

	// TODO Fix this query with LIKE or create another method for it
	private String findByQuery = "SELECT * FROM search_stats WHERE query = ?";
	private String delete = "DELETE FROM search_stats WHERE search_stats_id = ?";
	private ArrayList columns;

	public SearchItemDAOImpl(Context context) {
		this.context=context;
		this.columns = new ArrayList();
		columns.add("search_stats_id");
		columns.add("query");
		columns.add("count");
		columns.add("last_modified");
	}

	public boolean commit(SearchItem si) throws SearchItemException {
		TableRow row;
		try {
			SearchItem results[]=findByQuery(si.getQuery());
			if (results==null) {
				row = DatabaseManager.create(context, "search_stats");
				row.setColumn("last_modified", new Date());
		        row.setColumn("query", si.getQuery());
		        row.setColumn("count", 1);
		        int res=DatabaseManager.update(context, row);
			}
			else {
				SearchItem saved_si=results[0];
		        TableRow tr = new TableRow("search_stats",columns);
		        tr.setColumn("search_stats_id", saved_si.getID());
		        tr.setColumn("query", saved_si.getQuery());
		        tr.setColumn("count", saved_si.getCount()+1);
		        tr.setColumn("last_modified", new Date());
		        DatabaseManager.update(context, tr);
			}
			return true;
		} catch (SQLException e) {
			throw new SearchItemException(e);
		}

	}

	public SearchItem create() throws SearchItemException {
		return new SearchItem(StatisticsDAOFactory.getSearchItemDAOFactory(context));
	}

	public boolean delete(SearchItem si) throws SearchItemException {
		try
        {
                Object[] param = { new Integer(si.getID()) };
                DatabaseManager.updateQuery(context, delete, param);

                DatabaseManager.delete(context, "search_stats", si.getID());
        }
        catch (SQLException e)
        {
                log.error("Caught exception: ", e);
                throw new SearchItemException(e);
        }
        return true;
	}

	public SearchItem find(int id) throws SearchItemException {
		return null;
	}

	public SearchItem[] findAll() throws SearchItemException {
		return null;
	}

	public SearchItem[] findByQuery(String query) throws SearchItemException {
		try
        {
	        ArrayList mappings = new ArrayList();
	        Object[] params = { query };
	        TableRowIterator tri = DatabaseManager.query(context, findByQuery, params);
	        int count=0;
	        int id=0;
	        Date timestamp;

	        if (!tri.hasNext())
	        {
	                return null;
	        }
	        else {
	        	TableRow tr = tri.next();
	        	count=tr.getIntColumn("count");
	        	id=tr.getIntColumn("search_stats_id");
	        	timestamp=tr.getDateColumn("last_modified");

	        	SearchItem si=create();
	        	si.setID(id);
	        	si.setCount(count);
	        	si.setQuery(query);
	        	si.setTimestamp(timestamp);
	        	mappings.add(si);
	        }
	        tri.close();

	        SearchItem[] maps = new SearchItem[mappings.size()];
	        return (SearchItem[]) mappings.toArray((SearchItem[]) maps);
        }
        catch (SQLException e)
        {
                log.error("caught exception: ", e);
                throw new SearchItemException(e);
        }
	}

	public SearchItem findBySearchItem(SearchItem searchItem) throws SearchItemException {
		return null;
	}

	public int getID() throws SearchItemException {
		return 0;
	}

	public boolean setID(int id) throws SearchItemException {
		return false;
	}

}
