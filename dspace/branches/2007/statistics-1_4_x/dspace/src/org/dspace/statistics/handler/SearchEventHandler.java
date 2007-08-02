package org.dspace.statistics.handler;
import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.statistics.dao.*;
import org.dspace.statistics.event.LogEvent;

public class SearchEventHandler implements StatisticalEventHandler {
   private LogEvent logEvent;
   private Context context;
   private SearchEventDAO searchEventDAO;
   private static Logger log = Logger.getLogger(SearchEventHandler.class);

   public void setLogEvent(LogEvent logEvent) {
	   this.logEvent=logEvent;
   }

   public void setContext(Context context) {
	   this.context=context;
   }

   public void process(){
      try {
    	searchEventDAO=StatisticsDAOFactory.getSearchEventDAO(context);
    	searchEventDAO.commit(logEvent);
		if (context != null && context.isValid()) {
			context.commit();
	    }
      } catch (Exception e) {
		log.error(e.toString());
	  }
   }

}
