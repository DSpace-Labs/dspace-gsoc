package org.dspace.statistics;

import org.dspace.statistics.event.LogEvent;

/**
 * Statistics logger
 *
 * @author Federico Paparoni
 * @version $Revision: 1 $
 */

public class StatsLogger {
    public static void logEvent(LogEvent event) {
    	JMSLogger.logEvent(event);
    }
}
