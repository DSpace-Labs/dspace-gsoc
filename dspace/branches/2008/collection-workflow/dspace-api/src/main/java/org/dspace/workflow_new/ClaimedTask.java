package org.dspace.workflow_new;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRowIterator;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Bram De Schouwer
 */
/*
 * Claimed task representing the database representation of an action claimed by an eperson
 */
public class ClaimedTask {
     /** Our context */
    private Context myContext;

    /** The row in the table representing this object */
    private TableRow myRow;

    /**
     * Construct an Claimed Task
     *
     * @param context
     *            the context this object exists in
     * @param row
     *            the corresponding row in the table
     */
    ClaimedTask(Context context, TableRow row)
    {
        myContext = context;
        myRow = row;
    }

    public static ClaimedTask find(Context context, int id)
            throws SQLException {
        TableRow row = DatabaseManager.find(context, "taskowner", id);

        if (row == null)
        {
            return null;
        }
        else
        {
            return new ClaimedTask(context, row);
        }
    }

    public static List<ClaimedTask> findByEperson(Context context, int epersonID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(context,"taskowner",
                "SELECT * FROM taskowner WHERE owner_id= "+epersonID);
        List<ClaimedTask> list = new ArrayList<ClaimedTask>();
        while(tri.hasNext()) {
            TableRow row = tri.next();
            list.add(new ClaimedTask(context, row));
        }
        return list;
    }
    public static List<ClaimedTask> find(Context c, int epersonID, int wfiID, String stepID, String actionID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(c,"taskowner",
                "SELECT * FROM taskowner WHERE workflow_item_id="+wfiID+"AND owner_id= "+epersonID+" AND action_id= ? AND step_id= ?",actionID, stepID);
        List<ClaimedTask> list = new ArrayList<ClaimedTask>();
        while(tri.hasNext()) {
            TableRow row = tri.next();
            list.add(new ClaimedTask(c, row));
        }
        return list;
    }
    public static List<ClaimedTask> find(Context c, int wfiID, String stepID) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(c,"taskowner",
                "SELECT * FROM taskowner WHERE workflow_item_id="+wfiID+" AND step_id= ?", stepID);
        List<ClaimedTask> list = new ArrayList<ClaimedTask>();
        while(tri.hasNext()) {
            TableRow row = tri.next();
            list.add(new ClaimedTask(c, row));
        }
        return list;
    }

    public static List<ClaimedTask> find(Context c, WorkflowItem workflowItem) throws SQLException {
        TableRowIterator tri = DatabaseManager.queryTable(c,"taskowner",
                "SELECT * FROM taskowner WHERE workflow_item_id="+workflowItem.getID());
        List<ClaimedTask> list = new ArrayList<ClaimedTask>();
        while(tri.hasNext()) {
            TableRow row = tri.next();
            list.add(new ClaimedTask(c, row));
        }
        return list;
    }

    public static ClaimedTask create(Context context) throws SQLException {

        TableRow row = DatabaseManager.create(context, "taskowner");

        return new ClaimedTask(context, row);
    }


    public void delete() throws SQLException
    {
        DatabaseManager.delete(myContext, myRow);
    }


    public void update() throws SQLException
    {
        DatabaseManager.update(myContext, myRow);
    }

    public void setOwnerID(int ownerID){
        myRow.setColumn("owner_id", ownerID);
    }
    public int getOwnerID(){
        return myRow.getIntColumn("owner_id");
    }
    public void setWorkflowItemID(int workflowItemID){
        myRow.setColumn("workflow_item_id", workflowItemID);
    }
    public int getWorkflowItemID(){
        return myRow.getIntColumn("workflow_item_id");     
    }
    public void setActionID(String actionID){
        myRow.setColumn("action_id", actionID);
    }
    public String getActionID(){
        return myRow.getStringColumn("action_id");
    }
    public void setStepID(String stepID){
        myRow.setColumn("step_id", stepID);
    }
    public String getStepID(){
        return myRow.getStringColumn("step_id");
    }
}
