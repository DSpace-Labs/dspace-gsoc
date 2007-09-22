/*
 * SearchConsumer.java
 *
 * Location: $URL$
 * 
 * Version: $Revision$
 * 
 * Date: $Date$
 *
 * Copyright (c) 2002-2007, Hewlett-Packard Company and Massachusetts
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

package org.dspace.search;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.uri.ObjectIdentifier;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;

/**
 * Class for updating search indices from content events.
 * 
 * @version $Revision$
 */
public class SearchConsumer implements Consumer
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(SearchConsumer.class);

    // collect Items, Collections, Communities newly created.
    private Set<DSpaceObject> objectsCreated = null;

    // collect Items, Collections, Communities that need reindexing
    private Set<DSpaceObject> objectsToUpdate = null;

    // handles to delete since IDs are not useful by now.
    private Set<ObjectIdentifier> objectsToDelete = null;

    public void initialize() throws Exception
    {
        // No-op

    }

    /**
     * Consume a content event -- just build the sets of objects to add (new) to
     * the index, update, and delete.
     * 
     * @param ctx
     *            DSpace context
     * @param event
     *            Content event
     */
    public void consume(Context ctx, Event event) throws Exception
    {

        if (objectsCreated == null)
        {
            objectsCreated = new HashSet<DSpaceObject>();
            objectsToUpdate = new HashSet<DSpaceObject>();
            objectsToDelete = new HashSet<ObjectIdentifier>();
        }

        int st = event.getSubjectType();
        if (!(st == Constants.ITEM || st == Constants.BUNDLE
                || st == Constants.COLLECTION || st == Constants.COMMUNITY))
        {
            log
                    .warn("SearchConsumer should not have been given this kind of Subject in an event, skipping: "
                            + event.toString());
            return;
        }
        DSpaceObject dso = event.getSubject(ctx);

        // If event subject is a Bundle and event was Add or Remove,
        // transform the event to be a Modify on the owning Item.
        // It could be a new bitstream in the TEXT bundle which
        // would change the index.
        int et = event.getEventType();
        if (st == Constants.BUNDLE)
        {
            if ((et == Event.ADD || et == Event.REMOVE) && dso != null
                    && ((Bundle) dso).getName().equals("TEXT"))
            {
                st = Constants.ITEM;
                et = Event.MODIFY;
                dso = ((Bundle) dso).getItems()[0];
                if (log.isDebugEnabled())
                    log.debug("Transforming Bundle event into MODIFY of Item "
//                            + dso.getHandle());
                            + dso.getID());
            }
            else
                return;
        }

        switch (et)
        {
        case Event.CREATE:
            if (dso == null)
                log.warn("CREATE event, could not get object for "
                        + event.getSubjectTypeAsString() + " id="
                        + String.valueOf(event.getSubjectID())
                        + ", perhaps it has been deleted.");
            else
                objectsCreated.add(dso);
            break;
        case Event.MODIFY:
        case Event.MODIFY_METADATA:
            if (dso == null)
                log.warn("MODIFY event, could not get object for "
                        + event.getSubjectTypeAsString() + " id="
                        + String.valueOf(event.getSubjectID())
                        + ", perhaps it has been deleted.");
            else
                objectsToUpdate.add(dso);
            break;
        case Event.DELETE:
            String detail = event.getDetail();
            if (detail == null)
                log.warn("got null detail on DELETE event, skipping it.");
            else
                objectsToDelete.add(ObjectIdentifier.fromString(detail));
            break;
        default:
            log
                    .warn("SearchConsumer should not have been given a event of type="
                            + event.getEventTypeAsString()
                            + " on subject="
                            + event.getSubjectTypeAsString());
            break;
        }
    }

    /**
     * Process sets of objects to add, update, and delete in index. Correct for
     * interactions between the sets -- e.g. objects which were deleted do not
     * need to be added or updated, new objects don't also need an update, etc.
     */
    public void end(Context ctx) throws Exception
    {
        // add new created items to index, unless they were deleted.
        for (DSpaceObject ic : objectsCreated)
        {
            if (ic.getType() != Constants.ITEM || ((Item) ic).isArchived())
            {
                // if handle is NOT in list of deleted objects, index it:
                ObjectIdentifier oid = ic.getIdentifier();
                if (oid != null && !objectsToDelete.contains(oid))
                {
                    try
                    {
                        DSIndexer.indexContent(ctx, ic);
                        if (log.isDebugEnabled())
                            log.debug("Indexed NEW "
                                    + Constants.typeText[ic.getType()]
                                    + ", id=" + String.valueOf(ic.getID())
                                    + ", oid=" + oid.getCanonicalForm());
                    }
                    catch (Exception e)
                    {
                        log.error("Failed while indexing new object: ", e);
                        objectsCreated = null;
                        objectsToUpdate = null;
                        objectsToDelete = null;
                    }
                }
            }
            // remove it from modified list since we just indexed it.
            objectsToUpdate.remove(ic);
        }

        // update the changed Items not deleted because they were on create list
        for (DSpaceObject iu : objectsToUpdate)
        {
            if (iu.getType() != Constants.ITEM || ((Item) iu).isArchived())
            {
                // if handle is NOT in list of deleted objects, index it:
                ObjectIdentifier oid = iu.getIdentifier();
                if (oid != null && !objectsToDelete.contains(oid))
                {
                    try
                    {
                        DSIndexer.reIndexContent(ctx, iu);
                        if (log.isDebugEnabled())
                            log.debug("RE-Indexed "
                                    + Constants.typeText[iu.getType()]
                                    + ", id=" + String.valueOf(iu.getID())
                                    + ", oid=" + oid.getCanonicalForm());
                    }
                    catch (Exception e)
                    {
                        log.error("Failed while RE-indexing object: ", e);
                        objectsCreated = null;
                        objectsToUpdate = null;
                        objectsToDelete = null;
                    }
                }
            }
        }

        for (ObjectIdentifier oid : objectsToDelete)
        {
            try
            {
                DSIndexer.unIndexContent(ctx, oid.getObject(ctx));
                if (log.isDebugEnabled())
                    log.debug("UN-Indexed Item, oid=" + oid.getCanonicalForm());
            }
            catch (Exception e)
            {
                log.error("Failed while UN-indexing object: " + oid.getCanonicalForm(), e);
            objectsCreated = new HashSet<DSpaceObject>();
            objectsToUpdate = new HashSet<DSpaceObject>();
            objectsToDelete = new HashSet<ObjectIdentifier>();
            }

        }

        // "free" the resources
        objectsCreated = null;
        objectsToUpdate = null;
        objectsToDelete = null;
    }

    public void finish(Context ctx) throws Exception
    {
        // No-op

    }

}
