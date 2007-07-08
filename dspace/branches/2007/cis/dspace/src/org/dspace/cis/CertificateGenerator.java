package org.dspace.cis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

//import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
//import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Utils;

/**
 * This is the <code>CertificateGeneration</code> task that is run in each
 * hour. This is added to the schedule of the listener
 * <code>CisTimerListener</code>.
 * <p>
 * The process could be described like this:
 * <p>
 * 1) Get all the <code>hash-values</code> in the previous
 * <code>time-interval</code>.
 * <p>
 * 2) For each item installed in the previous <code>time-interval</code>,
 * generate its certificate. This includes item's <code>handle</code>,
 * information of the <code>time-interval<code>
 * and <code>assistant-hash-values</code>.
 * <p>
 * 3) Archive the certificates in the file system.
 * <p>
 * 4) Delete the entries of <code>hash-values</code> in the database in the previous
 * time-interval.
 * 
 * 
 * @author Administrator
 *
 */
public class CertificateGenerator extends TimerTask
{

    private static final int CERTIFICATE_FORMAT_ID = 37;

    /** Our context */
    private Context context;

    /** log4j logger */
    private static Logger log = Logger.getLogger(CertificateGenerator.class);

    @Override
    public void run()
    {

        log.info("The certificate-generation process started!");
        // First, we should get all the hash values in the previous time
        // interval.
        // Get the last hour's time interval ID
        Date date = new Date();
        int timeInterval_id = org.dspace.cis.Utils.getTimeInterval_id(date) - 1;
        List<HashvalueofItem> hashvaluesOfItemList = new ArrayList<HashvalueofItem>();
        List<String> hashvalues = new ArrayList<String>();
        HashvalueofItem[] hashvaluesOfItemArray = null;
        DigestFactory dF = new DigestFactory();
        // List certificates = new ArrayList();
        TableRow bitstreamTR = null;
        Item item = null;
        Bundle bundle = null;
        Bitstream bitstream = null;
        // Date from = org.dspace.cis.Utils.getLastFrom(date);
        // Date to = org.dspace.cis.Utils.getLastTo(date);

        try
        {
            context = new Context();
            TableRowIterator tri = DatabaseManager.query(context,
                    "select * from hashvalueofitem where time_interval_id = '"
                            + timeInterval_id + "'");
            while (tri.hasNext())
            {
                bitstreamTR = tri.next();
                hashvaluesOfItemList.add(new HashvalueofItem(context)
                        .setHash_Algorithm(
                                bitstreamTR.getStringColumn("hash_algorithm"))
                        .setHashValue(bitstreamTR.getStringColumn("hashvalue"))
                        .setItem_id(bitstreamTR.getIntColumn("item_id"))
                        .setTime_interval_id(
                                bitstreamTR.getIntColumn("time_interval_id")));
                hashvalues.add(bitstreamTR.getStringColumn("hashvalue"));
            }
            // hashvaluesOfItemArray = (HashvalueofItem[])
            // hashvaluesOfItemList.toArray();
            int size = hashvaluesOfItemList.size();
            hashvaluesOfItemArray = new HashvalueofItem[size];
            for (int i = 0; i < size; i++)
            {
                hashvaluesOfItemArray[i] = hashvaluesOfItemList.get(i);
            }

            for (int i = 0; i < hashvaluesOfItemArray.length; i++)
            {
                // Find the item this certificate belongs to.
                item = Item
                        .find(context, hashvaluesOfItemArray[i].getItem_id());
                // Add a bundle named "CERTIFICATE" to this item.
                bundle = item.createBundleWithoutAuthorization("CERTIFICATE");
                // Add a entry in the table "bitstream" for this certificate.
                bitstreamTR = DatabaseManager.create(context, "bitstream");
                // Set values in the entry for this certificate.
                setCertificateDatabase(bitstreamTR, item);
                // Add this certificate to the bundle named "CERTIFICATE"
                // created before.
                bitstream = new Bitstream(context, bitstreamTR);
                bundle.addBitstreamWithoutAuthorization(bitstream);
                // Create a certificate with the entry in the table "bitstream".
                Certificate cer = new Certificate(context, bitstreamTR);
                // Create witnesses for this certificate, set handle and hash
                // function name, and archive it.
                cer.setAlgorithm(dF.getPRIMITIVE().toString());
                cer.setHandle(item.getHandle());
                // cer.setFrom(from);
                // cer.setTo(to);
                cer.setLastModifiedTime(item.getLastModified());
                setAssistValues(cer, i, hashvaluesOfItemArray, dF);
                // Write the certificate in file system.
                try
                {
                    cer.archive();
                }
                catch (IOException e)
                {
                    log
                            .error("Some IOException occurred in certificate archive procedure.");
                }
            }
            // Create witness for this time-interval and archive it in the
            // database.
            createWitness(timeInterval_id, hashvalues, dF);
            context.complete();
        }
        catch (SQLException e)
        {
            log.debug("SQLException thrown when generate certificates!");
        }
    }

