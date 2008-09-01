package org.dspace.app.xmlui.workflow_new.actions;

import org.dspace.app.xmlui.wing.element.*;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.workflow_new.Step;
import org.dspace.app.util.Util;
import org.dspace.workflow_new.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.authorize.AuthorizeException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.avalon.framework.parameters.Parameters;

import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;

/**
 * @author Bram De Schouwer
 */
/*
 * Represents the claim action executed at the beginning of each step
 */
public class ClaimAction extends OneNextActionAction {

    private int nbClaims;
    private String title;
    private String content;

    private ArrayList<String> requiredCompletedStepIDs;

    public ClaimAction(Node node, Step step) throws WorkflowConfigurationException {
        super(node, step);
    }

    public void activate(Context c, WorkflowItem wi, EPerson e){
        //Should not do anything at the moment
    }

    //TODO public bezien
    /*
     * Configures this action
     */
    public void configure(Node node) throws WorkflowConfigurationException {
        super.configure(node);
        requiredCompletedStepIDs = new ArrayList<String>();
        Node nbClaims = node.getAttributes().getNamedItem("nbClaims");
        if(nbClaims!=null){
            this.nbClaims = Integer.valueOf(nbClaims.getFirstChild().getNodeValue());
        }else{
            this.nbClaims = 1;
        }
        NodeList children = node.getChildNodes();
        Node requiredSteps = null;
        for(int i = 0; i < children.getLength(); i++){
            if(children.item(i).getNodeName().equals("requiredCompletedSteps")){
                requiredSteps = children.item(i);
            }else if(children.item(i).getNodeName().equals("description")){
                title = children.item(i).getAttributes().getNamedItem("title").getFirstChild().getNodeValue();
                NodeList description = children.item(i).getChildNodes();
                for(int j = 0; j < description.getLength(); j++){
                    if(description.item(j).getNodeName().equals("content")){
                        this.content = "";
                        for(int k = 0; k < description.item(j).getChildNodes().getLength(); k++){
                            if(description.item(j).getChildNodes().item(k).getNodeType() == Node.TEXT_NODE){
                                this.content += description.item(j).getChildNodes().item(k).getNodeValue();
                            }
                        }
                        break;
                    }
                }
            }
        }
        if(requiredSteps!=null){
            NodeList steps = requiredSteps.getChildNodes();
            for(int i = 0; i < steps.getLength(); i++){
                if(steps.item(i).getNodeName().equals("requiredCompletedStep")){
                    requiredCompletedStepIDs.add(steps.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeName());
                }
            }
        }
    }

    public Action execute(Context c, WorkflowItem wi, EPerson e, HttpServletRequest request) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException, WorkflowException {
        if(Util.getSubmitButton(request, "default").equals("submit_claim")){
            if(getNbClaims(c, wi)< nbClaims){
                next.activate(c, wi, e);
                WorkflowManager.deleteFromPool(c,wi,e, parentStep);
                return (Action) next;
            }else{
                throw new WorkflowException("WorkflowException: action:"+this.getId()+" this action is no longer available for execution");
            }
        }
        return null;
    }
    /*
     * If the required previous steps are completed: true
     */
    public boolean canAdvance(Context c, WorkflowItem wi) throws SQLException {
        if(requiredCompletedStepIDs.size()==0){
            return true;
        }else{
            return requiredStepsCompleted(c, wi);
        }
    }

    private boolean requiredStepsCompleted(Context c, WorkflowItem wi) throws SQLException {
        String steps = "";
        for(int i = 0; i < requiredCompletedStepIDs.size(); i++){
            if(i < requiredCompletedStepIDs.size()-1){
                steps += "tasklistitem.step_id= ? OR ";
            }else{
                steps += "tasklistitem.step_id= ? ";
            }
        }
        String myquery = "SELECT COUNT(*) FROM tasklistitem, taskowner" +
        		" WHERE tasklistitem.workflow_id= " + wi.getID() +" AND taskowner.workflow_item_id= "+ wi.getID()+
        		" AND "+steps+" AND tasklistitem.step_id=taskowner.step_id";

        TableRow tri = DatabaseManager.querySingle(c, myquery, requiredCompletedStepIDs.toArray());
        if(tri.getIntColumn("count")>0){
            return false;
        }
        return true;
    }


    private long getNbClaims(Context c, WorkflowItem wi) throws SQLException {
        String myQuery = "SELECT COUNT(*) FROM taskowner WHERE workflow_item_id= " + wi.getID() +
                " AND step_id= ?";
        return DatabaseManager.querySingle(c,myQuery,parentStep.getId()).getLongColumn("count");
    }

    public void addBody(Body body, Parameters parameters, Map objectModel, int wfiID) throws WingException, AuthorizeException, IOException, SQLException, WorkflowConfigurationException {

        Context c = ContextUtil.obtainContext(objectModel);
        Request request = ObjectModelHelper.getRequest(objectModel);
        String contextPath = request.getContextPath();
        WebContinuation knot = FlowHelper.getWebContinuation(objectModel);
        WorkflowItem item = WorkflowItem.find(c, wfiID);

        if(getNbClaims(c,item)<nbClaims){
            Division div = body.addDivision("mainDiv");
            Division claim = div.addInteractiveDivision("claim",contextPath+"/handle/"+item.getCollection().getHandle()+"/workflow", Division.METHOD_POST);
            claim.addHidden("submission-continue").setValue(knot.getId());
            Table table = claim.addTable("table",5,2);
            Row row1 = table.addRow();
            row1.addCell("header").addContent(title);
            Row row2 = table.addRow();
            row2.addCell().addContent(content);
            table.addRow();
            Row row3 = table.addRow();
            row3.addCell().addButton("submit_claim").setValue("Claim");
            row3.addCell().addButton("submit_return").setValue("Return to pool");
            table.addRow();
            claim.addHidden("action_id").setValue(this.getId());
            claim.addHidden("step_id").setValue(this.parentStep.getId());
            claim.addHidden("workflow_item_id").setValue(wfiID);

        }else{
            Division div = body.addDivision("error");
            Table table1 = div.addTable("table1",2,1);
            table1.addRow().addCell().addContent("This task is no longer available.");
            table1.addRow().addCell().addButton("button0").setValue("Return to submissions page");
            div.addHidden("action_id").setValue(this.getId());
            div.addHidden("step_id").setValue(this.parentStep.getId());
            div.addHidden("workflow_item_id").setValue(wfiID);

        }
    }
}
