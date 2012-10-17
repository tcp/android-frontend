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
package netinf.common.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.messages.ESFFetchMissedEventsRequest;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetRequest;
import netinf.common.messages.RSGetResponse;
import netinf.common.messages.RSMDHTAck;
import netinf.common.messages.TCChangeTransferRequest;
import netinf.common.messages.TCChangeTransferResponse;
import netinf.common.messages.TCStartTransferRequest;
import netinf.common.messages.TCStartTransferResponse;
import netinf.common.utils.Utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

/**
 * Encodes/decodes NetInfMessages to/from XML messages
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class MessageEncoderXML extends MessageEncoderAbstract {
   public static final int ENCODER_ID = 2;
   private static final Logger LOG = Logger.getLogger(MessageEncoderXML.class);

   private static final String EL_SERIALIZE_FORMAT = "SerializeFormat";
   private static final String EL_PRIVATE_KEY = "PrivateKey";
   private static final String EL_USER_NAME = "UserName";
   private static final String EL_ERROR_MESSAGE = "ErrorMessage";
   private static final String EL_IDENTIFIER = "Identifier";
   private static final String EL_FETCH_ALL_VERSIONS = "FetchAllVersions";
   private static final String EL_DOWNLOAD_BINARY_OBJECT = "DownloadBinaryObject";
   private static final String EL_RESOLUTION_SERVICES_TO_USE = "ResolutionServicesToUse";
   private static final String EL_INFORMATION_OBJECT = "InformationObject";
   private static final String EL_INFORMATION_OBJECTS = "InformationObjects";
   private static final String EL_IDENTITY = "Identity";
   private static final String EL_PROCEED = "Proceed";
   private static final String EL_NEW_DESTINATION = "NewDestination";
   private static final String EL_JOB_ID = "JobId";
   private static final String EL_TRANSFER_SERVICE_TO_USE = "TransferServiceToUse";
   private static final String EL_SOURCE = "Source";
   private static final String EL_DESTINATION = "Destination";
   private static final String VALUE_TRUE = "true";

   private DatamodelFactory datamodelFactory;

   @Inject
   public void injectDatamodelFactory(DatamodelFactory datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
   }

   @Override
   public int getUniqueEncoderId() {
      return ENCODER_ID;
   }

   @Override
   public byte[] encodeMessage(NetInfMessage m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();

      Document xml = new DocumentImpl();
      Element rootElement = xml.createElement(m.getClass().getSimpleName());
      xml.appendChild(rootElement);

      appendElementWithValue(xml, rootElement, EL_SERIALIZE_FORMAT, serializeFormat.toString());

      if (m.getUserName() != null) {
         appendElementWithValue(xml, rootElement, EL_USER_NAME, m.getUserName());
      }

      if (m.getPrivateKey() != null) {
         appendElementWithValue(xml, rootElement, EL_PRIVATE_KEY, m.getPrivateKey());
      }

      if (m.getErrorMessage() != null) {
         appendElementWithValue(xml, rootElement, EL_ERROR_MESSAGE, m.getErrorMessage());
      }

      if (m instanceof RSGetRequest) {
         encodeRSGetRequest(xml, (RSGetRequest) m, serializeFormat);
      } else if (m instanceof RSGetResponse) {
         encodeRSGetResponse(xml, (RSGetResponse) m, serializeFormat);
      } else if (m instanceof TCChangeTransferRequest) {
         encodeTCChangeTransferRequest(xml, (TCChangeTransferRequest) m, serializeFormat);
      } else if (m instanceof TCChangeTransferResponse) {
         encodeTCChangeTransferResponse(xml, (TCChangeTransferResponse) m, serializeFormat);
      } else if (m instanceof TCStartTransferRequest) {
         encodeTCStartTransferRequest(xml, (TCStartTransferRequest) m, serializeFormat);
      } else if (m instanceof TCStartTransferResponse) {
         encodeTCStartTransferResponse(xml, (TCStartTransferResponse) m, serializeFormat);
      } else if (m instanceof ESFFetchMissedEventsRequest) {
         encodeESFFetchMissedEventsRequest(xml, (ESFFetchMissedEventsRequest) m, serializeFormat);
      } else if (m instanceof ESFFetchMissedEventsResponse) {
         encodeESFFetchMissedEventsResponse(xml, (ESFFetchMissedEventsResponse) m, serializeFormat);
      } else if (m instanceof RSMDHTAck) {
          encodeRSMDHTAck(xml, (RSMDHTAck) m, serializeFormat);
      } else {
         throw new NetInfUncheckedException("Don't know how to encode this NetInfMessage");
      }

      return buildString(xml);
   }

   private void encodeRSMDHTAck(Document xml, RSMDHTAck m,
		SerializeFormat serializeFormat) {
	   
	   appendElementWithValue(xml, xml.getFirstChild(), EL_USER_NAME, m.getUserName().toString());
   }

   private void encodeESFFetchMissedEventsResponse(Document xml, ESFFetchMissedEventsResponse m, SerializeFormat serializeFormat) {
      appendElementWithValue(xml, xml.getFirstChild(), EL_PRIVATE_KEY, m.getPrivateKey().toString());
      appendElementWithValue(xml, xml.getFirstChild(), EL_USER_NAME, m.getUserName().toString());
      appendElementWithValue(xml, xml.getFirstChild(), EL_ERROR_MESSAGE, m.getErrorMessage().toString());

   }

   private void encodeESFFetchMissedEventsRequest(Document xml, ESFFetchMissedEventsRequest m, SerializeFormat serializeFormat) {
      appendElementWithValue(xml, xml.getFirstChild(), EL_PRIVATE_KEY, m.getPrivateKey().toString());
      appendElementWithValue(xml, xml.getFirstChild(), EL_USER_NAME, m.getUserName().toString());
      appendElementWithValue(xml, xml.getFirstChild(), EL_ERROR_MESSAGE, m.getErrorMessage().toString());

   }

   private void encodeRSGetRequest(Document xml, RSGetRequest m, SerializeFormat serializeFormat) {
      appendElementWithValue(xml, xml.getFirstChild(), EL_IDENTIFIER, m.getIdentifier().toString());

      if (m.isFetchAllVersions()) {
         appendElementWithValue(xml, xml.getFirstChild(), EL_FETCH_ALL_VERSIONS, VALUE_TRUE);
      }

      if (m.isDownloadBinaryObject()) {
         appendElementWithValue(xml, xml.getFirstChild(), EL_DOWNLOAD_BINARY_OBJECT, VALUE_TRUE);
      }

      if (m.getResolutionServicesToUse() != null) {
         Element resolutionServicesToUseElement = xml.createElement(EL_RESOLUTION_SERVICES_TO_USE);
         for (ResolutionServiceIdentityObject rsIdentityObject : m.getResolutionServicesToUse()) {
            String value = serializeObject(rsIdentityObject, serializeFormat);
            appendElementWithValue(xml, resolutionServicesToUseElement, EL_IDENTITY, value);
         }

         xml.getDocumentElement().appendChild(resolutionServicesToUseElement);
      }
   }

   private void encodeRSGetResponse(Document xml, RSGetResponse m, SerializeFormat serializeFormat) {
      Element informationObjectsElement = xml.createElement(EL_INFORMATION_OBJECTS);

      if (m.getInformationObjects() != null) {
         for (InformationObject io : m.getInformationObjects()) {
            String value = serializeObject(io, serializeFormat);
            appendElementWithValue(xml, informationObjectsElement, EL_INFORMATION_OBJECT, value);
         }
      }

      xml.getDocumentElement().appendChild(informationObjectsElement);
   }

   private void encodeTCChangeTransferRequest(Document xml, TCChangeTransferRequest m, SerializeFormat serializeFormat) {
      if (m.isProceed()) {
         appendElementWithValue(xml, xml.getFirstChild(), EL_PROCEED, VALUE_TRUE);
      }

      if (m.getNewDestination() != null) {
         appendElementWithValue(xml, xml.getFirstChild(), EL_NEW_DESTINATION, m.getNewDestination());
      }

      appendElementWithValue(xml, xml.getFirstChild(), EL_JOB_ID, m.getJobId());
   }

   private void encodeTCChangeTransferResponse(Document xml, TCChangeTransferResponse m, SerializeFormat serializeFormat) {
      appendElementWithValue(xml, xml.getFirstChild(), EL_SOURCE, m.getSource());

      if (m.getNewDestination() != null) {
         appendElementWithValue(xml, xml.getFirstChild(), EL_NEW_DESTINATION, m.getNewDestination());
      }

      appendElementWithValue(xml, xml.getFirstChild(), EL_JOB_ID, m.getJobId());
   }

   private void encodeTCStartTransferRequest(Document xml, TCStartTransferRequest m, SerializeFormat serializeFormat) {
      appendElementWithValue(xml, xml.getFirstChild(), EL_SOURCE, m.getSource());

      if (m.getDestination() != null) {
         appendElementWithValue(xml, xml.getFirstChild(), EL_DESTINATION, m.getDestination());
      }

      if (m.getTransferServiceToUse() != null) {
         appendElementWithValue(xml, xml.getFirstChild(), EL_TRANSFER_SERVICE_TO_USE, m.getTransferServiceToUse());
      }
   }

   private void encodeTCStartTransferResponse(Document xml, TCStartTransferResponse m, SerializeFormat serializeFormat) {
      appendElementWithValue(xml, xml.getFirstChild(), EL_SOURCE, m.getSource());

      if (m.getDestination() != null) {
         appendElementWithValue(xml, xml.getFirstChild(), EL_DESTINATION, m.getDestination());
      }

      appendElementWithValue(xml, xml.getFirstChild(), EL_JOB_ID, m.getJobId());
   }

   private static void appendElementWithValue(Document xml, Node node, String name, String value) {
      Element element = xml.createElement(name);
      element.setTextContent(value);
      node.appendChild(element);
   }

   @Override
   public NetInfMessage decodeMessage(byte[] payload) {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(payload);

      try {
         Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
         Element documentElement = xml.getDocumentElement();

         String messageName = documentElement.getNodeName();

         Node serializeFormatElement = getFirstElementByTagName(documentElement, EL_SERIALIZE_FORMAT);
         if (serializeFormatElement == null) {
            throw new NetInfUncheckedException("NetInfMessage lacks required field: " + EL_SERIALIZE_FORMAT);
         }

         SerializeFormat serializeFormat = SerializeFormat.getSerializeFormat(serializeFormatElement.getTextContent());

         NetInfMessage message;
         if (messageName.equals(RSGetRequest.class.getSimpleName())) {
            message = decodeRSGetRequest(documentElement, serializeFormat);
         } else if (messageName.equals(RSGetResponse.class.getSimpleName())) {
            message = decodeRSGetResponse(documentElement, serializeFormat);
         } else if (messageName.equals(TCChangeTransferRequest.class.getSimpleName())) {
            message = decodeTCChangeTransferRequest(documentElement, serializeFormat);
         } else if (messageName.equals(TCChangeTransferResponse.class.getSimpleName())) {
            message = decodeTCChangeTransferResponse(documentElement, serializeFormat);
         } else if (messageName.equals(TCStartTransferRequest.class.getSimpleName())) {
            message = decodeTCStartTransferRequest(documentElement, serializeFormat);
         } else if (messageName.equals(TCStartTransferResponse.class.getSimpleName())) {
            message = decodeTCStartTransferResponse(documentElement, serializeFormat);
         } else if (messageName.equals(ESFFetchMissedEventsResponse.class.getSimpleName())) {
            message = decodeESFFetchMissedEventsResponse(documentElement, serializeFormat);
         } else if (messageName.equals(ESFFetchMissedEventsRequest.class.getSimpleName())) {
            message = decodeESFFetchMissedEventsRequest(documentElement, serializeFormat);
         } else if (messageName.equals(RSMDHTAck.class.getSimpleName())) {
            message = decodeRSMDHTAck(documentElement, serializeFormat);
         } else {
            throw new NetInfUncheckedException("Don't know how to decode this NetInfMessage");
         }

         Node errorMessageElement = getFirstElementByTagName(documentElement, EL_ERROR_MESSAGE);
         if (errorMessageElement != null) {
            message.setErrorMessage(errorMessageElement.getTextContent());
         }

         message.setSerializeFormat(serializeFormat);

         Node userNameNode = getFirstElementByTagName(documentElement, EL_USER_NAME);
         if (userNameNode != null) {
            message.setUserName(userNameNode.getTextContent());
         }

         Node privateKeyNode = getFirstElementByTagName(documentElement, EL_PRIVATE_KEY);
         if (privateKeyNode != null) {
            message.setPrivateKey(privateKeyNode.getTextContent());
         }

         return message;
      } catch (SAXException e) {
         LOG.error(e.getMessage(), e);
         throw new NetInfUncheckedException(e);
      } catch (IOException e) {
         LOG.error(e.getMessage(), e);
         throw new NetInfUncheckedException(e);
      } catch (ParserConfigurationException e) {
         LOG.error(e.getMessage(), e);
         throw new NetInfUncheckedException(e);
      }
   }

   private NetInfMessage decodeRSMDHTAck(Node root,
		SerializeFormat serializeFormat) {
	   RSMDHTAck decodedMsg = new RSMDHTAck();
	   //Add any special processing instructions for this message here
	   return decodedMsg;
}

/**
    * Method is not yet 100% checked and correct. Use at your own risk
    * 
    * @param root
    * @param serializeFormat
    * @return Return ESFFetchMissedEventsRequest object decoded from the XML document
    */
   private ESFFetchMissedEventsRequest decodeESFFetchMissedEventsRequest(Node root, SerializeFormat serializeFormat) {
      ESFFetchMissedEventsRequest decodedMsg = new ESFFetchMissedEventsRequest();
      Node sourceNode = getFirstElementByTagName(root, EL_PRIVATE_KEY);
      if (sourceNode == null) {
         throw new NetInfUncheckedException("ESFFetchMissedEventsRequest lacks required field: " + EL_PRIVATE_KEY);
      }
      decodedMsg.setPrivateKey(sourceNode.getTextContent());
      Node errorMsgNode = getFirstElementByTagName(root, EL_ERROR_MESSAGE);
      if (errorMsgNode != null) {
         decodedMsg.setErrorMessage(errorMsgNode.getTextContent());
      }

      Node idNode = getFirstElementByTagName(root, EL_IDENTITY);
      if (idNode != null) {
         decodedMsg.setErrorMessage(errorMsgNode.getTextContent());
      }

      return decodedMsg;
   }

   private RSGetRequest decodeRSGetRequest(Node root, SerializeFormat serializeFormat) {
      Node identifierNode = getFirstElementByTagName(root, EL_IDENTIFIER);
      if (identifierNode == null) {
         throw new NetInfUncheckedException("RSGetRequest lacks required field: " + EL_IDENTIFIER);
      }
      Identifier identifier = this.datamodelFactory.createIdentifierFromString(identifierNode.getTextContent());

      RSGetRequest message = new RSGetRequest(identifier);

      Node fetchAllVersionsNode = getFirstElementByTagName(root, EL_FETCH_ALL_VERSIONS);
      if (fetchAllVersionsNode != null && fetchAllVersionsNode.getTextContent().equals(VALUE_TRUE)) {
         message.setFetchAllVersions(true);
      }

      Node downloadBinaryObjectNode = getFirstElementByTagName(root, EL_DOWNLOAD_BINARY_OBJECT);
      if (downloadBinaryObjectNode != null && downloadBinaryObjectNode.getTextContent().equals(VALUE_TRUE)) {
         message.setDownloadBinaryObject(true);
      }

      Node resolutionServicesToUseNode = getFirstElementByTagName(root, EL_RESOLUTION_SERVICES_TO_USE);
      if (resolutionServicesToUseNode != null) {
         NodeList childNodes = resolutionServicesToUseNode.getChildNodes();

         ArrayList<ResolutionServiceIdentityObject> rsIdentityObjects = new ArrayList<ResolutionServiceIdentityObject>();
         for (int i = 0; i < childNodes.getLength(); i++) {
            rsIdentityObjects.add((ResolutionServiceIdentityObject) unserializeObject(childNodes.item(i), serializeFormat));
         }

         message.setResolutionServicesToUse(rsIdentityObjects);
      }

      return message;
   }

   private NetInfMessage decodeRSGetResponse(Node root, SerializeFormat serializeFormat) {
      RSGetResponse message = new RSGetResponse();

      Node informationObjectsNode = getFirstElementByTagName(root, EL_INFORMATION_OBJECTS);
      if (informationObjectsNode != null) {
         NodeList childNodes = informationObjectsNode.getChildNodes();

         for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (EL_INFORMATION_OBJECT.equals(childNode.getNodeName())) {
               InformationObject io = (InformationObject) unserializeObject(childNodes.item(i), serializeFormat);
               message.addInformationObject(io);
            }
         }
      }

      return message;
   }

   private NetInfMessage decodeTCChangeTransferRequest(Node root, SerializeFormat serializeFormat) {
      TCChangeTransferRequest message = new TCChangeTransferRequest();

      Node proceedNode = getFirstElementByTagName(root, EL_PROCEED);
      if (proceedNode == null) {
         throw new NetInfUncheckedException("TCChangeTransferRequest lacks required field: " + EL_PROCEED);
      }
      message.setProceed(proceedNode.getTextContent().equals(VALUE_TRUE));

      Node newDestinationNode = getFirstElementByTagName(root, EL_NEW_DESTINATION);
      if (newDestinationNode != null) {
         message.setNewDestination(newDestinationNode.getTextContent());
      }

      Node jobIdNode = getFirstElementByTagName(root, EL_JOB_ID);
      if (jobIdNode == null) {
         throw new NetInfUncheckedException("TCChangeTransferRequest lacks required field: " + EL_JOB_ID);
      }
      message.setJobId(jobIdNode.getTextContent());

      return message;
   }

   private TCChangeTransferResponse decodeTCChangeTransferResponse(Node root, SerializeFormat serializeFormat) {
      TCChangeTransferResponse message = new TCChangeTransferResponse();

      Node sourceNode = getFirstElementByTagName(root, EL_SOURCE);
      if (sourceNode == null) {
         throw new NetInfUncheckedException("TCChangeTransferResponse lacks required field: " + EL_SOURCE);
      }
      message.setSource(sourceNode.getTextContent());

      Node newDestinationNode = getFirstElementByTagName(root, EL_NEW_DESTINATION);
      if (newDestinationNode != null) {
         message.setNewDestination(newDestinationNode.getTextContent());
      }

      Node jobIdNode = getFirstElementByTagName(root, EL_JOB_ID);
      if (jobIdNode == null) {
         throw new NetInfUncheckedException("TCChangeTransferResponse lacks required field: " + EL_JOB_ID);
      }
      message.setJobId(jobIdNode.getTextContent());

      return message;
   }

   private TCStartTransferRequest decodeTCStartTransferRequest(Node root, SerializeFormat serializeFormat) {
      TCStartTransferRequest message = new TCStartTransferRequest();

      Node sourceNode = getFirstElementByTagName(root, EL_SOURCE);
      if (sourceNode == null) {
         throw new NetInfUncheckedException("TCStartTransferRequest lacks required field: " + EL_SOURCE);
      }
      message.setSource(sourceNode.getTextContent());

      Node destinationNode = getFirstElementByTagName(root, EL_NEW_DESTINATION);
      if (destinationNode != null) {
         message.setDestination(destinationNode.getTextContent());
      }

      Node transferServiceToUseNode = getFirstElementByTagName(root, EL_TRANSFER_SERVICE_TO_USE);
      if (transferServiceToUseNode != null) {
         message.setTransferServiceToUse(transferServiceToUseNode.getTextContent());
      }

      return message;
   }

   private TCStartTransferResponse decodeTCStartTransferResponse(Node root, SerializeFormat serializeFormat) {
      TCStartTransferResponse message = new TCStartTransferResponse();

      Node sourceNode = getFirstElementByTagName(root, EL_SOURCE);
      if (sourceNode == null) {
         throw new NetInfUncheckedException("TCStartTransferResponse lacks required field: " + EL_SOURCE);
      }
      message.setSource(sourceNode.getTextContent());

      Node destinationNode = getFirstElementByTagName(root, EL_DESTINATION);
      if (destinationNode != null) {
         message.setDestination(destinationNode.getTextContent());
      }

      Node jobIdNode = getFirstElementByTagName(root, EL_JOB_ID);
      if (jobIdNode == null) {
         throw new NetInfUncheckedException("TCStartTransferResponse lacks required field: " + EL_JOB_ID);
      }
      message.setJobId(jobIdNode.getTextContent());

      return message;
   }

   /**
    * Method is not yet 100% checked and correct. Use at your own risk
    * 
    * @param root
    *           Root element of the XML document
    * @param serializeFormat
    * @return Returns an ESFFetchMissedEventsResponse object with the data read from the xml object
    */
   private ESFFetchMissedEventsResponse decodeESFFetchMissedEventsResponse(Node root, SerializeFormat serializeFormat) {
      ESFFetchMissedEventsResponse decodedMsg = new ESFFetchMissedEventsResponse();
      Node sourceNode = getFirstElementByTagName(root, EL_PRIVATE_KEY);
      if (sourceNode == null) {
         throw new NetInfUncheckedException("ESFFetchMissedEventsResponse lacks required field: " + EL_PRIVATE_KEY);
      }
      decodedMsg.setPrivateKey(sourceNode.getTextContent());
      Node errorMsgNode = getFirstElementByTagName(root, EL_ERROR_MESSAGE);
      if (errorMsgNode != null) {
         decodedMsg.setErrorMessage(errorMsgNode.getTextContent());
      }

      Node idNode = getFirstElementByTagName(root, EL_IDENTITY);
      if (idNode != null) {
         decodedMsg.setErrorMessage(errorMsgNode.getTextContent());
      }

      return decodedMsg;
   }

   private Node getFirstElementByTagName(Node xml, String tagName) {
      NodeList childNodes = xml.getChildNodes();

      for (int i = 0; i < childNodes.getLength(); i++) {
         Node childNode = childNodes.item(i);

         if (tagName.equals(childNode.getNodeName())) {
            return childNode;
         }
      }

      return null;
   }

   private byte[] buildString(Node xml) {
      try {
         TransformerFactory transfac = TransformerFactory.newInstance();
         Transformer trans = transfac.newTransformer();
         trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         trans.setOutputProperty(OutputKeys.INDENT, "yes");

         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         StreamResult result = new StreamResult(outputStream);
         DOMSource source = new DOMSource(xml);
         trans.transform(source, result);
         return outputStream.toByteArray();
      } catch (TransformerException e) {
         throw new NetInfUncheckedException(e);
      }
   }

   private String serializeObject(NetInfObjectWrapper object, SerializeFormat serializeFormat) {
      if (serializeFormat.equals(SerializeFormat.JAVA)) {
         return Base64.encodeBase64String(serializeNetInfObjectToBytes(object, serializeFormat));
      } else {
         return Utils.bytesToString(serializeNetInfObjectToBytes(object, serializeFormat));
      }
   }

   private NetInfObjectWrapper unserializeObject(Node node, SerializeFormat serializeFormat) {
      if (serializeFormat.equals(SerializeFormat.JAVA)) {
         return unserializeNetInfObjectFromBytes(Base64.decodeBase64(node.getTextContent()), serializeFormat);
      } else {
         return unserializeNetInfObjectFromBytes(buildString(node), serializeFormat);
      }
   }
}
