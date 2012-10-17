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

import netinf.common.utils.Utils;

/**
 * The smallest possible message than can be sent/received by the Connection interface
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class AtomicMessage {
   private final int encoding;
   private final byte[] payload;

   /**
    * Constructor
    * 
    * @param encoding
    *           the encoding of the <code>payload</code> (e.g. XML or Protobuf)
    * @param payload
    *           the actual data
    */
   public AtomicMessage(int encoding, byte[] payload) {
      this.encoding = encoding;
      this.payload = payload;
   }

   public int getEncoding() {
      return this.encoding;
   }

   public byte[] getPayload() {
      return this.payload;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Encoder ID: " + this.encoding + "\n");
      sb.append("Payload Size: " + this.payload.length + "\n");
      sb.append(Utils.bytesToString(this.payload));
      sb.append("\n");
      return sb.toString();
   }
}
