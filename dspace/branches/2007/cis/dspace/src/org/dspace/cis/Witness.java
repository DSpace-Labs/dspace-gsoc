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
 * @author Administrator
 * 
 */
public class Witness {

    private int witness_id;

    private int time_interval_id;

    private String hashvalue;

    private String hash_algorithm;

    /** Our context */
    private Context ourContext;
    
//    public Witness(int timeIntervalID, String hashvalue, String hash_algorithm, Context context)
//    {
//        this.time_interval_id = timeIntervalID;
//        this.hashvalue = hashvalue;
//        this.hash_algorithm = hash_algorithm;
//        this.ourContext = context;
//    }

    public String getHash_algorithm() {
        return hash_algorithm;
    }

    public Witness setHash_algorithm(String hash_algorithm) {
        this.hash_algorithm = hash_algorithm;
        return this;
    }

    public String getHashvalue() {
        return hashvalue;
    }

    public Witness setHashvalue(String hashvalue) {
        this.hashvalue = hashvalue;
        return this;
    }

    public int getTime_interval_id() {
        return time_interval_id;
    }

    public Witness setTime_interval_id(int time_interval_id) {
        this.time_interval_id = time_interval_id;
        return this;
    }

    public int getWitness_id() {
        return witness_id;
    }

    public Witness setWitness_id(int witness_id) {
        this.witness_id = witness_id;
        return this;
    }

    public Context getOurContext() {
        return ourContext;
    }

    public Witness setOurContext(Context ourContext) {
        this.ourContext = ourContext;
        return this;
    }

    /**
     * Write this witness into the database
     * 
     * @throws SQLException
     */
    public void archive() throws SQLException {
        TableRow tR = DatabaseManager.row("witness");
        // tR.setColumn("witness_id", this.witness_id);
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
     * @param timeinterval_id
     * @return
     * @throws SQLException
     */
    public Witness buildWithInterval(Context c, int timeinterval_id)
            throws SQLException {

        Witness result = new Witness();

        List<String> hashvalues = hashValuesOfInterval(c, timeinterval_id);

        DigestFactory dF = new DigestFactory();

        String tmpString = Utils.witHash(hashvalues, dF);

        result.setHash_algorithm(dF.getPRIMITIVE().toString());
        result.setHashvalue(tmpString);
        result.setTime_interval_id(timeinterval_id);
        result.setOurContext(ourContext);

        return result;
    }

    /**
     * Retrieve a list of hash values of the items in a given time_interal
     * 
     * @param c
     * @param timeinterval_id
     * @return
     * @throws SQLException
     */
    private List<String> hashValuesOfInterval(Context c, int timeinterval_id)
            throws SQLException {

        /** execute the sql command to get the TableRowIterator as a result */
        String query = "SELECT * FROM hashvalueofitem WHERE time_interval_id= ?";
        TableRowIterator tri = DatabaseManager.queryTable(c, "hashvalueofitem",
                query, timeinterval_id);

        /** hold all the hash values in the time_interval */
        List<String> hashvalues = new ArrayList<String>();
        while (tri.hasNext()) {
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
     * @param c
     * @param timeinterval_id
     * @param from
     * @param to
     * @return
     * @throws SQLException
     */
    public String witHash(Context c, int timeinterval_id, int from, int to)
            throws SQLException {

        List<String> hashvalues = hashValuesOfInterval(c, timeinterval_id)
                .subList(from, to);

        DigestFactory dF = new DigestFactory();

        return Utils.witHash(hashvalues, dF);
    }

}
