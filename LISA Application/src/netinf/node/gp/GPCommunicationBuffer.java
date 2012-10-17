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

import java.util.Hashtable;
import java.util.concurrent.Semaphore;

import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer;
import netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.NIMessageType;

import org.apache.log4j.Logger;

/**
 * Represents an intermediator between GP and NetInf. GP puts {@link NIMessageContainer} into the buffer and NetInf takes
 * {@link NIMessageContainer} out of the buffer. The taking out of messages blocks until the according message is at hand.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class GPCommunicationBuffer {

   private static final Logger LOG = Logger.getLogger(GPCommunicationBuffer.class);

   private final Hashtable<Integer, Semaphore> permits;
   private final Hashtable<Integer, NIMessageContainer> messageBuffer;

   public GPCommunicationBuffer() {
      LOG.trace(null);
      permits = new Hashtable<Integer, Semaphore>();
      messageBuffer = new Hashtable<Integer, NIMessageContainer>();
   }

   public synchronized void putMessage(NIMessageContainer niMessageContainer) {
      Integer callbackId = getCallbackId(niMessageContainer);
      LOG.debug("Received message with ID: " + callbackId + ". Waking up according handler");

      messageBuffer.put(callbackId, niMessageContainer);
      Semaphore associatedSemaphore = getSemaphore(callbackId);
      associatedSemaphore.release();
   }

   /**
    * This message is synchronous and waits for the response of GP
    * 
    * @param callbackId
    * @return
    */
   public NIMessageContainer getMessage(Integer callbackId) {
      LOG.debug("Going to wait for reply with callbackId: " + callbackId);

      Semaphore associatedSemaphore = getSemaphore(callbackId);
      try {
         associatedSemaphore.acquire();

         // We got a permit, we can know fetch the required message
      } catch (InterruptedException e) {
         LOG.error("Could not wait for callbackId: " + callbackId);
         throw new NetInfUncheckedException("Could not receive the message", e);
      }

      LOG.debug("Informing handler which waits for id '" + callbackId + "' about the presence of the desired message");
      return messageBuffer.get(callbackId);
   }

   private synchronized Semaphore getSemaphore(Integer callbackId) {
      LOG.trace(null);
      Semaphore requiredSemaphore = permits.get(callbackId);

      if (requiredSemaphore == null) {
         LOG.debug("Creating a new semaphore for callbackID " + callbackId);
         // Request for new Semaphore. Create a new Semaphore initialized with 0
         requiredSemaphore = new Semaphore(0);
         permits.put(callbackId, requiredSemaphore);
      }

      return requiredSemaphore;
   }

   private Integer getCallbackId(NIMessageContainer messageContainer) {
      LOG.trace(null);
      if (messageContainer.getType() == NIMessageType.RESOLVECALLBACK) {
         return messageContainer.getResolveCallback().getCallbackId();
      } else {
         // Search for next one, if nothing found, return 0
         return null;
      }
   }

}
