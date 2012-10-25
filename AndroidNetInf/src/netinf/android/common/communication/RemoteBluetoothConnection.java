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
package netinf.android.common.communication;

import java.util.ArrayList;
import java.util.List;

import netinf.common.communication.Communicator;
import netinf.common.communication.NetInfDeletedIOException;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DeleteMode;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetRequest;
import netinf.common.messages.RSGetResponse;
import netinf.common.messages.SCGetByQueryTemplateRequest;
import netinf.common.messages.SCGetBySPARQLRequest;
import netinf.common.messages.SCGetTimeoutAndNewSearchIDRequest;
import netinf.common.messages.SCGetTimeoutAndNewSearchIDResponse;
import netinf.common.messages.SCSearchResponse;
import netinf.common.messages.TCChangeTransferRequest;
import netinf.common.messages.TCChangeTransferResponse;
import netinf.common.messages.TCStartTransferRequest;
import netinf.common.messages.TCStartTransferResponse;
import netinf.common.search.DefinedQueryTemplates;
import netinf.common.transfer.TransferJob;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Simplifies common communication patterns between two NetInf Nodes or between an application and a NetInf Node
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class RemoteBluetoothConnection implements NetInfNodeConnection {

   private Communicator communicator;
   private Provider<Communicator> communicatorProvider;

   private static final Logger LOG = Logger.getLogger(RemoteBluetoothConnection.class);

   @Inject
   public void setCommunicatorProvider(Provider<Communicator> communicatorProvider) {
      LOG.trace(null);
      this.communicatorProvider = communicatorProvider;
   }

    /**
     * Initiates the connection to a NetInf Node If connection fails an
     * exception will be thrown
     * 
     * @throws NetInfCheckedException
     */
  public void setupCommunicator(BluetoothConnection newConnection) throws NetInfCheckedException {
      LOG.trace(null);
            this.communicator = this.communicatorProvider.get();
            this.communicator.setSerializeFormat(SerializeFormat.JAVA);
            this.communicator.setConnection(newConnection);
   }


   /**
    * Closes the connection to the remote NetInf Node
    */
   public void tearDown() {
      LOG.trace(null);
      if (this.communicator != null) {
         try {
            LOG.log(DemoLevel.DEMO, "(CCOMM) Disconnecting from node");
            LOG.info("Disconnecting from node");
            this.communicator.close();
         } catch (NetInfCheckedException e) {
            LOG.warn("Soft tearing down failed, doing it the hard way", e);
         }
         this.communicator = null;
      }
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#getIOs(netinf.common.datamodel.Identifier)
    */
   public ArrayList<InformationObject> getIOs(Identifier identifier) throws NetInfCheckedException {
      LOG.trace(null);
      return getIOs(identifier, null, null);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#getIOs(netinf.common.datamodel.Identifier, java.lang.String,
    * java.lang.String)
    */
   public ArrayList<InformationObject> getIOs(Identifier identifier, String userName, String privateKey)
   throws NetInfCheckedException {
      LOG.trace(null);
//      setupCommunicator();

      LOG.log(DemoLevel.DEMO, "(CCOMM) Requesting any IO that " + identifier.describe() + " from node");
      RSGetRequest request = new RSGetRequest(identifier);
      request.setUserName(userName);
      request.setPrivateKey(privateKey);
      this.communicator.send(request);
      NetInfMessage message = this.communicator.receive();

      if (message.getErrorMessage() != null) {
         throw new NetInfCheckedException(message.getErrorMessage());
      }

      if (message instanceof RSGetResponse) {

         // TODO check for errors in message?
         ArrayList<InformationObject> incomingIOs = ((RSGetResponse) message).getInformationObjects();
         ArrayList<InformationObject> successfullyCheckedIOs = new ArrayList<InformationObject>();

         LOG.log(DemoLevel.DEMO, "(CCOMM) Received " + (incomingIOs.size() == 1 ? "1 IO" : incomingIOs.size() + " IOs"));
         LOG.info("Received " + (incomingIOs.size() == 1 ? "1 IO" : incomingIOs.size() + " IOs"));


         successfullyCheckedIOs = incomingIOs;
         

         if (successfullyCheckedIOs.size() < 1) {
            LOG.info("None of the InformationObjects was checked for Security properties successfully.");
            throw new NetInfCheckedException("None of the InformationObjects was checked for Security properties successfully.");
         }

         for (InformationObject io : successfullyCheckedIOs) {
            LOG.log(DemoLevel.DEMO, "(CCOMM) Received " + io.describe());
         }
         return successfullyCheckedIOs;
      }

      throw new NetInfCheckedException("wrong answer type for RSGetRequest: " + message.getClass().getSimpleName());
   }

   
   public InformationObject getIO(Identifier identifier, BluetoothConnection newConnection) throws NetInfCheckedException {
	      LOG.trace(null);
	      ArrayList<InformationObject> informationObjects = getIOs(identifier, newConnection);

	      if (informationObjects.size() != 0) {
	         InformationObject io = informationObjects.get(0);
	         if (!io.getAttribute(DefinedAttributeIdentification.DELETE.getURI()).isEmpty()) {
	            throw new NetInfDeletedIOException("This IO has been deleted!");
	         }
	         return informationObjects.get(0);
	      }

	      return null;
	   }
   
   
   
   public ArrayList<InformationObject> getIOs(Identifier identifier, BluetoothConnection newConnection)
   throws NetInfCheckedException {
      LOG.trace(null);
      setupCommunicator(newConnection);

      LOG.log(DemoLevel.DEMO, "(CCOMM) Requesting any IO that " + identifier.describe() + " from node");
      RSGetRequest request = new RSGetRequest(identifier);

      this.communicator.send(request);
      NetInfMessage message = this.communicator.receive();

      if (message.getErrorMessage() != null) {
         throw new NetInfCheckedException(message.getErrorMessage());
      }

      if (message instanceof RSGetResponse) {

         // TODO check for errors in message?
         ArrayList<InformationObject> incomingIOs = ((RSGetResponse) message).getInformationObjects();
         if (incomingIOs.size() < 1) {
            LOG.info("None of the InformationObjects was checked for Security properties successfully.");
            throw new NetInfCheckedException("None of the InformationObjects was checked for Security properties successfully.");
         }

         for (InformationObject io : incomingIOs) {
            LOG.log(DemoLevel.DEMO, "(CCOMM) Received " + io.describe());
         }
         return incomingIOs;
      }

      throw new NetInfCheckedException("wrong answer type for RSGetRequest: " + message.getClass().getSimpleName());
   }
   
   
   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#getIO(netinf.common.datamodel.Identifier)
    */
   public InformationObject getIO(Identifier identifier) throws NetInfCheckedException {
      LOG.trace(null);
      return getIO(identifier, null, null);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#getIO(netinf.common.datamodel.Identifier, java.lang.String,
    * java.lang.String)
    */
   public InformationObject getIO(Identifier identifier, String userName, String privateKey) throws NetInfCheckedException {
      LOG.trace(null);
      ArrayList<InformationObject> informationObjects = getIOs(identifier, userName, privateKey);

      if (informationObjects.size() != 0) {
         InformationObject io = informationObjects.get(0);
         if (!io.getAttribute(DefinedAttributeIdentification.DELETE.getURI()).isEmpty()) {
            throw new NetInfDeletedIOException("This IO has been deleted!");
         }
         return informationObjects.get(0);
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#putIO(netinf.common.datamodel.InformationObject)
    */
   public void putIO(InformationObject informationObject) throws NetInfCheckedException {
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#putIO(netinf.common.datamodel.InformationObject, java.lang.String,
    * java.lang.String)
    */
   public void putIO(InformationObject informationObject, String userName, String privateKey) throws NetInfCheckedException {

   }

   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#deleteIO(netinf.common.datamodel.InformationObject,
    * netinf.common.datamodel.DeleteMode)
    */
   public void deleteIO(InformationObject informationObject, DeleteMode deleteMode) throws NetInfCheckedException {

   }

   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#performSearch(java.lang.String, int)
    */
   public List<Identifier> performSearch(String sparqlQuery, int desiredTimeout) throws NetInfCheckedException {
      LOG.trace(null);
//      setupCommunicator();

      LOG.log(DemoLevel.DEMO, "(CCOMM) Asking the node for a searchID and requesting timeout " + desiredTimeout);
      LOG.info("Asking the node for a searchID and requesting timeout " + desiredTimeout);

      this.communicator.send(new SCGetTimeoutAndNewSearchIDRequest(desiredTimeout));
      NetInfMessage message = this.communicator.receive();

      if (message.getErrorMessage() != null) {
         throw new NetInfCheckedException(message.getErrorMessage());
      }

      int searchID;
      int usedTimeout;
      if (message instanceof SCGetTimeoutAndNewSearchIDResponse) {
         searchID = ((SCGetTimeoutAndNewSearchIDResponse) message).getSearchID();
         usedTimeout = ((SCGetTimeoutAndNewSearchIDResponse) message).getUsedTimeout();
         LOG.debug("got search ID " + searchID);
         LOG.debug("used timeout will be " + usedTimeout);
         LOG.log(DemoLevel.DEMO, "(CCOMM) Got ID " + searchID + ", timeout will be " + usedTimeout);
      } else {
         throw new NetInfCheckedException("wrong answer type for SCGetNewSearchIDRequest: " + message.getClass().getSimpleName());
      }

      LOG.log(DemoLevel.DEMO, "(CCOMM) Sending actual search Query \"" + sparqlQuery + "\"");
      this.communicator.send(new SCGetBySPARQLRequest(sparqlQuery, searchID));
      message = this.communicator.receive();

      if (message.getErrorMessage() != null) {
         throw new NetInfCheckedException(message.getErrorMessage());
      }

      List<Identifier> results = new ArrayList<Identifier>();
      if (message instanceof SCSearchResponse) {
         results = ((SCSearchResponse) message).getResultIdentifiers();
         LOG.log(DemoLevel.DEMO, "(CCOMM) Received " + (results.size() == 1 ? "1 result" : results.size() + " results"));
         LOG.info("Received " + (results.size() == 1 ? "1 result" : results.size() + " results"));
      } else {
         throw new NetInfCheckedException("wrong answer type for SCGetBySPARQLRequest: " + message.getClass().getSimpleName());
      }
      if (results.isEmpty()) {
         return null;
      }
      return results;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.communication.NetInfNodeConnection#performSearch(netinf.common.search.DefinedQueryTemplates,
    * java.lang.String[], int)
    */
   public List<Identifier> performSearch(DefinedQueryTemplates type, String[] parameters, int desiredTimeout)
   throws NetInfCheckedException {
      LOG.trace(null);
//      setupCommunicator();

      LOG.log(DemoLevel.DEMO, "(CCOMM) Asking the node for a searchID and requesting timeout " + desiredTimeout);
      LOG.info("Asking the node for a searchID and requesting timeout " + desiredTimeout);

      this.communicator.send(new SCGetTimeoutAndNewSearchIDRequest(desiredTimeout));
      NetInfMessage message = this.communicator.receive();

      if (message.getErrorMessage() != null) {
         throw new NetInfCheckedException(message.getErrorMessage());
      }

      int searchID;
      int usedTimeout;
      if (message instanceof SCGetTimeoutAndNewSearchIDResponse) {
         searchID = ((SCGetTimeoutAndNewSearchIDResponse) message).getSearchID();
         usedTimeout = ((SCGetTimeoutAndNewSearchIDResponse) message).getUsedTimeout();
         LOG.debug("got search ID " + searchID);
         LOG.debug("used timeout will be " + usedTimeout);
         LOG.log(DemoLevel.DEMO, "(CCOMM) Got ID " + searchID + ", timeout will be " + usedTimeout);
      } else {
         throw new NetInfCheckedException("wrong answer type for SCGetNewSearchIDRequest: " + message.getClass().getSimpleName());
      }

      LOG.log(DemoLevel.DEMO, "(CCOMM) Sending actual search Query via template " + type.getQueryTemplateName() + " with "
            + parameters.length + " Parameters");
      SCGetByQueryTemplateRequest request = new SCGetByQueryTemplateRequest(type.getQueryTemplateName(), searchID);
      for (int i = 0; i < parameters.length; i++) {
         request.addParameter(parameters[i]);
      }

      this.communicator.send(request);
      message = this.communicator.receive();

      if (message.getErrorMessage() != null) {
         throw new NetInfCheckedException(message.getErrorMessage());
      }

      List<Identifier> results = new ArrayList<Identifier>();
      if (message instanceof SCSearchResponse) {
         results = ((SCSearchResponse) message).getResultIdentifiers();
         LOG.log(DemoLevel.DEMO, "(CCOMM) Received " + (results.size() == 1 ? "1 result" : results.size() + " results"));
         LOG.info("Received " + (results.size() == 1 ? "1 result" : results.size() + " results"));
      } else {
         throw new NetInfCheckedException("wrong answer type for SCGetByQueryTemplateRequest: "
               + message.getClass().getSimpleName());
      }
      if (results.isEmpty()) {
         return null;
      }
      return results;
   }

   /**
    * @param source
    * @param destination
    *           might be set to <code>null</code>. Then the according TransferService infers the destination.
    * @return
    * @throws NetInfCheckedException
    */
   public TransferJob startTransfer(String source, String destination) throws NetInfCheckedException {
      LOG.trace(null);
//      setupCommunicator();

      TCStartTransferRequest tcStartTransferRequest = new TCStartTransferRequest();
      tcStartTransferRequest.setSource(source);
      tcStartTransferRequest.setDestination(destination);

      // The Node should decide which TransferService to use.
      tcStartTransferRequest.setTransferServiceToUse(null);

      LOG.log(DemoLevel.DEMO, "(CCOMM) Initiating transfer from \"" + source + "\" to " + destination + "\"");
      this.communicator.send(tcStartTransferRequest);
      NetInfMessage message = this.communicator.receive();

      if (message.getErrorMessage() != null) {
         LOG.error("Received message with error: '" + message.getErrorMessage() + "'");
         throw new NetInfUncheckedException(message.getErrorMessage());
      }

      if (!(message instanceof TCStartTransferResponse)) {
         LOG.error("Received message of kind '" + message.getClass().getCanonicalName()
               + "', But expected to receive TCStartTransferResponse");
         throw new NetInfUncheckedException("Wrong Message received");
      }

      TCStartTransferResponse tcStartTransferResponse = (TCStartTransferResponse) message;

      LOG.log(DemoLevel.DEMO, "(CCOMM) Received job ID " + tcStartTransferResponse.getJobId());
      if (!(tcStartTransferResponse.getSource().equals(source) && tcStartTransferResponse.getDestination().equals(destination))) {
         LOG.log(DemoLevel.DEMO, "(CCOMM) Transferring from \"" + tcStartTransferResponse.getSource() + "\" to \""
               + tcStartTransferResponse.getDestination() + "\" now");
      }
      TransferJob result = new TransferJob(tcStartTransferResponse.getJobId(), tcStartTransferResponse.getSource(),
            tcStartTransferResponse.getDestination());
      return result;
   }

   /**
    * @param jobId
    * @param newDestination
    *           might be set to <code>null</code>. Then the according TransferService infers the destination.
    * @return
    * @throws NetInfCheckedException
    */
   public TransferJob changeTransfer(String jobId, String newDestination, boolean proceed) throws NetInfCheckedException {
      LOG.trace(null);
//      setupCommunicator();

      LOG.log(DemoLevel.DEMO, "(CCOMM) Changing transfer with ID " + jobId + " to \"" + newDestination + ". "
            + (proceed ? "Resuming" : "Restarting") + " transfer");

      TCChangeTransferRequest tcChangeTransferRequest = new TCChangeTransferRequest();
      tcChangeTransferRequest.setJobId(jobId);
      tcChangeTransferRequest.setNewDestination(newDestination);
      tcChangeTransferRequest.setProceed(proceed);

      this.communicator.send(tcChangeTransferRequest);

      NetInfMessage message = this.communicator.receive();

      if (message.getErrorMessage() != null) {
         LOG.error("Received message with error: '" + message.getErrorMessage() + "'");
         throw new NetInfUncheckedException(message.getErrorMessage());
      }

      if (!(message instanceof TCChangeTransferResponse)) {
         LOG.error("Received message of kind '" + message.getClass().getCanonicalName()
               + "', But expected to receive TCChangeTransferResponse");
         throw new NetInfUncheckedException("Wrong Message received");
      }

      TCChangeTransferResponse tcChangeTransferResponse = (TCChangeTransferResponse) message;

      TransferJob result = new TransferJob(tcChangeTransferResponse.getJobId(), tcChangeTransferResponse.getSource(),
            tcChangeTransferResponse.getNewDestination());

      return result;
   }
}
