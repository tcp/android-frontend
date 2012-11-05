/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
package project.cs.lisa.netinf.node.resolution;

import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.eventprocessing.EventPublisher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * A base class for Resolution Services. Provides some basic functionality;
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class LisaAbstractResolutionServiceWithoutId implements ResolutionService {

   private static final Logger LOG = Logger.getLogger(LisaAbstractResolutionServiceWithoutId.class);
   private final List<EventPublisher> eventPublishers;
   private ResolutionServiceIdentityObject identityObject;

   public LisaAbstractResolutionServiceWithoutId() {
      eventPublishers = new ArrayList<EventPublisher>();
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
