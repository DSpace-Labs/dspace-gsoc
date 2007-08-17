package org.dspace.statistics.web;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.statistics.dao.StatisticsDAO;
import org.dspace.statistics.dao.StatisticsDAOException;
import org.dspace.statistics.dao.StatisticsDAOFactory;
import org.dspace.statistics.event.LogEvent;

public class ListRenderer {
	private String list;
	private String name;
	private String action;
	private boolean date;
	private String attributes;
	private int range;
	private StatisticsDAO dao;
	private Context context;
	private static Logger log = Logger.getLogger(ListRenderer.class);

	public ListRenderer() {
		try {
			this.context=new Context();
		} catch (SQLException e) {
			log.error(e.toString());
		}
	}
	public LogEvent[] render() {
		//DAO Selection
		if (action.equals("SIMPLE_SEARCH"))
			dao=StatisticsDAOFactory.getSearchEventDAO(context);
		else if ((action.equals("ITEM_VIEW"))||(action.equals("COMMUNITY_VIEW"))||(action.equals("COLLECTION_VIEW"))||(action.equals("BITSTREAM_VIEW")))
			dao=StatisticsDAOFactory.getContentEventDAO(context);
		try {
			LogEvent[] logEvent=dao.find(action, attributes, date,range);
			return logEvent;
		} catch (Exception e) {
			log.error(e.toString());
			return null;
		}

	}

	public void setList(String list) {
		this.list = list;
	}
	public String getList() {
		return list;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getAction() {
		return action;
	}
	public void setDate(boolean date) {
		this.date = date;
	}
	public boolean isDate() {
		return date;
	}
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	public String getAttributes() {
		return attributes;
	}
	public void setRange(int range) {
		this.range = range;
	}
	public int getRange() {
		return range;
	}
}
