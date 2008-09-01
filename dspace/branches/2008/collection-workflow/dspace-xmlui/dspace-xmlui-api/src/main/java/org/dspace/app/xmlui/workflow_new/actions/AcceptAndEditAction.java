package org.dspace.app.xmlui.workflow_new.actions;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.dspace.workflow_new.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.workflow_new.Step;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;

import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;
import java.sql.SQLException;
import java.io.IOException;
import java.util.Map;

/**
 * @author Bram De Schouwer
 */
/*
 * Action to accept and eventually edit a submission
 */
public class AcceptAndEditAction extends Action{


    public WorkflowPartInterface accept;
    public WorkflowPartInterface refuse;

    public AcceptAndEditAction(Node node, Step step) throws WorkflowConfigurationException {
        super(node, step);
    }

    public void configure(Node node) throws WorkflowConfigurationException {
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            if(children.item(i).getNodeName().equals("next-accept")){
                if(children.item(i).getAttributes().getNamedItem("type").getFirstChild().getNodeValue().equals("step")){
                    accept = parentStep.getWorkflow().createStep(children.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeValue());
                }
                else{
                    accept = parentStep.getAction(children.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeValue());
                }
            }else if(children.item(i).getNodeName().equals("next-decline")){
                if(children.item(i).getAttributes().getNamedItem("type").getFirstChild().getNodeValue().equals("step")){
                    refuse = parentStep.getWorkflow().createStep(children.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeValue());
                }
                else{
                    accept = parentStep.getAction(children.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeValue());
                }
            }
        }
    }
    public Action execute(Context c, WorkflowItem wi, EPerson e, HttpServletRequest request) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException {
        if(Util.getSubmitButton(request, "default").equals("submit_edit")){
        }else if(Util.getSubmitButton(request, "default").equals("submit_accept")){
            endActionAndClaimNext(accept,c,e,wi);
        }else if(Util.getSubmitButton(request, "default").equals("submit_decline")){
            endActionAndClaimNext(refuse,c,e,wi);
        }
        return null;
    }

    public void addBody(Body body, Parameters parameters, Map objectModel, int wfiID) throws WingException, AuthorizeException, IOException, SQLException, WorkflowConfigurationException, ParameterException {
        Context c = ContextUtil.obtainContext(objectModel);
        Request request = ObjectModelHelper.getRequest(objectModel);
        String contextPath = request.getContextPath();
        WebContinuation knot = FlowHelper.getWebContinuation(objectModel);
        boolean showfull = parameters.getParameterAsBoolean("showfull");

        WorkflowItem item = WorkflowItem.find(c, wfiID);
        Division main = body.addDivision("mainDiv");
        Division itemDiv = main.addInteractiveDivision("itemDiv", contextPath+"/handle/"+item.getCollection().getHandle()+"/workflow",Division.METHOD_POST);
        ReferenceSet referenceSet;
        if(showfull){
            referenceSet = itemDiv.addReferenceSet("narf",ReferenceSet.TYPE_DETAIL_VIEW);
	        itemDiv.addPara().addButton("submit_simple_item_info").setValue("Show simple item info");
            referenceSet.addReference(item.getItem());
        }else{
            referenceSet = itemDiv.addReferenceSet("narf", ReferenceSet.TYPE_SUMMARY_VIEW);
	        itemDiv.addPara().addButton("submit_full_item_info").setValue("Show full item info");
            referenceSet.addReference(item.getItem());
        }
        itemDiv.addHidden("action_id").setValue(this.getId());
        itemDiv.addHidden("step_id").setValue(parentStep.getId());
        itemDiv.addHidden("workflow_item_id").setValue(wfiID);
        itemDiv.addHidden("submission-continue").setValue(knot.getId());
        Table table0 = itemDiv.addTable("table0", 2, 2);
        Row row0 = table0.addRow();
        row0.addCell().addButton("submit_accept").setValue("Accept Item");
        row0.addCell().addButton("submit_refuse").setValue("Refuse Item");
        Row row1 = table0.addRow();
        row1.addCell().addButton("submit_edit").setValue("Edit Item");
    }
}
