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

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfConnectionClosedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.NetInfMessage;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Sends and receives NetInfMessage instances (a)synchronously TODO: (Ede) The communicator needs some kind of status, whether it
 * is connected or not.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class Communicator {
   private static final Logger LOG = Logger.getLogger(Communicator.class);

   private Connection connection;
   private AsyncReceiver asyncReceiver;
   private AsyncSender asyncSender;

   private MessageEncoderController messageEncoderController;

   private MessageEncoder messageEncoder;
   private SerializeFormat serializeFormat = SerializeFormat.JAVA;

   private final LinkedList<NetInfMessage> outgoingMessageQueue = new LinkedList<NetInfMessage>();

   private String host;

   private int port;

   public void setConnection(Connection connection) {
      this.connection = connection;
   }

   @Inject
   public void injectMessageEncoderController(MessageEncoderController messageEncoderController) {
      this.messageEncoderController = messageEncoderController;
      this.messageEncoder = messageEncoderController.getDefaultEncoder();
   }

   public void setSerializeFormat(SerializeFormat serializeFormat) {
      this.serializeFormat = serializeFormat;
   }

   /**
    * There might be several setup methods. Depending on the kind of setup, the kind of {@link Connection} is determined.
    * 
    * @param host
    * @param port
    * @throws IOException
    */
   public void setup(String host, int port) throws IOException {
      if (this.connection != null) {
         try {
            this.connection.close();
         } catch (IOException ex) {
            LOG.warn("Communicator could not be closed");
         }
      }
      this.host = host;
      this.port = port;
      Socket socket = new Socket();
      socket.bind(null);
      socket.connect(new InetSocketAddress(host, port), 1000);
      LOG.debug("Connected to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
      Connection connection = new TCPConnection(socket);
      setConnection(connection);
   }

   public void send(NetInfMessage message) throws NetInfCheckedException {
      // Set the encoding format directly at the beginning.
      message.setSerializeFormat(this.serializeFormat);

      LOG.debug("NetInfMessage to be send: \n" + message);

      int encoding = this.messageEncoder.getUniqueEncoderId();
      LOG.debug("Encoding message with encoder " + encoding);

      byte[] payload = this.messageEncoder.encodeMessage(message);
      LOG.debug("Payload size: " + payload.length);

      LOG.log(DemoLevel.DEMO, "(COMM ) Sending " + message.describe() + " (size: " + payload.length + "B)");

      try {
         this.connection.send(new AtomicMessage(encoding, payload));
      } catch (IOException e) {
         try {
            LOG.warn("Error sending message: Retry...");
            setup(this.host, this.port);
            this.connection.send(new AtomicMessage(encoding, payload));
         } catch (IOException ex) {
            throw new NetInfCheckedException(ex);
         }
      }
   }

   public NetInfMessage receive() throws NetInfCheckedException {
      LOG.trace(null);
      AtomicMessage atomicMessage = null;
      try {
         atomicMessage = this.connection.receive();
      } catch (IOException ex) {

         if (ex instanceof EOFException) {
            throw new NetInfConnectionClosedException("The connection was closed remotely");
         } else {
            LOG.error("Failed to receive NetInfMessage from connection", ex);
            throw new NetInfCheckedException(ex);
         }
      }
      int encoding = atomicMessage.getEncoding();
      LOG.debug("Received message with Encoding-ID " + encoding);

      byte[] payload = atomicMessage.getPayload();
      LOG.debug("Received payload of size " + payload.length + " bytes");

      this.messageEncoder = this.messageEncoderController.getEncoderById(encoding);

      if (this.messageEncoder == null) {
         throw new NetInfCheckedException("Cannot decode message with encoding " + encoding);
      }

      NetInfMessage message = this.messageEncoder.decodeMessage(payload);
      LOG.info("NetInfMessage received: \n" + message);

      this.serializeFormat = message.getSerializeFormat();

      LOG.log(DemoLevel.DEMO, "(COMM ) Received " + message.describe() + " (size: " + payload.length + "B)");

      return message;

   }

   public void sendAsync(final NetInfMessage message) {
      LOG.trace(null);

      LOG.debug("Adding message to outgoing queue");
      this.outgoingMessageQueue.add(message);

      if (this.asyncSender == null) {
         LOG.debug("Creating a new AsyncSender");
         this.asyncSender = new AsyncSender();
         this.asyncSender.start();
      } else {
         synchronized (this.asyncSender) {
            LOG.debug("Notifying AsyncSender");
            this.asyncSender.notify();
         }
      }
   }

   public void close() throws NetInfCheckedException {
      LOG.trace(null);

      try {
         this.connection.close();
      } catch (IOException e) {
         throw new NetInfCheckedException(e);
      }
   }

   public void startAsyncReceive(AsyncReceiveHandler handler, boolean loop) {
      LOG.trace(null);

      this.asyncReceiver = new AsyncReceiver(handler, loop);
      this.asyncReceiver.start();
   }

   public void stopAsyncReceive() {
      this.asyncReceiver.interrupt();
   }

   /**
    * The Class AsyncSender.
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   private class AsyncSender extends Thread {
      @Override
      public void run() {
         LOG.trace(null);

         while (!Communicator.this.outgoingMessageQueue.isEmpty()) {
            LOG.debug("Trying to send message asynchronously");

            try {
               send(Communicator.this.outgoingMessageQueue.pop());

               synchronized (Communicator.this.asyncSender) {
                  if (Communicator.this.outgoingMessageQueue.isEmpty()) {
                     LOG.debug("Waiting...");
                     Communicator.this.asyncSender.wait();
                     LOG.debug("Got notified...");
                  }
               }

            } catch (NetInfCheckedException e) {
               LOG.error("Failed to send message asynchronously", e);
            } catch (InterruptedException e) {
               LOG.error("Failed to send message asynchronously", e);
            }
         }

      }
   }

   /**
    * The Class AsyncReceiver.
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   private class AsyncReceiver extends Thread {
      private boolean running = true;
      private final AsyncReceiveHandler handler;

      public AsyncReceiver(AsyncReceiveHandler handler, boolean loop) {
         this.handler = handler;
         this.running = loop;
      }

      @Override
      public void run() {
         try {
            LOG.trace(null);
            do {
               LOG.trace(null);
               NetInfMessage message = receive();
               this.handler.receivedMessage(message, Communicator.this);
            } while (this.running);
         } catch (NetInfConnectionClosedException e) {
            LOG.debug("The connection was closed remotely");
         } catch (NetInfCheckedException e) {
            LOG.error("Failed to receive message asynchronously", e);
         } finally {
            this.running = false;
         }
      }

      @Override
      public void interrupt() {
         super.interrupt();
         this.running = false;
      }
   }
}
