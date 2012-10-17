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

import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.resolution.eventprocessing.EventPublisher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdFactory;

/**
 * A base class for Resolution Services. Provides some basic functionality;
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class AbstractResolutionService implements ResolutionService {

   private static final Logger LOG = Logger.getLogger(AbstractResolutionService.class);
   private final List<EventPublisher> eventPublishers;
   private IdFactory idFactory;
   private ResolutionServiceIdentityObject identityObject;

   public AbstractResolutionService() {
      eventPublishers = new ArrayList<EventPublisher>();
   }

   public void setIdFactory(IdFactory idFactory) {
      this.idFactory = idFactory;
   }

   @Override
   public void addEventService(EventPublisher eventPublisher) {
      eventPublishers.add(eventPublisher);
   }

   protected void publishDelete(InformationObject ioToDelete) {
      List<EventPublisher> toDelete = new ArrayList<EventPublisher>();
      for (EventPublisher ep : eventPublishers) {
         try {
            ep.publishDelete(ioToDelete);
         } catch (NetInfUncheckedException e) {
            // Remove not working event publishers
            toDelete.add(ep);
            LOG.warn("Removing Event Publisher because of error", e);
         }
      }
      eventPublishers.removeAll(toDelete);
   }

   protected Identifier createIdentifierWithoutVersion(Identifier identifier) {
      Identifier id = (Identifier) identifier.clone();
      id.removeIdentifierLabel(DefinedLabelName.VERSION_NUMBER.getLabelName());
      return id;
   }

   protected void publishPut(InformationObject oldIo, InformationObject newIo) {
      List<EventPublisher> toDelete = new ArrayList<EventPublisher>();
      for (EventPublisher ep : eventPublishers) {
         try {
            ep.publishPut(oldIo, newIo);
         } catch (NetInfUncheckedException e) {
            toDelete.add(ep);
            LOG.warn("Removing Event Publisher because of error", e);
         }
      }
      eventPublishers.removeAll(toDelete);
   }

   protected Id buildId(Identifier identifier) {
      return idFactory.buildId(new String(identifier.serializeToBytes()));
   }

   protected Id buildId(InformationObject informationObject) {
      return buildId(informationObject.getIdentifier());
   }

   protected Identifier getIdToLookup(Identifier identifier) {
      Identifier idToLookup = identifier;
      if (identifier.isVersioned()) {
         String version = null;
         if (identifier.getIdentifierLabel(DefinedLabelName.VERSION_NUMBER.getLabelName()) != null) {
            version = identifier.getIdentifierLabel(DefinedLabelName.VERSION_NUMBER.getLabelName()).getLabelValue();
         }
         if (StringUtils.isEmpty(version)) {
            idToLookup = getNewestVersionIdentifier(identifier);
         }
      }

      return idToLookup;
   }

   protected Identifier getNewestVersionIdentifier(Identifier identifier) {
      // TODO Think about a more efficient way to get the newest version
      List<Identifier> identifiers = getAllVersions(identifier);
      if (!identifiers.isEmpty()) {
         return identifiers.get(identifiers.size() - 1);
      }
      return null;
   }

   protected void validateIOForPut(InformationObject informationObject) {
      LOG.trace(null);
      if (informationObject.getIdentifier().isVersioned()) {
         IdentifierLabel versionLabel = informationObject.getIdentifier().getIdentifierLabel(
               DefinedLabelName.VERSION_NUMBER.getLabelName());
         Validate.notNull(versionLabel, "Version label is null");
         Validate.notEmpty(versionLabel.getLabelValue(), "Value of version label is empty");
      }
   }

   @Override
   public ResolutionServiceIdentityObject getIdentity() {
      if (identityObject == null) {
         identityObject = createIdentityObject();
      }
      return identityObject;
   }

   protected abstract ResolutionServiceIdentityObject createIdentityObject();

}
