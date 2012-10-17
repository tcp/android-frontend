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
package netinf.node.api;

import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.identity.NodeIdentityObject;
import netinf.common.messages.NetInfMessage;
import netinf.node.gp.GPNetInfInterface;
import netinf.node.gp.datamodel.Capability;
import netinf.node.gp.datamodel.Resolution;
import netinf.node.resolution.ResolutionController;
import netinf.node.search.SearchController;
import netinf.node.transfer.TransferController;

/**
 * This is the interface that represents a whole running NetInfNode. It can be seen as a facade to access the
 * {@link ResolutionController}, the {@link SearchController}, and the {@link TransferController}. Accordingly, it is possible to
 * access each of these controllers directly from the instance that has this type. There is always only one instance of this type
 * within the whole NetInfNode - this is guaranteed via GUICE. The most important method of this interface is
 * {@link NetInfNode#processNetInfMessage(NetInfMessage)}. This method can be called with every request message. It processes the
 * message, by delegating it to the appropriate controller, and returns the result as a new {@link NetInfMessage}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface NetInfNode {

   /**
    * @return the sum of the supported operations of the managed controllers.
    */
   List<Class<? extends NetInfMessage>> getSupportedOperations();

   NodeIdentityObject getNodeIdentityObject();

   Identifier getNodeIdentityObjectIdentifier();

   ResolutionController getResolutionController();

   SearchController getSearchController();

   TransferController getTransferController();

   NetInfMessage processNetInfMessage(NetInfMessage netInfMessage);

   List<Resolution> resolveCapabilities(List<Capability> capabilities, String fromName);

   /**
    * This method is specific to a tcp port, where the node is listening.
    * 
    * @param gpNetInfInterface
    * @param port
    * @param capabilities
    */
   void addCapabilities(GPNetInfInterface gpNetInfInterface, int port, Capability... capabilities);
}
