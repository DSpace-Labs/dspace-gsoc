package org.dspace.statistics.handler;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.statistics.dao.ContentEventDAO;
import org.dspace.statistics.dao.StatisticsDAOFactory;
import org.dspace.statistics.event.LogEvent;
import org.dspace.statistics.tools.CrawlerCheck;

public class ContentEventHandler implements StatisticalEventHandler {

	private LogEvent logEvent;
	private Context context;
	private ContentEventDAO contentEventDAO;
	private static Logger log = Logger.getLogger(ContentEventHandler.class);

	public void process() {
		//Checks if it's crawler
		//if so it discard it

		boolean isCrawler=CrawlerCheck.check(logEvent);
		if (isCrawler)
			return;

		//Checks if it has a referer from a Search Engine
		//if so it adds another attribute to the event

		try {
			contentEventDAO=StatisticsDAOFactory.getContentEventDAO(context);
			contentEventDAO.commit(logEvent);
			if (context != null && context.isValid()) {
				context.commit();
		    }
		} catch (Exception e) {
			log.error(e.toString());
		}
	}

	public void setContext(Context context) {
		this.context=context;
	}

	public void setLogEvent(LogEvent logEvent) {
		this.logEvent=logEvent;
	}

}
