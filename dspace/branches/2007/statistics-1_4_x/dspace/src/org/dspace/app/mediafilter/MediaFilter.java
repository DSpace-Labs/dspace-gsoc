/*
 * MediaFilter.java
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
package org.dspace.app.mediafilter;

import java.io.InputStream;

import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.Context;

public abstract class MediaFilter
{
	protected Item item = null;
	
    /* To create your own filter, implement the following virtual methods */

    /**
     * Get a filename for a newly created filtered bitstream
     * 
     * @param sourceName
     *            name of source bitstream
     * @return filename generated by the filter - for example, document.pdf
     *         becomes document.pdf.txt
     */
    public abstract String getFilteredName(String sourceName);

    /**
     * @return name of the bundle this filter will stick its generated
     *         Bitstreams
     */
    public abstract String getBundleName();

    /**
     * @return name of the bitstream format (say "HTML" or "Microsoft Word")
     *         returned by this filter look in the bitstream format registry or
     *         mediafilter.cfg for valid format strings.
     */
    public abstract String getFormatString();

    /**
     * @return string to describe the newly-generated Bitstream's - how it was
     *         produced is a good idea
     */
    public abstract String getDescription();

    /**
     * @param source
     *            input stream
     * 
     * @return result of filter's transformation, written out to a bitstream
     */
    public abstract InputStream getDestinationStream(InputStream source)
            throws Exception;

    /* end of methods you need to implement! */

    /**
     * processBitstream is a utility class that calls the above virtual methods -
     * it is unlikely that you will need to override it. It scans the bitstreams
     * in an item, and decides if a bitstream has already been filtered, and if
     * not or if overWrite is set, invokes the filter.
     * 
     * @param c
     *            context
     * @param item
     *            item containing bitstream to process
     * @param source
     *            source bitstream to process
     * 
     * @return true if new rendition is created, false if rendition already
     *         exists and overWrite is not set
     */
    public boolean processBitstream(Context c, Item item, Bitstream source)
            throws Exception
    {
        boolean overWrite = MediaFilterManager.isForce;
        
        this.item = item;

        // get bitstream filename, calculate destination filename
        String newName = getFilteredName(source.getName());

        Bitstream existingBitstream = null; // is there an existing rendition?
        Bundle targetBundle = null; // bundle we're modifying

        Bundle[] bundles = item.getBundles(getBundleName());

        // check if destination bitstream exists
        if (bundles.length > 0)
        {
            // only finds the last match (FIXME?)
            for (int i = 0; i < bundles.length; i++)
            {
                Bitstream[] bitstreams = bundles[i].getBitstreams();

                for (int j = 0; j < bitstreams.length; j++)
                {
                    if (bitstreams[j].getName().equals(newName))
                    {
                        targetBundle = bundles[i];
                        existingBitstream = bitstreams[j];
                    }
                }
            }
        }

        // if exists and overwrite = false, exit
        if (!overWrite && (existingBitstream != null))
        {
            System.out.println("SKIPPED: bitstream " + source.getID()
                    + " because '" + newName + "' already exists");

            return false;
        }

        InputStream destStream = getDestinationStream(source.retrieve());

        // create new bundle if needed
        if (bundles.length < 1)
        {
            targetBundle = item.createBundle(getBundleName());
        }
        else
        {
            // take the first match
            targetBundle = bundles[0];
        }

        Bitstream b = targetBundle.createBitstream(destStream);

        // Now set the format and name of the bitstream
        b.setName(newName);
        b.setSource("Written by MediaFilter " + this.getClass().getName()); // or
                                                                            // obj.getClass().getName();
        b.setDescription(getDescription());

        // Find the proper format
        BitstreamFormat bf = BitstreamFormat.findByShortDescription(c,
                getFormatString());
        b.setFormat(bf);
        b.update();

        // fixme - set date?
        // we are overwriting, so remove old bitstream
        if (existingBitstream != null)
        {
            targetBundle.removeBitstream(existingBitstream);
        }

        System.out.println("FILTERED: bitstream " + source.getID()
                + " and created '" + newName + "'");

        return true;
    }
}
