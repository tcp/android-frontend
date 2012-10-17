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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import netinf.common.communication.AtomicMessage;
import netinf.common.communication.Connection;
import netinf.common.communication.MessageEncoderXML;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * Server-side HTTP connection that can receive and send AtomicMessage instances
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class HTTPServerConnection implements Connection {
   private static final Logger LOG = Logger.getLogger(HTTPServerConnection.class);
   private static final String ENCODING_HEADER_NAME = "X-NetInf-Encoding";
   private final HttpExchange httpExchange;

   public HTTPServerConnection(HttpExchange httpExchange) {
      this.httpExchange = httpExchange;
   }

   @Override
   public void close() throws IOException {
      this.httpExchange.close();
   }

   @Override
   public AtomicMessage receive() throws IOException {
      LOG.trace(null);

      byte[] data = new byte[1024];
      InputStream requestBody = this.httpExchange.getRequestBody();

      ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
      while (requestBody.available() > 0) {
         int count = requestBody.read(data);
         buffer.write(data, 0, count);
      }
      requestBody.close();

      int encoding;
      Headers headers = this.httpExchange.getRequestHeaders();
      if (headers.containsKey(ENCODING_HEADER_NAME)) {
         encoding = Integer.parseInt(headers.getFirst(ENCODING_HEADER_NAME));
      } else {
         encoding = MessageEncoderXML.ENCODER_ID;
      }

      return new AtomicMessage(encoding, buffer.toByteArray());
   }

   @Override
   public void send(AtomicMessage message) throws IOException {
      LOG.trace(null);

      this.httpExchange.getResponseHeaders().add("Content-type", "text/xml");
      this.httpExchange.getResponseHeaders().add(ENCODING_HEADER_NAME, Integer.toString(message.getEncoding()));
      this.httpExchange.sendResponseHeaders(200, message.getPayload().length);
      this.httpExchange.getResponseBody().write(message.getPayload());
      this.httpExchange.close();
   }
}
