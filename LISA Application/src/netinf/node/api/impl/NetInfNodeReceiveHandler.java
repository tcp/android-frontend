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

import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.NetInfMessage;
import netinf.node.api.NetInfNode;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Component of a NetInf Node that handles incoming messages
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class NetInfNodeReceiveHandler implements AsyncReceiveHandler {
   private static final Logger LOG = Logger.getLogger(NetInfNodeReceiveHandler.class);

   private final NetInfNode netInfNode;

   @Inject
   public NetInfNodeReceiveHandler(NetInfNode netInfNode) {
      this.netInfNode = netInfNode;
   }

   @Override
   public void receivedMessage(NetInfMessage message, Communicator communicator) {
      LOG.trace(null);
      NetInfMessage result = this.netInfNode.processNetInfMessage(message);
      boolean isAckOrOtherMessage = false;
      if (null == result) {
    	  isAckOrOtherMessage = true;
      }
    	  
      try {
    	  if (isAckOrOtherMessage) {
    		// We do not need to send a response to this type of messages
    		LOG.info("(NetInf Node) Got an ACK message or some other message not requiring a response");
    	  } else {
    		  communicator.send(result);
    	  }
      } catch (NetInfCheckedException e) {
         LOG.error("The following message could not be sent: " + result, e);
      }
   }
}
