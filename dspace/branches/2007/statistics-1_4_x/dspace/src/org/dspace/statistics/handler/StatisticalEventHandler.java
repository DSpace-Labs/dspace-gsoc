package org.dspace.statistics.handler;

import org.dspace.core.Context;
import org.dspace.statistics.event.LogEvent;;

public interface StatisticalEventHandler {

   public void setLogEvent(LogEvent le);

   public void setContext(Context context);

   public void process();

}
