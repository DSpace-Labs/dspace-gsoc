package org.dspace.statistics;

/**
 * Statistics logger, which other kinds of logger
 *
 * @author Federico Paparoni
 * @version $Revision: 1 $
 */

public class StatsLogger {
    public static void logEvent(LogEvent event) {
    	JMSLogger.logEvent(event);
    }
}
