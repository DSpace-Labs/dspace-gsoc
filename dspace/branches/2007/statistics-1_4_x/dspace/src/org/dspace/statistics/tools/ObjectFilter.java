package org.dspace.statistics.tools;

import java.util.Hashtable;

import org.dspace.core.Context;
import org.dspace.statistics.event.LogEvent;

/**
 * Interface for filter classes
 *
 * @author Federico Paparoni
 */

public interface ObjectFilter {
	public Hashtable resolve(Context context,Hashtable hashtable);
	public LogEvent[] resolve(Context context, LogEvent[] logEvent,String old_param,String new_param);
}