    /**
     * Create witness for this time-interval and archive it in the database.
     * 
     * @param timeInterval_id
     * @param hashvalues
     * @param dF
     * @throws SQLException
     */
    private void createWitness(int timeInterval_id, List<String> hashvalues,
            DigestFactory dF) throws SQLException
    {
        int lastWitTimeIntervalID = timeInterval_id - 1;
        TableRowIterator tr = DatabaseManager.query(context,
                "select hashvalue from witness where time_interval_id = '"
                        + lastWitTimeIntervalID + "'");
        // It's the first time-interval since cis was set up.
        if (tr.hasNext())
        {
            hashvalues.add(tr.next().getStringColumn("hashvalue"));
        }

        Witness wit = new Witness().setHash_algorithm(
                dF.getPRIMITIVE().toString()).setHashvalue(
                org.dspace.cis.Utils.witHash(hashvalues, dF)).setOurContext(
                context).setTime_interval_id(timeInterval_id);
        wit.archive();
    }

    /**
     * Set the certificate's information in the database.
     * 
     * @param bitstreamTR
     *            the tableRow representing the certificate
     * @param item
     *            the item this certificate belongs to
     * @throws SQLException
     */
    private void setCertificateDatabase(TableRow bitstreamTR, Item item)
            throws SQLException
    {
        bitstreamTR.setColumn("bitstream_format_id", CERTIFICATE_FORMAT_ID);
        bitstreamTR.setColumn("internal_id", Utils.generateKey());
        bitstreamTR.setColumn("deleted", false);
        bitstreamTR.setColumn("store_number", ConfigurationManager
                .getIntProperty("assetstore.incoming"));
        bitstreamTR.setColumn("sequence_id", getSequenceOfItem(item));
        DatabaseManager.update(context, bitstreamTR);
    }

    /**
     * Get the given item's next available sequence number.
     * 
     * @param item
     *            given item
     * @return given item's next available sequence number
     * @throws SQLException
     */
    private int getSequenceOfItem(Item item) throws SQLException
    {
        int sequence = 0;
        Bundle[] bunds = item.getBundles();

        // find the highest current sequence number
        for (int i = 0; i < bunds.length; i++)
        {
            Bitstream[] streams = bunds[i].getBitstreams();

            for (int k = 0; k < streams.length; k++)
            {
                if (streams[k].getSequenceID() > sequence)
                {
                    sequence = streams[k].getSequenceID();
                }
            }
        }
        return sequence + 1;
    }

    /**
     * Set given certificate's assistant hash-values.
     * 
     * @param cer
     *            the certificate whose assistant hash-values are to be set.
     * @param index
     *            the item's index in the time-interval.
     * @param hashvaluesOfItem
     *            all hash-values in the time-interval.
     * @throws SQLException
     */
    private void setAssistValues(Certificate cer, int index,
            HashvalueofItem[] hashvaluesOfItem, DigestFactory df)
            throws SQLException
    {
        int length = hashvaluesOfItem.length;
        if (length > 1)
        {
            HashvalueofItem[] tmpArray = null;
            List<HashvalueofItem> tmpList = new ArrayList<HashvalueofItem>();
            if (index <= max2N(length))
            {
                if (length > max2N(length))
                {
                    cer
                            .addWitness(new AssistHash(catHash(
                                    hashvaluesOfItem, max2N(length) + 1,
                                    length, df), AssistHashPos.RIGHT));
                }

                setAssistValuesWithBound(cer, index, hashvaluesOfItem, 1,
                        max2N(length), df);
            }
            else
            {
                cer.addWitness(new AssistHash(catHash(hashvaluesOfItem, 1,
                        max2N(length), df), AssistHashPos.LEFT));
                for (int i = max2N(length); i < length; i++)
                {
                    tmpList.add(hashvaluesOfItem[i]);
                }
                tmpArray = (HashvalueofItem[]) tmpList.toArray();
                setAssistValues(cer, index - max2N(length), tmpArray, df);
            }
        }
        addLastWitness(cer);
    }

