package org.dspace.workflow_new;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.authorize.AuthorizeException;
import org.dspace.eperson.Group;

import java.sql.SQLException;

/**
 * @author
 */
/*
 * Represents a workflow assignments database representation.
 * These assignments describe roles and the groups connected
 * to these roles for each collection
 */
public class WorkflowAssignment {
    /** Our context */
    private Context myContext;

    /** The row in the table representing this object */
    private TableRow myRow;

    /**
     * Construct an ResourcePolicy
     *
     * @param context
     *            the context this object exists in
     * @param row
     *            the corresponding row in the table
     */
    WorkflowAssignment(Context context, TableRow row)
    {
        myContext = context;
        myRow = row;
    }

    public static WorkflowAssignment find(Context context, int id)
            throws SQLException
    {
        TableRow row = DatabaseManager.find(context, "WorkflowAssignemnt", id);

        if (row == null)
        {
            return null;
        }
        else
        {
            return new WorkflowAssignment(context, row);
        }
    }

    public static WorkflowAssignment find(Context context, int collection, String role) throws SQLException {
         TableRowIterator tri = DatabaseManager.queryTable(context,"WorkflowAssignment",
                "SELECT * FROM WorkflowAssignment WHERE collection_id="+collection+" AND role_id= ? ", 
                role);

        TableRow row = null;
        if (tri.hasNext())
        {
            row = tri.next();
        }

        // close the TableRowIterator to free up resources
        tri.close();

        if (row == null)
        {
            return null;
        }
        else
        {
            return new WorkflowAssignment(context, row);
        }
    }

    public static WorkflowAssignment create(Context context) throws SQLException,
            AuthorizeException {

        TableRow row = null;
        try{
            row = DatabaseManager.create(context, "WorkflowAssignment");
        }catch(Exception e){
            e.printStackTrace();
        }

        return new WorkflowAssignment(context, row);
    }


    public void delete() throws SQLException
    {
        DatabaseManager.delete(myContext, myRow);
    }


    public void update() throws SQLException
    {
        DatabaseManager.update(myContext, myRow);
    }

    public void setRoleId(String id){
        myRow.setColumn("role_id",id);
    }

    public String getRoleId(){
        return myRow.getStringColumn("role_id");
    }

    public void setCollectionId(int id){
        myRow.setColumn("collection_id", id);
    }

    public int getCollectionId(){
        return myRow.getIntColumn("collection_id");
    }

    public void setGroupId(Group group){
        myRow.setColumn("group_id", group.getID());
    }

    public Group getGroup() throws SQLException {
        return Group.find(myContext, myRow.getIntColumn("group_id"));
    }

}
