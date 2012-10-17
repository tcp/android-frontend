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

import netinf.common.communication.NetInfNodeConnection;

/**
 * After a subscriber has sent an {@link ESFRegistrationRequest} to the Event Service, he may subscribe itself for certain events
 * by means of an {@link ESFSubscriptionRequest}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ESFSubscriptionRequest extends NetInfMessage {
   /**
    * An identification for this subscription that has to be unique for this subscriber and that will be contained in resulting
    * {@link ESFEventMessage} instances. Its format is arbitrary.
    */
   private String subscriptionIdentification;

   /**
    * Specifies the relative offset after how many seconds the Event Service will remove the subscription automatically.
    */
   private long expires;

   /**
    * The "WHERE"-part of a SparQL query. Have a look at {@link NetInfNodeConnection#performSearch(String, int)} for the syntax.
    */
   private String sparqlSubscription;

   public ESFSubscriptionRequest(String subscriptionIdentification, String sparqlSubscription, long expires) {
      this.subscriptionIdentification = subscriptionIdentification;
      this.sparqlSubscription = sparqlSubscription;
      this.expires = expires;
   }

   public long getExpires() {
      return this.expires;
   }

   public void setExpires(long expires) {
      this.expires = expires;
   }

   public String getSubscriptionIdentification() {
      return this.subscriptionIdentification;
   }

   public void setSubscriptionIdentification(String subscriptionIdentification) {
      this.subscriptionIdentification = subscriptionIdentification;
   }

   public void setSparqlSubscription(String sparqlSubscription) {
      this.sparqlSubscription = sparqlSubscription;
   }

   public String getSparqlSubscription() {
      return this.sparqlSubscription;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + (int) (this.expires ^ (this.expires >>> 32));
      result = prime * result + ((this.sparqlSubscription == null) ? 0 : this.sparqlSubscription.hashCode());
      result = prime * result + ((this.subscriptionIdentification == null) ? 0 : this.subscriptionIdentification.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (!super.equals(obj)) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      ESFSubscriptionRequest other = (ESFSubscriptionRequest) obj;
      if (this.expires != other.expires) {
         return false;
      }
      if (this.sparqlSubscription == null) {
         if (other.sparqlSubscription != null) {
            return false;
         }
      } else if (!this.sparqlSubscription.equals(other.sparqlSubscription)) {
         return false;
      }
      if (this.subscriptionIdentification == null) {
         if (other.subscriptionIdentification != null) {
            return false;
         }
      } else if (!this.subscriptionIdentification.equals(other.subscriptionIdentification)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      stringBuilder.append("\nSubscriptionIdentification: " + this.subscriptionIdentification);
      stringBuilder.append("\nExpires: " + this.expires);
      stringBuilder.append("\nSparqlSubscription: " + this.sparqlSubscription);

      return stringBuilder.toString();
   }

}
