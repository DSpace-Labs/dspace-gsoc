package org.dspace.workflow_new;

import org.w3c.dom.Node;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.authorize.AuthorizeException;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.sql.SQLException;
import java.io.IOException;

/**
 * @author Bram De Schouwer
 */

public class Workflow extends WorkflowPart{

    private HashMap<String, Step> steps;

    private String startStep;

    /*
     * Constructor
     */
    public Workflow(String id, Node node) throws WorkflowConfigurationException {
        super(id);
        this.steps = new HashMap<String, Step>();
        String firstStepId = node.getAttributes().getNamedItem("start").getFirstChild().getNodeValue();
        this.startStep = firstStepId;
        this.steps.put(firstStepId, new Step(this, WorkflowFactory.getStepNode(this.getId(), firstStepId)));
    }
    /*
     * Activate the first step of this workflow system
     */
    public void activate(Context c, WorkflowItem wi, EPerson e) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException {
        steps.get(startStep).activate(c, wi, e);
    }
    /*
     * Creates a step with a given id and includes it in the workflow system
     */
    public Step createStep(String id) throws WorkflowConfigurationException {
        if(steps.get(id)!=null){
            return steps.get(id);
        }else{
            Step step = new Step(this, WorkflowFactory.getStepNode(this.getId(), id));
            steps.put(id,step);
            return step;
        }
    }
    /*
     * Return a step with a given id
     */
    public Step getStep(String id) throws WorkflowConfigurationException {
        if(steps.get(id)!=null){
            return steps.get(id);
        }else{
            return createStep(id);
        }
    }
}
