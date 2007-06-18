package org.dspace.cis;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.dspace.content.Bitstream;
import org.dspace.core.ConfigurationManager;

public class Utils {
    /**
     * Return the intermediate path derived from the internal_id. This method
     * splits the id into groups which become subdirectories.
     * 
     * @param iInternalId
     *            The internal_id
     * @return The path based on the id without leading or trailing separators
     */
    public static String getIntermediatePath(String iInternalId)
    {
        // These settings control the way an identifier is hashed into
        // directory and file names
        //
        // With digitsPerLevel 2 and directoryLevels 3, an identifier
        // like 12345678901234567890 turns into the relative name
        // /12/34/56/12345678901234567890.
        //
        // You should not change these settings if you have data in the
        // asset store, as the BitstreamStorageManager will be unable
        // to find your existing data.
        int digitsPerLevel = 2;

        int directoryLevels = 3;

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < directoryLevels; i++)
        {
            int digits = i * digitsPerLevel;
            if (i > 0)
            {
                buf.append(File.separator);
            }
            buf.append(iInternalId.substring(digits, digits + digitsPerLevel));
        }
        buf.append(File.separator);
        buf.append(iInternalId);
        return buf.toString();
    }
    
    /**
     * 
     * @param bitstream
     *            the input bitstream
     * @return file's path that the bitstream represents
     */
    public static String getBitstreamFilePath(Bitstream bitstream)
    {
        String sAssetstoreDir;
        // Get the store to use
        int storeNumber = bitstream.getStoreNumber();

        // Default to zero ('assetstore.dir') for backwards compatibility
        if (storeNumber == -1)
        {
            storeNumber = 0;
        }
        if (storeNumber == 0)
        {
            // 'assetstore.dir' is always store number 0
            sAssetstoreDir = ConfigurationManager.getProperty("assetstore.dir");
            // else backup store numbers
        }
        else
        {
            sAssetstoreDir = ConfigurationManager.getProperty("assetstore.dir"
                    + "." + (new Integer(storeNumber)).toString());
        }

        String intermediatePath = getIntermediatePath(bitstream.getInternalID());

        StringBuffer bufFilename = new StringBuffer();
        // maybe this will cause some problem with the file separator
        bufFilename.append(sAssetstoreDir);
        bufFilename.append(File.separator);
        bufFilename.append(intermediatePath);

        return bufFilename.toString();

    }
    
	/**
	 * The interval would be get to an hour Both <code>from</code> and
	 * <code>to<code> are set by current time
	 *
	 */
	public static Date[] getInterval() {
		Date[] interval = new Date[2];
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int date = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);

		c.set(year, month, date, hour, 0, 0);
		Date from = c.getTime();

		long miliSeconds = from.getTime();
		miliSeconds += 3600000;

		Date to = new Date(miliSeconds);

		interval[0] = from;
		interval[1] = to;
		
		return interval;
	}
}
