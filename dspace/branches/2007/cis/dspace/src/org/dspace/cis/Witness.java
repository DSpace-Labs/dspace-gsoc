package org.dspace.cis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

/**
 * Corresponding to the table *witness*
 * 
 * @author Wang Jiahui
 * 
 */
public class Witness
{

    /**
     * The witness's id.
     */
    private int witness_id;

    /**
     * The time interval's id.
     */
    private int time_interval_id;

    /**
     * The hash value.
     */
    private String hashvalue;

    /**
     * The hash algorithm.
     */
    private String hash_algorithm;

    /** Our context */
    private Context ourContext;

    /**
     * Get the hash algorithm.
     * @return the hash algorithm
     */
    public String getHash_algorithm()
    {
        return hash_algorithm;
    }

    /**
     * Set the hash algorithm
     * @param hash_algorithm the hash algorithm
     * @return this object
     */
    public Witness setHash_algorithm(String hash_algorithm)
    {
        this.hash_algorithm = hash_algorithm;
        return this;
    }

    /**
     * Get the hash value.
     * @return the hash value
     */
    public String getHashvalue()
    {
        return hashvalue;
    }

    /**
     * Set the hash value.
     * @param hashvalue the hashvalue
     * @return this object
     */
    public Witness setHashvalue(String hashvalue)
    {
        this.hashvalue = hashvalue;
        return this;
    }

    /**
     * Get the time interval's id.
     * @return the time interval's id
     */
    public int getTime_interval_id()
    {
        return time_interval_id;
    }

    /**
     * Set the time interval's id.
     * @param time_interval_id the time interval's id
     * @return this object
     */
    public Witness setTime_interval_id(int time_interval_id)
    {
        this.time_interval_id = time_interval_id;
        return this;
    }

    /**
     * Get the witness's id.
     * @return the witness's id
     */
    public int getWitness_id()
    {
        return witness_id;
    }

    /**
     * Set the witness's id.
     * @param witness_id the witness's id
     * @return this object
     */
    public Witness setWitness_id(int witness_id)
    {
        this.witness_id = witness_id;
        return this;
    }

    /**
     * Get the context.
     * @return the context
     */
    public Context getOurContext()
    {
        return ourContext;
    }

    /**
     * Set the context
     * @param ourContext the context
     * @return this object
     */
    public Witness setOurContext(Context ourContext)
    {
        this.ourContext = ourContext;
        return this;
    }

    /**
     * Write this witness into the database
     * 
     * @throws SQLException SQLException in SQL process
     */
    public void archive() throws SQLException
    {
        TableRow tR = DatabaseManager.row("witness");
        tR.setColumn("time_interval_id", this.time_interval_id);
        tR.setColumn("hashvalue", this.hashvalue);
        tR.setColumn("hash_algorithm", this.hash_algorithm);

        DatabaseManager.insert(ourContext, tR);
    }

    /**
     * Build the witness value for the given interval
     * 
     * @param c
     *            the Context
     * @param timeinterval_id the time interval's id
     * @return the witness
     * @throws SQLException SQLException in SQL process
     */
    public Witness buildWithInterval(Context c, int timeinterval_id)
            throws SQLException
    {

        Witness result = new Witness();

        List hashvalues = hashValuesOfInterval(c, timeinterval_id);

        DigestFactory dF = new DigestFactory();

        String tmpString = CisUtils.witHash(hashvalues, dF);

        result.setHash_algorithm(dF.getPRIMITIVE().toString());
        result.setHashvalue(tmpString);
        result.setTime_interval_id(timeinterval_id);
        result.setOurContext(ourContext);

        return result;
    }

    /**
     * Retrieve a list of hash values of the items in a given time_interal
     * 
     * @param c the context
     * @param timeinterval_id the time interval's id
     * @return the list of hashvalues of this interval
     * @throws SQLException SQLException in SQL process
     */
    private List hashValuesOfInterval(Context c, int timeinterval_id)
            throws SQLException
    {

        // execute the sql command to get the TableRowIterator as a result
        String query = "SELECT * FROM hashvalueofitem WHERE time_interval_id= ?";
        TableRowIterator tri = DatabaseManager.queryTable(c, "hashvalueofitem",
                query, timeinterval_id);

        // hold all the hash values in the time_interval
        List hashvalues = new ArrayList();
        while (tri.hasNext())
        {
            TableRow tR = tri.next();
            String hashvalue = tR.getStringColumn("hashvalue");
            hashvalues.add(hashvalue);
        }
        return hashvalues;
    }

    /**
     * Generate the assistant hash value for the given time_interval and request
     * numbers (<code>from</code> and <code>to</code>)
     * 
     * @param c the context
     * @param timeinterval_id the time interval's id
     * @param from the from time
     * @param to the to time
     * @return the assistant hash value
     * @throws SQLException SQLException in SQL process
     */
    public String witHash(Context c, int timeinterval_id, int from, int to)
            throws SQLException
    {

        List hashvalues = hashValuesOfInterval(c, timeinterval_id)
                .subList(from, to);

        DigestFactory dF = new DigestFactory();

        return CisUtils.witHash(hashvalues, dF);
    }

}