    /**
     * Add last witness to this certificate. The witness's position will always
     * be <code>AssistHashPos.LEFT</code> because the witness is generated in
     * the last time-interval.
     * 
     * @param cer
     * @throws SQLException
     */
    private void addLastWitness(Certificate cer) throws SQLException
    {
        int lastWitTimeIntervalID = org.dspace.cis.Utils.getTimeInterval_id(cer
                .getLastModifiedTime());
        TableRowIterator tr = DatabaseManager.query(context,
                "select hashvalue from witness where time_interval_id = '"
                        + lastWitTimeIntervalID + "'");
        // It's the first time-interval since cis was set up.
        if (!tr.hasNext())
        {
            return;
        }
        else
        {
            cer.addWitness(new AssistHash(tr.next()
                    .getStringColumn("hashvalue"), AssistHashPos.LEFT));
        }
    }

    /**
     * Set given certificate's assistant hash-value. The hash-values in the
     * time-interval are specified by the bound (<code>from</code> and <code>
     * to</code>).
     * 
     * @param cer
     *            the certificate whose assistant hash-values are to be set.
     * @param index
     *            the item's index in the time-interval.
     * @param hashvaluesOfItem
     *            all hash-values in the time-interval.
     * @param from
     *            bound of the array.
     * @param to
     *            bound of the array.
     * @param df
     *            the <code>DigestFactory</code> instance used to digest.
     */

    private void setAssistValuesWithBound(Certificate cer, int index,
            HashvalueofItem[] hashvaluesOfItem, int from, int to,
            DigestFactory df)
    {
        if (from + 1 == to)
        {
            if (from == index)
            {
                cer.addWitness(new AssistHash(hashvaluesOfItem[to - 1]
                        .getHashValue(), AssistHashPos.RIGHT));
            }
            else
            {
                cer.addWitness(new AssistHash(hashvaluesOfItem[from - 1]
                        .getHashValue(), AssistHashPos.LEFT));
            }
        }
        else if (from + to - 1 / 2 < index)
        {
            cer.addWitness(new AssistHash(catHash(hashvaluesOfItem, from, from
                    + to - 1 / 2, df), AssistHashPos.LEFT));
            setAssistValuesWithBound(cer, index, hashvaluesOfItem, from + to
                    + 1 / 2, to, df);
        }
        else
        {
            cer.addWitness(new AssistHash(catHash(hashvaluesOfItem, from + to
                    + 1 / 2, to, df), AssistHashPos.RIGHT));
            setAssistValuesWithBound(cer, index, hashvaluesOfItem, from, from
                    + to - 1 / 2, df);
        }

    }

    /**
     * Return the largest integer value which is 2^n and less than or equal to
     * the argument.
     * 
     * @param num
     *            a integer number
     * @return the largest integer value which is 2^n and less than or equal to
     *         the argument
     */
    private int max2N(int num)
    {
        double lg = Math.log(num) / Math.log(2);
        return (int) Math.pow(2, Math.floor(lg));
    }

    /**
     * Catenate some hash-values of a given array and digest it.
     * 
     * @param hashvaluesOfItem
     * @param from
     * @param to
     * @return the final hash-value
     */
    private String catHash(HashvalueofItem[] hashvaluesOfItem, int from,
            int to, DigestFactory df)
    {
        List<String> hashvalues = new ArrayList<String>();
        for (int i = from - 1; i <= to - 1; i++)
        {
            hashvalues.add(hashvaluesOfItem[i].getHashValue());
        }

        return org.dspace.cis.Utils.witHash(hashvalues, df);
        // if (from + 1 == to) {
        // return df.digest(hashvaluesOfItem[from - 1].getHashValue()
        // + hashvaluesOfItem[from].getHashValue());
        // } else {
        // return df.digest(catHash(hashvaluesOfItem, from,
        // (from + to - 1) / 2, df)
        // + catHash(hashvaluesOfItem, (from + to + 1) / 2, to, df));
        // }
    }

    // public static void main(String[] args) {
    // System.out.println(max2N(1));
    // System.out.println(max2N(2));
    // System.out.println(max2N(3));
    // System.out.println(max2N(4));
    // System.out.println(max2N(5));
    // System.out.println(max2N(6));
    // System.out.println(max2N(7));
    // System.out.println(max2N(8));
    // System.out.println(max2N(9));
    // System.out.println(max2N(10));
    // }
    // public static void main(String[] args)
    // {
    // new CertificateGenerator().run();
    // }

}
