package org.dspace.citations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

//import org.dspace.core.*;
//import org.dspace.storage.rdbms.DatabaseManager;

public class ToRIS {

	/**
	 * @param args outputfilename handle1 handle2 ...
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
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
		int i = 1;
		while (i < args.length){
			System.out.print("Processing " + args[i]+"...");
			outfile.print(args[i]);
			outfile.println();
			i++;			
		}   
		//end the line
		System.out.println();

		
		//close file
			outfile.close();
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}
}