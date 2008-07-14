/*
 * DSpaceObject.java
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

import org.dspace.uri.Identifiable;

import java.util.List;
import org.dspace.metadata.URIResource;
import org.dspace.uri.ExternalIdentifier;

/**
 * Abstract base class for DSpace objects
 */
public interface DSpaceObject extends Identifiable, URIResource
{

    /**
     * Get the type of this object, found in Constants
     * 
     * @return type of the object
     */
    public int getType();

    /**
     * Get the internal ID (database primary key) of this object
     * 
     * @return internal ID of object
     */
    public int getID();
    
    /**
     * Get a proper name for the object. This may return <code>null</code>.
     * Name should be suitable for display in a user interface.
     *
     * @return Name for the object, or <code>null</code> if it doesn't have
     *         one
     */
    public String getName();
    
    /**
     * Reset the cache of event details.
     */
    //protected void clearDetails();
    
    /**
     * Add a string to the cache of event details.  Automatically
     * separates entries with a comma.
     * Subclass can just start calling addDetails, since it creates
     * the cache if it needs to.
     * @param d detail string to add.
     */
    //protected void addDetails( String detail );

    /**
     * Get a single external identifiers associated with the object
     *
     * @return
     */
    public ExternalIdentifier getExternalIdentifier();

    /**
     * @return summary of event details, or null if there are none.
     */
    //protected String getDetails();

    ////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////

    public boolean equals( DSpaceObject other );

    public boolean contains( List<? extends DSpaceObject> dsos,
                              DSpaceObject dso );

    public int hashCode();

}
