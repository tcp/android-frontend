/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node.resolution;

import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.node.resolution.eventprocessing.EventPublisher;

/**
 * Stores and retrieves InformationObjects
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface ResolutionService extends StorageService {

   /**
    * Returns the InformationObject with the given identifier.
    * <p>
    * If the identifier is versioned but misses a version label or contains an empty version label, the most recent version of
    * this Information Object is returned.
    * </p>
    * <p>
    * If no Information object with the given identifier is found the method returns <code>null</code>.
    * </p>
    * 
    * @param identifier
    *           the identifier of the requested InformationObject
    * @return the InformationObject
    * @throws NetInfResolutionException
    *            If something goes wrong during the resolution process.
    */
   InformationObject get(Identifier identifier);

   /**
    * Returns the identifiers of all versions of the InformationObject with the given Identifier. May only be called with a
    * <code>versioned</code> identifier.
    * 
    * @param identifier
    *           the identifier of the requested InformationObject without version
    * @return all versions of the InformationObject
    * @throws NetInfResolutionException
    *            if something goes wrong during the resolution or if the identifier is unversioned.
    */
   List<Identifier> getAllVersions(Identifier identifier);

   /**
    * Stores the given InformationObject. A versioned InformationObject has to contain a version label.
    * 
    * @param informationObject
    *           the InformationObject that should be stored
    * @throws NetInfResolutionException
    *            if something goes wrong during the storage process
    */
   void put(InformationObject informationObject);

   /**
    * Deletes the InformationObject with the given Identifier. Only an unversioned InformationObject can be deleted. If there is
    * no information Object with the given identifier nothing happens. Especially no method on the {@link EventPublisher} is
    * called.
    * 
    * @param identifier
    *           the Identifier of the IO that should be deleted
    * @throws NetInfResolutionException
    *            if it is tried to delete a versioned InformationObject or if something went wrong during the deletion process.
    */
   void delete(Identifier identifier);

   /**
    * Add an EventPublisher that will be notified on put and delete events. In case of an error in the event publisher it will be
    * dropped from the list of event publishers.
    * 
    * @param eventPublisher
    *           the EventPublisher, forwards events to an EventService
    */
   void addEventService(EventPublisher eventPublisher);

   ResolutionServiceIdentityObject getIdentity();

   /**
    * Returns a textual description ("I can resolve via "+describe())
    * 
    * @return textual description
    */
   String describe();
}
