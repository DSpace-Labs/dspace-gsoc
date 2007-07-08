package org.dspace.cis;

import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;

public class HashvalueofItem
{

    private int hashvalue_id;

    private int time_interval_id;

    private int item_id;

    private String hashValue;

    private String hash_Algorithm;

    /** Our context */
    private Context ourContext;

    public HashvalueofItem(Context ourContext)
    {
        this.ourContext = ourContext;
    }

    public String getHash_Algorithm()
    {
        return hash_Algorithm;
    }

    public HashvalueofItem setHash_Algorithm(String hash_Algorithm)
    {
        this.hash_Algorithm = hash_Algorithm;
        return this;
    }

    public String getHashValue()
    {
        return hashValue;
    }

    public HashvalueofItem setHashValue(String hashValue)
    {
        this.hashValue = hashValue;
        return this;
    }

    public int getHashvalue_id()
    {
        return hashvalue_id;
    }

    // public HashvalueofItem setHashvalue_id(int hashvalue_id) {
    // this.hashvalue_id = hashvalue_id;
    // return this;
    // }

    public int getItem_id()
    {
        return item_id;
    }

    public HashvalueofItem setItem_id(int item_id)
    {
        this.item_id = item_id;
        return this;
    }

    public int getTime_interval_id()
    {
        return time_interval_id;
    }

    public HashvalueofItem setTime_interval_id(int time_interval_id)
    {
        this.time_interval_id = time_interval_id;
        return this;
    }

    public Context getOurContext()
    {
        return ourContext;
    }

    public HashvalueofItem setOurContext(Context ourContext)
    {
        this.ourContext = ourContext;
        return this;
    }

    /**
     * write this hash value into the database
     * 
     * @throws SQLException
     */
    public void archive() throws SQLException
    {
        // List<String> collums = new ArrayList<String>();
        // collums.add("hashvalue_id");
        // collums.add("time_interval_id");
        // collums.add("item_id");
        // collums.add("hashvalue");
        // collums.add("hash_algorithm");
        //      
        // TableRow tR = new TableRow("timeinterval", collums);
        TableRow tR = DatabaseManager.row("hashvalueofitem");
        // tR.setColumn("hashvalue_id", this.hashvalue_id);
        tR.setColumn("time_interval_id", this.time_interval_id);
        tR.setColumn("item_id", this.item_id);
        tR.setColumn("hashvalue", this.hashValue);
        tR.setColumn("hash_algorithm", this.hash_Algorithm);

        DatabaseManager.insert(ourContext, tR);
    }

}
