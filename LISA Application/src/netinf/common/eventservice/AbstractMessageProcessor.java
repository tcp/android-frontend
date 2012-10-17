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
package netinf.common.eventservice;

import java.util.concurrent.LinkedBlockingQueue;

import netinf.common.messages.ESFEventMessage;

import org.apache.log4j.Logger;

/**
 * Part of the ESF connector which can be used to subscribe to events and receive events from an event service. This class is
 * responsible for the processing of received events.
 * <p>
 * A {@link MessageReceiver} receives the ESF* messages and puts the {@link ESFEventMessage}s into a queue. This class fetches the
 * messages from the queue and handles them in method {@link #handleESFEventMessage(ESFEventMessage)}. This method has to be
 * implemented in an extending class.
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see AbstractEsfConnector
 * @see MessageReceiver
 */
public abstract class AbstractMessageProcessor extends Thread {

   protected static final Logger LOG = Logger.getLogger(AbstractMessageProcessor.class);

   private LinkedBlockingQueue<ESFEventMessage> messageQueue;
   private boolean waiting = true;

   @Override
   public void run() {
      LOG.debug("Message processor started");
      ESFEventMessage message;
      try {
         while (true) {
            LOG.debug("Wait for new event message...");
            waiting = true;
            message = messageQueue.take();
            waiting = false;
            handleESFEventMessage(message);
         }
      } catch (InterruptedException e) {
         LOG.warn(e.toString());
      }
   }

   /**
    * Called by {@link AbstractEsfConnector}
    * 
    * @param messageQueue
    */
   public void setMessageQueue(final LinkedBlockingQueue<ESFEventMessage> messageQueue) {
      this.messageQueue = messageQueue;
   }

   /**
    * Called by {@link AbstractEsfConnector}
    * 
    * @return true iff it is waiting for a new message to process
    */
   public boolean isWaiting() {
      return waiting;
   }

   /**
    * The specific handling of incoming {@link ESFEventMessage}s has to be implemented in this method
    * 
    * @param eventMessage
    *           the ESFEventMessage to process
    */
   protected abstract void handleESFEventMessage(ESFEventMessage eventMessage);
}
