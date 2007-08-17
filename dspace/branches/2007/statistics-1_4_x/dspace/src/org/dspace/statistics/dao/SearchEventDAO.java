package org.dspace.statistics.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.dspace.core.Context;
import org.dspace.statistics.event.LogEvent;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class SearchEventDAO implements StatisticsDAO{

	private static Logger log = Logger.getLogger(SearchEventDAO.class);

	private Context context;
	private String find = "SELECT * FROM search_stats WHERE search_stats_id = ?";
	private String findAll = "SELECT * FROM search_stats";

	// TODO Fix this query with LIKE or create another method for it
	private String findByQuery = "SELECT * FROM search_stats WHERE query = ?";
	private String delete = "DELETE FROM search_stats WHERE search_stats_id = ?";
	private ArrayList log_columns;
	private ArrayList attributes_columns;

	public SearchEventDAO(Context context) {
		this.context=context;
		this.log_columns = new ArrayList();
		this.log_columns.add("logs_id");
		this.log_columns.add("event_date");
		this.log_columns.add("action");
		this.log_columns.add("ip");
		this.attributes_columns = new ArrayList();
		this.attributes_columns.add("attributes_id");
		this.attributes_columns.add("param");
		this.attributes_columns.add("value");
		this.attributes_columns.add("logs_id");
	}

	public boolean commit(LogEvent logEvent) throws StatisticsDAOException {
		TableRow row;
		Hashtable attributes;
		Enumeration enumeration;
		String param,value;
		int logID;

		attributes=logEvent.getAttributes();
		enumeration=attributes.keys();

		try {
				//Creation of the entry in logs table
				row = DatabaseManager.create(context, "logs");
				row.setColumn("event_date", new Date());
		        row.setColumn("action", logEvent.getType());
		        row.setColumn("ip", (String)attributes.get("ip"));

		        DatabaseManager.update(context, row);
		        logID=row.getIntColumn("logs_id");
		        //Creation of the entry in logs table

		        while (enumeration.hasMoreElements()) {
		        	row = DatabaseManager.create(context, "logs_attributes");
		        	param=(String)enumeration.nextElement();
		        	value=(String)attributes.get(param);
		        	row.setColumn("param", param);
		        	row.setColumn("value", value);
		        	row.setColumn("logs_id",logID);
		        	DatabaseManager.update(context, row);
		        }
		        context.commit();
		        return true;
		} catch (SQLException e) {
			throw new StatisticsDAOException(e);
		}

	}

	public LogEvent create() throws StatisticsDAOException {
		return null;
	}

	public boolean delete(LogEvent si) throws StatisticsDAOException {
		//TODO
        return true;
	}

	public LogEvent find(int id) throws StatisticsDAOException {
		return null;
	}

	public LogEvent[] findAll() throws StatisticsDAOException {
		return null;
	}


	public int getID() throws StatisticsDAOException {
		return 0;
	}

	public boolean setID(int id) throws StatisticsDAOException {
		return false;
	}

	public LogEvent[] find(String action, String attributes, boolean date, int range) throws StatisticsDAOException {
		log.info("Prova microfono");
		LogEvent resultEvent;
		Object[] params;
		try {
			String sqlLogs="";
			String sqlLogsAttributes="";


			if (date)
				sqlLogs="SELECT logs_id,event_date FROM logs WHERE action = ? ";
			else
				sqlLogs="SELECT logs_id FROM logs WHERE action = ? ";

			if (range>0) {
				long now=System.currentTimeMillis();
				long start=now-(range*86400000);
				sqlLogs+=" AND event_date> ?";
				params = new Object[2];
				params[0]=new String(action);
				params[1]=new java.sql.Date(start);
			}
			else {
				params = new Object[1];
				params[0]=new String(action);
			}

			log.info("SQL STRING "+sqlLogs);

			StringTokenizer tokenizer=new StringTokenizer(attributes,",");
			Vector vectorAttr=new Vector();
			String tempString="";

			while(tokenizer.hasMoreTokens()) {
				tempString=tokenizer.nextToken();
				tempString=tempString.trim();
				vectorAttr.add(tempString);
			}

			TableRowIterator iterator,iteratorAttr;
			TableRow tableRow,tableRowAttr;

			iterator = DatabaseManager.query(context, sqlLogs, params);
			Vector logEventVector=new Vector();

			while(iterator.hasNext()) {
				log.info("Orca l'oca");
				resultEvent=new LogEvent();
				tableRow=iterator.next();
				resultEvent.setId(tableRow.getIntColumn("logs_id"));
				if (date)
					resultEvent.setAttribute("date",tableRow.getDateColumn("event_date").toString());

				//SELECT REQUESTED ATTRIBUTES FOR THE EVENT FOUND
				sqlLogsAttributes="SELECT param,value FROM logs_attributes WHERE logs_id='"+resultEvent.getId()+"'";
				iteratorAttr = DatabaseManager.query(context, sqlLogsAttributes);
				log.info("SQL PER ATTRIBUTI "+sqlLogsAttributes);

				while(iteratorAttr.hasNext()) {
					tableRowAttr=iteratorAttr.next();
					if (vectorAttr.contains(tableRowAttr.getStringColumn("param")))
						resultEvent.setAttribute(tableRowAttr.getStringColumn("param"), tableRowAttr.getStringColumn("value"));
				}
				iteratorAttr.close();
				logEventVector.add(resultEvent);
			}
			iterator.close();
			log.info("Gira ma non l'oca "+logEventVector.size());
			LogEvent[] results=new LogEvent[logEventVector.size()];
			for(int i=0;i<logEventVector.size();i++)
				results[i]=(LogEvent)logEventVector.elementAt(i);

			return results;
		} catch (Exception e) {
			log.error(e.toString());
			return null;
		}
	}

}
