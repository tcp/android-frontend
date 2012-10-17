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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.gp.datamodel.Capability;
import netinf.node.gp.datamodel.GPFactory;
import netinf.node.gp.datamodel.Resolution;
import netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer;
import netinf.node.gp.messages.GPNetInfMessages.NIResolution;
import netinf.node.gp.messages.GPNetInfMessages.NIaddName;
import netinf.node.gp.messages.GPNetInfMessages.NImoveEP;
import netinf.node.gp.messages.GPNetInfMessages.NIprepareGP;
import netinf.node.gp.messages.GPNetInfMessages.NIresolve;
import netinf.node.gp.messages.GPNetInfMessages.NIresolveCallback;
import netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.NIMessageType;
import netinf.node.gp.messages.GPNetInfMessages.NIaddName.Builder;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.protobuf.GeneratedMessage;

/**
 * This is an interface to the GP world. It should be used as the only possibility to communicate with GP.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class GPNetInfInterfaceImpl implements GPNetInfInterface {

   public static final String NAME_PORT_SEPERATOR = ":";

   private static final Logger LOG = Logger.getLogger(GPNetInfInterfaceImpl.class);

   private final GPFactory gpFactory;

   private Socket connection;
   private DataInputStream in;
   private DataOutputStream out;

   private final Random callbackIdGenerator;
   private final GPListener gpListener;

   private final GPCommunicationBuffer gpCommunicationBuffer;

   @Inject
   public GPNetInfInterfaceImpl(GPFactory gpFactory, @Named("netinf.gp.interface.host") final String host,
         @Named("netinf.gp.interface.port") final String port, GPListener gpListener, GPCommunicationBuffer gpCommunicationBuffer) {
      LOG.trace("Creating gp-interface on port '" + port + "'");

      this.gpFactory = gpFactory;
      this.gpListener = gpListener;
      this.gpCommunicationBuffer = gpCommunicationBuffer;
      callbackIdGenerator = new Random();

      // Try to connect to our gp counterpart and start listener
      try {
         connection = new Socket(host, Integer.parseInt(port));

         this.in = new DataInputStream(connection.getInputStream());
         this.out = new DataOutputStream(connection.getOutputStream());

         // Start to listen, since the connection is know open
         gpListener.start(this);
      } catch (NumberFormatException e) {
         LOG.error("Could not connect to GP", e);
      } catch (UnknownHostException e) {
         LOG.error("Could not connect to GP", e);
      } catch (IOException e) {
         LOG.error("Could not connect to GP", e);
      } finally {
         if (in == null || out == null || connection == null) {
            throw new NetInfUncheckedException("Could not create GPNetInfInterface, since connection to GP not possible.");
         }
      }
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.gp.GPNetInfInterface#tearDown()
    */
   public void tearDown() {
      LOG.trace(null);

      try {
         in.close();
         out.close();
         connection.close();
         gpListener.stop();
      } catch (IOException e) {
         LOG.error("Could not tear down connection to GP", e);
      }
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.gp.GPNetInfInterface#addName(java.lang.String, java.util.List)
    */
   public void addName(String name, List<Capability> capabilities) {
      LOG.trace(null);

      Builder builder = NIaddName.newBuilder();
      builder.setName(name);

      for (Capability capability : capabilities) {
         builder.addCapabilities(capability.toProto());
      }

      NIaddName niAddName = builder.build();
      sendMessage(niAddName);
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.gp.GPNetInfInterface#resolve(java.lang.String, java.util.List)
    */
   public List<Resolution> resolve(String destinationName, List<Capability> capabilities) {
      LOG.trace(null);

      netinf.node.gp.messages.GPNetInfMessages.NIresolve.Builder builder = NIresolve.newBuilder();
      builder.setDestinationName(destinationName);

      for (Capability capability : capabilities) {
         builder.addCapabilities(capability.toProto());
      }

      // Register callback
      int callbackId = callbackIdGenerator.nextInt();
      if (callbackId < 0) {
         callbackId = callbackId * (-1);
      }
      builder.setCallbackId(callbackId);

      // Send message
      NIresolve niResolve = builder.build();

      LOG.debug("HasCBID=" + niResolve.hasCallbackId());

      sendMessage(niResolve);

      // Block until answer is received
      NIMessageContainer message = gpCommunicationBuffer.getMessage(callbackId);
      NIresolveCallback resolveCallback = message.getResolveCallback();

      List<NIResolution> niResolutionsList = resolveCallback.getResolutionsList();
      final List<Resolution> resolutionList = new ArrayList<Resolution>();

      for (NIResolution niResolution : niResolutionsList) {
         Resolution resolution = gpFactory.createResolution();
         resolution.fromProto(niResolution);

         // Here is a minor hack. We can only deliver the port number within the destination name. Accordingly we have to modify
         // the targetAddress and the destinationName so that the values are stored correctly.
         String wrongTargetAddress = resolution.getTargetAddress();
         String wrongDestinationName = resolution.getDestinationName();

         int lastIndexOf = wrongDestinationName.lastIndexOf(NAME_PORT_SEPERATOR);
         String port = wrongDestinationName.substring(lastIndexOf + 1);
         String correctDestinationName = wrongDestinationName.substring(0, lastIndexOf);
         String correctTargetAddress = wrongTargetAddress + NAME_PORT_SEPERATOR + port;

         resolution.setTargetAddress(correctTargetAddress);
         resolution.setDestinationName(correctDestinationName);

         resolutionList.add(resolution);
      }

      return resolutionList;
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.gp.GPNetInfInterface#prepareGP(java.lang.String, java.lang.String)
    */
   public void prepareGP(String jobID, String source) {
      LOG.trace(null);

      netinf.node.gp.messages.GPNetInfMessages.NIprepareGP.Builder builder = NIprepareGP.newBuilder();
      builder.setUrl(source);

      NIprepareGP niPrepareGP = builder.build();
      sendMessage(niPrepareGP);
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.gp.GPNetInfInterface#moveGP(java.lang.String, java.lang.String)
    */
   public void moveGP(String jobID, String targetEntity) {
      LOG.trace(null);

      netinf.node.gp.messages.GPNetInfMessages.NImoveEP.Builder builder = NImoveEP.newBuilder();
      builder.setTargetEntity(targetEntity);

      NImoveEP niMoveEP = builder.build();

      sendMessage(niMoveEP);
   }

   private void sendMessage(GeneratedMessage message) {
      LOG.trace(null);
      NIMessageContainer messageContainer = null;

      if (message instanceof NIaddName) {
         NIaddName niAddName = (NIaddName) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setAddName(niAddName);
         builder.setType(NIMessageType.ADDNAME);
         messageContainer = builder.build();
      } else if (message instanceof NIresolve) {
         NIresolve niResolve = (NIresolve) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setResolve(niResolve);
         builder.setType(NIMessageType.RESOLVE);
         messageContainer = builder.build();
      } else if (message instanceof NIresolveCallback) {
         NIresolveCallback niResolveCallback = (NIresolveCallback) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setResolveCallback(niResolveCallback);
         builder.setType(NIMessageType.RESOLVECALLBACK);
         messageContainer = builder.build();
      } else if (message instanceof NIprepareGP) {
         NIprepareGP niPrepareVLCGP = (NIprepareGP) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setPrepareGP(niPrepareVLCGP);
         builder.setType(NIMessageType.PREPAREGP);
         messageContainer = builder.build();
      } else if (message instanceof NImoveEP) {
         NImoveEP niMoveEP = (NImoveEP) message;
         netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.Builder builder = NIMessageContainer.newBuilder();
         builder.setMoveEP(niMoveEP);
         builder.setType(NIMessageType.MOVEEP);
         messageContainer = builder.build();
      } else {
         throw new NetInfUncheckedException("Could not determine kind of message");
      }

      try {
         LOG.debug("Sending the following message to GP:\n" + messageContainer.toString());
         byte[] toBeSend = messageContainer.toByteArray();
         out.writeInt(toBeSend.length);
         out.write(toBeSend);
      } catch (IOException e) {
         LOG.error("Could not send message to GP counterpart", e);
      }
   }

   /**
    * This method is blocking.
    * 
    * @return <code>null</code>, in case the connection was closed remotely.
    */
   public byte[] receiveBytes() throws IOException {
      LOG.trace(null);

      try {
         int size = in.readInt();

         LOG.debug("Size: " + size);

         byte[] bytes = new byte[size];
         in.readFully(bytes);
         /*
          * char[] c = new char[size]; for(int i=0;i<size;i++) { bytes[i] = in.readByte(); c[i] = (char)bytes[i]; //s = new
          * String(c); if(bytes[i]<0) { int z = bytes[i]; LOG.debug("Int: " + z); z = 256 + z; LOG.debug("Int+256: " + z);
          * bytes[i] = (byte) (z); } LOG.debug("BYTE: " + bytes[i]); for( int j = 0; j<8;j++) { System.out.print( ( ( bytes[i] & (
          * 1<<i ) ) > 0 ) + ",\t" ); } java.util.BitSet bs = new java.util.BitSet(8); bs.set(0,(bytes[i]&128)==128);
          * bs.set(1,(bytes[i]&64)==64); bs.set(2,(bytes[i]&32)==32); bs.set(3,(bytes[i]&16)==16); bs.set(4,(bytes[i]&8)==8);
          * bs.set(5,(bytes[i]&4)==4); bs.set(6,(bytes[i]&2)==2); bs.set(7,(bytes[i]&1)==1); LOG.debug("Bits: " + bs.toString());
          * //LOG.debug("CHAR: " + c[i]); //LOG.debug("STRING: " + s); } LOG.debug("Bytes: " + bytes);
          */

         return bytes;
      } catch (IOException e) {
         if (e instanceof EOFException) {
            LOG.debug("Connection to GP was closed remotely");

            // Returns null, in case the connection was closed remotely
            return null;
         } else {
            LOG.error("Could not receive message from GP", e);
            throw e;
         }
      }
   }
}
