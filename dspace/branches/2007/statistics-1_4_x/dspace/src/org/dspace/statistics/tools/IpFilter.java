package org.dspace.statistics.tools;
import com.maxmind.geoip.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.statistics.event.LogEvent;

/**
 * Filter class used for Country/IP
 *
 * @author Federico Paparoni
 */

public class IpFilter implements ObjectFilter {
	private static Logger log = Logger.getLogger(IpFilter.class);
	private static String dbfile = ConfigurationManager.getProperty("geoip.file");

	public Hashtable resolve(Context context,Hashtable hashtable) {
		Hashtable resolved=new Hashtable();

		try {
			Enumeration keys=hashtable.keys();
			String ip=null;
			String country=null;
			LookupService cl = new LookupService(dbfile,LookupService.GEOIP_MEMORY_CACHE);
			while(keys.hasMoreElements()) {
				ip=(String)keys.nextElement();
				country=cl.getCountry(ip).getName();
				resolved.put(country, hashtable.get(ip));
			}
			return resolved;
		} catch (Exception e) {
			log.error(e.toString());
			return hashtable;
		}
	}

	public LogEvent[] resolve(Context context, LogEvent[] logEvent, String old_param, String new_param) {
		Hashtable attributes;
		String country=null;
		String ip=null;
		LookupService cl;
		try {
			cl = new LookupService(dbfile,LookupService.GEOIP_MEMORY_CACHE);
		} catch (IOException e) {
			log.error(e.toString());
			return logEvent;
		}

		for(int i=0;i<logEvent.length;i++) {
			attributes=logEvent[i].getAttributes();
			if (attributes.containsKey(old_param)) {
				ip=(String)attributes.get(old_param);
				country=cl.getCountry(ip).getName();
				attributes.remove(old_param);
				attributes.put(new_param, country);
				logEvent[i].setAttributes(attributes);
			}
		}

		return logEvent;
	}

}