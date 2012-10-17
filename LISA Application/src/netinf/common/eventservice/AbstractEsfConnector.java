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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import netinf.common.communication.Communicator;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsRequest;
import netinf.common.messages.ESFRegistrationRequest;
import netinf.common.messages.ESFRegistrationResponse;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.common.messages.ESFUnsubscriptionRequest;
import netinf.common.messages.NetInfMessage;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Can be used to connect a component of a NetInf node or application with an event service to subscribe to events and receive
 * them.
 * <p>
 * This class establishes the connection to the event service and does the initialization (registration and optional initial
 * subscription). The handling of incoming ESF* messages is done by the {@link MessageReceiver} and an implementation of
 * {@link AbstractMessageProcessor} which are started in new threads by this class. Furthermore, it can be used to subscribe to
 * events {@link #sendSubscription(String, String, long)} or unsubscribe from them {@link #sendUnsubscription(String)}.
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see MessageReceiver
 * @see AbstractMessageProcessor
 */
public abstract class AbstractEsfConnector extends Thread {

   protected static final Logger LOG = Logger.getLogger(AbstractEsfConnector.class);

   private final DatamodelFactory dmFactory;
   private final AbstractMessageProcessor procHandler;
   private final MessageReceiver receiveHandler;
   private final String host;
   private final Integer port;
   private final LinkedBlockingQueue<ESFEventMessage> messageQueue;

   private Identifier identityIdentifier = null;

   private Provider<Communicator> communicatorProvider;
   private Communicator communicator;

   private List<String> subscriptionIdentifications = null;
   private List<String> subscriptionQueries = null;
   private List<Long> subscriptionExpireTimes = null;

   public AbstractEsfConnector(final DatamodelFactory dmFactory, final MessageReceiver receiveHandler,
         final AbstractMessageProcessor procHandler, final String host, final String port) {
      this.dmFactory = dmFactory;
      this.receiveHandler = receiveHandler;
      this.procHandler = procHandler;
      this.host = host;
      this.port = Integer.valueOf(port);

      this.messageQueue = new LinkedBlockingQueue<ESFEventMessage>();
   }

   @Inject
   public void setCommunicatorProvider(final Provider<Communicator> communicatorProvider) {
      this.communicatorProvider = communicatorProvider;
   }

   /**
    * This method has to be called before starting the esfConnector
    * 
    * @param identifier
    *           the identifier of the NetInf component or application that uses this esfConnector
    */
   public void setIdentityIdentifier(final Identifier identifier) {
      this.identityIdentifier = identifier;
   }

   /**
    * This method can to be called before starting the esfConnector if subscriptions shall already be sent while the
    * initialization.
    * <p>
    * Elements with the same index in the parameter lists belong together (form one subscription). The meaning of the elements of
    * the lists is analog to the parameters of an {@link ESFSubscriptionRequest} message.
    * 
    * @param subscriptionIdentifications
    *           list of subscription identifications
    * @param subscriptionQueries
    *           list of subscription queries
    * @param subscriptionExpireTimes
    *           list of expiries
    */
   public void setInitialSubscriptionInformation(final List<String> subscriptionIdentifications,
         final List<String> subscriptionQueries, final List<Long> subscriptionExpireTimes) {
      this.subscriptionIdentifications = subscriptionIdentifications;
      this.subscriptionQueries = subscriptionQueries;
      this.subscriptionExpireTimes = subscriptionExpireTimes;
   }

   @Override
   public void run() {
      super.run();

      boolean result = true;

      result = systemReadyToHandleReceivedMessage();

      if (result) {
         result = setup();
      }

      if (result) {
         result = sendESFRegistrationRequest();
      }

      if (result) {
         result = sendESFSubscriptionRequest();
      }

      if (result) {
         // start message processor
         procHandler.setMessageQueue(messageQueue);
         procHandler.setName("EsfConnector_MessageProcessor");
         procHandler.start();

         // start async receiver
         receiveHandler.setMessageQueue(messageQueue);
         communicator.startAsyncReceive(receiveHandler, true);

         // send ESFFetchMissedEvents (response will be handled by async receiver)
         LOG.debug("Send ESFFetchMissedEventsRequest message");
         try {
            communicator.send(new ESFFetchMissedEventsRequest());
         } catch (NetInfCheckedException e) {
            LOG.error(e.toString());
            result = false;
         }
      }

      if (result) {
         LOG.log(DemoLevel.DEMO, "(ESCON) I am connected to an event service at " + this.host + ":" + this.port);
      } else {
         LOG.error("Something went wrong while initializing the ESF connector. See above error message for details");
         // if connection is open, close it as cleanup
         try {
            this.communicator.close();
         } catch (Exception e) {
            LOG.warn("Communicator could not be closed");
         }
      }
   }

   private boolean sendESFRegistrationRequest() {
      if (this.identityIdentifier != null) {
         ESFRegistrationRequest regRequest = new ESFRegistrationRequest(this.identityIdentifier);
         LOG.debug("Send ESFRegistrationRequest message");
         try {
            communicator.send(regRequest);
         } catch (NetInfCheckedException e) {
            LOG.error("The following error occured while sending the ESFRegistrationRequest: " + e.toString());
            return false;
         }

         NetInfMessage message = null;
         LOG.debug("Wait for receiving ESFRegistrationResponse message");
         try {
            message = communicator.receive();
         } catch (NetInfCheckedException e1) {
            LOG.error("The following error occured while waiting for / receiving ESFRegistrationResponse: " + e1);
            return false;
         }

         if (message.getErrorMessage() != null) {
            LOG.warn("Received NetInf message with the following error message: " + message.getErrorMessage());
            return false;
         }

         if (!(message instanceof ESFRegistrationResponse)) {
            LOG.warn("Expected ESFRegistrationResponse");
            return false;
         }

         return true;
      } else {
         LOG.error("Identifier of identity object not set. Needed for ESFRegistrationMessage");
         return false;
      }
   }

   private boolean sendESFSubscriptionRequest() {

      if (subscriptionIdentifications != null && subscriptionQueries != null && subscriptionExpireTimes != null) {

         if (subscriptionIdentifications.size() == subscriptionQueries.size()
               && subscriptionQueries.size() == subscriptionExpireTimes.size()) {

            boolean success = true;
            for (int i = 0; i < subscriptionIdentifications.size(); i++) {

               if (subscriptionIdentifications.get(i).length() > 0 && subscriptionQueries.get(i).length() > 0
                     && subscriptionExpireTimes.get(0) > 0) {
                  // case: variables are set

                  ESFSubscriptionRequest subRequest = new ESFSubscriptionRequest(subscriptionIdentifications.get(i),
                        subscriptionQueries.get(i), subscriptionExpireTimes.get(i));

                  // send ESFSubscriptionRequest (maybe already set)
                  LOG.debug("Send ESFSubscriptionRequest message");
                  try {
                     communicator.send(subRequest);
                  } catch (NetInfCheckedException e) {
                     LOG.error("The following error occured while sending the ESFSubscriptionRequest: " + e.toString());
                     tryRollback(i);
                     success = false;
                     break;
                  }
                  NetInfMessage message = null;
                  LOG.debug("Wait for receiving ESFSubscriptionResponse message");
                  try {
                     message = communicator.receive();
                  } catch (NetInfCheckedException e1) {
                     LOG.error("The following error occured while waiting for / receiving ESFSubscriptionResponse: " + e1);
                     tryRollback(i);
                     success = false;
                     break;
                  }
                  if (message.getErrorMessage() != null) {
                     if (message.getErrorMessage().equals(
                           "A subscription message with the same identification was already processed. "
                                 + "First unsubscribe the old one")) {
                        LOG.info("No effect. Subscription was already set");
                     } else {
                        LOG.error("Received NetInf message with the following error message: " + message.getErrorMessage());
                        tryRollback(i);
                        success = false;
                        break;
                     }
                  }

               } else {
                  // case: variables are not set => no subscription possible
                  LOG.error("Subscription parameters of subscription " + i + "not properly set. Abort.");
                  tryRollback(i);
               }
            }

            // this is the correct end of this method
            if (!success) {
               return false;
            }
            return true;

         } else {
            // case: different number of entries in the 3 lists => something not ok => no subscription possible
            LOG.error("Different number of elements in the three subscription parameter lists.");
            // stop initialization => return false
            return false;
         }
      } else {
         // case: variables are not set => no subscription possible
         LOG.warn("Initial subscription parameters not set. This can be planned or be an error");
         // do not stop initialization => return true
         return true;
      }

   }

   private void tryRollback(final int indexOfErroneousSubscription) {
      LOG.info("Try rollback of former subscriptions");
      boolean success = true;
      for (int i = 0; i < indexOfErroneousSubscription; i++) {
         ESFUnsubscriptionRequest unsubRequest = new ESFUnsubscriptionRequest(subscriptionIdentifications.get(i));

         // send ESFUnsubscriptionRequest
         LOG.debug("Send ESFUnsubscriptionRequest message");
         try {
            communicator.send(unsubRequest);
         } catch (NetInfCheckedException e) {
            LOG.error("The following error occured while sending the ESFUnsubscriptionRequest: " + e.toString());
            success = false;
            break;
         }
         NetInfMessage message = null;
         LOG.debug("Wait for receiving ESFUnsubscriptionResponse message");
         try {
            message = communicator.receive();
         } catch (NetInfCheckedException e1) {
            LOG.error("The following error occured while waiting for / receiving ESFUnsubscriptionResponse: " + e1);
            success = false;
            break;
         }
         if (message.getErrorMessage() != null) {
            LOG.error("Received NetInf message with the following error message: " + message.getErrorMessage());
            success = false;
         }
      }
      if (success) {
         LOG.info("Rollback successful");
      } else {
         LOG.info("Rollback not successful");
      }

   }

   private boolean setup() {
      try {
         this.communicator = communicatorProvider.get();
         this.communicator.setup(this.host, this.port);
      } catch (IOException e) {
         LOG.error("An error occured while connecting to event service: " + e.toString());
         return false;
      }

      this.communicator.setSerializeFormat(this.dmFactory.getSerializeFormat());
      LOG.debug("Successfully connected to event service");
      return true;
   }

   /**
    * The method closes the connection to the event service. It waits till all received {@link ESFEventMessage}s are processed by
    * {@link AbstractMessageProcessor#handleESFEventMessage(ESFEventMessage)} before it returns.
    */
   public void tearDown() {
      LOG.trace(null);
      try {
         this.communicator.stopAsyncReceive();
         this.communicator.close();
      } catch (Exception e) {
         LOG.warn("Communicator could not be closed");
      }
      while (!(messageQueue.isEmpty() && procHandler.isWaiting())) {
         try {
            Thread.sleep(500);
         } catch (InterruptedException e) {
            LOG.debug("Got interrupted while waiting for message processor to finish processing of messages");
         }
      }
   }

   /**
    * Should be used to check whether the component that uses this esfConnector is ready to handle incoming event messages. For
    * example, if the information objects that are contained in an event message are stored somewhere, one might check if this
    * storage is ready. If nothing has to be checked before the connection to the event service is established, just implement a
    * <code>return true;</code>
    * 
    * @return true, if the overall system (node or application) is ready to handle incoming event messages
    */
   protected abstract boolean systemReadyToHandleReceivedMessage();

   /**
    * Sends a subscription to the event service.
    * <p>
    * The meaning of the parameters is analog to the parameters of the {@link ESFSubscriptionRequest} message.
    * 
    * @param subscriptionIdentification
    * @param subscriptionQuery
    * @param subscriptionExpires
    */
   public void sendSubscription(final String subscriptionIdentification, final String subscriptionQuery,
         final long subscriptionExpires) {
      ESFSubscriptionRequest subRequest = new ESFSubscriptionRequest(subscriptionIdentification, subscriptionQuery,
            subscriptionExpires);

      // send ESFSubscriptionRequest (maybe already set)
      LOG.debug("Send ESFSubscriptionRequest message");
      communicator.sendAsync(subRequest);
   }

   /**
    * Sends an unsubscription to the event service.
    * <p>
    * The meaning of the parameter is analog to the meaning of the parameter of the {@link ESFUnsubscriptionRequest} message.
    * 
    * @param subscriptionIdentification
    */
   public void sendUnsubscription(final String subscriptionIdentification) {
      ESFUnsubscriptionRequest unsubRequest = new ESFUnsubscriptionRequest(subscriptionIdentification);

      // send ESFUnsubscriptionRequest
      LOG.debug("Send ESFUnsubscriptionRequest message");
      communicator.sendAsync(unsubRequest);
   }
}
