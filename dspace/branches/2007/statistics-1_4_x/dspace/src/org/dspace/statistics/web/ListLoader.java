package org.dspace.statistics.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.servlet.DSpaceServlet;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.statistics.dao.SearchEventDAO;
import org.dspace.statistics.dao.StatisticsDAO;
import org.dspace.statistics.dao.StatisticsDAOFactory;
import org.dspace.statistics.event.LogEvent;
import org.dspace.statistics.event.SearchEvent;
import org.dspace.statistics.tools.ObjectFilter;
import org.dspace.statistics.web.bean.ListBean;

/**
 * GraphLoader retrieves informations for 'List-Report'
 *
 * @author Federico Paparoni
 */

public class ListLoader extends DSpaceServlet {

	private static Logger log = Logger.getLogger(ListLoader.class);
	private StatisticsDAO dao;

	protected void doDSGet(Context c,
	        HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException, SQLException, AuthorizeException
	{
	    // forward all requests to the post handler
		doDSPost(c, request, response);
	}

	protected void doDSPost(Context c,
	        HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException, SQLException, AuthorizeException
	    {
	        // check to see if the statistics are restricted to administrators
	        boolean publicise = ConfigurationManager.getBooleanProperty("report.public");

	        // determine the navigation bar to be displayed
	        String navbar = (publicise == false ? "admin" : "default");
	        request.setAttribute("navbar", navbar);

	        // is the user a member of the Administrator (1) group
	        boolean admin = Group.isMember(c, 1);

	        if (publicise || admin)
	        {
	            showStatistics(c, request, response);
	        }
	        else
	        {
	            throw new AuthorizeException();
	        }
	}

	public void showStatistics(Context context, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id=request.getParameter("id");
		String action=ConfigurationManager.getProperty(id+".action");
		String attributes=ConfigurationManager.getProperty(id+".attributes");
		String name=ConfigurationManager.getProperty(id+".name");
		String filter=ConfigurationManager.getProperty(id+".filter");
		boolean date=ConfigurationManager.getBooleanProperty(id+".date");
		int range=ConfigurationManager.getIntProperty(id+".range");
		Hashtable changeValues=new Hashtable();;

		LogEvent[] logEvent=null;

		if (action.equals("SIMPLE_SEARCH"))
			dao=StatisticsDAOFactory.getSearchEventDAO(context);
		else if ((action.equals("ITEM_VIEW"))||(action.equals("COMMUNITY_VIEW"))||(action.equals("COLLECTION_VIEW"))||(action.equals("BITSTREAM_VIEW")))
			dao=StatisticsDAOFactory.getContentEventDAO(context);
		else
			dao=StatisticsDAOFactory.getAuthenticationEventDAO(context);
		try {
			logEvent=dao.find(action, attributes, date,range);
		} catch (Exception e) {
			log.error(e.toString());
		}

		if (logEvent==null) {
			log.info("No LogEvent found");
			logEvent=new LogEvent[0];
		}
		else {
			log.info("LogEvents found "+logEvent.length);
			if (!filter.equals("0")) {
				StringTokenizer tokenizer=new StringTokenizer(filter,"|");
				String singleFilter="";
				StringTokenizer singleTokenizer;
				String old_param,new_param;
				String filterClass;
				Class c;
				ObjectFilter objectFilter;

				while(tokenizer.hasMoreElements()) {
					singleFilter=tokenizer.nextToken();
					singleTokenizer=new StringTokenizer(singleFilter,",");
					old_param=singleTokenizer.nextToken();
					filterClass=singleTokenizer.nextToken();
					new_param=singleTokenizer.nextToken();
					changeValues.put(old_param, new_param);
					try {
						c=Class.forName(filterClass);
						objectFilter=(ObjectFilter)c.newInstance();
						logEvent=objectFilter.resolve(context, logEvent, old_param, new_param);
					} catch (ClassNotFoundException e) {
						log.error(e.toString());
					} catch (InstantiationException e) {
						log.error(e.toString());
					} catch (IllegalAccessException e) {
						log.error(e.toString());
					}

				}
			}
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
			if (changeValues.containsKey(tempString)) {
				tempString=(String)changeValues.get(tempString);
			}
			java.util.Map temp = new java.util.HashMap();
			temp.put("property", tempString);
			temp.put("title", tempString.toUpperCase());
			columns.add(temp);
		}

		if (date) {
			java.util.Map temp = new java.util.HashMap();
			temp.put("property", "date");
			temp.put("title", "DATE");
			columns.add(temp);
		}

		getServletContext().setAttribute( "title", name);
		getServletContext().setAttribute( "logEvents", logEvents);
		getServletContext().setAttribute( "columns", columns);
		getServletContext().setAttribute( "id_list", StatisticsServlet.getReportList());
		getServletContext().setAttribute( "id_graph", StatisticsServlet.getGraphList());
		getServletContext().setAttribute( "itemsView", ConfigurationManager.getIntProperty(id+".items"));

		JSPManager.showJSP(request, response, "statistics/list-report.jsp");

		try {
			context.complete();
		} catch (SQLException e) {
			log.error(e.toString());
		}
	}
}
