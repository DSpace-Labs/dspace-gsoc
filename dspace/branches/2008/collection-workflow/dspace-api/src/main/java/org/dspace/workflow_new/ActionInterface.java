package org.dspace.workflow_new;

import org.w3c.dom.Node;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.authorize.AuthorizeException;
import org.dspace.workflow_new.Step;

import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;
import java.sql.SQLException;
import java.io.IOException;


public interface ActionInterface extends WorkflowPartInterface{

    public void configure(Node node) throws WorkflowConfigurationException;

    public String getNodeId(Node node);

    public ActionInterface execute(Context c, WorkflowItem wi, EPerson e, HttpServletRequest request) throws SQLException, IOException, MessagingException, WorkflowException, AuthorizeException, WorkflowConfigurationException;

    public void activate(Context c, WorkflowItem wi, EPerson e) throws SQLException;

    public Step getParentStep();

    public void endActionAndClaimNext(WorkflowPartInterface next, Context c, EPerson e, WorkflowItem wi) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException ;

    public String getName();
}
