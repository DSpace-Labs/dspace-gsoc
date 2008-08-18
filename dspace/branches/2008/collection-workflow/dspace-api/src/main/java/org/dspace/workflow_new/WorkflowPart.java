package org.dspace.workflow_new;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.authorize.AuthorizeException;

import javax.mail.MessagingException;
import java.sql.SQLException;
import java.io.IOException;

/**
 * @author: Bram De Schouwer
 */
/*
 * Top class for all workflow parts (workflow, step, action)
 */
public abstract class WorkflowPart {

    /*
     * Id of the workflow part.
     */
    protected String id;

    public WorkflowPart(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    /*
     * Activates this workflow part
     */
    public abstract void activate(Context c, WorkflowItem wi, EPerson e) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException;

    /*
     * Checks the possibility to advance to this workflow part. Default is true.
     */
    public boolean canAdvance(Context c, WorkflowItem wi) throws SQLException {
        return true;
    }
}
