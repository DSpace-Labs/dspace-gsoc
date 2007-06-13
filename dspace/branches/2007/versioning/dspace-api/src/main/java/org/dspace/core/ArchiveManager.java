/*
 * ArchiveManager.java
 *
 * Version: $Revision: 1727 $
 *
 * Date: $Date: 2007-01-19 10:52:10 +0000 (Fri, 19 Jan 2007) $
 *
 * Copyright (c) 2002-2005, Hewlett-Packard Company and Massachusetts
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
package org.dspace.core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.browse.Browse;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Bundle;
import org.dspace.content.InstallItem;
import org.dspace.content.MetadataSchema;
import org.dspace.content.dao.BundleDAOFactory;
import org.dspace.content.dao.BundleDAO;
import org.dspace.content.dao.ItemDAO;
import org.dspace.content.dao.ItemDAOFactory;
import org.dspace.content.dao.CollectionDAO;
import org.dspace.content.dao.CollectionDAOFactory;
import org.dspace.content.dao.CommunityDAO;
import org.dspace.content.dao.CommunityDAOFactory;
import org.dspace.content.uri.ObjectIdentifier;
import org.dspace.content.uri.PersistentIdentifier;
import org.dspace.content.uri.dao.PersistentIdentifierDAO;
import org.dspace.content.uri.dao.PersistentIdentifierDAOFactory;
import org.dspace.eperson.EPerson;
import org.dspace.history.HistoryManager;
import org.dspace.search.DSIndexer;
import org.dspace.eperson.EPerson;

/**
 * This class could really do with a CLI...
 */
public class ArchiveManager
{
    private static Logger log = Logger.getLogger(ArchiveManager.class);

    public static DSpaceObject getObject(Context context,
            PersistentIdentifier identifier)
    {
        return getObject(context, identifier.getResourceID(),
                identifier.getResourceTypeID());
    }

    public static DSpaceObject getObject(Context context, int id, int type)
    {
        switch(type)
        {
            case (Constants.BITSTREAM):
                try
                {
                    return Bitstream.find(context, id);
                }
                catch (SQLException sqle)
                {
                    throw new RuntimeException(sqle);
                }
            case (Constants.BUNDLE):
                return BundleDAOFactory.getInstance(context).retrieve(id);
            case (Constants.ITEM):
                return ItemDAOFactory.getInstance(context).retrieve(id);
            case (Constants.COLLECTION):
                return CollectionDAOFactory.getInstance(context).retrieve(id);
            case (Constants.COMMUNITY):
                return CommunityDAOFactory.getInstance(context).retrieve(id);
            default:
                throw new RuntimeException("Not a valid DSpaceObject type");
        }
    }

    /**
     * Gets an Item by its OriginalItemID and Revision numbers
     */
    public static DSpaceObject getVersionedItem(Context context, int originalItemID, int revision)
    {

    	return ItemDAOFactory.getInstance(context).getByOriginalItemIDAndRevision(originalItemID, revision);
    }
    
    /**
     * Gets the HEAD of an OriginalItemID
     */
    public static DSpaceObject getHeadRevision(Context context, int originalItemID)
    {

    	return ItemDAOFactory.getInstance(context).getHeadRevision(originalItemID);
    }
    
