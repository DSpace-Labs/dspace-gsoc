package org.dspace.statistics.web;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.servlet.DSpaceServlet;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.statistics.dao.ContentEventDAO;
import org.dspace.statistics.dao.StatisticsDAOFactory;
import org.dspace.statistics.event.LogEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

import com.keypoint.PngEncoder;

public class ChartServlet extends DSpaceServlet {

	protected String id;
	private static Logger log = Logger.getLogger(ChartServlet.class);

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

        // is the user a member of the Administrator (1) group
        boolean admin = Group.isMember(c, 1);

        if (publicise || admin)
        {
            createChart(c, request, response);
        }
        else
        {
            throw new AuthorizeException();
        }
    }

    protected void createChart(Context c, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	id=(String)request.getParameter("id");
    	JFreeChart chart=null;

    	if (id.equals("home")) {
    		CategoryDataset dataset = createDataset(c);
    		chart = createChart(dataset);
    	} else {
    		DefaultPieDataset dataset = createPieDataset();
    		chart = createPieChart(dataset);
    	}


        response.setContentType( "image/png" );
        BufferedImage buf = chart.createBufferedImage(640, 400, null);
        PngEncoder encoder = new PngEncoder( buf, false, 0, 9 );
        response.getOutputStream().write( encoder.pngEncode() );
    }

    private DefaultPieDataset createPieDataset() {
    	Hashtable graphValues=(Hashtable)getServletContext().getAttribute("graphValues");
    	DefaultPieDataset data = new DefaultPieDataset();
    	Enumeration keys=graphValues.keys();
    	String key="";

    	while(keys.hasMoreElements()) {
    		key=(String)keys.nextElement();
    		data.setValue(key, new Double((String)graphValues.get(key)));
    	}

    	return data;
    }

    private JFreeChart createPieChart(DefaultPieDataset dataset) {
    	JFreeChart chart = ChartFactory.createPieChart3D(
    			ConfigurationManager.getProperty(id+".name"),  // chart title
                dataset,                   // data
                true,                   // include legend
                true,
                false
            );

        chart.setBackgroundPaint(Color.BLUE);
        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(270);
        plot.setDirection(Rotation.ANTICLOCKWISE);
        plot.setForegroundAlpha(0.60f);
        plot.setInteriorGap(0.33);
    	return chart;
    }

    private CategoryDataset createDataset(Context c) {
    	//Load data for Statistics Homepage
		ContentEventDAO dao=StatisticsDAOFactory.getContentEventDAO(c);
		LogEvent[][] logEvents=new LogEvent[7][];
		double[] item=new double[7];
		double[] collection=new double[7];
		double[] community=new double[7];
		double[] bitstream=new double[7];

		try {
			for(int j=0;j<7;j++) {
				logEvents[j]=dao.find(j+1, j, null, "ALL");
				for(int i=0;i<logEvents[j].length;i++) {
					LogEvent temp=logEvents[j][i];
					if (temp.getType().equals("ITEM_VIEW"))
						item[j]++;
					else if (temp.getType().equals("COLLECTION_VIEW"))
						collection[j]++;
					else if (temp.getType().equals("COMMUNITY_VIEW"))
						community[j]++;
					else if (temp.getType().equals("BITSTREAM_VIEW"))
						bitstream[j]++;
				}
			}
		} catch (SQLException e) {
			log.error(e.toString());
		}

        double[][] data = new double[][] {item,collection,community,bitstream};
        String[] rowKeys = {"Item View","Collection View","Community View","Bitstream View"};
        String[] columnKeys = {"1","2","3","4","5","6","7"};
        return DatasetUtilities.createCategoryDataset(rowKeys,columnKeys, data);
    }

    /**
     * Creates a chart.
     *
     * @param dataset  the dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(final CategoryDataset dataset) {

        JFreeChart chart = ChartFactory.createBarChart3D(
            "Last week activity",      // chart title
            "Last week",               // domain axis label
            "Values",                  // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        CategoryPlot plot = chart.getCategoryPlot();
        CategoryAxis axis = plot.getDomainAxis();
        axis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 8.0)
        );
        BarRenderer3D renderer = (BarRenderer3D) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        return chart;

    }
}
