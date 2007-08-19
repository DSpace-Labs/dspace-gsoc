package org.dspace.cis;

import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;

/**
 * This class presents a hash value of an item.
 * @author Wang Jiahui
 *
 */
public class HashvalueofItem
{

    /**
     * Hash value's id.
     */
    private int hashvalue_id;

    /**
     * The time-interval's id.
     */
    private int time_interval_id;

    /**
     * The Item's id.
     */
    private int item_id;

    /**
     * The hashvlue.
     */
    private String hashValue;

    /**
     * The hash value's algorithm.
     */
    private String hash_Algorithm;

    /** Our context */
    private Context ourContext;

    /**
     * The constructor.
     * @param ourContext our context
     */
    public HashvalueofItem(Context ourContext)
    {
        this.ourContext = ourContext;
    }

    /**
     * Get the hash algorithm.
     * @return the hash algorithm
     */
    public String getHash_Algorithm()
    {
        return hash_Algorithm;
    }

    /**
     * Set the hash algorithm
     * @param hash_Algorithm the hash algorithm
     * @return this object
     */
    public HashvalueofItem setHash_Algorithm(String hash_Algorithm)
    {
        this.hash_Algorithm = hash_Algorithm;
        return this;
    }

    /**
     * Get the hash value.
     * @return the hash value
     */
    public String getHashValue()
    {
        return hashValue;
    }

    /**
     * Set the hash value.
     * @param hashValue the hash value
     * @return this object
     */
    public HashvalueofItem setHashValue(String hashValue)
    {
        this.hashValue = hashValue;
        return this;
    }

    /**
     * Get the hashvalue's id.
     * @return the hashvalue's id
     */
    public int getHashvalue_id()
    {
        return hashvalue_id;
    }

    /**
     * Get the item's id.
     * @return the item's id
     */
    public int getItem_id()
    {
        return item_id;
    }

    /**
     * Set the item's id.
     * @param item_id the item's id
     * @return the this object
     */
    public HashvalueofItem setItem_id(int item_id)
    {
        this.item_id = item_id;
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
     * Set the time interval's id
     * @param time_interval_id the time interval's id
     * @return this object
     */
    public HashvalueofItem setTime_interval_id(int time_interval_id)
    {
        this.time_interval_id = time_interval_id;
        return this;
    }

    /**
     * Get our context.
     * @return the context
     */
    public Context getOurContext()
    {
        return ourContext;
    }

    /**
     * Set the context.
     * @param ourContext the context
     * @return this object
     */
    public HashvalueofItem setOurContext(Context ourContext)
    {
        this.ourContext = ourContext;
        return this;
    }

    /**
     * write this hash value into the database
     * 
     * @throws SQLException SQLException in SQL process
     */
    public void archive() throws SQLException
    {
        TableRow tR = DatabaseManager.row("hashvalueofitem");
        tR.setColumn("time_interval_id", this.time_interval_id);
        tR.setColumn("item_id", this.item_id);
        tR.setColumn("hashvalue", this.hashValue);
        tR.setColumn("hash_algorithm", this.hash_Algorithm);

        DatabaseManager.insert(ourContext, tR);
    }

}
