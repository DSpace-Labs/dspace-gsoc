/*
 * Bundle.java
 *
 * Version: $Revision$
 *
 * Date: $Date$
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
package org.dspace.content;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.content.dao.BundleDAO;        // Naughty!
import org.dspace.content.dao.BundleDAOFactory; // Naughty!
import org.dspace.content.dao.ItemDAO;          // Naughty!
import org.dspace.content.dao.ItemDAOFactory;   // Naughty!

/**
 * Class representing bundles of bitstreams stored in the DSpace system
 * <P>
 * The corresponding Bitstream objects are loaded into memory. At present,
 * there isn't reallyt any metadata associated with bundles - they are simple
 * containers.  Thus, the <code>update</code> method doesn't do much yet.
 * Creating, adding or removing bitstreams has instant effect in the database.
 *
 * @author James Rutherford
 * @version $Revision$
 */
public class Bundle extends DSpaceObject
{
    private static Logger log = Logger.getLogger(Bundle.class);

    private int id;
    private String name;
    private int primaryBitstreamId;
    private List<Bitstream> bitstreams;

    private Context context;
    private BundleDAO dao;
    private ItemDAO itemDAO;

    public Bundle(Context context)
    {
        this(context, -1);
    }

    public Bundle(Context context, int id)
    {
        this.id = id;
        this.context = context;
        this.dao = BundleDAOFactory.getInstance(context);
        this.itemDAO = ItemDAOFactory.getInstance(context);

        this.name = "";
        this.primaryBitstreamId = -1;
        this.bitstreams = new ArrayList<Bitstream>();
    }

    public int getID()
    {
        return id;
    }

    public void setID(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void unsetPrimaryBitstreamID()
    {
    	primaryBitstreamId = -1;
    }

    public int getPrimaryBitstreamID()
    {
        return primaryBitstreamId;
    }

    public void setPrimaryBitstreamID(int primaryBitstreamId)
    {
        this.primaryBitstreamId = primaryBitstreamId;
    }

    public Bitstream getBitstreamByName(String name)
    {
        Bitstream target = null;

        Iterator i = bitstreams.iterator();

        while (i.hasNext())
        {
            Bitstream b = (Bitstream) i.next();

            if (name.equals(b.getName()))
            {
                target = b;
                break;
            }
        }

        return target;
    }

    public Bitstream[] getBitstreams()
    {
        return (Bitstream[]) bitstreams.toArray(new Bitstream[0]);
    }

    public void setBitstreams(List<Bitstream> bitstreams)
    {
        this.bitstreams = bitstreams;
    }

    public Bitstream createBitstream(InputStream is) throws AuthorizeException,
            IOException, SQLException
    {
        AuthorizeManager.authorizeAction(context, this, Constants.ADD);

        Bitstream b = Bitstream.create(context, is);

        // FIXME: Set permissions for bitstream

        addBitstream(b);

        return b;
    }

    public Bitstream registerBitstream(int assetstore, String bitstreamPath)
        throws AuthorizeException, IOException, SQLException
    {
        AuthorizeManager.authorizeAction(context, this, Constants.ADD);

        Bitstream b = Bitstream.register(context, assetstore, bitstreamPath);

        // FIXME: Set permissions for bitstream

        addBitstream(b);

        return b;
    }

    public void addBitstream(Bitstream b) throws SQLException,
           AuthorizeException
    {
        log.info(LogManager.getHeader(context, "add_bitstream", "bundle_id="
                + getID() + ",bitstream_id=" + b.getID()));

        for (Bitstream bitstream : bitstreams)
        {
            if (b.getID() == bitstream.getID())
            {
                return;
            }
        }

        bitstreams.add(b);
    }

    public void removeBitstream(Bitstream b) throws AuthorizeException,
            SQLException, IOException
    {
        log.info(LogManager.getHeader(context, "remove_bitstream",
                "bundle_id=" + getID() + ",bitstream_id=" + b.getID()));

        Iterator<Bitstream> i = bitstreams.iterator();
        while (i.hasNext())
        {
            Bitstream bitstream = i.next();
            if (bitstream.getID() == b.getID())
            {
                i.remove();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////

    public int getType()
    {
        return Constants.BUNDLE;
    }

    ////////////////////////////////////////////////////////////////////
    // Deprecated methods
    ////////////////////////////////////////////////////////////////////

    @Deprecated
    Bundle(Context context, org.dspace.storage.rdbms.TableRow row)
    {
        this(context, row.getIntColumn("bundle_id"));
    }

    @Deprecated
    public static Bundle find(Context context, int id)
    {
        return BundleDAOFactory.getInstance(context).retrieve(id);
    }

    @Deprecated
    static Bundle create(Context context) throws AuthorizeException
    {
        return BundleDAOFactory.getInstance(context).create();
    }

    @Deprecated
    public void update() throws SQLException, AuthorizeException
    {
        dao.update(this);
    }

    @Deprecated
    void delete() throws AuthorizeException, IOException
    {
        dao.delete(this.getID());
    }

    @Deprecated
    public Item[] getItems()
    {
        List<Item> items = itemDAO.getParentItems(this);
        return (Item[]) items.toArray(new Item[0]);
    }
}
