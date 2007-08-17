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

public class CrawlerCheck {

	private static Logger log = Logger.getLogger(CrawlerCheck.class);

	public static boolean check(LogEvent logEvent) {
		try {
			Hashtable attributes=logEvent.getAttributes();
			String ip=(String)attributes.get("ip");
			String filename=ConfigurationManager.getProperty("crawlers.list");

			File f=new File(filename);
			Vector crawlerList=new Vector();
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String tempLine="";
			tempLine=reader.readLine();

			while(tempLine!=null) {
				crawlerList.add(tempLine);
				tempLine=reader.readLine();
			}

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
