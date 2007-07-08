package org.dspace.citations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

//import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.PluginManager;
import org.dspace.handle.HandleManager;
import org.dspace.content.crosswalk.CrosswalkInternalException;
import org.dspace.content.crosswalk.XSLTDisseminationCrosswalk;
import org.dspace.content.packager.PackageDisseminator;

import org.jdom.Document;
//import org.jdom.output.XMLOutputter;
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
		
		XSLTDisseminationCrosswalk myXSLTDisseminationCrosswalk = new XSLTDisseminationCrosswalk(); 

		String CROSSWALK_PLUGIN = "echom"; 			//pass-through stylesheet, echoes metadata
		String STYLESHEET = "DIM2RIS.xsl";
		PackageDisseminator dip = (PackageDisseminator)
          PluginManager.getNamedPlugin(PackageDisseminator.class, CROSSWALK_PLUGIN);
				
		if (dip == null)
            throw new CrosswalkInternalException("Cannot find a disseminate plugin for package=" + CROSSWALK_PLUGIN);
		//get outfilename
		String outfilename = args[0];

		System.setProperty( "javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");

		//open file for reading
		try
		{
			PrintWriter outfile = new PrintWriter (
					new FileOutputStream(outfilename));

			Item myItem = null;
			Context context = new Context();
			context.setIgnoreAuthorization(true);

			//get handles and process them
			for (int i = 1; i < args.length; i++){
				String myIDString = args[i]; //Expected user to input handles
				System.out.print("Processing " + myIDString +"...");

				// first, is myIDString a handle?
				if (myIDString.indexOf('/') != -1)
				{
					myItem = (Item) HandleManager.resolveToObject(context, myIDString);
					outfile.print(myIDString + "/n");
					List dimlist = myXSLTDisseminationCrosswalk.disseminateList(myItem);
					XSLTransformer newstylesheet = new XSLTransformer(STYLESHEET);
					Document styled = (Document) newstylesheet.transform(dimlist); 
					// Output the document
					
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
					.println("Error, item cannot be found: " + myIDString);
				}
			}   
			
			outfile.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}