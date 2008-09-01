package org.dspace.app.xmlui.aspect.submission.workflow_new;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.workflow_new.*;
import org.dspace.app.xmlui.workflow_new.actions.Action;
import org.dspace.workflow_new.Step;
import org.dspace.workflow_new.Workflow;
import org.apache.avalon.framework.parameters.ParameterException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Bram De Schouwer
 */
public class WorkflowTransformer extends AbstractDSpaceTransformer {

   public void addBody(Body body) throws SAXException, WingException, SQLException, IOException, AuthorizeException {
      try {
            String stepID = parameters.getParameter("step_id");
            String actionID = parameters.getParameter("action_id");
            int workflowID = parameters.getParameterAsInteger("workflow_item_id");
            WorkflowItem wfi = WorkflowItem.find(context, workflowID);
            Workflow wf = WorkflowFactory.getWorkflow(context, wfi.getCollection().getID());
            Step step = wf.getStep(stepID);
            Action action;
            if(actionID == null || actionID.equals("")){
                action = (Action) step.getFirstAction();
            }else{
                action = (Action) step.getAction(actionID);
            }
            action.addBody(body, parameters, objectModel, workflowID);
        } catch (ParameterException e) {
            assert false;
        } catch (WorkflowConfigurationException e) {
            assert false;
        }
//        super.addBody(body);    //To change body of overridden methods use File | Settings | File Templates.
        
    }
}
