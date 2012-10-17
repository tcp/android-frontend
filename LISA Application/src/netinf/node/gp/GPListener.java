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

import java.io.IOException;

import netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * This class is responsible for receiving messages from our GP counterpart. It has to be started by {@link GPNetInfInterfaceImpl}
 * .
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class GPListener implements Runnable {

   private static final Logger LOG = Logger.getLogger(GPListener.class);

   private final GPCommunicationBuffer gpCommunicationBuffer;
   private GPNetInfInterfaceImpl gpNetInfInterface;

   private boolean interrupted;

   @Inject
   public GPListener(GPCommunicationBuffer gpCommunicationBuffer) {
      LOG.trace(null);
      this.gpCommunicationBuffer = gpCommunicationBuffer;
      interrupted = true;
   }

   @Override
   public void run() {
      LOG.debug("Starting to listen for new messages from GP");
      while (!interrupted) {
         try {
            byte[] data = gpNetInfInterface.receiveBytes();

            if (data != null) {
               LOG.debug("Received new bytes");

               NIMessageContainer parse = NIMessageContainer.parseFrom(data);
               LOG.debug("Received message " + parse);
               gpCommunicationBuffer.putMessage(parse);
            } else {
               // The connection was closed remotely
               LOG.debug("Detected closed connection, stopping listening");
               stop();
            }

         } catch (InvalidProtocolBufferException e) {
            LOG.error("Could not parse GP message", e);
         } catch (IOException e) {
            LOG.error("Could not receive message from GP, stopping to listen for new messages from GP.", e);
            stop();
         } catch (Throwable e) {
            LOG.error("Something went seriously wrong");
         }
      }
   }

   /**
    * The listener might be started several times. If it is currently not running, it is started, otherwise the call is simply
    * ignored.
    * 
    * @param gpNetInfInterface
    *           The connection to GP must already be created at this point.
    */
   public synchronized void start(GPNetInfInterfaceImpl gpNetInfInterface) {
      if (interrupted) {
         this.gpNetInfInterface = gpNetInfInterface;

         LOG.trace(null);
         interrupted = false;
         new Thread(this).start();
      }
   }

   /**
    * Can be stopped several times.
    * 
    * @see GPListener#start(GPNetInfInterfaceImpl)
    */
   public synchronized void stop() {
      LOG.trace(null);
      interrupted = true;
   }

}
