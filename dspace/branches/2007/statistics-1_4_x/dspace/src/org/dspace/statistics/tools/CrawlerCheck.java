package org.dspace.statistics.tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.statistics.event.LogEvent;

/**
 * CrawlerCheck checks the ip of visitors
 * and return true if it's a crawler, otherwise false
 *
 * @author Federico Paparoni
 */

public class CrawlerCheck {

	private static Logger log = Logger.getLogger(CrawlerCheck.class);
	private static Vector crawlerList;

	static {
		try {
			String filename=ConfigurationManager.getProperty("crawlers.list");
			File f=new File(filename);
			crawlerList=new Vector();
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

			String tempLine="";
			tempLine=reader.readLine();

			while(tempLine!=null) {
				crawlerList.add(tempLine);
				tempLine=reader.readLine();
			}
		} catch (Exception e) {
			log.error(e.toString());
		}
	}

	public static boolean check(LogEvent logEvent) {
		try {
			Hashtable attributes=logEvent.getAttributes();
			String ip=(String)attributes.get("ip");
			if (crawlerList.contains(ip))
				return true;
			else
				return false;
		} catch (Exception e) {
			log.error(e.toString());
			return false;
		}
	}
}