    /**
     * Creates a Item in the database that maintains all the same
     * attributes and metadata as the Item it supplants with a new
     * revision number and a link to the given Item as the previousRevision
     *
     * @param item The Item to create a new version of
     */
    public static Item newVersionOfItem(Context context, Item originalItem)
    {
        try
        {
            ArchiveManager am = new ArchiveManager();
            ItemDAO itemDAO = ItemDAOFactory.getInstance(context);
            Item item = itemDAO.create();
            Item head = itemDAO.getHeadRevision(originalItem.getOriginalItemID());
            PersistentIdentifierDAO identifierDAO =
            PersistentIdentifierDAOFactory.getInstance(context);
            PersistentIdentifier identifier;

            // Persistent Identfier Stuff
            // Create persistent identifier. Note that this will create an
            // identifier of the default type (as specified in the
            // configuration).
            identifier = identifierDAO.create(item);
            String uri = identifier.getURI().toString();

            item.setArchived(originalItem.isArchived());
            item.setWithdrawn(originalItem.isWithdrawn());
            // Done by ItemDAO.update ... item.setLastModified();

            item.setOriginalItemID(originalItem.getOriginalItemID());

            item.setRevision(head.getRevision()+1);
            item.setPreviousItemID(head.getID());

            item.setOwningCollectionId(originalItem.getOwningCollection().getID());
            item.setSubmitter(originalItem.getSubmitter().getID());

            item.setMetadata(originalItem.getMetadata());
            // Add uri as identifier.uri DC value
            item.clearMetadata("dc", "identifier", "uri", null);
            item.addMetadata("dc", "identifier", "uri", null, uri);

            for (Bundle bundle : originalItem.getBundles())
            {
                item.addBundle(am.dupeBundle(context, bundle));
            }

//          create collection2item mapping
            originalItem.getOwningCollection().addItem(item);

            itemDAO.update(item);

            return item;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Withdraw the item from the archive. It is kept in place, and the content
     * and metadata are not deleted, but it is not publicly accessible.
     */
    public static void withdrawItem(Context context, Item item)
        throws AuthorizeException, IOException
    {
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);

        String timestamp = DCDate.getCurrent().toString();

        // Build some provenance data while we're at it.
        String collectionProv = "";
        Collection[] colls = item.getCollections();

        for (int i = 0; i < colls.length; i++)
        {
            collectionProv = collectionProv + colls[i].getMetadata("name")
                    + " (ID: " + colls[i].getID() + ")\n";
        }

        // Check permission. User either has to have REMOVE on owning collection
        // or be COLLECTION_EDITOR of owning collection
        if (!AuthorizeManager.authorizeActionBoolean(context,
                item.getOwningCollection(), Constants.COLLECTION_ADMIN)
                && !AuthorizeManager.authorizeActionBoolean(context,
                        item.getOwningCollection(), Constants.REMOVE))
        {
            throw new AuthorizeException("To withdraw item must be " +
                    "COLLECTION_ADMIN or have REMOVE authorization on " +
                    "owning Collection.");
        }

        item.setWithdrawn(true);
        item.setArchived(false);

        EPerson e = context.getCurrentUser();
        try
        {
            // Add suitable provenance - includes user, date, collections +
            // bitstream checksums
            String prov = "Item withdrawn by " + e.getFullName() + " ("
                    + e.getEmail() + ") on " + timestamp + "\n"
                    + "Item was in collections:\n" + collectionProv
                    + InstallItem.getBitstreamProvenanceMessage(item);

            item.addMetadata(MetadataSchema.DC_SCHEMA, "description", "provenance",
                    "en", prov);

            // Update item in DB
            itemDAO.update(item);

            // Invoke History system
            HistoryManager.saveHistory(context, item, HistoryManager.MODIFY, e,
                    context.getExtraLogInfo());

            // Remove from indicies
            Browse.itemRemoved(context, item.getID());
            DSIndexer.unIndexContent(context, item);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }

        // and all of our authorization policies
        // FIXME: not very "multiple-inclusion" friendly
        AuthorizeManager.removeAllPolicies(context, item);

        log.info(LogManager.getHeader(context, "withdraw_item", "user="
                + e.getEmail() + ",item_id=" + item.getID()));
    }

    /**
     * Reinstate a withdrawn item.
     */
    public static void reinstateItem(Context context, Item item)
        throws AuthorizeException, IOException
    {
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);

        String timestamp = DCDate.getCurrent().toString();

        // Check permission. User must have ADD on all collections.
        // Build some provenance data while we're at it.
        String collectionProv = "";
        Collection[] colls = item.getCollections();

        for (int i = 0; i < colls.length; i++)
        {
            collectionProv = collectionProv + colls[i].getMetadata("name")
                    + " (ID: " + colls[i].getID() + ")\n";
            AuthorizeManager.authorizeAction(context, colls[i],
                    Constants.ADD);
        }

        item.setWithdrawn(false);
        item.setArchived(true);

        // Add suitable provenance - includes user, date, collections +
        // bitstream checksums
        EPerson e = context.getCurrentUser();
        try
        {
            String prov = "Item reinstated by " + e.getFullName() + " ("
                    + e.getEmail() + ") on " + timestamp + "\n"
                    + "Item was in collections:\n" + collectionProv
                    + InstallItem.getBitstreamProvenanceMessage(item);

            item.addMetadata(MetadataSchema.DC_SCHEMA, "description", "provenance",
                    "en", prov);

            // Update item in DB
            itemDAO.update(item);

            // Invoke History system
            HistoryManager.saveHistory(context, item, HistoryManager.MODIFY, e,
                    context.getExtraLogInfo());

            // Add to indicies
            // Remove - update() already performs this
            // Browse.itemAdded(context, this);
            DSIndexer.indexContent(context, item);

            // authorization policies
            if (colls.length > 0)
            {
                // FIXME: not multiple inclusion friendly - just apply access
                // policies from first collection
                // remove the item's policies and replace them with
                // the defaults from the collection
                item.inheritCollectionDefaultPolicies(colls[0]);
            }
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }

        log.info(LogManager.getHeader(context, "reinstate_item", "user="
                + e.getEmail() + ",item_id=" + item.getID()));
    }

