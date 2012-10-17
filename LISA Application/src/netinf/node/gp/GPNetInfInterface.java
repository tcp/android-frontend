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
package netinf.node.gp;

import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.identity.NodeIdentityObject;
import netinf.common.transfer.TransferJob;
import netinf.node.api.NetInfNode;
import netinf.node.gp.datamodel.Capability;
import netinf.node.gp.datamodel.Resolution;
import netinf.node.transfer.ExecutableTransferJob;

/**
 * This is the general interface to communicate with our GP counterpart.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface GPNetInfInterface {

   /**
    * Adds the name for this node. The <code>name</code> is the {@link Identifier} of this nodes {@link NodeIdentityObject}. The
    * list <code>capabilities</code> is a list of {@link Capability} of this node.
    * 
    * @param name
    * @param capabilities
    */
   void addName(String name, List<Capability> capabilities);

   /**
    * This method is blocking. It resolves the capabilities and returns the list of {@link Resolution}s.
    * 
    * @param destinationName
    *           if the empty String is used as destination name we get an arbitrary adjacent {@link NetInfNode}.
    * @param capabilities
    * @return
    */
   List<Resolution> resolve(String destinationName, List<Capability> capabilities);

   /**
    * This is a method that is especially created in order to demonstrate the behavior of the vlc-player scenario.
    * 
    * @param jobID
    * @param source
    * @return
    */
   void prepareGP(String jobID, String source);

   /**
    * Moves the gp belonging to the {@link ExecutableTransferJob} with the id <code>jobID</code> to the <code>targetEntity</code>.
    * 
    * @param jobID
    * @param targetEntity
    *           The targetEntity is the new destination, and can accoringly be used from {@link TransferJob#getDestination()}
    */
   void moveGP(String jobID, String targetEntity);

   /**
    * Closes the connection to gp.
    */
   void tearDown();

}
