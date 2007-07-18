package org.dspace.statistics.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
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


	public SearchItemDAOImpl(Context context) {
		this.context=context;
	}

	public boolean commit(SearchItem si) throws SearchItemException {
		TableRow row;
		try {
			row = DatabaseManager.create(context, "search_stats");
			log.info("Dice "+row.getIntColumn("search_stats_id"));
			//row.setColumn("search_stats_id", row.getIntColumn("search_stats_id"));
	        row.setColumn("last_modified", new Date());
	        row.setColumn("query", si.getQuery());
	        int res=DatabaseManager.update(context, row);

	        log.info("Dice dopo"+res);
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
		return null;
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
