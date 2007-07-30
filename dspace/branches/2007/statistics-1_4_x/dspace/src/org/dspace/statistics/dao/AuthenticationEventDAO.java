package org.dspace.statistics.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.statistics.event.LogEvent;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;

public class AuthenticationEventDAO implements StatisticsDAO {

	private static Logger log = Logger.getLogger(AuthenticationEventDAO.class);

	private Context context;
	private ArrayList log_columns;
	private ArrayList attributes_columns;

	public AuthenticationEventDAO(Context context) {
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
		// TODO Auto-generated method stub
		return null;
	}

	public boolean delete(LogEvent pi) throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return false;
	}

	public LogEvent find(int id) throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public LogEvent[] findAll() throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getID() throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean setID(int id) throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return false;
	}

}
