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

import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.messages.ESFSubscriptionResponse;
import netinf.common.messages.ESFUnsubscriptionResponse;
import netinf.common.messages.NetInfMessage;

import org.apache.log4j.Logger;

/**
 * Part of the ESF connector which can be used to subscribe to events and receive events from an event service. This class is
 * responsible for the receiving of incoming ESF* messages.
 * <p>
 * It accepts incoming {@link ESFFetchMissedEventsResponse} and {@link ESFEventMessage} messages and stores the (contained)
 * {@link ESFEventMessage}s in a queue. An implementation of {@link AbstractMessageProcessor} will fetch these messages from the
 * queue and further process them. Furthermore it accepts {@link ESFSubscriptionResponse} and {@link ESFUnsubscriptionResponse}
 * messages. They are not further processed.
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see AsyncReceiveHandler
 * @see AbstractEsfConnector
 * @see AbstractMessageProcessor
 */
public class MessageReceiver implements AsyncReceiveHandler {

   private static final Logger LOG = Logger.getLogger(MessageReceiver.class);

   private LinkedBlockingQueue<ESFEventMessage> messageQueue;

   /**
    * Called by {@link AbstractEsfConnector}
    * 
    * @param messageQueue
    */
   public void setMessageQueue(final LinkedBlockingQueue<ESFEventMessage> messageQueue) {
      this.messageQueue = messageQueue;
   }

   @Override
   public void receivedMessage(final NetInfMessage message, final Communicator arrivedOver) {
      LOG.trace(null);

      if (message.getErrorMessage() != null) {
         LOG.warn("Received NetInf message with the following error message: " + message.getErrorMessage());
         return;
      }

      if (message instanceof ESFEventMessage) {
         LOG.debug("Received an ESFEventMessage");
         final ESFEventMessage eventMessage = (ESFEventMessage) message;
         try {
            messageQueue.put(eventMessage);
         } catch (InterruptedException e) {
            LOG.warn(e.toString());
         }
      } else if (message instanceof ESFFetchMissedEventsResponse) {
         LOG.debug("Received an ESFFetchMissedEventsResponse");
         final ESFFetchMissedEventsResponse missedEventsMessage = (ESFFetchMissedEventsResponse) message;
         for (ESFEventMessage missedMessage : missedEventsMessage.getEventMessages()) {
            try {
               messageQueue.put(missedMessage);
               LOG.debug("Put message into message queue");
            } catch (InterruptedException e) {
               LOG.warn(e.toString());
            }
         }
      } else if (message instanceof ESFSubscriptionResponse) {
         LOG.debug("Received ESFSubscriptionResponse: Subscription was successfull");
      } else if (message instanceof ESFUnsubscriptionResponse) {
         LOG.debug("Received ESFUnsubscriptionResponse: Unsubscription was successfull");
      } else {
         LOG.warn("Unsupported/Unexpected netinf message (type) received");
      }

   }
}
