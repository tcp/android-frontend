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

import netinf.common.datamodel.InformationObject;
import netinf.common.utils.DatamodelUtils;

/**
 * This message describes the creation, deletion and modification of {@link InformationObject} instances and is used by the
 * ResolutionService as well as the EventService.
 * <p>
 * The ResolutionService sends this message to the EventService whenever an {@link InformationObject} is created, deleted or
 * modified. The EventService sends such a message to its subscribers if an event matches a subscription.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ESFEventMessage extends NetInfMessage {
   /**
    * If this message is sent from the EventService to a subscriber this attribute is set to the identification which the
    * subscriber has chosen for the corresponding subscription in its {@link ESFSubscriptionRequest}. If this message is sent from
    * the ResolutionService to the EventService then this attribute is <code>null</code>.
    */
   private String matchedSubscriptionIdentification;

   /**
    * If an {@link InformationObject} has been created this attribute is <code>null</code>. If an {@link InformationObject} has
    * been modified or deleted this attribute contains the old {@link InformationObject}.
    */
   private InformationObject oldInformationObject;

   /**
    * If an {@link InformationObject} has been created or modified this attribute contains the new {@link InformationObject}. If
    * an {@link InformationObject} has been deleted this attribute is <code>null</code>.
    */
   private InformationObject newInformationObject;

   public InformationObject getOldInformationObject() {
      return this.oldInformationObject;
   }

   public void setOldInformationObject(InformationObject oldInformationObject) {
      this.oldInformationObject = oldInformationObject;
   }

   public InformationObject getNewInformationObject() {
      return this.newInformationObject;
   }

   public void setNewInformationObject(InformationObject newInformationObject) {
      this.newInformationObject = newInformationObject;
   }

   public String getMatchedSubscriptionIdentification() {
      return this.matchedSubscriptionIdentification;
   }

   public void setMatchedSubscriptionIdentification(String matchedSubscriptionIdentification) {
      this.matchedSubscriptionIdentification = matchedSubscriptionIdentification;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      stringBuilder.append("\nMatchedSubscriptionIdentification: " + this.matchedSubscriptionIdentification);

      stringBuilder.append("\nOldInformationObject: \n"
            + DatamodelUtils.toStringInformationObject(this.oldInformationObject, DatamodelUtils.INDENT));

      stringBuilder.append("\nNewInformationObject: \n"
            + DatamodelUtils.toStringInformationObject(this.newInformationObject, DatamodelUtils.INDENT));

      return stringBuilder.toString();
   }
}
