package org.dspace.workflow_new;

import org.dspace.core.*;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.content.*;
import org.dspace.content.Collection;
import org.dspace.authorize.AuthorizeException;
import org.dspace.handle.HandleManager;
import org.dspace.workflow_new.Step;
import org.dspace.workflow_new.Workflow;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;
import java.util.*;
import java.sql.SQLException;
import java.io.IOException;
/**
 * @author Bram De Schouwer
 */
public class WorkflowManager {

    private static Logger log = Logger.getLogger(WorkflowManager.class);

    /*
     * Returns the current claimed tasks for given eperson
     */
    public static List<ClaimedTask> getOwnedTasks(Context c, EPerson e) throws SQLException {
          List<ClaimedTask> tasks = ClaimedTask.findByEperson(c,e.getID());
          return tasks;
      }
    /*
     * Returns list of taskpools for a given eperson
     */
    public static List<PoolTask> getPooledTasks(Context c, EPerson e) throws SQLException {
            List<PoolTask> mylist = PoolTask.findByEperson(c,e.getID());

        return mylist;
    }

    /*
     * Creates a task pool for a given step
     */
    public static void createPoolTasks(Context context, WorkflowItem wi, Group group, Step step)
             throws SQLException {
        EPerson[] epa = group.getMembers();
        // create a tasklist entry for each eperson
        for (int i = 0; i < epa.length; i++)
        {
            PoolTask task = PoolTask.create(context);
            task.setStepID(step.getId());
            task.setWorkflowID(wi.getID());
            task.setEpersonID(epa[i].getID());
            task.update();
        }
    }

    // deletes all pool tasks associated with a workflowitem
    public static void deletePoolTasks(Context c, WorkflowItem wi, Step step) throws SQLException
    {
        String myrequest = "DELETE FROM TaskListItem WHERE workflow_id= ? AND step_id= ?";

        DatabaseManager.updateQuery(c, myrequest, wi.getID(), step.getId());
    }
    /*
     * Claims an action for a given eperson
     */
    public static void claim(Context c, WorkflowItem wi, Step step, ActionInterface action, EPerson e) throws SQLException {
        ClaimedTask task = ClaimedTask.create(c);
        task.setWorkflowItemID(wi.getID());
        task.setStepID(step.getId());
        task.setActionID(action.getId());
        task.setOwnerID(e.getID());
        task.update();
    }
    /*
     * Deletes the claim for a given eperson
     */
    public static void deleteActionClaim(Context c, WorkflowItem wi, Step step, ActionInterface action, EPerson e) throws SQLException {
        List<ClaimedTask> list = ClaimedTask.find(c,e.getID(),wi.getID(),step.getId(),action.getId());
        for(ClaimedTask task: list){
            task.delete();
        }
    }
    /*
     * Executes an action and returns the next.
     */
    public static String doState(Context c, HttpServletRequest request) throws SQLException, AuthorizeException, IOException, MessagingException, WorkflowConfigurationException, WorkflowException {
        try{WorkflowItem wi = WorkflowItem.find(c, Integer.valueOf(request.getParameter("workflow_item_id")));
            Workflow wf = WorkflowFactory.getWorkflow(c, wi.getCollection().getID());
            Step step = wf.getStep(request.getParameter("step_id"));
            ActionInterface currentAction = step.getAction(request.getParameter("action_id"));
            ActionInterface next = currentAction.execute(c,wi,c.getCurrentUser(),request);
            if(next!=null){
                return next.getId();
            }else{
                return null;
            }
        }catch(WorkflowException e){
            UIUtil.sendAlert(request, e);
            throw e;
        }catch(WorkflowConfigurationException e){
            UIUtil.sendAlert(request, e);
            throw e;
        }
    }

    /*
     * Emails a group of epersons and sends them the given emails
     */
    public static void emailRecipients(EPerson[] epa, Email email)
            throws SQLException, MessagingException {
        for (int i = 0; i < epa.length; i++)
        {
            email.addRecipient(epa[i].getEmail());
        }

        email.send();
    }

