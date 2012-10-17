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
package netinf.access;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sql.ConnectionEvent;

import netinf.common.communication.Communicator;
import netinf.common.communication.TCPConnection;
import netinf.common.exceptions.NetInfCheckedException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * Provides access to a NetInf Node via TCP
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TCPServer extends NetInfServer {
   private static final Logger LOG = Logger.getLogger(TCPServer.class);
   private Provider<Communicator> communicatorProvider;

   private ServerSocket serverSocket;
   private ConnectionListener connectionListener;

   private final int port;

   @Inject
   public TCPServer(@Named("access.tcp.port") int port) {
      this.port = port;
   }

   @Inject
   public void injectProviderCommunicator(Provider<Communicator> provider) {
      communicatorProvider = provider;
   }

   @Override
   public void start() throws NetInfCheckedException {
      LOG.trace(null);

      try {
         serverSocket = new ServerSocket(port);
      } catch (IOException e) {
         LOG.error("Error encountered while initializing the TCPServer on port: " + port, e);
         throw new NetInfCheckedException(e);
      }

      connectionListener = new ConnectionListener();
      connectionListener.start();
   }

   @Override
   public void stop() throws IOException {
      LOG.trace(null);

      if (connectionListener != null) {
         connectionListener.interrupt();
      }

      try {
         if (serverSocket != null) {
            serverSocket.close();
         }
      } catch (IOException e) {
         throw e;
      }
   }

   public int getPort() {
      return port;
   }

   public String getAddress() {
      return serverSocket.getInetAddress().toString();
   }

   @Override
   public boolean isRunning() {
      return connectionListener.isRunning();
   }

   @Override
   public String describe() {
      return "TCP on port " + port;
   }

   /**
    * The listener interface for receiving connection events. The class that is interested in processing a connection event
    * implements this interface, and the object created with that class is registered with a component using the component's
    * <code>addConnectionListener<code> method. When
    * the connection event occurs, that object's appropriate
    * method is invoked.
    * 
    * @see ConnectionEvent
    * @author PG Augnet 2, University of Paderborn
    */
   class ConnectionListener extends Thread {
      private boolean running;

      public ConnectionListener() {
         running = true;
      }

      @Override
      public void run() {
         LOG.trace(null);

         try {
            LOG.debug("Starting to listen for new connection within the TCPServer on port " + serverSocket.getLocalPort());

            while (running) {
               LOG.debug("In listen loop");
               Socket socket = serverSocket.accept();
               LOG.debug("Accepted new connection.");
               TCPConnection newConnection = new TCPConnection(socket);
               Communicator newCommunicator = communicatorProvider.get();
               newCommunicator.setConnection(newConnection);

               startCommunicator(newCommunicator, true);
            }
         } catch (IOException e) {
            LOG.error("The TCP Server encountered an error", e);
         }
      }

      @Override
      public void interrupt() {
         LOG.trace(null);

         running = false;
         super.interrupt();
      }

      public boolean isRunning() {
         return running;
      }
   }

}
