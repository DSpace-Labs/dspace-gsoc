package org.dspace.citations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

//import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

public class TESTMEExportToRIS {

	/**
	 * @param args outputfilename handle1 handle2 ...
	 * @throws SQLException 
	 */

	public static void main(String[] args) throws SQLException {
		/** log4j category */
		//Logger log = Logger.getLogger(TESTMEExportToRIS.class); //private static gives me an error--huh?
		//TODO: use log.warn and write to log

		//get outfilename
		String outfilename = args[0];

		//open file for reading
		try
		{
			PrintWriter outfile = new PrintWriter (
					new FileOutputStream(outfilename));

			Context context = new Context();

			//get handles and process them
			for (int i = 1; i < args.length; i++){
				String suspectedhandle = args[i]; //Expected user to input handles
				System.out.print("Processing " + suspectedhandle +"...");
				outfile.print(suspectedhandle);
				outfile.println();
				//NOTE: changed HandleManager.getHandlesForPrefix visibility to public
				List handleslist = 
					HandleManager.getHandlesForPrefix(context, suspectedhandle); 
				if (handleslist.isEmpty())
					System.out.print("No handle found starting with " 
							+ suspectedhandle + ".\n");
				else if (!handleslist.contains(suspectedhandle)){
					System.out.print("Sorry" + suspectedhandle + 
							"was not found. The handles starting with" 
							+ suspectedhandle + "are: ");
					for(Iterator j = handleslist.iterator(); j.hasNext(); )
						System.out.println(j.next());
					System.out.println();
				}
				else //suspectedhandle is a real handle; do real work here
					System.out.print("Found handle" + suspectedhandle + "\n");
				DSpaceObject dso = 
					HandleManager.resolveToObject(context, suspectedhandle);
				if (dso.getType() != Constants.ITEM)
			          System.out.print("Export to RIS can only handle Items.");
				//TODO: do something with mydso here
				//We know it's an Item.
				//
				

			}   

			outfile.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}