package org.dspace.cis;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

//import org.dspace.core.Context;

/**
 * This <code>TimerListener</code> is added to start certificate-generation
 * the process. This process will run in each hour.
 * <p>
 * There should be an entry in the DD (<code>web.xml</code>). The entry
 * should looks like this:
 * <p>
 * &lt;listener&gt;
 * <p>
 * 
 * &lt;listener-class&gt;org.dspace.cis.CisTimerListener&lt;/listener-class&gt;
 * <p>
 * 
 * &lt;/listener&gt;
 * <p>
 * And it must be placed before any <code>servlet</code> and after all the
 * <code>filter-mapping</code>s.
 * 
 * @author Administrator
 * @see CertificateGenerator
 * @see web.xml
 */
public class CisTimerListener implements ServletContextListener
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(CisTimerListener.class);

    private static final int HOUR_INTERVAL = 60 * 60 * 1000;

    private Timer timer = null;

    public void contextDestroyed(ServletContextEvent event)
    {

        timer.cancel();

        if (log.isDebugEnabled())
        {
            log.info("The cis-timer-listener has been cancelled.");
        }
    }

    public void contextInitialized(ServletContextEvent event)
    {
        timer = new Timer(true);

        // try
        // {
        // Make sure that the first task is happened at a whole number time.
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(new Date());

        int currentHour = currentTime.get(Calendar.HOUR);

        currentTime.set(Calendar.HOUR, currentHour + 1);
        currentTime.set(Calendar.MINUTE, 0);
        currentTime.set(Calendar.SECOND, 0);
        currentTime.set(Calendar.MILLISECOND, 0);

        Date nextHour = currentTime.getTime();

        log.info("The cis-timer-listener has been started.");

        timer.scheduleAtFixedRate(new CertificateGenerator(),
                nextHour/*new Date()*/, HOUR_INTERVAL);

        log.info("The task has been added to the cis-timer-listener.");

        // }
        // catch (SQLException e)
        // {
        // log
        // .error("Context creation failed when initialize the
        // CisTimerListner.");
        // }

    }

}
