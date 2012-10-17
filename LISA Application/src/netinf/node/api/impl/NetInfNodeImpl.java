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
package netinf.node.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.identity.NodeIdentityObject;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.messages.NetInfMessage;
import netinf.node.api.NetInfNode;
import netinf.node.gp.GPNetInfInterface;
import netinf.node.gp.GPNetInfInterfaceImpl;
import netinf.node.gp.datamodel.Capability;
import netinf.node.gp.datamodel.Resolution;
import netinf.node.resolution.ResolutionController;
import netinf.node.search.SearchController;
import netinf.node.transfer.TransferController;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import com.google.inject.name.Named;

/**
 * This is a stereotypical representation of a {@link NetInfNode}. The {@link ResolutionController}, {@link SearchController} and
 * {@link TransferController} are all optional. The {@link NetInfMessage}s are simply delegated to one of these controller.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class NetInfNodeImpl implements NetInfNode {

   private static final Logger LOG = Logger.getLogger(NetInfNodeImpl.class);

   private final Identifier nodeIdentityObjectIdentifier;
   private ResolutionController resolutionController;
   private SearchController searchController;
   private TransferController transferController;

   /**
    * This array contains the set of all the supported operations (NetInfMessages). In case a new controller is set, or the list
    * of operations is modified, ths list has to be set to null (update then happens in the method
    * {@link NetInfNodeImpl#getSupportedOperations()}.
    */
   private ArrayList<Class<? extends NetInfMessage>> supportedOperations;

   private GPNetInfInterface gpNetInfInterface;

   /*************************************
    ****** Setup of the NetInfNode ******
    *************************************/

   @Inject
   public NetInfNodeImpl(DatamodelFactory datamodelFactory, @Named("identity.nodeIdentity") String nodeIdentityObjectIdentifier) {
      Identifier tmpIdentifier = datamodelFactory.createIdentifierFromString(nodeIdentityObjectIdentifier);
      this.nodeIdentityObjectIdentifier = tmpIdentifier;
   }

   @Inject(optional = true)
   public void setGpNetInfInterface(@Nullable GPNetInfInterface gpNetInfInterface) {
      if (this.gpNetInfInterface == null) {
         this.gpNetInfInterface = gpNetInfInterface;
      }
   }

   @Inject(optional = true)
   public void setResolutionController(@Nullable ResolutionController resolutionController) {
      LOG.trace("Setting ResolutionController");
      this.resolutionController = resolutionController;
      supportedOperations = null;
   }

   @Inject(optional = true)
   public void setSearchController(@Nullable SearchController searchController) {
      LOG.trace("Setting SearchController");
      this.searchController = searchController;
      supportedOperations = null;
   }

   @Inject(optional = true)
   public void setTransferController(@Nullable TransferController transferController) {
      LOG.trace("Setting TransferController");
      this.transferController = transferController;
      supportedOperations = null;
   }

   /********************************************
    ****** Methods of the netInfNode Impl ******
    ********************************************/

   @Override
   public ResolutionController getResolutionController() {
      return resolutionController;
   }

   @Override
   public SearchController getSearchController() {
      return searchController;
   }

   @Override
   public TransferController getTransferController() {
      return transferController;
   }

   @Override
   public NodeIdentityObject getNodeIdentityObject() {
      return null;
   }

   @Override
   public Identifier getNodeIdentityObjectIdentifier() {
      return nodeIdentityObjectIdentifier;
   }

   @Override
   public List<Class<? extends NetInfMessage>> getSupportedOperations() {
      LOG.trace(null);

      if (supportedOperations == null) {
         int foundOperationsCounter = 0;
         supportedOperations = new ArrayList<Class<? extends NetInfMessage>>();

         if (resolutionController != null) {
            List<Class<? extends NetInfMessage>> resSupportedOperations = resolutionController.getSupportedOperations();
            if (resSupportedOperations != null) {
               supportedOperations.addAll(resSupportedOperations);
            }

            foundOperationsCounter += resSupportedOperations.size();
         }

         if (searchController != null) {
            List<Class<? extends NetInfMessage>> serSupportedOperations = searchController.getSupportedOperations();
            if (serSupportedOperations != null) {
               supportedOperations.addAll(serSupportedOperations);
            }

            foundOperationsCounter += serSupportedOperations.size();
         }

         if (transferController != null) {
            List<Class<? extends NetInfMessage>> tranSupportedOperations = transferController.getSupportedOperations();
            if (tranSupportedOperations != null) {
               supportedOperations.addAll(tranSupportedOperations);
            }

            foundOperationsCounter += tranSupportedOperations.size();
         }

         if (supportedOperations.size() < foundOperationsCounter) {
            throw new NetInfUncheckedException("One operation supported by multiple controllers");
         }
      }

      return supportedOperations;
   }

   @Override
   public NetInfMessage processNetInfMessage(NetInfMessage netInfMessage) {
      LOG.trace(null);

      // Perform check, whether the operation is supported
      if (!isSupportedOperation(netInfMessage)) {
         LOG.error("The operation " + netInfMessage.getClass() + " is not supported by this NetInfNode");
         throw new NetInfUncheckedException("This operation is not supported");
      }

      NetInfMessage result = null;

      if (resolutionController != null && resolutionController.getSupportedOperations() != null
            && resolutionController.getSupportedOperations().contains(netInfMessage.getClass())) {
         result = resolutionController.processNetInfMessage(netInfMessage);
      }

      if (transferController != null && transferController.getSupportedOperations() != null
            && transferController.getSupportedOperations().contains(netInfMessage.getClass())) {
         result = transferController.processNetInfMessage(netInfMessage);
      }

      if (searchController != null && searchController.getSupportedOperations() != null
            && searchController.getSupportedOperations().contains(netInfMessage.getClass())) {
         result = searchController.processNetInfMessage(netInfMessage);
      }

      return result;
   }

   public boolean isSupportedOperation(NetInfMessage netInfMessage) {
      return getSupportedOperations().contains(netInfMessage.getClass());
   }

   @Override
   public List<Resolution> resolveCapabilities(List<Capability> capabilities, String destinationName) {
      LOG.trace(null);

      if (gpNetInfInterface != null) {

         List<Resolution> resolutions = gpNetInfInterface.resolve(destinationName, capabilities);
         return resolutions;
      } else {
         throw new NetInfUncheckedException("No connection to gp given");
      }
   }

   @Override
   @Inject(optional = true)
   public void addCapabilities(GPNetInfInterface gpNetInfInterface, @Named("access.tcp.port") int port,
         Capability... capabilities) {
      LOG.trace(null);
      List<Capability> list = Arrays.asList(capabilities);
      if (this.gpNetInfInterface == null) {
         this.gpNetInfInterface = gpNetInfInterface;
      }

      gpNetInfInterface.addName(nodeIdentityObjectIdentifier.toString() + GPNetInfInterfaceImpl.NAME_PORT_SEPERATOR + port, list);
   }
}
