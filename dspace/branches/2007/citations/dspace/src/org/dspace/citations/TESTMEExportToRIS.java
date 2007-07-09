package org.dspace.citations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

//import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.PluginManager;
import org.dspace.handle.HandleManager;
import org.dspace.content.crosswalk.*;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformer;



public class TESTMEExportToRIS {

	/**
	 * @param args outputfilename handle1 handle2 ...
	 * @throws Exception 
	 */

	public static void main(String[] args) throws Exception {
		/** log4j category */
		//Logger log = Logger.getLogger(TESTMEExportToRIS.class); //private static gives me an error--huh?
		//TODO: use log.warn and write to log		

		//initialize Crosswalk 
		String CROSSWALK_PLUGIN = "echom"; 			//pass-through stylesheet, echoes metadata
		String STYLESHEET = "DIM2RIS.xsl";	        //put in /bin directory; starts with DIM
		DisseminationCrosswalk dip = (DisseminationCrosswalk)PluginManager.getNamedPlugin(org.dspace.content.crosswalk.XSLTDisseminationCrosswalk.class, "echom");

		//Self-named plugin of CROSSWALK_PLUGIN must be initialized with PluginManager		
		if (dip == null)
		{
			throw new CrosswalkInternalException("Cannot find a disseminate plugin for package=" + CROSSWALK_PLUGIN);
		}

		System.setProperty( "javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");

		//		get outfilename
		String outfilename = args[0];

		Item myItem = null;
		Context context = new Context();
		context.setIgnoreAuthorization(true);

		//get Item ID's and process them
		for (int i = 1; i < args.length; i++){
			String myIDString = args[i]; //Expected user to input handles

			// first, is myIDString a handle?
			if (myIDString.indexOf('/') != -1)
			{
				//TODO: Prevent Cast Error Exception on (myItem.getType() != Constants.ITEM))
				myItem = (Item) HandleManager.resolveToObject(context, myIDString); 

				if ((myItem == null) || (myItem.getType() != Constants.ITEM))
				{
					myItem = null;
				}
			}
			else 
			{
				myItem = Item.find(context, Integer.parseInt(myIDString));
			}

			if (myItem == null)
			{
				System.out
				.println("Error, item cannot be found: " + myIDString);}
			else
			{
				try
				{
					List dimlist = dip.disseminateList(myItem);
					XMLOutputter x = new XMLOutputter();
					Iterator it = dimlist.iterator();
					while (it.hasNext()) {
						System.out.println(x.outputString((Element)it.next()));
					}
					
					//transform and print to screen
					XSLTransformer newstylesheet = new XSLTransformer(STYLESHEET);
					List styled = newstylesheet.transform(dimlist); 					
					System.out.println(styled.toString()); //for testing 

					//print styled output
					PrintWriter outfile = 
						new PrintWriter (new FileOutputStream(outfilename));
					x.output(styled, outfile);
					outfile.flush();
					outfile.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}