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

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
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
 * @author Wang Jiahui
 *
 */
public class CertificateGenerator extends TimerTask
{

    /**
     * The format_id for certificate bitstream.
     */
    private static final int CERTIFICATE_FORMAT_ID = 37;

    /**
     * Our context.
     */
    private Context context;

    /**
     * log4j logger.
     */
    private static Logger log = Logger.getLogger(CertificateGenerator.class);

    /**
     * The main method of certificate-generation method.
     */
    public void run()
    {

        log.info("The certificate-generation process started!");
        // First, we should get all the hash values in the previous time
        // interval.
        // Get the last hour's time interval ID
        Date date = new Date();
        int timeInterval_id = CisUtils.getTimeInterval_id(date) - 1;
        // the list of hashvalues of item
        List hashvaluesOfItemList = new ArrayList();
        // the array of hashvalues of item
        HashvalueofItem[] hashvaluesOfItemArray = null;
        // the list of hashvalues.
        List hashvalues = new ArrayList();
        // the digest factory object
        DigestFactory dF = new DigestFactory();
        // the bitstream table row for certificate
        TableRow bitstreamTR = null;
        // the conrespponding item, bundle and bitstram for this certificate
        Item item = null;
        Bundle bundle = null;
        Bitstream bitstream = null;

        try
        {
            context = new Context();
            // get the hashvalues of last time interval to both list and array
            TableRowIterator tri = DatabaseManager.query(context,
                    "select * from hashvalueofitem where time_interval_id = '"
                            + timeInterval_id + "' order by hashvalue_id");
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
            int size = hashvaluesOfItemList.size();
            hashvaluesOfItemArray = new HashvalueofItem[size];
            for (int i = 0; i < size; i++)
            {
                hashvaluesOfItemArray[i] = (HashvalueofItem) hashvaluesOfItemList
                        .get(i);
            }

            for (int i = 0; i < hashvaluesOfItemArray.length; i++)
            {
                // Find the item this certificate belongs to.
                item = Item
                        .find(context, hashvaluesOfItemArray[i].getItem_id());
                if (item.getBundles("CERTIFICATE").length == 0)
                {
                    // Add a bundle named "CERTIFICATE" to this item.
                    bundle = item
                            .createBundleWithoutAuthorization("CERTIFICATE");
                }
                else
                {
                    bundle = item.getBundles("CERTIFICATE")[0];
                    bundle.removeBitstreamWithoutAuthorization(bundle
                            .getBitstreamByName("CERTIFICATE"));
                }
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
                cer.setLastModifiedTime(item.getLastModified());
                setAssistValues(cer, i, hashvaluesOfItemArray, dF);
                cer.reverse();
                addLastWitness(cer, timeInterval_id);
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
            // archive the changes in the database.
            context.complete();
        }
        catch (SQLException e)
        {
            log.debug("SQLException thrown when generate certificates!");
        }
        catch (AuthorizeException e)
        {
            log.debug("AuthorizeException thrown when generate certificates!");
        }
        catch (IOException e)
        {
            log.debug("IOException thrown when generate certificates!");
        }
    }

    /**
     * Create witness for this time-interval and archive it in the database.
     * 
     * @param timeInterval_id
     *            the timeInterval_id for this witness
     * @param hashvalues
     *            the hashvalues to make this witness
     * @param dF
     *            the digest factory for this witness-generation process
     * @throws SQLException
     *             some Exceptions in SQL process
     */
    private void createWitness(int timeInterval_id, List hashvalues,
            DigestFactory dF) throws SQLException
    {
        int lastWitTimeIntervalID = timeInterval_id - 1;
        TableRowIterator tr = DatabaseManager.query(context,
                "select hashvalue from witness where time_interval_id = '"
                        + lastWitTimeIntervalID + "'");
        // If it's not the first time-interval since cis was set up.
        String lastWitValue = "";
        if (tr.hasNext())
        {
            lastWitValue = tr.next().getStringColumn("hashvalue");
        }

        Witness wit = new Witness().setHash_algorithm(
                dF.getPRIMITIVE().toString()).setHashvalue(
                dF.digest(lastWitValue + CisUtils.witHash(hashvalues, dF)))
                .setOurContext(context).setTime_interval_id(timeInterval_id);
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
     *             some Exceptions in SQL process
     */
    private void setCertificateDatabase(TableRow bitstreamTR, Item item)
            throws SQLException
    {
        bitstreamTR.setColumn("bitstream_format_id", CERTIFICATE_FORMAT_ID);
        bitstreamTR.setColumn("internal_id", Utils.generateKey());
        bitstreamTR.setColumn("name", "CERTIFICATE");
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
     *             some Exceptions in SQL process
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
     *             some Exceptions in SQL process
     */
    private void setAssistValues(Certificate cer, int index,
            HashvalueofItem[] hashvaluesOfItem, DigestFactory df)
            throws SQLException
    {
        // get the length of hashvalues
        int length = hashvaluesOfItem.length;
        // this process is necessary only if there are more than one hashvalues
        // in the same time-interval. If there is only one hashvalue, the
        // certificate is empty.
        if (length > 1)
        {
            HashvalueofItem[] tmpArray = null;
            List tmpList = new ArrayList();
            // if the index is less than the max 2^N value under length, we
            // should first process the values beyond max2N(length), make it as
            // the last assistHash, then hashvalues between 1 and max2N(length).
            if (index + 1 <= max2N(length))
            {
                if (length > max2N(length))
                {
                    cer
                            .addWitness(new AssistHash(catHash(
                                    hashvaluesOfItem, max2N(length) + 1,
                                    length, df), AssistHashPos.RIGHT));
                }

                setAssistValuesWithBounds(cer, index, hashvaluesOfItem, 1,
                        max2N(length), df);
            }
            // if the index is more than the max 2^N value under length, we
            // should first process the values between 1 and max2N(length), then
            // the values beyond max2N(length) (in a recursive way).
            else
            {
                cer.addWitness(new AssistHash(catHash(hashvaluesOfItem, 1,
                        max2N(length), df), AssistHashPos.LEFT));
                for (int i = max2N(length); i < length; i++)
                {
                    tmpList.add(hashvaluesOfItem[i]);
                }
                int size = tmpList.size();
                tmpArray = new HashvalueofItem[size];
                for (int i = 0; i < size; i++)
                {
                    tmpArray[i] = (HashvalueofItem) tmpList.get(i);
                }
                // set the other assistHashvalues in a recursive way.
                setAssistValues(cer, index - max2N(length), tmpArray, df);
            }
        }
    }

    /**
     * Add last witness to this certificate. The witness's position will always
     * be <code>AssistHashPos.LEFT</code> because the witness is generated in
     * the last time-interval.
     * 
     * @param cer
     *            the certificate
     * @throws SQLException
     *             some Exceptions in SQL process
     */
    private void addLastWitness(Certificate cer, int timeInterval_id)
            throws SQLException
    {
        int lastWitTimeIntervalID = timeInterval_id - 1;
        TableRowIterator tr = DatabaseManager.query(context,
                "select hashvalue from witness where time_interval_id = '"
                        + lastWitTimeIntervalID + "'");
        // If it's not the first time-interval since cis was set up.
        if (tr.hasNext())
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

    private void setAssistValuesWithBounds(Certificate cer, int index,
            HashvalueofItem[] hashvaluesOfItem, int from, int to,
            DigestFactory df)
    {
        // if there are only two hashvalues in this bound, we simply add the
        // other hashvalue as an assistHash.
        if (from + 1 == to)
        {
            if (from == index + 1)
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
        // if there are more than 2 hashvalues, we generate the assistHashes in
        // a half-and-half and recursive way.
        else if ((from + to - 1) / 2 < index + 1)
        {
            cer.addWitness(new AssistHash(catHash(hashvaluesOfItem, from, (from
                    + to - 1) / 2, df), AssistHashPos.LEFT));
            setAssistValuesWithBounds(cer, index, hashvaluesOfItem,
                    (from + to + 1) / 2, to, df);
        }
        else
        {
            cer.addWitness(new AssistHash(catHash(hashvaluesOfItem,
                    (from + to + 1) / 2, to, df), AssistHashPos.RIGHT));
            setAssistValuesWithBounds(cer, index, hashvaluesOfItem, from, (from
                    + to - 1) / 2, df);
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
     *            the hashvalues to be catenated.
     * @param from
     *            the from index of the array.
     * @param to
     *            the to index of the array.
     * @return the final hash-value
     */
    private String catHash(HashvalueofItem[] hashvaluesOfItem, int from,
            int to, DigestFactory df)
    {
        List hashvalues = new ArrayList();
        for (int i = from - 1; i <= to - 1; i++)
        {
            hashvalues.add(hashvaluesOfItem[i].getHashValue());
        }

        return CisUtils.witHash(hashvalues, df);
    }

}
