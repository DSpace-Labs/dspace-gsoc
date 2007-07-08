package org.dspace.cis;

import java.io.File;
//import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.dspace.content.Bitstream;
import org.dspace.core.ConfigurationManager;
//import org.dspace.core.Context;
//import org.dspace.storage.rdbms.DatabaseManager;
//import org.dspace.storage.rdbms.TableRowIterator;

public class Utils {
    
    //the number of milliseconds of an hour
    private static final int MILLISECONDS_OF_AN_HOUR = 3600000;
    // 271752 is the number of hours from 00:00:00 CST 1970 to 00:00:00 CST
    // 2001
    private static final int HOURS_OFFSET = 271752;
    /**
     * Return the intermediate path derived from the internal_id. This method
     * splits the id into groups which become subdirectories.
     * 
     * @param iInternalId
     *            The internal_id
     * @return The path based on the id without leading or trailing separators
     */
    public static String getIntermediatePath(String iInternalId) {
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
        for (int i = 0; i < directoryLevels; i++) {
            int digits = i * digitsPerLevel;
            if (i > 0) {
                buf.append(File.separator);
            }
            buf.append(iInternalId.substring(digits, digits + digitsPerLevel));
        }
        buf.append(File.separator);
//      buf.append(iInternalId);
        return buf.toString();
    }

    /**
     * 
     * @param bitstream
     *            the input bitstream
     * @return file's path that the bitstream represents
     */
    public static String getBitstreamFilePath(Bitstream bitstream) {
        String sAssetstoreDir;
        // Get the store to use
        int storeNumber = bitstream.getStoreNumber();

        // Default to zero ('assetstore.dir') for backwards compatibility
        if (storeNumber == -1) {
            storeNumber = 0;
        }
        if (storeNumber == 0) {
            // 'assetstore.dir' is always store number 0
            sAssetstoreDir = ConfigurationManager.getProperty("assetstore.dir");
            // else backup store numbers
        } else {
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
        miliSeconds += MILLISECONDS_OF_AN_HOUR;

        Date to = new Date(miliSeconds);

        interval[0] = from;
        interval[1] = to;

        return interval;
    }

    
    /**
     * Get from time of the time-interval of a given time.
     */
    public static Date getFrom(Date date)
    {
       
        long milliseds = date.getTime();
        long tmp = milliseds % (MILLISECONDS_OF_AN_HOUR);
        milliseds = milliseds - tmp;
        Date result = new Date(milliseds);
        return result;
    }
    
    /**
     * Get to time of the time-interval of a given time.
     */
    public static Date getTo(Date date)
    {
        long milliseds = date.getTime();
        long tmp = milliseds % (MILLISECONDS_OF_AN_HOUR);
        milliseds = milliseds - tmp + MILLISECONDS_OF_AN_HOUR;
        Date result = new Date(milliseds);
        return result;
    }
     
    /**
     * The timeInterval_id is number of hours since Mon Jan 01 00:00:00 CST 2001
     */
    public static int getTimeInterval_id(Date date)
    {
        Long tmp = new Long(date.getTime());
        
        tmp = tmp / MILLISECONDS_OF_AN_HOUR - HOURS_OFFSET;
        
        return (new Long(tmp)).intValue();
    }

    /**
     * Get from time of the time-interval in previous of a given time.
     */
    public static Date getLastFrom(Date date)
    {
       
        long milliseds = date.getTime();
        long tmp = milliseds % (MILLISECONDS_OF_AN_HOUR);
        milliseds = milliseds - tmp - MILLISECONDS_OF_AN_HOUR;
        Date result = new Date(milliseds);
        return result;
    }
    
    /**
     * Get to time of the time-interval in previous of a given time.
     * @param date
     * @return
     */
    public static Date getLastTo(Date date)
    {
        long milliseds = date.getTime();
        long tmp = milliseds % (MILLISECONDS_OF_AN_HOUR);
        milliseds = milliseds - tmp;
        Date result = new Date(milliseds);
        return result;
    }

    /**
     * Generate the witness hash value given a list of items' hash values. It's
     * finished by a assistant Merkley Tree. In each iteration, a temporary list
     * holds the nodes of a level, generate the parents' hash values and assign
     * them to the temporary List. This procedure will run in recursion until
     * the List just hold one string object, which is the output of this
     * function.
     * 
     * @param hashvalues
     * @param dF
     * @return
     */
    public static String witHash(List<String> hashvalues, DigestFactory dF) {
        /** temp values to help traversing the hashvalues */
        // Here we can't just use hashvalues.toArray(), or this will kill the 
        // certificate-generation thread. Haven't find the reason yet.
        int size = hashvalues.size();
        String[] tmpArray = new String[size];
        for (int i = 0; i < size; i++)
        {
            tmpArray[i] = hashvalues.get(i);
        }
        List<String> tmpList = new ArrayList<String>();
        String tmpString = null;
        while (tmpArray.length != 1) {
            /** if the size of the temp array is an even */
            if (tmpArray.length % 2 == 0) {
                for (int i = 0; i < tmpArray.length / 2; i++) {
                    tmpString = dF.digest(tmpArray[2 * i])
                            + dF.digest(tmpArray[2 * i + 1]);
                    tmpList.add(dF.digest(tmpString));
                }
//              tmpArray = (String[]) tmpList.toArray();
//                tmpArray = (String[]) listToArray(tmpList);
                size = tmpList.size();
                tmpArray = new String[size];
                for (int i = 0; i < size; i++)
                {
                    tmpArray[i] = tmpList.get(i);
                }
                tmpList.clear();
            } else {
                for (int i = 0; i < (tmpArray.length - 1) / 2; i++) {
                    tmpString = dF.digest(tmpArray[2 * i])
                            + dF.digest(tmpArray[2 * i + 1]);
                    tmpList.add(dF.digest(tmpString));
                }
                tmpList.add(tmpArray[tmpArray.length - 1]);
//              tmpArray = (String[]) tmpList.toArray();
//                tmpArray = (String[]) listToArray(tmpList);
                size = tmpList.size();
                tmpArray = new String[size];
                for (int i = 0; i < size; i++)
                {
                    tmpArray[i] = tmpList.get(i);
                }
                tmpList.clear();
            }
        }

        tmpString = tmpArray[0];
        return tmpString;
    }
    
    /**
     * A help method implement the function of <code>list.toArray()</code>.
     * Use that function would kill the certificate-generation thread.
     * Haven't find out the reason yet.
     * @param list
     * @return
     */
    public static Object[] listToArray(List list)
    {
        int size = list.size();
        Object[] result = new Object[size];
        for (int i = 0; i < size; i++)
        {
            result[i] = list.get(i);
        }
        return result;
    }
//  public static void main(String[] args) {
//      int timeInterval_id = TimeInterval.getTimeInterval_id(new Date());
//      System.out.println("The time_interval_ID is " + timeInterval_id);
//      Context context;
//      try {
//          context = new Context();
//          TableRowIterator tR = DatabaseManager.query(context,
//                  "select * from Hashvalueifitem where time_interval_id = "
//                          + timeInterval_id);
//          if (!tR.hasNext()) {
//              System.out.println("No entries return!");
//          }
//          while (tR.hasNext()) {
//              System.out.println(tR.next().getStringColumn("hashvalue"));
//          }
//
//      } catch (SQLException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      }
//
//  }
}
