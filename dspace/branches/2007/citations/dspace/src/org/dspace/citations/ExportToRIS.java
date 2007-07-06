package org.dspace.citations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

//import org.apache.log4j.Logger;
//import org.dspace.core.*;

public class ExportToRIS {

	/**
	 * @param args outputfilename handle1 handle2 ...
	 * @throws SQLException 
	 */
	 
	 /** log4j category */
 
	 
	public static void main(String[] args) throws SQLException {
		
		//private static Logger log = Logger.getLogger(ExportToRIS.class); 
		
		//get outfilename
		String outfilename = args[0];
		
        //open file for reading
		try
		{
			PrintWriter outfile = new PrintWriter (
					new FileOutputStream(outfilename));
	
		//do something with handles
		//Context context = new Context();
		
		//get handles and process them
		for (int i = 1; i < args.length; i++){
			System.out.print("Processing " + args[i]+"...");
			outfile.print(args[i]);
			outfile.println();	
		}   
		//end the line
		System.out.println();
		
			outfile.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}