    /**
     * Call with a null source to add to the destination; call with a null
     * destination to remove from the source; used in this way, move() can act
     * as an alias for add() and delete().
     *
     * WARNING: Communities, Collection, and Items that are orphaned after
     * being removed from a container will be *deleted*. It may be better to
     * leave them in the system with a means for re-associating them with other
     * containers, but that doesn't really fit with the strict hierarchical
     * nature of DSpace containers. ie: if you delete a Community, you expect
     * everything beneath it to get deleted as well, not just to be marked as
     * being orphaned.
     *
     * WARNING 2: This needs to include some sanity checks to make sure we
     * don't end up with circular parent-child relationships.
     */
    public static void move(Context context,
            DSpaceObject dso, DSpaceObject source, DSpaceObject dest)
        throws AuthorizeException
    {
        assert((dso instanceof Item) ||
               (dso instanceof Collection) ||
               (dso instanceof Community));
        assert((source != null) || (dest != null));

        logMove(dso, source, dest);

        if (dso instanceof Item)
        {
            if (dest != null)
            {
                addItemToCollection(context,
                        (Item) dso, (Collection) dest);
            }
            else
            {
                removeItemFromCollection(context,
                        (Item) dso, (Collection) source);
            }
        }
        else if (dso instanceof Collection)
        {
            if (dest != null)
            {
                addCollectionToCommunity(context,
                        (Collection) dso, (Community) dest);
            }
            else
            {
                removeCollectionFromCommunity(context,
                        (Collection) dso, (Community) source);
            }
        }
        else if (dso instanceof Community)
        {
            if (dest != null)
            {
                addCommunityToCommunity(context,
                        (Community) dso, (Community) dest);
            }
            else
            {
                removeCommunityFromCommunity(context,
                        (Community) dso, (Community) source);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////

    /**
     * Add an item to the collection. This simply adds a relationship between
     * the item and the collection - it does nothing like set an issue date,
     * remove a personal workspace item etc. This has instant effect;
     * <code>update</code> need not be called.
     */
    private static void addItemToCollection(Context context,
            Item item, Collection collection)
        throws AuthorizeException
    {
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);
        CollectionDAO collectionDAO = CollectionDAOFactory.getInstance(context);

        collectionDAO.link(collection, item);
    }

    private static void removeItemFromCollection(Context context,
            Item item, Collection collection)
        throws AuthorizeException
    {
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);
        CollectionDAO collectionDAO = CollectionDAOFactory.getInstance(context);

        // Remove mapping
        collectionDAO.unlink(collection, item);

        if (collectionDAO.getParentCollections(item).size() == 0)
        {
            // make the right to remove the item explicit because the implicit
            // relation has been removed. This only has to concern the
            // currentUser because he started the removal process and he will
            // end it too. also add right to remove from the item to remove
            // it's bundles.
            try
            {
                AuthorizeManager.addPolicy(context, item, Constants.DELETE,
                        context.getCurrentUser());
                AuthorizeManager.addPolicy(context, item, Constants.REMOVE,
                        context.getCurrentUser());
            }
            catch (SQLException sqle)
            {
                throw new RuntimeException(sqle);
            }

            itemDAO.delete(item.getID());
        }
    }

    private static void addCollectionToCommunity(Context context,
            Collection child, Community parent)
        throws AuthorizeException
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);

