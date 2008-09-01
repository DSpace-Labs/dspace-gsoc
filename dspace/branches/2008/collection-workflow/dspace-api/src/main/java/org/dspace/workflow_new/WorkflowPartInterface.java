package org.dspace.workflow_new;

import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.authorize.AuthorizeException;

import javax.mail.MessagingException;
import java.sql.SQLException;
import java.io.IOException;


public interface WorkflowPartInterface {

    public boolean canAdvance(Context c, WorkflowItem wi) throws SQLException;

    public void activate(Context c, WorkflowItem wi, EPerson e) throws SQLException, IOException, MessagingException, AuthorizeException, WorkflowConfigurationException;
    
    public String getId();
}
