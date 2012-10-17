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

import netinf.common.datamodel.Identifier;

/**
 * The first message that a subscriber sends to the EventService after the connection has been established.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ESFRegistrationRequest extends NetInfMessage {
   /**
    * A required attribute that specifies the {@link Identifier} of the subscriber's PersonObject
    */
   private Identifier personObjectIdentifier;

   /**
    * An optional attribute that specifies the {@link Identifier} of the EventContainer IO that the EventService should use to
    * store events that occur while the subscriber is off-line (see {@link ESFFetchMissedEventsRequest}). If unspecified, then the
    * EventService will generate an {@link Identifier} for the EventContainer IO on its own. The idea here is that a subscriber
    * might want to partition its subscriptions.
    */
   private Identifier eventContainerIdentifier;

   public ESFRegistrationRequest(Identifier personObjectIdentifier) {
      this.personObjectIdentifier = personObjectIdentifier;
   }

   public void setPersonObjectIdentifier(Identifier personObjectIdentifier) {
      this.personObjectIdentifier = personObjectIdentifier;
   }

   public Identifier getPersonObjectIdentifier() {
      return this.personObjectIdentifier;
   }

   public void setEventContainerIdentifier(Identifier eventContainerIdentifier) {
      this.eventContainerIdentifier = eventContainerIdentifier;
   }

   public Identifier getEventContainerIdentifier() {
      return this.eventContainerIdentifier;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((this.eventContainerIdentifier == null) ? 0 : this.eventContainerIdentifier.hashCode());
      result = prime * result + ((this.personObjectIdentifier == null) ? 0 : this.personObjectIdentifier.hashCode());
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
      ESFRegistrationRequest other = (ESFRegistrationRequest) obj;
      if (this.eventContainerIdentifier == null) {
         if (other.eventContainerIdentifier != null) {
            return false;
         }
      } else if (!this.eventContainerIdentifier.equals(other.eventContainerIdentifier)) {
         return false;
      }
      if (this.personObjectIdentifier == null) {
         if (other.personObjectIdentifier != null) {
            return false;
         }
      } else if (!this.personObjectIdentifier.equals(other.personObjectIdentifier)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      stringBuilder.append("\nPersonObjectIdentifier: " + this.personObjectIdentifier);
      stringBuilder.append("\nEventContainerIdentifier: " + this.eventContainerIdentifier);

      return stringBuilder.toString();
   }

}