        communityDAO.link((DSpaceObject) parent, (DSpaceObject) child);
    }

    private static void removeCollectionFromCommunity(Context context,
            Collection child, Community parent)
        throws AuthorizeException
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);
        CollectionDAO collectionDAO = CollectionDAOFactory.getInstance(context);

        communityDAO.unlink((DSpaceObject) parent, (DSpaceObject) child);

        if (communityDAO.getParentCommunities(child).size() == 0)
        {
            // make the right to remove the child explicit because the
            // implicit relation has been removed. This only has to concern the
            // currentUser because he started the removal process and he will
            // end it too. also add right to remove from the child to
            // remove it's items.
            try
            {
                AuthorizeManager.addPolicy(context, child, Constants.DELETE,
                        context.getCurrentUser());
                AuthorizeManager.addPolicy(context, child, Constants.REMOVE,
                        context.getCurrentUser());
            }
            catch (SQLException sqle)
            {
                throw new RuntimeException(sqle);
            }

            // Orphan; delete it
            collectionDAO.delete(child.getID());
        }
    }

    private static void addCommunityToCommunity(Context context,
            Community child, Community parent)
        throws AuthorizeException
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);

        communityDAO.link((DSpaceObject) parent, (DSpaceObject) child);
    }

    private static void removeCommunityFromCommunity(Context context,
            Community child, Community parent)
        throws AuthorizeException
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);

        communityDAO.unlink((DSpaceObject) parent, (DSpaceObject) child);

        if (communityDAO.getParentCommunities(child).size() == 0)
        {
            // make the right to remove the collection explicit because the
            // implicit relation has been removed. This only has to concern the
            // currentUser because he started the removal process and he will
            // end it too. also add right to remove from the collection to
            // remove it's items.
            try
            {
                AuthorizeManager.addPolicy(context, child, Constants.DELETE,
                        context.getCurrentUser());
                AuthorizeManager.addPolicy(context, child, Constants.REMOVE,
                        context.getCurrentUser());
            }
            catch (SQLException sqle)
            {
                throw new RuntimeException(sqle);
            }

            communityDAO.delete(child.getID());
        }
    }

    private static void logMove(DSpaceObject dso, DSpaceObject source,
            DSpaceObject dest)
    {
        String dsoStr = "";
        String sourceStr = "";
        String destStr = "";

        switch (dso.getType())
        {
            case Constants.ITEM:
                dsoStr = "Item ";
                break;
            case Constants.COLLECTION:
                dsoStr = "Collection ";
                break;
            case Constants.COMMUNITY:
                dsoStr = "Community ";
                break;
            default:
        }

        if (source != null)
        {
            switch (source.getType())
            {
                case Constants.ITEM:
                    sourceStr = "Item ";
                    break;
                case Constants.COLLECTION:
                    sourceStr = "Collection ";
                    break;
                case Constants.COMMUNITY:
                    sourceStr = "Community ";
                    break;
                default:
            }
        }

        if (dest != null)
        {
            switch (dest.getType())
            {
                case Constants.ITEM:
                    destStr = "Item ";
                    break;
                case Constants.COLLECTION:
                    destStr = "Collection ";
                    break;
                case Constants.COMMUNITY:
                    destStr = "Community ";
                    break;
                default:
            }
        }

        sourceStr = sourceStr + (source == null ? "null" : source.getID());
        destStr = destStr + (dest == null ? "null" : dest.getID());
        dsoStr = dsoStr + (dso == null ? "null" : dso.getID());

        log.warn("***************************************************");
        log.warn("Moving " + dsoStr + " from " + sourceStr + " to " + destStr);
        log.warn("***************************************************");
    }

    /**
     * CLI for Versioning
     * Should be extenisble for other actions.
     */
    public static void main(String[] argv)
    {
        Context c = null;
        try {
            c = new Context();
            CommandLineParser parser = new PosixParser();
            Options options = new Options();
            ArchiveManager am = new ArchiveManager();
            ItemDAO itemDAO = ItemDAOFactory.getInstance(c);

            options.addOption("a", "all", false, "print all items");
            options.addOption("m", "metadata", false, "print item metadata");
            options.addOption("r", "revision", false, "new revision of item");
            options.addOption("p", "print", false, "print item");
            options.addOption("u", "user", true, "eperson email address or id");
            options.addOption("i", "item_id", true, "id of the item");
            options.addOption("z", "identifiers", false, "print the presistent ids");
            CommandLine line = parser.parse(options, argv);



            if (line.hasOption("a"))
            {
                am.printItems(itemDAO.getItems());
            }
            else if (line.hasOption("m") && line.hasOption("i"))
            {
                am.printItemMetadata(itemDAO.retrieve(Integer.parseInt(line.getOptionValue("i"))));
            }
            else if (line.hasOption("p") && line.hasOption("i"))
            {
                int id = Integer.parseInt(line.getOptionValue("i"));
                System.out.println(itemDAO.retrieve(id).toString());
            }
            else if (line.hasOption("z") && line.hasOption("i"))
            {
                System.out.println("id go");
                am.printPersistentIdentifiers(itemDAO.retrieve(Integer.parseInt(line.getOptionValue("i"))));
            }
            else if (line.hasOption("r") && line.hasOption("i"))
            {
//            	 find the EPerson, assign to context
                EPerson myEPerson = null;
                String eperson = null;
                if (line.hasOption('u'))
                {
                    eperson = line.getOptionValue("u");
                }
                else
                {
                    System.out.println("Error, eperson cannot be found: " + eperson);
                    System.exit(1);
                }
                if (eperson.indexOf('@') != -1)
                {
                    // @ sign, must be an email
                    myEPerson = EPerson.findByEmail(c, eperson);
                }
                else
                {
                    myEPerson = EPerson.find(c, Integer.parseInt(eperson));
                }

                if (myEPerson == null)
                {
                    System.out.println("Error, eperson cannot be found: " + eperson);
                    System.exit(1);
                }

                c.setCurrentUser(myEPerson);

                int id = Integer.parseInt(line.getOptionValue("i"));
                Item i = ArchiveManager.newVersionOfItem(c, itemDAO.retrieve(id));
                System.out.println("Original Item: \n");
                System.out.println(itemDAO.retrieve(id).toString());
                System.out.println("New Item: \n");
                System.out.println(i.toString());
            }
            c.complete();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void printItems(List<Item> items)
    {
        for (Item i : items)
        {
            System.out.println(i.toString());
        }
    }

    private void printItemMetadata(Item item)
    {
        System.out.println(item.getMetadata().toString());
        for (Object o : item.getMetadata())
        {
            System.out.println(o.toString());
        }
    }

    private void printPersistentIdentifiers(Item item)
    {
        System.out.println("one pi: " + item.getPersistentIdentifier().getCanonicalForm());
        System.out.println(item.getPersistentIdentifiers().toString());
        for (PersistentIdentifier id : item.getPersistentIdentifiers())
        {
            System.out.println(id.getCanonicalForm());
        }
    }

    /**
     *  Takes in a bundle and makes a deep copy of it.
     *
     *  @param bundle
     */
    private Bundle dupeBundle (Context context, Bundle bundle)
    throws SQLException, AuthorizeException
    {
        BundleDAO bdao = BundleDAOFactory.getInstance(context);
        Bundle dupe = bdao.create();
        Bitstream[] bitstreams = null;
        int primary = bundle.getPrimaryBitstreamID();

        bitstreams = bundle.getBitstreams();
        for (Bitstream b : bitstreams)
        {
            dupe.addBitstream(b);
            if (primary == b.getID())
            {
                dupe.setPrimaryBitstreamID(b.getID());
            }
        }

        dupe.setName(bundle.getName());
        return dupe;
    }
}
