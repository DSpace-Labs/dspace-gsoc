package org.dspace.statistics.tools;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.dspace.content.Community;
import org.dspace.content.DCValue;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.statistics.event.LogEvent;

/**
 * Filter class used for Collection id
 *
 * @author Federico Paparoni
 */

public class CollectionFilter implements ObjectFilter {
	private static Logger log = Logger.getLogger(CollectionFilter.class);

	public Hashtable resolve(Context context,Hashtable hashtable) {
		Hashtable resolved=new Hashtable();
		try {
			Enumeration keys=hashtable.keys();
			Collection collection;
			String title=null;
			String id=null;
			while(keys.hasMoreElements()) {
				id=(String)keys.nextElement();
				collection=Collection.find(context, Integer.parseInt(id));
				title=collection.getMetadata("name");
				resolved.put(title, hashtable.get(id));
			}
			return resolved;
		} catch (Exception e) {
			log.error(e.toString());
			return hashtable;
		}
	}

	public LogEvent[] resolve(Context context, LogEvent[] logEvent, String old_param, String new_param) {
		Hashtable attributes;
		Collection collection;
		String title=null;
		String id=null;
		for(int i=0;i<logEvent.length;i++) {
			attributes=logEvent[i].getAttributes();

			if (attributes.containsKey(old_param)) {
				try {
					id=(String)attributes.get(old_param);
					collection=Collection.find(context, Integer.parseInt(id));
					title=collection.getMetadata("name");
					attributes.remove(old_param);
					attributes.put(new_param, title);
					logEvent[i].setAttributes(attributes);
				} catch (NumberFormatException e) {
					log.error(e.toString());
				} catch (SQLException e) {
					log.error(e.toString());
				}
			}
		}

		return logEvent;
	}

}