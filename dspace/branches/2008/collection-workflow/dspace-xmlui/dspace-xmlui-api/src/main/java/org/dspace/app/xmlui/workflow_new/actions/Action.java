package org.dspace.app.xmlui.workflow_new.actions;

import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.workflow_new.Step;
import org.dspace.workflow_new.WorkflowPart;
import org.dspace.workflow_new.*;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.authorize.AuthorizeException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;
import org.w3c.dom.Node;

import javax.mail.MessagingException;
import java.sql.SQLException;
import java.io.IOException;
import java.util.Map;

/**
 * @author Bram De Schouwer
 */
public abstract class Action extends WorkflowPart implements ActionInterface{

    protected Step parentStep;

    protected String name;

    public Action(Node node, Step step) throws WorkflowConfigurationException {
        super(node.getAttributes().getNamedItem("id").getFirstChild().getNodeValue());
        parentStep = step;
        parentStep.addAction(this);
        this.name = node.getAttributes().getNamedItem("name").getFirstChild().getNodeValue();
        if(this.name==null){
            throw new WorkflowConfigurationException("WorkflowConfigurationException: no name specified in action:"+this.getId());
        }
        configure(node);
    }

    public String getNodeId(Node node){
        return node.getAttributes().getNamedItem("id").getFirstChild().getNodeValue();
    }

    public abstract void addBody(Body body, Parameters parameters, Map objectModel, int wfiID) throws WingException, AuthorizeException, IOException, SQLException, WorkflowConfigurationException, ParameterException;

    public void activate(Context c, WorkflowItem wi, EPerson e) throws SQLException {
        WorkflowManager.claim(c,wi,parentStep,this,e);
    }
    public Step getParentStep() {
        return parentStep;
    }
    public void endActionAndClaimNext(WorkflowPartInterface next, Context c, EPerson e, WorkflowItem wi) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException {
        WorkflowManager.deleteActionClaim(c,wi,parentStep,this,e);
        next.activate(c,wi,e);
    }

    public String getName() {
        return this.name;
    }
}
