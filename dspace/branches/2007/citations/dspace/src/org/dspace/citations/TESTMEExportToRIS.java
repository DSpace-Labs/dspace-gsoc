package org.dspace.citations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

//import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.PluginManager;
import org.dspace.handle.HandleManager;
import org.dspace.content.crosswalk.*;

import org.jdom.Document;
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
		PrintWriter outfile = 
			new PrintWriter (new FileOutputStream(outfilename));
		try
		{
			XMLOutputter x = new XMLOutputter();
			Context context = new Context();
			context.setIgnoreAuthorization(true);

			//get Item ID's and process them
			for (int i = 1; i < args.length; i++)
			{
				DSpaceObject myItem = null;  //We only process Items but cannot guarantee what we'll get when we look up user input
				String myIDString = args[i]; //Expected user to input item IDs

				// first, is myIDString a handle?
				if (myIDString.indexOf('/') != -1)
				{
					myItem = HandleManager.resolveToObject(context, myIDString); 

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
					Element dimel = dip.disseminateElement(myItem);
					Document mydoc = dimel.getDocument(); 

					//FIXME: get extra XMLDocument declaration on multiple inputs
					//transform with specified STYLESHEET
					XSLTransformer finalstylesheet = new XSLTransformer(STYLESHEET);
					Document styled = finalstylesheet.transform(mydoc); 					

					//print styled output		
					x.output(styled, outfile);
				}
			}
			//outfile.flush();
			outfile.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}