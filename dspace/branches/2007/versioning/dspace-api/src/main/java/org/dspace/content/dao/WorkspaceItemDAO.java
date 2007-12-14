package org.dspace.content.dao;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.dao.CRUD;
import org.dspace.workflow.WorkflowItem;

public abstract class WorkspaceItemDAO extends ContentDAO<WorkspaceItemDAO>
        implements CRUD<WorkspaceItem>
{
    protected Logger log = Logger.getLogger(WorkspaceItemDAO.class);

    protected Context context;
    protected ItemDAO itemDAO;

    protected WorkspaceItemDAO childDAO;

    public WorkspaceItemDAO(Context context)
    {
        this.context = context;
    }

    public WorkspaceItemDAO getChild()
    {
        return childDAO;
    }

    public void setChild(WorkspaceItemDAO childDAO)
    {
        this.childDAO = childDAO;
    }

    public abstract WorkspaceItem create() throws AuthorizeException;

    /**
     * Create a new workspace item, with a new ID. An Item is also created. The
     * submitter is the current user in the context.
     */
    public abstract WorkspaceItem create(Collection collection,
            boolean template) throws AuthorizeException;

    /**
     * Create a WorkspaceItem from a WorkflowItem. This is for returning Items
     * to a user without submitting it to the archive.
     */
    public abstract WorkspaceItem create(WorkflowItem wfi)
        throws AuthorizeException;

    public abstract WorkspaceItem create(WorkspaceItem wsi, WorkflowItem wfi)
        throws AuthorizeException;

    public abstract WorkspaceItem retrieve(int id);

    public abstract WorkspaceItem retrieve(UUID uuid);

    /**
     * Create a new workspace item, with a new ID. This is FROM an existing Item.
     * This is to allow for new versions of Items to be tucked back into Workspaces.
     * 
     * @param item Item
     * @return WorkspaceItem The new workspace item.
     */
    public WorkspaceItem create(Item item)
    throws AuthorizeException
    {
		WorkspaceItem wsi = this.create(item.getOwningCollection(),false);
		return create(wsi, item, item.getOwningCollection());
		
    }

    /**
	 * Create a new workspace item, with a new ID. This is FROM an existing
	 * Item. This is to allow for new versions of Items to be tucked back into
	 * Workspaces.
	 * 
	 * @param item
	 *            Item
	 * @return WorkspaceItem The new workspace item.
	 */
    protected WorkspaceItem create(WorkspaceItem wsi, Item item, Collection collection)
        throws AuthorizeException
    {
        // Check the user has permission to ADD to the collection
        AuthorizeManager.authorizeAction(context, collection, Constants.ADD);

        EPerson currentUser = context.getCurrentUser();

        // Create an item
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);
        item.setSubmitter(currentUser);

        // Now create the policies for the submitter and workflow users to
        // modify item and contents (contents = bitstreams, bundles)
        // FIXME: hardcoded workflow steps
        Group stepGroups[] = {
            collection.getWorkflowGroup(1),
            collection.getWorkflowGroup(2),
            collection.getWorkflowGroup(3)
        };

        int actions[] = {
            Constants.READ,
            Constants.WRITE,
            Constants.ADD,
            Constants.REMOVE
        };

        // Give read, write, add, and remove privileges to the current user
        for (int action : actions)
        {
            AuthorizeManager.addPolicy(context, item, action, currentUser);
        }

        // Give read, write, add, and remove privileges to the various
        // workflow groups (if any).
        for (Group stepGroup : stepGroups)
        {
            if (stepGroup != null)
            {
                for (int action : actions)
                {
                    AuthorizeManager.addPolicy(context, item, action,
                            stepGroup);
                }
            }
        }

        itemDAO.update(item);

        wsi.setItem(item);
        wsi.setCollection(collection);
        update(wsi);

        log.info(LogManager.getHeader(context, "create_workspace_item",
                "workspace_item_id=" + wsi.getID() +
                "item_id=" + item.getID() +
                "collection_id=" + collection.getID()));

        return wsi;
    }

    public WorkspaceItem retrieve(int id)
    {
        return (WorkspaceItem) context.fromCache(WorkspaceItem.class, id);
    }

    public WorkspaceItem retrieve(UUID uuid)
    {
        return null;
    }

    /**
     * Update the workspace item, including the unarchived item.
     */
    public abstract void update(WorkspaceItem wsi) throws AuthorizeException;

    public abstract void delete(int id) throws AuthorizeException;

    /**
     * Delete the workspace item. The entry in workspaceitem, the unarchived
     * item and its contents are all removed (multiple inclusion
     * notwithstanding.)
     */
    public abstract void deleteAll(int id) throws AuthorizeException;

    public abstract List<WorkspaceItem> getWorkspaceItems();

    public abstract List<WorkspaceItem> getWorkspaceItems(EPerson eperson);

    public abstract List<WorkspaceItem> getWorkspaceItems(Collection collection);

    /**
     * FIXME: I don't like doing this, but it's the least filthy way I can
     * think of achieving what I want.
     */
    public abstract <T extends WorkspaceItem> void populate(T t);
}
