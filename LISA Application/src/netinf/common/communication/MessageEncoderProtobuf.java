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

import java.util.ArrayList;
import java.util.List;

import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFEventMessage;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFFetchMissedEventsRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFFetchMissedEventsResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFRegistrationRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFRegistrationResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFSubscriptionRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFSubscriptionResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFUnsubscriptionRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoESFUnsubscriptionResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoNetInfMessage;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSGetNameRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSGetNameResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSGetPriorityRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSGetPriorityResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSGetRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSGetResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSGetServicesRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSGetServicesResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSPutRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoRSPutResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoSCGetByQueryTemplateRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoSCGetBySPARQLRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoSCGetTimeoutAndNewSearchIDRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoSCGetTimeoutAndNewSearchIDResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoSCSearchResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoSerializeFormat;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoTCChangeTransferRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoTCChangeTransferResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoTCGetServicesRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoTCGetServicesResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoTCStartTransferRequest;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoTCStartTransferResponse;
import netinf.common.communication.protobuf.ProtobufMessages.ProtoNetInfMessage.Builder;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsRequest;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.messages.ESFRegistrationRequest;
import netinf.common.messages.ESFRegistrationResponse;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.common.messages.ESFSubscriptionResponse;
import netinf.common.messages.ESFUnsubscriptionRequest;
import netinf.common.messages.ESFUnsubscriptionResponse;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetNameRequest;
import netinf.common.messages.RSGetNameResponse;
import netinf.common.messages.RSGetPriorityRequest;
import netinf.common.messages.RSGetPriorityResponse;
import netinf.common.messages.RSGetRequest;
import netinf.common.messages.RSGetResponse;
import netinf.common.messages.RSGetServicesRequest;
import netinf.common.messages.RSGetServicesResponse;
import netinf.common.messages.RSPutRequest;
import netinf.common.messages.RSPutResponse;
import netinf.common.messages.SCGetByQueryTemplateRequest;
import netinf.common.messages.SCGetBySPARQLRequest;
import netinf.common.messages.SCGetTimeoutAndNewSearchIDRequest;
import netinf.common.messages.SCGetTimeoutAndNewSearchIDResponse;
import netinf.common.messages.SCSearchResponse;
import netinf.common.messages.TCChangeTransferRequest;
import netinf.common.messages.TCChangeTransferResponse;
import netinf.common.messages.TCGetServicesRequest;
import netinf.common.messages.TCGetServicesResponse;
import netinf.common.messages.TCStartTransferRequest;
import netinf.common.messages.TCStartTransferResponse;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Encodes/decodes NetInfMessages to/from Google Protocol Buffer messages
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class MessageEncoderProtobuf extends MessageEncoderAbstract {
   public static final int ENCODER_ID = 1;

   @Override
   public int getUniqueEncoderId() {
      return ENCODER_ID;
   }

   @Override
   public byte[] encodeMessage(NetInfMessage m) {
      ProtoNetInfMessage.Builder builder;

      if (m instanceof RSGetRequest) {
         builder = encodeRSGetRequest((RSGetRequest) m);
      } else if (m instanceof RSGetResponse) {
         builder = encodeRSGetResponse((RSGetResponse) m);
      } else if (m instanceof RSPutRequest) {
         builder = encodeRSPutRequest((RSPutRequest) m);
      } else if (m instanceof RSPutResponse) {
         builder = encodeRSPutResponse((RSPutResponse) m);
      } else if (m instanceof RSGetNameRequest) {
         builder = encodeRSGetNameRequest((RSGetNameRequest) m);
      } else if (m instanceof RSGetNameResponse) {
         builder = encodeRSGetNameResponse((RSGetNameResponse) m);
      } else if (m instanceof RSGetPriorityRequest) {
         builder = encodeRSGetPriorityRequest((RSGetPriorityRequest) m);
      } else if (m instanceof RSGetPriorityResponse) {
         builder = encodeRSGetPriorityResponse((RSGetPriorityResponse) m);
      } else if (m instanceof RSGetServicesRequest) {
         builder = encodeRSGetServicesRequest((RSGetServicesRequest) m);
      } else if (m instanceof RSGetServicesResponse) {
         builder = encodeRSGetServicesResponse((RSGetServicesResponse) m);
      } else if (m instanceof ESFRegistrationRequest) {
         builder = encodeESFRegistrationRequest((ESFRegistrationRequest) m);
      } else if (m instanceof ESFRegistrationResponse) {
         builder = encodeESFRegistrationResponse((ESFRegistrationResponse) m);
      } else if (m instanceof ESFFetchMissedEventsRequest) {
         builder = encodeESFFetchMissedEventsRequest((ESFFetchMissedEventsRequest) m);
      } else if (m instanceof ESFFetchMissedEventsResponse) {
         builder = encodeESFFetchMissedEventsResponse((ESFFetchMissedEventsResponse) m);
      } else if (m instanceof ESFEventMessage) {
         builder = encodeESFEventMessage((ESFEventMessage) m);
      } else if (m instanceof ESFSubscriptionRequest) {
         builder = encodeESFSubscriptionRequest((ESFSubscriptionRequest) m);
      } else if (m instanceof ESFSubscriptionResponse) {
         builder = encodeESFSubscriptionResponse((ESFSubscriptionResponse) m);
      } else if (m instanceof ESFUnsubscriptionRequest) {
         builder = encodeESFUnsubscriptionRequest((ESFUnsubscriptionRequest) m);
      } else if (m instanceof ESFUnsubscriptionResponse) {
         builder = encodeESFUnsubscriptionResponse((ESFUnsubscriptionResponse) m);
      } else if (m instanceof TCGetServicesRequest) {
         builder = encodeTCGetServicesRequest((TCGetServicesRequest) m);
      } else if (m instanceof TCGetServicesResponse) {
         builder = encodeTCGetServicesResponse((TCGetServicesResponse) m);
      } else if (m instanceof TCStartTransferRequest) {
         builder = encodeTCStartTransferRequest((TCStartTransferRequest) m);
      } else if (m instanceof TCStartTransferResponse) {
         builder = encodeTCStartTransferResponse((TCStartTransferResponse) m);
      } else if (m instanceof TCChangeTransferRequest) {
         builder = encodeTCChangeTransferRequest((TCChangeTransferRequest) m);
      } else if (m instanceof TCChangeTransferResponse) {
         builder = encodeTCChangeTransferResponse((TCChangeTransferResponse) m);
      } else if (m instanceof SCGetByQueryTemplateRequest) {
         builder = encodeSCGetByQueryTemplateRequest((SCGetByQueryTemplateRequest) m);
      } else if (m instanceof SCGetBySPARQLRequest) {
         builder = encodeSCGetBySPARQLRequest((SCGetBySPARQLRequest) m);
      } else if (m instanceof SCSearchResponse) {
         builder = encodeSCSearchResponse((SCSearchResponse) m);
      } else if (m instanceof SCGetTimeoutAndNewSearchIDRequest) {
         builder = encodeSCGetTimeoutAndNewSearchIDRequest((SCGetTimeoutAndNewSearchIDRequest) m);
      } else if (m instanceof SCGetTimeoutAndNewSearchIDResponse) {
         builder = encodeSCGetTimeoutAndNewSearchIDResponse((SCGetTimeoutAndNewSearchIDResponse) m);
      } else {
         throw new NetInfUncheckedException("Don't know how to encode this NetInfMessage");
      }

      builder.setSerializeFormat(convertSerializeFormat(m.getSerializeFormat()));

      if (m.getErrorMessage() != null) {
         builder.setErrorMessage(m.getErrorMessage());
      }

      if (m.getUserName() != null) {
         builder.setUserName(m.getUserName());
      }

      if (m.getPrivateKey() != null) {
         builder.setPrivateKey(m.getPrivateKey());
      }

      return builder.build().toByteArray();
   }

   private ProtoNetInfMessage.Builder encodeRSGetRequest(RSGetRequest m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoRSGetRequest.Builder builder = ProtoRSGetRequest.newBuilder();

      builder.setIdentifier(serializeObject(m.getIdentifier(), serializeFormat));
      if (m.getResolutionServicesToUse() != null) {
         for (ResolutionServiceIdentityObject rsIdentityObject : m.getResolutionServicesToUse()) {
            builder.addResolutionServicesToUse(serializeObject(rsIdentityObject, serializeFormat));
         }
      }

      if (m.isFetchAllVersions()) {
         builder.setFetchAllVersions(true);
      }

      if (m.isDownloadBinaryObject()) {
         builder.setDownloadBinaryObject(true);
      } else {
         builder.setDownloadBinaryObject(false);
      }

      return ProtoNetInfMessage.newBuilder().setRSGetRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSGetResponse(RSGetResponse m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoRSGetResponse.Builder builder = ProtoRSGetResponse.newBuilder();

      for (InformationObject io : m.getInformationObjects()) {
         builder.addInformationObjects(serializeObject(io, serializeFormat));
      }

      return ProtoNetInfMessage.newBuilder().setRSGetResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSPutRequest(RSPutRequest m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoRSPutRequest.Builder builder = ProtoRSPutRequest.newBuilder();
      builder.setInformationObject(serializeObject(m.getInformationObject(), serializeFormat));
      if (m.getResolutionServicesToUse() != null) {
         for (ResolutionServiceIdentityObject rsIdentity : m.getResolutionServicesToUse()) {
            builder.addResolutionServicesToUse(serializeObject(rsIdentity, serializeFormat));
         }
      }
      return ProtoNetInfMessage.newBuilder().setRSPutRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSPutResponse(RSPutResponse m) {
      ProtoRSPutResponse.Builder builder = ProtoRSPutResponse.newBuilder();
      return ProtoNetInfMessage.newBuilder().setRSPutResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSGetNameRequest(RSGetNameRequest m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoRSGetNameRequest.Builder builder = ProtoRSGetNameRequest.newBuilder();
      builder.setResolutionServiceIdentifier(serializeObject(m.getResolutionServiceIdentifier(), serializeFormat));
      return ProtoNetInfMessage.newBuilder().setRSGetNameRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSGetNameResponse(RSGetNameResponse m) {
      ProtoRSGetNameResponse.Builder builder = ProtoRSGetNameResponse.newBuilder();
      builder.setName(m.getName());
      return ProtoNetInfMessage.newBuilder().setRSGetNameResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSGetPriorityRequest(RSGetPriorityRequest m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoRSGetPriorityRequest.Builder builder = ProtoRSGetPriorityRequest.newBuilder();
      builder.setResolutionServiceIdentifier(serializeObject(m.getResolutionServiceIdentifier(), serializeFormat));
      return ProtoNetInfMessage.newBuilder().setRSGetPriorityRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSGetPriorityResponse(RSGetPriorityResponse m) {
      ProtoRSGetPriorityResponse.Builder builder = ProtoRSGetPriorityResponse.newBuilder();
      builder.setPriority(m.getPriority());
      return ProtoNetInfMessage.newBuilder().setRSGetPriorityResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSGetServicesRequest(RSGetServicesRequest m) {
      ProtoRSGetServicesRequest.Builder builder = ProtoRSGetServicesRequest.newBuilder();
      return ProtoNetInfMessage.newBuilder().setRSGetServicesRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeRSGetServicesResponse(RSGetServicesResponse m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoRSGetServicesResponse.Builder builder = ProtoRSGetServicesResponse.newBuilder();
      for (Identifier resolutionServiceIdentifier : m.getResolutionServices()) {
         builder.addResolutionServiceIdentifiers(serializeObject(resolutionServiceIdentifier, serializeFormat));
      }
      return ProtoNetInfMessage.newBuilder().setRSGetServicesResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFRegistrationRequest(ESFRegistrationRequest m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoESFRegistrationRequest.Builder builder = ProtoESFRegistrationRequest.newBuilder();
      builder.setPersonObjectIdentifier(serializeObject(m.getPersonObjectIdentifier(), serializeFormat));

      if (m.getEventContainerIdentifier() != null) {
         builder.setEventContainerIdentifier(serializeObject(m.getEventContainerIdentifier(), serializeFormat));
      }

      return ProtoNetInfMessage.newBuilder().setESFRegistrationRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFRegistrationResponse(ESFRegistrationResponse m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoESFRegistrationResponse.Builder builder = ProtoESFRegistrationResponse.newBuilder();
      builder.setEventContainerIdentifier(serializeObject(m.getEventContainerIdentifier(), serializeFormat));

      return ProtoNetInfMessage.newBuilder().setESFRegistrationResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFFetchMissedEventsRequest(ESFFetchMissedEventsRequest m) {
      ProtoESFFetchMissedEventsRequest.Builder builder = ProtoESFFetchMissedEventsRequest.newBuilder();
      return ProtoNetInfMessage.newBuilder().setESFFetchMissedEventsRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFFetchMissedEventsResponse(ESFFetchMissedEventsResponse m) {
      ProtoESFFetchMissedEventsResponse.Builder builder = ProtoESFFetchMissedEventsResponse.newBuilder();

      for (ESFEventMessage eventMessage : m.getEventMessages()) {
         builder.addEventMessages(encodeESFEventMessage(eventMessage).getESFEventMessage());
      }

      return ProtoNetInfMessage.newBuilder().setESFFetchMissedEventsResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFEventMessage(ESFEventMessage m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoESFEventMessage.Builder builder = ProtoESFEventMessage.newBuilder();

      if (m.getMatchedSubscriptionIdentification() != null) {
         builder.setMatchedSubscriptionIdentification(m.getMatchedSubscriptionIdentification());
      }

      if (m.getOldInformationObject() != null) {
         builder.setOldInformationObject(serializeObject(m.getOldInformationObject(), serializeFormat));
      }

      if (m.getNewInformationObject() != null) {
         builder.setNewInformationObject(serializeObject(m.getNewInformationObject(), serializeFormat));
      }
     

      return ProtoNetInfMessage.newBuilder().setESFEventMessage(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFSubscriptionRequest(ESFSubscriptionRequest m) {
      ProtoESFSubscriptionRequest.Builder builder = ProtoESFSubscriptionRequest.newBuilder();
      builder.setSubscriptionIdentification(m.getSubscriptionIdentification());
      builder.setSparqlSubscription(m.getSparqlSubscription());
      builder.setExpires(m.getExpires());
      return ProtoNetInfMessage.newBuilder().setESFSubscriptionRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFSubscriptionResponse(ESFSubscriptionResponse m) {
      ProtoESFSubscriptionResponse.Builder builder = ProtoESFSubscriptionResponse.newBuilder();
      return ProtoNetInfMessage.newBuilder().setESFSubscriptionResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFUnsubscriptionRequest(ESFUnsubscriptionRequest m) {
      ProtoESFUnsubscriptionRequest.Builder builder = ProtoESFUnsubscriptionRequest.newBuilder();
      builder.setSubscriptionIdentification(m.getSubscriptionIdentification());
      return ProtoNetInfMessage.newBuilder().setESFUnsubscriptionRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeESFUnsubscriptionResponse(ESFUnsubscriptionResponse m) {
      ProtoESFUnsubscriptionResponse.Builder builder = ProtoESFUnsubscriptionResponse.newBuilder();
      return ProtoNetInfMessage.newBuilder().setESFUnsubscriptionResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeTCGetServicesRequest(TCGetServicesRequest m) {
      ProtoTCGetServicesRequest.Builder builder = ProtoTCGetServicesRequest.newBuilder();
      return ProtoNetInfMessage.newBuilder().setTCGetServicesRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeTCGetServicesResponse(TCGetServicesResponse m) {
      ProtoTCGetServicesResponse.Builder builder = ProtoTCGetServicesResponse.newBuilder();
      for (String transferIdentifierString : m.getTransferServices()) {
         builder.addTransferServices(transferIdentifierString);
      }
      return ProtoNetInfMessage.newBuilder().setTCGetServicesResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeTCChangeTransferRequest(TCChangeTransferRequest m) {
      ProtoTCChangeTransferRequest.Builder builder = ProtoTCChangeTransferRequest.newBuilder();

      builder.setJobId(m.getJobId());
      if (m.getNewDestination() != null) {
         builder.setNewDestination(m.getNewDestination());
      }
      builder.setProceed(m.isProceed());

      return ProtoNetInfMessage.newBuilder().setTCChangeTransferRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeTCChangeTransferResponse(TCChangeTransferResponse m) {
      ProtoTCChangeTransferResponse.Builder builder = ProtoTCChangeTransferResponse.newBuilder();

      builder.setJobId(m.getJobId());
      builder.setSource(m.getSource());

      if (m.getNewDestination() != null) {
         builder.setNewDestination(m.getNewDestination());
      }

      return ProtoNetInfMessage.newBuilder().setTCChangeTransferResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeTCStartTransferRequest(TCStartTransferRequest m) {
      ProtoTCStartTransferRequest.Builder builder = ProtoTCStartTransferRequest.newBuilder();

      builder.setSource(m.getSource());
      if (m.getDestination() != null) {
         builder.setDestination(m.getDestination());
      }
      if (m.getTransferServiceToUse() != null) {
         builder.setTransferServiceToUse(m.getTransferServiceToUse());
      }

      return ProtoNetInfMessage.newBuilder().setTCStartTransferRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeTCStartTransferResponse(TCStartTransferResponse m) {
      ProtoTCStartTransferResponse.Builder builder = ProtoTCStartTransferResponse.newBuilder();

      builder.setSource(m.getSource());

      if (m.getDestination() != null) {
         builder.setDestination(m.getDestination());
      }

      builder.setJobId(m.getJobId());

      return ProtoNetInfMessage.newBuilder().setTCStartTransferResponse(builder);
   }

   private Builder encodeSCGetByQueryTemplateRequest(SCGetByQueryTemplateRequest m) {
      ProtoSCGetByQueryTemplateRequest.Builder builder = ProtoSCGetByQueryTemplateRequest.newBuilder();
      builder.setType(m.getType());
      builder.setSearchID(m.getSearchID());
      for (String parameter : m.getParameters()) {
         builder.addParameters(parameter);
      }
      return ProtoNetInfMessage.newBuilder().setSCGetByQueryTemplateRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeSCGetBySPARQLRequest(SCGetBySPARQLRequest m) {
      ProtoSCGetBySPARQLRequest.Builder builder = ProtoSCGetBySPARQLRequest.newBuilder();
      builder.setRequest(m.getRequest());
      builder.setSearchID(m.getSearchID());
      return ProtoNetInfMessage.newBuilder().setSCGetBySPARQLRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeSCSearchResponse(SCSearchResponse m) {
      SerializeFormat serializeFormat = m.getSerializeFormat();
      ProtoSCSearchResponse.Builder builder = ProtoSCSearchResponse.newBuilder();
      for (Identifier resultIdentifier : m.getResultIdentifiers()) {
         builder.addResultIdentifiers(serializeObject(resultIdentifier, serializeFormat));
      }
      return ProtoNetInfMessage.newBuilder().setSCSearchResponse(builder);
   }

   private ProtoNetInfMessage.Builder encodeSCGetTimeoutAndNewSearchIDRequest(SCGetTimeoutAndNewSearchIDRequest m) {
      ProtoSCGetTimeoutAndNewSearchIDRequest.Builder builder = ProtoSCGetTimeoutAndNewSearchIDRequest.newBuilder();
      builder.setDesiredTimeout(m.getDesiredTimeout());
      return ProtoNetInfMessage.newBuilder().setSCGetTimeoutAndNewSearchIDRequest(builder);
   }

   private ProtoNetInfMessage.Builder encodeSCGetTimeoutAndNewSearchIDResponse(SCGetTimeoutAndNewSearchIDResponse m) {
      ProtoSCGetTimeoutAndNewSearchIDResponse.Builder builder = ProtoSCGetTimeoutAndNewSearchIDResponse.newBuilder();
      builder.setSearchID(m.getSearchID());
      builder.setUsedTimeout(m.getUsedTimeout());
      return ProtoNetInfMessage.newBuilder().setSCGetTimeoutAndNewSearchIDResponse(builder);
   }

   @Override
   public NetInfMessage decodeMessage(byte[] payload) {
      ProtoNetInfMessage m;

      try {
         m = ProtoNetInfMessage.parseFrom(payload);
      } catch (InvalidProtocolBufferException e) {
         throw new NetInfUncheckedException(e.getMessage());
      }

      NetInfMessage message;
      SerializeFormat serializeFormat = convertSerializeFormat(m.getSerializeFormat());

      if (m.hasRSGetRequest()) {
         message = decodeRSGetRequest(m.getRSGetRequest(), serializeFormat);
      } else if (m.hasRSGetResponse()) {
         message = decodeRSGetResponse(m.getRSGetResponse(), serializeFormat);
      } else if (m.hasRSPutRequest()) {
         message = decodeRSPutRequest(m.getRSPutRequest(), serializeFormat);
      } else if (m.hasRSPutResponse()) {
         message = decodeRSPutResponse(m.getRSPutResponse(), serializeFormat);
      } else if (m.hasRSGetNameRequest()) {
         message = decodeRSGetNameRequest(m.getRSGetNameRequest(), serializeFormat);
      } else if (m.hasRSGetNameResponse()) {
         message = decodeRSGetNameResponse(m.getRSGetNameResponse(), serializeFormat);
      } else if (m.hasRSGetPriorityRequest()) {
         message = decodeRSGetPriorityRequest(m.getRSGetPriorityRequest(), serializeFormat);
      } else if (m.hasRSGetPriorityResponse()) {
         message = decodeRSGetPriorityResponse(m.getRSGetPriorityResponse(), serializeFormat);
      } else if (m.hasRSGetServicesRequest()) {
         message = decodeRSGetServicesRequest(m.getRSGetServicesRequest(), serializeFormat);
      } else if (m.hasRSGetServicesResponse()) {
         message = decodeRSGetServicesResponse(m.getRSGetServicesResponse(), serializeFormat);
      } else if (m.hasESFRegistrationRequest()) {
         message = decodeESFRegistrationRequest(m.getESFRegistrationRequest(), serializeFormat);
      } else if (m.hasESFRegistrationResponse()) {
         message = decodeESFRegistrationResponse(m.getESFRegistrationResponse(), serializeFormat);
      } else if (m.hasESFFetchMissedEventsRequest()) {
         message = decodeESFFetchMissedEventsRequest(m.getESFFetchMissedEventsRequest(), serializeFormat);
      } else if (m.hasESFFetchMissedEventsResponse()) {
         message = decodeESFFetchMissedEventsResponse(m.getESFFetchMissedEventsResponse(), serializeFormat);
      } else if (m.hasESFEventMessage()) {
         message = decodeESFEventMessage(m.getESFEventMessage(), serializeFormat);
      } else if (m.hasESFSubscriptionRequest()) {
         message = decodeESFSubscriptionRequest(m.getESFSubscriptionRequest(), serializeFormat);
      } else if (m.hasESFSubscriptionResponse()) {
         message = decodeESFSubscriptionResponse(m.getESFSubscriptionResponse(), serializeFormat);
      } else if (m.hasESFUnsubscriptionRequest()) {
         message = decodeESFUnsubscriptionRequest(m.getESFUnsubscriptionRequest(), serializeFormat);
      } else if (m.hasESFUnsubscriptionResponse()) {
         message = decodeESFUnsubscriptionResponse(m.getESFUnsubscriptionResponse(), serializeFormat);
      } else if (m.hasTCGetServicesRequest()) {
         message = decodeTCGetServicesRequest(m.getTCGetServicesRequest(), serializeFormat);
      } else if (m.hasTCGetServicesResponse()) {
         message = decodeTCGetServicesResponse(m.getTCGetServicesResponse(), serializeFormat);
      } else if (m.hasTCStartTransferRequest()) {
         message = decodeTCStartTransferRequest(m.getTCStartTransferRequest(), serializeFormat);
      } else if (m.hasTCStartTransferResponse()) {
         message = decodeTCStartTransferResponse(m.getTCStartTransferResponse(), serializeFormat);
      } else if (m.hasTCChangeTransferRequest()) {
         message = decodeTCChangeTransferRequest(m.getTCChangeTransferRequest(), serializeFormat);
      } else if (m.hasTCChangeTransferResponse()) {
         message = decodeTCChangeTransferResponse(m.getTCChangeTransferResponse(), serializeFormat);
      } else if (m.hasSCGetByQueryTemplateRequest()) {
         message = decodeSCGetByQueryTemplateRequest(m.getSCGetByQueryTemplateRequest(), serializeFormat);
      } else if (m.hasSCGetBySPARQLRequest()) {
         message = decodeSCGetBySPARQLRequest(m.getSCGetBySPARQLRequest(), serializeFormat);
      } else if (m.hasSCSearchResponse()) {
         message = decodeSCSearchResponse(m.getSCSearchResponse(), serializeFormat);
      } else if (m.hasSCGetTimeoutAndNewSearchIDRequest()) {
         message = decodeSCGetNewSearchIDRequest(m.getSCGetTimeoutAndNewSearchIDRequest(), serializeFormat);
      } else if (m.hasSCGetTimeoutAndNewSearchIDResponse()) {
         message = decodeSCGetNewSearchIDResponse(m.getSCGetTimeoutAndNewSearchIDResponse(), serializeFormat);
      } else {
         throw new NetInfUncheckedException("Don't know how to decode this NetInfMessage");
      }

      message.setSerializeFormat(serializeFormat);

      if (m.hasErrorMessage()) {
         message.setErrorMessage(m.getErrorMessage());
      }

      if (m.hasUserName()) {
         message.setUserName(m.getUserName());
      }

      if (m.hasPrivateKey()) {
         message.setPrivateKey(m.getPrivateKey());
      }

      return message;
   }

   private NetInfMessage decodeESFSubscriptionRequest(ProtoESFSubscriptionRequest esfSubscriptionRequest,
         SerializeFormat serializeFormat) {
      ESFSubscriptionRequest message = new ESFSubscriptionRequest(esfSubscriptionRequest.getSubscriptionIdentification(),
            esfSubscriptionRequest.getSparqlSubscription(), esfSubscriptionRequest.getExpires());
      return message;
   }

   private NetInfMessage decodeESFSubscriptionResponse(ProtoESFSubscriptionResponse esfSubscriptionResponse,
         SerializeFormat serializeFormat) {
      ESFSubscriptionResponse message = new ESFSubscriptionResponse();
      return message;
   }

   private NetInfMessage decodeESFUnsubscriptionRequest(ProtoESFUnsubscriptionRequest esfUnsubscriptionRequest,
         SerializeFormat serializeFormat) {
      ESFUnsubscriptionRequest message = new ESFUnsubscriptionRequest(esfUnsubscriptionRequest.getSubscriptionIdentification());
      return message;
   }

   private NetInfMessage decodeESFUnsubscriptionResponse(ProtoESFUnsubscriptionResponse esfUnsubscriptionResponse,
         SerializeFormat serializeFormat) {
      ESFUnsubscriptionResponse message = new ESFUnsubscriptionResponse();
      return message;
   }

   private RSGetRequest decodeRSGetRequest(ProtoRSGetRequest proto, SerializeFormat serializeFormat) {
      Identifier identifier = (Identifier) unserializeObject(proto.getIdentifier(), serializeFormat);

      RSGetRequest message = new RSGetRequest(identifier);

      if (proto.hasFetchAllVersions()) {
         message.setFetchAllVersions(proto.getFetchAllVersions());
      }

      if (proto.hasDownloadBinaryObject()) {
         message.setDownloadBinaryObject(proto.getDownloadBinaryObject());
      }

      if (proto.getResolutionServicesToUseCount() != 0) {
         List<ResolutionServiceIdentityObject> resolutionServicesToUse = new ArrayList<ResolutionServiceIdentityObject>();
         for (int i = 0; i < proto.getResolutionServicesToUseCount(); i++) {
            ResolutionServiceIdentityObject rsIdentity = (ResolutionServiceIdentityObject) unserializeObject(proto
                  .getResolutionServicesToUse(i), serializeFormat);
            resolutionServicesToUse.add(rsIdentity);
         }

         message.setResolutionServicesToUse(resolutionServicesToUse);
      }

      return message;
   }

   private RSGetResponse decodeRSGetResponse(ProtoRSGetResponse proto, SerializeFormat serializeFormat) {
      RSGetResponse message = new RSGetResponse();

      int informationObjectsCount = proto.getInformationObjectsCount();
      for (int i = 0; i < informationObjectsCount; i++) {
         InformationObject io = (InformationObject) unserializeObject(proto.getInformationObjects(i), serializeFormat);
         message.addInformationObject(io);
      }

      return message;
   }

   private RSPutRequest decodeRSPutRequest(ProtoRSPutRequest proto, SerializeFormat serializeFormat) {
      InformationObject io = (InformationObject) unserializeObject(proto.getInformationObject(), serializeFormat);
      List<ResolutionServiceIdentityObject> resolutionServicesToUse = new ArrayList<ResolutionServiceIdentityObject>();
      for (int i = 0; i < proto.getResolutionServicesToUseCount(); i++) {
         ResolutionServiceIdentityObject rsIdentity = (ResolutionServiceIdentityObject) unserializeObject(proto
               .getResolutionServicesToUse(i), serializeFormat);
         resolutionServicesToUse.add(rsIdentity);
      }
      RSPutRequest message = new RSPutRequest(io, resolutionServicesToUse);
      return message;
   }

   private RSPutResponse decodeRSPutResponse(ProtoRSPutResponse proto, SerializeFormat serializeFormat) {
      RSPutResponse message = new RSPutResponse();
      return message;
   }

   private RSGetNameRequest decodeRSGetNameRequest(ProtoRSGetNameRequest proto, SerializeFormat serializeFormat) {
      Identifier resolutionServiceIdentifier = (Identifier) unserializeObject(proto.getResolutionServiceIdentifier(),
            serializeFormat);
      RSGetNameRequest message = new RSGetNameRequest(resolutionServiceIdentifier);
      return message;
   }

   private RSGetNameResponse decodeRSGetNameResponse(ProtoRSGetNameResponse proto, SerializeFormat serializeFormat) {
      RSGetNameResponse message = new RSGetNameResponse(proto.getName());
      return message;
   }

   private RSGetPriorityRequest decodeRSGetPriorityRequest(ProtoRSGetPriorityRequest proto, SerializeFormat serializeFormat) {
      Identifier resolutionServiceIdentifier = (Identifier) unserializeObject(proto.getResolutionServiceIdentifier(),
            serializeFormat);
      RSGetPriorityRequest message = new RSGetPriorityRequest(resolutionServiceIdentifier);
      return message;
   }

   private RSGetPriorityResponse decodeRSGetPriorityResponse(ProtoRSGetPriorityResponse proto, SerializeFormat serializeFormat) {
      RSGetPriorityResponse message = new RSGetPriorityResponse(proto.getPriority());
      return message;
   }

   private RSGetServicesRequest decodeRSGetServicesRequest(ProtoRSGetServicesRequest proto, SerializeFormat serializeFormat) {
      RSGetServicesRequest message = new RSGetServicesRequest();
      return message;
   }

   private RSGetServicesResponse decodeRSGetServicesResponse(ProtoRSGetServicesResponse proto, SerializeFormat serializeFormat) {
      RSGetServicesResponse message = new RSGetServicesResponse();
      for (int i = 0; i < proto.getResolutionServiceIdentifiersCount(); i++) {
         Identifier identifier = (Identifier) unserializeObject(proto.getResolutionServiceIdentifiers(i), serializeFormat);
         message.addResolutionService(identifier);
      }
      return message;
   }

   private ESFRegistrationRequest decodeESFRegistrationRequest(ProtoESFRegistrationRequest proto,
         SerializeFormat serializeFormat) {
      Identifier personObjectIdentifier = (Identifier) unserializeObject(proto.getPersonObjectIdentifier(), serializeFormat);

      ESFRegistrationRequest message = new ESFRegistrationRequest(personObjectIdentifier);

      if (proto.hasEventContainerIdentifier()) {
         Identifier eventContainerIdentifier = (Identifier) unserializeObject(proto.getEventContainerIdentifier(),
               serializeFormat);
         message.setEventContainerIdentifier(eventContainerIdentifier);
      }

      return message;
   }

   private ESFRegistrationResponse decodeESFRegistrationResponse(ProtoESFRegistrationResponse proto,
         SerializeFormat serializeFormat) {
      Identifier eventContainerIdentifier = (Identifier) unserializeObject(proto.getEventContainerIdentifier(), serializeFormat);

      ESFRegistrationResponse message = new ESFRegistrationResponse(eventContainerIdentifier);
      return message;
   }

   private ESFFetchMissedEventsRequest decodeESFFetchMissedEventsRequest(ProtoESFFetchMissedEventsRequest proto,
         SerializeFormat serializeFormat) {
      return new ESFFetchMissedEventsRequest();
   }

   private ESFFetchMissedEventsResponse decodeESFFetchMissedEventsResponse(ProtoESFFetchMissedEventsResponse proto,
         SerializeFormat serializeFormat) {
      ESFFetchMissedEventsResponse message = new ESFFetchMissedEventsResponse();

      for (int i = 0; i < proto.getEventMessagesCount(); i++) {
         message.addEventMessage(decodeESFEventMessage(proto.getEventMessages(i), serializeFormat));
      }

      return message;
   }

   private ESFEventMessage decodeESFEventMessage(ProtoESFEventMessage proto, SerializeFormat serializeFormat) {
      ESFEventMessage message = new ESFEventMessage();

      if (proto.hasMatchedSubscriptionIdentification()) {
         message.setMatchedSubscriptionIdentification(proto.getMatchedSubscriptionIdentification());
      }

      if (proto.getOldInformationObject() != null && proto.getOldInformationObject().size() != 0) {
         message
         .setOldInformationObject((InformationObject) unserializeObject(proto.getOldInformationObject(), serializeFormat));
      }

      if (proto.getNewInformationObject() != null && proto.getNewInformationObject().size() != 0) {
         message
         .setNewInformationObject((InformationObject) unserializeObject(proto.getNewInformationObject(), serializeFormat));
      }

      return message;
   }

   private TCGetServicesRequest decodeTCGetServicesRequest(ProtoTCGetServicesRequest proto, SerializeFormat serializeFormat) {
      TCGetServicesRequest message = new TCGetServicesRequest();
      return message;
   }

   private TCGetServicesResponse decodeTCGetServicesResponse(ProtoTCGetServicesResponse proto, SerializeFormat serializeFormat) {
      TCGetServicesResponse message = new TCGetServicesResponse();
      for (int i = 0; i < proto.getTransferServicesCount(); i++) {
         message.addTransferService(proto.getTransferServices(i));
      }
      return message;
   }

   private TCStartTransferRequest decodeTCStartTransferRequest(ProtoTCStartTransferRequest proto,
         SerializeFormat serializeFormat) {
      TCStartTransferRequest message = new TCStartTransferRequest();

      message.setSource(proto.getSource());

      if (proto.hasDestination()) {
         message.setDestination(proto.getDestination());
      }

      if (proto.hasTransferServiceToUse()) {
         message.setTransferServiceToUse(proto.getTransferServiceToUse());
      }

      return message;
   }

   private TCStartTransferResponse decodeTCStartTransferResponse(ProtoTCStartTransferResponse proto,
         SerializeFormat serializeFormat) {
      TCStartTransferResponse message = new TCStartTransferResponse();

      message.setSource(proto.getSource());
      message.setDestination(proto.getDestination());
      message.setJobId(proto.getJobId());

      return message;
   }

   private TCChangeTransferRequest decodeTCChangeTransferRequest(ProtoTCChangeTransferRequest proto,
         SerializeFormat serializeFormat) {
      TCChangeTransferRequest message = new TCChangeTransferRequest();

      message.setProceed(proto.getProceed());
      if (proto.hasNewDestination()) {
         message.setNewDestination(proto.getNewDestination());
      }
      message.setJobId(proto.getJobId());

      return message;
   }

   private TCChangeTransferResponse decodeTCChangeTransferResponse(ProtoTCChangeTransferResponse proto,
         SerializeFormat serializeFormat) {
      TCChangeTransferResponse message = new TCChangeTransferResponse();

      message.setSource(proto.getSource());
      message.setNewDestination(proto.getNewDestination());
      message.setJobId(proto.getJobId());

      return message;
   }

   private NetInfMessage decodeSCGetByQueryTemplateRequest(ProtoSCGetByQueryTemplateRequest proto,
         SerializeFormat serializeFormat) {
      SCGetByQueryTemplateRequest message = new SCGetByQueryTemplateRequest(proto.getType(), proto.getSearchID());

      for (int i = 0; i < proto.getParametersCount(); i++) {
         String parameter = proto.getParameters(i);
         message.addParameter(parameter);
      }

      return message;
   }

   private SCGetBySPARQLRequest decodeSCGetBySPARQLRequest(ProtoSCGetBySPARQLRequest proto, SerializeFormat serializeFormat) {
      SCGetBySPARQLRequest message = new SCGetBySPARQLRequest(proto.getRequest(), proto.getSearchID());
      return message;
   }

   private SCSearchResponse decodeSCSearchResponse(ProtoSCSearchResponse proto, SerializeFormat serializeFormat) {
      SCSearchResponse message = new SCSearchResponse();
      for (int i = 0; i < proto.getResultIdentifiersCount(); i++) {
         Identifier identifier = (Identifier) unserializeObject(proto.getResultIdentifiers(i), serializeFormat);
         message.addResultIdentifier(identifier);
      }
      return message;
   }

   private SCGetTimeoutAndNewSearchIDRequest decodeSCGetNewSearchIDRequest(ProtoSCGetTimeoutAndNewSearchIDRequest proto,
         SerializeFormat serializeFormat) {
      SCGetTimeoutAndNewSearchIDRequest message = new SCGetTimeoutAndNewSearchIDRequest(proto.getDesiredTimeout());
      return message;
   }

   private SCGetTimeoutAndNewSearchIDResponse decodeSCGetNewSearchIDResponse(ProtoSCGetTimeoutAndNewSearchIDResponse proto,
         SerializeFormat serializeFormat) {
      SCGetTimeoutAndNewSearchIDResponse message = new SCGetTimeoutAndNewSearchIDResponse(proto.getUsedTimeout(), proto
            .getSearchID());
      return message;
   }

   private SerializeFormat convertSerializeFormat(ProtoSerializeFormat proto) {
      if (proto.equals(ProtoSerializeFormat.RDF)) {
         return SerializeFormat.RDF;
      }
      return SerializeFormat.JAVA;
   }

   private ProtoSerializeFormat convertSerializeFormat(SerializeFormat serializeFormat) {
      if (serializeFormat.equals(SerializeFormat.RDF)) {
         return ProtoSerializeFormat.RDF;
      }
      return ProtoSerializeFormat.JAVA;
   }

   private ByteString serializeObject(NetInfObjectWrapper object, SerializeFormat serializeFormat) {
      return ByteString.copyFrom(serializeNetInfObjectToBytes(object, serializeFormat));
   }

   private NetInfObjectWrapper unserializeObject(ByteString bytes, SerializeFormat serializeFormat) {
      return unserializeNetInfObjectFromBytes(bytes.toByteArray(), serializeFormat);
   }
}
