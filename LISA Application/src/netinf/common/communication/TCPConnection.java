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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Connection that uses TCP as transport protocol
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TCPConnection implements Connection {
   private final Socket socket;
   private final DataInputStream in;
   private final DataOutputStream out;

   private static final int MAX_PAYLOAD_SIZE = 1048576;

   public TCPConnection(Socket socket) throws IOException {
      this.socket = socket;
      this.in = new DataInputStream(socket.getInputStream());
      this.out = new DataOutputStream(socket.getOutputStream());
   }

   @Override
   public void send(AtomicMessage message) throws IOException {
      synchronized (this.out) {
         this.out.writeInt(message.getEncoding());
         this.out.writeInt(message.getPayload().length);
         this.out.write(message.getPayload());
      }
   }

   @Override
   public AtomicMessage receive() throws IOException {
      synchronized (this.in) {
         int encoderId = this.in.readInt();
         int payloadSize = this.in.readInt();

         if (payloadSize > MAX_PAYLOAD_SIZE) {
            throw new IOException("Payload size " + payloadSize + " exceeds " + MAX_PAYLOAD_SIZE + " bytes");
         }

         byte[] data = new byte[payloadSize];
         this.in.readFully(data);

         return new AtomicMessage(encoderId, data);
      }
   }

   @Override
   public void close() throws IOException {
      this.socket.close();
   }
}
