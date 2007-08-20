package org.dspace.statistics.tools;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;

/**
 * Command-line executed class for cleaning the DSpace Statistics database.
 *
 * @author Federico Paparoni
 */
public class CleanDatabase
{
    private static Logger log = Logger.getLogger(CleanDatabase.class);
    private static String logs_sql= "DELETE FROM logs WHERE event_date <= ? ";

    public static void main(String[] argv)
    {
        // Usage checks
        if (argv.length != 1)
        {
        	System.out.println("Period not specified");
            log.warn("Period not specified");
            System.exit(1);
        }
        String param=argv[0];
        int startDate=Integer.parseInt(param);
        Object[] params=new Object[1];

        GregorianCalendar calendar=new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -startDate);
		params[0]=new java.sql.Date(calendar.getTimeInMillis());

        System.out.println("Cleaning Database");
        log.info("Cleaning Database");
        Context context=null;
		try {
			context = new Context();
		} catch (SQLException e1) {
			e1.printStackTrace();
			log.fatal("Caught exception:", e1);
            System.exit(1);
		}
        try
        {

            DatabaseManager.updateQuery(context, logs_sql, params);
            context.complete();
            System.out.println("Database Cleaned");
            log.info("Database Cleaned");
            System.exit(0);
        }
        catch (Exception e)
        {
        	context.abort();
        	System.out.println("Caught exception:"+e.toString());
        	e.printStackTrace();
            log.fatal("Caught exception:", e);
            System.exit(1);
        }
    }
}