package org.dspace.statistics.dao;
import java.util.Date;

public class SearchItem {
	private int ID = -1;
	private String query = null;
	private Date timestamp;
	private SearchItemDAO dao;
	private int count = 0;
	
	public SearchItem(SearchItemDAO dao) {
		this.dao=dao;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public int getID() {
		return ID; 
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setDao(SearchItemDAO dao) {
		this.dao = dao;
	}

	public SearchItemDAO getDao() {
		return dao;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}


}
