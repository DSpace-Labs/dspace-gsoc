package org.dspace.statistics.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.core.ConfigurationManager;
import org.dspace.statistics.dao.SearchEventDAO;
import org.dspace.statistics.event.LogEvent;
import org.dspace.statistics.web.bean.ListBean;

public class ListLoader extends HttpServlet {

	private static Logger log = Logger.getLogger(ListLoader.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id=request.getParameter("id");
		ListRenderer listRenderer=new ListRenderer();
		listRenderer.setList(id);
		listRenderer.setName(ConfigurationManager.getProperty(id+".name"));
		listRenderer.setAction(ConfigurationManager.getProperty(id+".action"));
		String attributes=ConfigurationManager.getProperty(id+".attributes");
		listRenderer.setAttributes(attributes);
		listRenderer.setDate(ConfigurationManager.getBooleanProperty(id+".date"));
		listRenderer.setRange(ConfigurationManager.getIntProperty(id+".range"));

		LogEvent[] logEvent=listRenderer.render();

		if (logEvent==null) {
			log.info("No LogEvent found");
			logEvent=new LogEvent[0];
		}
		else {
			log.info("Ne ho trovati "+logEvent.length);
		}

		ArrayList logEvents=new ArrayList();
		for(int i=0;i<logEvent.length;i++)
			logEvents.add(logEvent[i]);

		ArrayList columns=new ArrayList();
		StringTokenizer tokenizer=new StringTokenizer(attributes,",");
		Vector attrVector=new Vector();
		String tempString="";

		while(tokenizer.hasMoreTokens()) {
			tempString=tokenizer.nextToken();
			tempString=tempString.trim();
			java.util.Map temp = new java.util.HashMap();
			temp.put("property", tempString);
			temp.put("title", tempString.toUpperCase());
			columns.add(temp);
		}

		if (listRenderer.isDate()) {
			java.util.Map temp = new java.util.HashMap();
			temp.put("property", "date");
			temp.put("title", "DATE");
			columns.add(temp);
		}

		getServletContext().setAttribute( "logEvents", logEvents);
		getServletContext().setAttribute( "columns", columns);
		getServletContext().setAttribute( "id_list", StatisticsServlet.getReportList());
		getServletContext().setAttribute( "id_graph", StatisticsServlet.getGraphList());
		getServletContext().setAttribute( "itemsView", ConfigurationManager.getIntProperty(id+".items"));

		JSPManager.showJSP(request, response, "statistics/list-report.jsp");
	}
}
