package org.dspace.cis;

//import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import org.dspace.storage.rdbms.DatabaseManager;

import org.dspace.core.Context;

/**
 * This is the <code>CertificateGeneration</code> task that is run in each
 * hour. This is added to the schedule of the listener
 * <code>CisTimerListener</code>.
 * <p>
 * The process could be described like this:
 * <p>
 * 1) Get all the <code>hash-values</code> in the previous
 * <code>time-interval</code>.
 * <p>
 * 2) For each item installed in the previous <code>time-interval</code>,
 * generate its certificate. This includes item's <code>handle</code>,
 * information of the <code>time-interval<code>
 * and <code>assistant-hash-values</code>.
 * <p>
 * 3) Archive the certificates in the file system.
 * <p>
 * 4) Delete the entries of <code>hash-values</code> in the database in the previous
 * time-interval.
 * 
 * 
 * @author Administrator
 *
 */
public class CertificateGeneration extends TimerTask {


    /** Our context */
    private Context context;
    
    public CertificateGeneration(Context context)
    {
    	this.context = context;
    }
    
	@Override
	public void run() {

		// First, we should get all the hash values in the previous time interval.
		// Get time interval ID at present
		int timeInterval_id = TimeInterval.getTimeInterval_id(new Date());
		TableRowIterator tR = DatabaseManager.query(context, "select * from Hashvalueifitem where time_interval_id = ''")
	}

}
