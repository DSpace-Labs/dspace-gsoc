package org.dspace.statistics.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

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

/**
 * StatisticsServlet is the main Servlet of Statistics Web UI
 *
 * @author Federico Paparoni
 */

public class StatisticsServlet extends DSpaceServlet {
	private static Logger log = Logger.getLogger(StatisticsServlet.class);

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
    	request.setAttribute("id_list", getReportList());
    	request.setAttribute("id_graph", getGraphList());
		JSPManager.showJSP(request, response, "statistics/list.jsp");
    }


    //Static method used to retrieve the list of 'List-Report'
    public static ArrayList getReportList() {
    	String stats_views=ConfigurationManager.getProperty("list.views");
		StringTokenizer tokenizer=new StringTokenizer(stats_views,",");
		String tempString="";
		ArrayList id_stat=new ArrayList();
		HashMap name_stat;

		while(tokenizer.hasMoreTokens()) {
			name_stat=new HashMap();
			tempString=tokenizer.nextToken();
			tempString=tempString.trim();
			name_stat.put("id",tempString);
			name_stat.put("name",ConfigurationManager.getProperty(tempString+".name"));
			id_stat.add(name_stat);
		}
		return id_stat;
    }

//  Static method used to retrieve the list of 'Graph-Report'
    public static ArrayList getGraphList() {
    	String stats_views=ConfigurationManager.getProperty("graph.views");
		StringTokenizer tokenizer=new StringTokenizer(stats_views,",");
		String tempString="";
		ArrayList id_stat=new ArrayList();
		HashMap name_stat;

		while(tokenizer.hasMoreTokens()) {
			name_stat=new HashMap();
			tempString=tokenizer.nextToken();
			tempString=tempString.trim();
			name_stat.put("id",tempString);
			name_stat.put("name",ConfigurationManager.getProperty(tempString+".name"));
			id_stat.add(name_stat);
		}
		return id_stat;
    }

}
