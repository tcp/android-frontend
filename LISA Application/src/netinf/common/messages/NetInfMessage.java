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
package netinf.common.messages;

import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfUncheckedException;

/**
 * Abstract super-class for all messages sent and received by a NetInf Node.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class NetInfMessage {
   /**
    * Some messages (like for example {@link RSGetResponse}) contain {@link InformationObject} instances which can be encoded
    * either as RDF (an XML string) or as a serialized Java object. The {@link SerializeFormat} tells the recipient of such a
    * message which encoding the sender has used. In addition to that, the {@link SerializeFormat} specified in a request causes
    * the response to use the same encoding.
    */
   private SerializeFormat serializeFormat = SerializeFormat.JAVA;

   /**
    * If the recipient of a request message cannot fulfill the request, then its response message contains a human-readable error
    * message that describes the error. In case of success it is <code>null</code>.
    */
   private String errorMessage = null;

   /**
    * Some clients may be unable to perform security-related checks on their own (for example because they are not written in Java
    * and the security packages have not yet been implemented in other programming languages). In this case the NetInf Node can
    * perform the security-related checks on behalf of the client. Obviously, the client then has to fully trust the NetInf Node
    * and an encrypted connection should be used since the client's username and private key have to be sent to the NetInf Node.
    */
   private String userName = null;

   /**
    * See {@link NetInfMessage#userName}.
    */
   private String privateKey = null;

   public void setSerializeFormat(SerializeFormat serializeFormat) {
      this.serializeFormat = serializeFormat;
   }

   public SerializeFormat getSerializeFormat() {
      return this.serializeFormat;
   }

   public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public String getUserName() {
      return this.userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public String getPrivateKey() {
      return this.privateKey;
   }

   public void setPrivateKey(String privateKey) {
      this.privateKey = privateKey;
   }

   /**
    * For a regular NetInf message the hash code consists of the private key, user name and error message. The absence of any of
    * these elements will result in a different hash code. The formula is: hashCode(n) = hashCode(n-1) * prime_number + field
    * value More info here: http://www.angelikalanger.com/Articles/EffectiveJava/03.HashCode/03.HashCode.html Method will throw an
    * unchecked exception, because there is nothing that the program can do to recover from that exception (privateKey or userName
    * are null).
    * 
    * @return
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 13;
      if (this.privateKey == null || this.userName == null) {
         throw new NetInfUncheckedException("Message is missing required parameters");
      }

      result = prime + ((this.errorMessage == null) ? 0 : this.errorMessage.hashCode());
      result *= prime + ((this.privateKey == null) ? 0 : this.privateKey.hashCode());
      result *= prime + ((this.userName == null) ? 0 : this.userName.hashCode());
      /*
       * WAS: result = prime * result + ((this.errorMessage == null) ? 0 : this.errorMessage.hashCode()); result = prime * result
       * + ((this.privateKey == null) ? 0 : this.privateKey.hashCode()); result = prime * result + ((this.userName == null) ? 0 :
       * this.userName.hashCode());
       */
      return result;
   }

   /***
    * Defined equality for NetInfMessages as being messages which have the same username, same private key and same error WARNING:
    * You MUST override this method to get usable result for subclasses
    **/
   @Override
   public boolean equals(Object obj) {

      if (obj == null) {
         return false;
      }
      if (this == obj) {
         return true;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }
      boolean bSameUser = false;
      boolean bSameKey = false;
      boolean bSameError = false;
      if (obj != null && /* (obj.getClass().equals(NetInfMessage.class)) */obj instanceof NetInfMessage) {
         NetInfMessage other = (NetInfMessage) obj;
         if (this.userName != null && other.userName != null) {
            if (this.userName.equalsIgnoreCase(other.userName)) {
               bSameUser = true;
            }
         }
         if (this.userName == null && other.userName == null) {
            bSameUser = true;
         }
         if (this.privateKey != null && other.privateKey != null) {
            if (this.privateKey.equalsIgnoreCase(other.privateKey)) {
               bSameKey = true;
            }
         }
         if (this.privateKey == null && other.privateKey == null) {
            bSameKey = true;
         }
         if (this.errorMessage != null && other.errorMessage != null) {
            if (this.errorMessage.equalsIgnoreCase(other.errorMessage)) {
               bSameError = true;
            }
         }
         if (this.errorMessage == null && other.errorMessage == null) {
            bSameError = true;
         }

      }
      return (bSameUser && bSameKey && bSameError);

      /*
       * if (this.errorMessage == null) { if (other.errorMessage != null) { return false; } } else if
       * (!this.errorMessage.equals(other.errorMessage)) { return false; } if (this.privateKey == null) { if (other.privateKey !=
       * null) { return false; } } else if (!this.privateKey.equals(other.privateKey)) { return false; } if (this.userName ==
       * null) { if (other.userName != null) { return false; } } else if (!this.userName.equals(other.userName)) { return false; }
       */
   }

   /*
    * Is not allowed to create a new line at the end of the serialization (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();

      stringBuilder.append("MessageKind: " + getClass().getSimpleName());

      stringBuilder.append("\nSerializationFormat: "
            + (this.serializeFormat != null ? this.serializeFormat.getSerializeFormat() : null));
      stringBuilder.append("\nErrorMessage: " + this.errorMessage);

      return stringBuilder.toString();
   }

   /**
    * Returns a textual description of this message ("I have received "+describe())
    * 
    * @return textual description
    */
   public String describe() {
      StringBuilder buf = new StringBuilder("a message called ");
      buf.append(this.getClass().getSimpleName());
      buf.append(getErrorMessage() != null ? "; Error: " + getErrorMessage() : "");
      return buf.toString();
   }
}
