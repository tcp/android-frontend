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

import java.util.ArrayList;
import java.util.List;

/**
 * The EventService sends this message as a response to an {@link ESFFetchMissedEventsRequest}. It consists of multiple
 * {@link ESFEventMessage} instances.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ESFFetchMissedEventsResponse extends NetInfMessage {
   private final List<ESFEventMessage> eventMessages = new ArrayList<ESFEventMessage>();
   private final transient int prime = 31;

   public void addEventMessage(final ESFEventMessage eventMessage) {
      this.eventMessages.add(eventMessage);
   }

   public List<ESFEventMessage> getEventMessages() {
      return this.eventMessages;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = prime * result + ((this.eventMessages == null) ? 0 : this.eventMessages.hashCode());
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
      ESFFetchMissedEventsResponse other = (ESFFetchMissedEventsResponse) obj;
      if (this.eventMessages == null) {
         if (other.eventMessages != null) {
            return false;
         }
      } else if (!this.eventMessages.equals(other.eventMessages)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      final String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      for (ESFEventMessage eventMessage : this.eventMessages) {
         stringBuilder.append("\nESFEventMessage: \n" + eventMessage);
      }

      return stringBuilder.toString();
   }
}
