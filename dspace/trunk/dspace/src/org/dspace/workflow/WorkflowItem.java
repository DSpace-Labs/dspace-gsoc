/*
 * WorkflowItem.java
 *
 * Version: $Revision$
 *
 * Date: $Date$
 *
 * Copyright (c) 2001, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.dspace.workflow;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.InProgressSubmission;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;


/**
 * Class representing an item going through the workflow process in DSpace
 *
 * @author   Robert Tansley
 * @version  $Revision$
 */
public class WorkflowItem implements InProgressSubmission
{
    /** log4j category */
    private static Logger log = Logger.getLogger(WorkflowItem.class);

    /** The item this workflow object pertains to */
    private Item item;
    
    /** Our context */
    private Context ourContext;

    /** The table row corresponding to this workflow item */
    private TableRow wfRow;

    /** The collection the item is being submitted to */
    private Collection collection;

    /** EPerson owning the current state */
    private EPerson owner;


    /**
     * Construct a workspace item corresponding to the given database row
     *
     * @param context  the context this object exists in
     * @param row      the database row
     */
    WorkflowItem(Context context, TableRow row)
        throws SQLException
    {
        ourContext = context;
        wfRow = row;

        item = Item.find(context, wfRow.getIntColumn("item_id"));
        collection = Collection.find(context,
            wfRow.getIntColumn("collection_id"));
        
        if( wfRow.isColumnNull( "owner" ) )
            owner = null;
        else
            owner = EPerson.find(context, wfRow.getIntColumn("owner"));
    }


    /**
     * Get a workflow item from the database.  The item, collection and
     * submitter are loaded into memory.
     *
     * @param  context  DSpace context object
     * @param  id       ID of the workspace item
     *   
     * @return  the workflow item, or null if the ID is invalid.
     */
    public static WorkflowItem find(Context context, int id)
        throws SQLException
    {
        TableRow row = DatabaseManager.find(context,
            "workflowitem",
            id);

        if (row == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context,
                    "find_workflow_item",
                    "not_found,workflow_item_id=" + id));
            }

            return null;
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context,
                    "find_workflow_item",
                    "workflow_item_id=" + id));
            }

            return new WorkflowItem(context, row);
        }
    }


    /**
     * Get the internal ID of this workflow item
     *
     * @return the internal identifier
     */
    public int getID()
    {
        return wfRow.getIntColumn("workflow_id");
    }


    public EPerson getOwner()
    {        
        return owner;
    }

    
    public void setOwner( EPerson ep )
    {
        owner = ep;
        
        if( ep == null )
            wfRow.setColumnNull( "owner" );
        else
            wfRow.setColumn( "owner", ep.getID() );
    }

    
    public int getState()
    {
        return wfRow.getIntColumn( "state" );
    }

    
    public void setState( int newstate )
    {
        wfRow.setColumn( "state", newstate );
    }



    /**
     * Update the workflow item, including the unarchived item.
     */
    public void update()
        throws SQLException, AuthorizeException
    {
        // FIXME check auth
    
        log.info(LogManager.getHeader(ourContext,
            "update_workflow_item",
            "workflow_item_id=" + getID()));

        // Update the item
        item.update();
        
        // Update ourselves
        DatabaseManager.update(ourContext, wfRow);
    }

    /**
     * Delete workflow
     *
     * @param context Context object
     */
    public void delete(Context context)
        throws SQLException
    {
        // FIXME - auth?
        DatabaseManager.delete(context, wfRow);
    }


    // InProgressSubmission methods

    public Item getItem()
    {
        return item;
    }
    

    public Collection getCollection()
    {
        return collection;
    }

    
    public EPerson getSubmitter()
    {
        return item.getSubmitter();
    }
    

    public boolean hasMultipleFiles()
    {
        return wfRow.getBooleanColumn("multiple_files");
    }

    
    public void setMultipleFiles(boolean b)
    {
        wfRow.setColumn("multiple_files", b);
    }
    

    public boolean hasMultipleTitles()
    {
        return wfRow.getBooleanColumn("multiple_titles");
    }
    

    public void setMultipleTitles(boolean b)
    {
        wfRow.setColumn("multiple_titles", b);
    }


    public boolean isPublishedBefore()
    {
        return wfRow.getBooleanColumn("published_before");
    }

    
    public void setPublishedBefore(boolean b)
    {
        wfRow.setColumn("published_before", b);
    }
}
