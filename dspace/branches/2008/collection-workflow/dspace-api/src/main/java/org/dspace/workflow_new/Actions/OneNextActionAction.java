package org.dspace.workflow_new.Actions;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.dspace.workflow_new.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.authorize.AuthorizeException;

import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;
import java.sql.SQLException;
import java.io.IOException;

/**
 * @author Bram De Schouwer.
 */
/*
 * All actions with only one possible next action
 */
public abstract class OneNextActionAction extends Action {
    /*
     * The workflow part that follows this one
     */
    protected WorkflowPart next;

    public OneNextActionAction(Node node, Step step) throws WorkflowConfigurationException {
        super(node, step);
    }
    //TODO public ding bezien
    public void configure(Node node) throws WorkflowConfigurationException {
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            if(children.item(i).getNodeName().equals("next")){
                NamedNodeMap next = children.item(i).getAttributes();
                if(next.getNamedItem("type").getFirstChild().getNodeValue().equals("step")){
                    this.next = parentStep.getWorkflow().createStep(next.getNamedItem("id").getFirstChild().getNodeValue());
                    break;
                }else{
                    this.next = WorkflowFactory.createAction(parentStep.getWorkflow().getId(),next.getNamedItem("id").getFirstChild().getNodeValue(),parentStep);
                    break;
                }
            }
        }
    }

    public Action execute(Context c, WorkflowItem wi, EPerson e, HttpServletRequest request) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException, WorkflowException {
        endActionAndClaimNext(next,c,e,wi);
        if(next instanceof Action){
            return (Action) next;
        }
        return null;
    }
}