    /*
     * Starts the workflow for a workspace item and ends the submission phase for that item
     */
    public static void start(Context context, WorkspaceItem wsi) throws SQLException, AuthorizeException, IOException, WorkflowConfigurationException, MessagingException {
        Item myitem = wsi.getItem();
        Collection collection = wsi.getCollection();
        Workflow wf = WorkflowFactory.getWorkflow(context, collection.getID());
        TableRow row = DatabaseManager.create(context, "workflowitem");
        row.setColumn("item_id", myitem.getID());
        row.setColumn("collection_id", wsi.getCollection().getID());
        WorkflowItem wfi = new WorkflowItem(context, row);
        wfi.setMultipleFiles(wsi.hasMultipleFiles());
        wfi.setMultipleTitles(wsi.hasMultipleTitles());
        wfi.setPublishedBefore(wsi.isPublishedBefore());
        try{
            wf.activate(context, wfi, null);
        }catch(WorkflowConfigurationException e){
            wfi.deleteWrapper();
            throw e;
        }

        // remove the WorkspaceItem
        wsi.deleteWrapper();
    }
    /*
     * Creates a role for a collection by linking a group of epersons to a role ID
     */
    public static void createWorkflowRole(Context context, int collectionId, String roleId, Group group) throws AuthorizeException, SQLException {
        WorkflowAssignment ass = WorkflowAssignment.create(context);
        ass.setCollectionId(collectionId);
        ass.setRoleId(roleId);
        ass.setGroupId(group);
        ass.update();        
    }
    /*
     * Returns the group of epersons linked to a role ID
     */
    public static Group getRoleGroup(Context c, int id, String roleId) throws SQLException {
        WorkflowAssignment wa = WorkflowAssignment.find(c,id,roleId);
        if(wa != null){
            return wa.getGroup();
        }
        return null;
    }
    /*
     * Deletes a role group linked to a given role and a collection
     */
    public static void deleteRoleGroup(Context context, int collectionID, String roleID) throws SQLException {
        WorkflowAssignment ass = WorkflowAssignment.find(context,collectionID,roleID);
        ass.delete();
    }
    /*
     * Deletes an eperson from the taskpool of a step
     */
    public static void deleteFromPool(Context c, WorkflowItem wi, EPerson e, Step step) throws SQLException {
        List<PoolTask> list = PoolTask.find(c,e.getID(),wi.getID(),step.getId());
        for(PoolTask task: list){
            task.delete();
        }
    }
    /*
     * Returns the existance of a pooltask for a given eperson
     */
    public static boolean poolTaskExists(Context c, WorkflowItem wi, EPerson e, Step parentStep) throws SQLException {
        List<PoolTask> list = PoolTask.find(c,e.getID(), wi.getID(), parentStep.getId());
        if(list.size()>0){
            return true;
        }
        return false;
    }
    /*
     * Returns the existance of an action for a given eperson
     */
    public static boolean tasksExist(Context c, WorkflowItem wi, EPerson e, Step step, ActionInterface action) throws SQLException {
        List<ClaimedTask> list = ClaimedTask.findByEperson(c,e.getID());
        for(ClaimedTask task: list){
            if(task.getStepID().equals(step.getId())&&task.getWorkflowItemID()==wi.getID()&&action.getId().equals(task.getActionID())){
                return true;
            }
        }
        return false; 
    }
    /*
     * Deletes all outstanding tasks for a given step
     */
    public static void deleteTasks(Context c, WorkflowItem wi, Step step) throws SQLException {
        deletePoolTasks(c,wi,step);
        List<ClaimedTask> tasks = ClaimedTask.find(c,wi.getID(),step.getId());
        for(ClaimedTask task: tasks){
            task.delete();
        }
    }

    /**
     * Commit the contained item to the main archive. The item is associated
     * with the relevant collection, added to the search index, and any other
     * tasks such as assigning dates are performed.
     *
     * @return the fully archived item.
     */
    public static Item archive(Context c, WorkflowItem wfi)
            throws SQLException, IOException, AuthorizeException
    {
        // FIXME: Check auth
        Item item = wfi.getItem();
        Collection collection = wfi.getCollection();

        //Notify
        notifyOfArchive(c,item,collection);

        log.info(LogManager.getHeader(c, "archive_item", "workflow_item_id="
                + wfi.getID() + "item_id=" + item.getID() + "collection_id="
                + collection.getID()));

        InstallItem.installItem(c, wfi);

        // Log the event
        log.info(LogManager.getHeader(c, "install_item", "workflow_id="
                + wfi.getID() + ", item_id=" + item.getID() + "handle=FIXME"));

        return item;
    }

    /**
     * notify the submitter that the item is archived
     */
    private static void notifyOfArchive(Context c, Item i, Collection coll)
            throws SQLException, IOException
    {
        try
        {
            // Get submitter
            EPerson ep = i.getSubmitter();
            // Get the Locale
            Locale supportedLocale = I18nUtil.getEPersonLocale(ep);
            Email email = ConfigurationManager.getEmail(I18nUtil.getEmailFilename(supportedLocale, "submit_archive"));

            // Get the item handle to email to user
            String handle = HandleManager.findHandle(c, i);

            // Get title
            DCValue[] titles = i.getDC("title", null, Item.ANY);
            String title = "";
            try
            {
                title = I18nUtil.getMessage("org.dspace.workflow.WorkflowManager.untitled");
            }
            catch (MissingResourceException e)
            {
                title = "Untitled";
            }
            if (titles.length > 0)
            {
                title = titles[0].value;
            }

            email.addRecipient(ep.getEmail());
            email.addArgument(title);
            email.addArgument(coll.getMetadata("name"));
            email.addArgument(HandleManager.getCanonicalForm(handle));

            email.send();
        }
        catch (MessagingException e)
        {
            log.warn(LogManager.getHeader(c, "notifyOfArchive",
                    "cannot email user" + " item_id=" + i.getID()));
        }
    }
    public static String getMyDSpaceLink()
    {
        return ConfigurationManager.getProperty("dspace.url") + "/mydspace";
    }
}
