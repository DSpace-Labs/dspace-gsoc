package org.dspace.statistics.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.servlet.DSpaceServlet;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.statistics.dao.ContentEventDAO;
import org.dspace.statistics.dao.StatisticsDAOFactory;
import org.dspace.statistics.event.LogEvent;

public class GraphLoader extends DSpaceServlet {

	private static Logger log = Logger.getLogger(GraphLoader.class);

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

	private void showStatistics(Context context,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, AuthorizeException
    {
		LogEvent[] logEvent=new LogEvent[0];
		String id=request.getParameter("id");
		String action=ConfigurationManager.getProperty(id+".action");
		String name=ConfigurationManager.getProperty(id+".name");
		String attribute=ConfigurationManager.getProperty(id+".attribute");
		String type=ConfigurationManager.getProperty(id+".type");

		if (type.trim().equals("Content")) {
			ContentEventDAO dao = StatisticsDAOFactory.getContentEventDAO(context);
			logEvent=dao.find(action, attribute);
		}
		else if (type.equals("Authorization")) {

		}
		else if (type.equals("Search")) {

		}

		if (logEvent==null) {
			log.info("No LogEvent found");
			logEvent=new LogEvent[0];
		}
		else {
			log.info("Ne ho trovati "+logEvent.length);
		}

		String tempString="";

		Hashtable graphValues=new Hashtable();
		for(int i=0;i<logEvent.length;i++) {
			LogEvent tempEvent=logEvent[i];
			tempString=(String)tempEvent.getAttributes().get(attribute);
			if (graphValues.containsKey(tempString)) {
				int how=Integer.parseInt((String)graphValues.get(tempString));
				graphValues.remove(tempString);
				how++;
				graphValues.put(tempString, ""+how);
			}
			else
				graphValues.put(tempString, "1");
		}

		Enumeration enumeration=graphValues.keys();
		ArrayList columns=new ArrayList();
		tempString="";
		ArrayList values=new ArrayList();

		while(enumeration.hasMoreElements()) {
			tempString=(String)enumeration.nextElement();
			java.util.Map temp = new java.util.HashMap();
			temp.put("property", tempString);
			temp.put("value", (String)graphValues.get(tempString));
			columns.add(temp);
		}

		java.util.Map temp = new java.util.HashMap();
		temp.put("property", "US");
		temp.put("value", "23");
		columns.add(temp);
		temp = new java.util.HashMap();
		temp.put("property", "EN");
		temp.put("value", "19");
		columns.add(temp);

		getServletContext().setAttribute( "graphValues", graphValues);
		getServletContext().setAttribute( "columns", columns);
		getServletContext().setAttribute( "id", id);
		getServletContext().setAttribute( "id_list", StatisticsServlet.getReportList());
		getServletContext().setAttribute( "id_graph", StatisticsServlet.getGraphList());

		JSPManager.showJSP(request, response, "statistics/graph-report.jsp");
    }

}
