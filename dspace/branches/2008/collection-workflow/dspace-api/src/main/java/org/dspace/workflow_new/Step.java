package org.dspace.workflow_new;

import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.I18nUtil;
import org.dspace.workflow_new.Actions.Action;
import org.dspace.authorize.AuthorizeException;
import org.w3c.dom.Node;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.SQLException;

/**
 * @author: Bram De Schouwer
 */
public class Step extends WorkflowPart{

    /*
     * Group of epersons which can perform the action(s) contained by this step
     */

    private ArrayList<Action> actions;

    private Workflow workflow;

    private String emailTemplate;

    private HashMap<Integer,String> email;

    private String firstAction;

    private String roleId;

    private String name;

    /*
     * Constructor
     */
    public Step(Workflow workflow, Node node) throws WorkflowConfigurationException {
        super(node.getAttributes().getNamedItem("id").getFirstChild().getNodeValue());
        this.workflow = workflow;
        if(!this.id.equals("EndWorkflow")){
            this.emailTemplate = node.getAttributes().getNamedItem("emailTemplate").getFirstChild().getNodeValue();
            this.firstAction = node.getAttributes().getNamedItem("firstAction").getFirstChild().getNodeValue();
            this.roleId = node.getAttributes().getNamedItem("role_id").getFirstChild().getNodeValue();
            this.name = node.getAttributes().getNamedItem("name").getFirstChild().getNodeValue();
            if(firstAction==null||roleId==null||name==null){
                throw new WorkflowConfigurationException("WorkflowConfigurationException: not enough parameters specified for step:"+this.getId());
            }
        }
    }
    /*
     * Creates the actoins in this step
     */
    public void createActions() throws WorkflowConfigurationException {
        WorkflowFactory.createActions(workflow, this);
    }
    /*
     * Returns an action with a given id
     */
    public Action getAction(String id) throws WorkflowConfigurationException {
        if(actions==null){
            actions = new ArrayList<Action>();
            createActions();
        }
        for(Action action: actions){
            if(action.getId().equals(id)){
                return action;
            }
        }
        return null;
    }
    /*
     * Activates this step, sends a notification email in case an email template exists for this step 
     */
    public void activate(Context c, WorkflowItem wi, EPerson e) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException {
       if(this.id.equals("EndWorkflow")){
            WorkflowManager.archive(c,wi);

        }else{
            Action action = getFirstAction();
            if(action.canAdvance(c, wi)){
                WorkflowManager.createPoolTasks(c,wi,WorkflowManager.getRoleGroup(c,wi.getCollection().getID(), roleId),this);
                action.activate(c,wi,e);
                if(emailTemplate!=null){
                    Group group = WorkflowManager.getRoleGroup(c,wi.getCollection().getID(), roleId);
                    EPerson[] epa = group.getMembers();
                    WorkflowManager.emailRecipients(epa,createEmail(c, wi));
                }
            }
        }
    }
    /*
     * Returns the first action of this step
     */
    public Action getFirstAction() throws SQLException, WorkflowConfigurationException {
        if(actions==null){
            actions = new ArrayList<Action>();
            createActions();
        }
        return getAction(firstAction);
    }
    /*
     * Returns the parenting workflow system for this step
     */
    public Workflow getWorkflow(){
        return this.workflow;
    }
    /*
     * Adds an action to this step
     */
    public void addAction(Action act) {
        actions.add(act);
    }
    /*
     * Creates an email to notify the eperons responsible for the execution of this step
     * about the creation of pool tasks for this step
     */
    public Email createEmail(Context c, WorkflowItem wi) throws IOException, SQLException {
        Email mail = ConfigurationManager.getEmail(I18nUtil.getEmailFilename(c.getCurrentLocale(),emailTemplate));
        mail.addArgument(wi.getItem().getName());
        mail.addArgument(wi.getCollection().getName());
        mail.addArgument(wi.getSubmitter().getFullName());
        //TODO: message
        mail.addArgument("New task available.");
        mail.addArgument(WorkflowManager.getMyDSpaceLink());
        return mail;
    }
    /*
     * Returns the name of this step
     */
    public String getName(){
        return this.name;
    }
}
