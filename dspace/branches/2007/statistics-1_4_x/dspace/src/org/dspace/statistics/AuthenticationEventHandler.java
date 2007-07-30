package org.dspace.statistics;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.statistics.dao.AuthenticationEventDAO;
import org.dspace.statistics.dao.StatisticsDAOFactory;
import org.dspace.statistics.event.LogEvent;

public class AuthenticationEventHandler implements StatisticalEventHandler {

	private LogEvent logEvent;
	private Context context;
	private AuthenticationEventDAO authenticationEventDAO;
	private static Logger log = Logger.getLogger(AuthenticationEventHandler.class);

	public void process() {
		try {
	    	authenticationEventDAO=StatisticsDAOFactory.getAuthenticationEventDAO(context);
	    	authenticationEventDAO.commit(logEvent);
			if (context != null && context.isValid()) {
				context.commit();
		    }
		} catch (Exception e) {
			log.error(e.toString());
		}
	}

	public void setLogEvent(LogEvent logEvent) {
		this.logEvent=logEvent;
	}

	public void setContext(Context context) {
		this.context=context;
	}

}
