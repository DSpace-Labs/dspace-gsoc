/*
 * This Servlet lists all the renderers configured
 * in the DSpace Statistics
 */

package org.dspace.statistics.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.core.ConfigurationManager;
import org.dspace.core.PluginManager;
import org.dspace.statistics.handler.StatisticalEventHandler;

/**
 * @author Federico
 *
 */
public class ListReportServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out=response.getWriter();
		out.println("Ciao<br><br>");

		String stats_views=ConfigurationManager.getProperty("stats.views");
		StringTokenizer tokenizer=new StringTokenizer(stats_views,",");
		String tempString="";

		while(tokenizer.hasMoreTokens()) {
			tempString=tokenizer.nextToken();
			tempString=tempString.trim();

			out.println(tempString+"<br>");
			out.println("Type: "+ConfigurationManager.getProperty(tempString+".type"));
			out.println("<a href='listloader?id="+tempString+"'>Display stats</a>");
		}

		out.close();
	}
}
