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

import netinf.common.datamodel.InformationObject;
import netinf.common.utils.DatamodelUtils;

/**
 * Sent by a ResolutionController in response to an {@link RSGetRequest}. Contains all retrieved {@link InformationObject}
 * instances.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class RSGetResponse extends NetInfMessage {
   private final ArrayList<InformationObject> informationObjects = new ArrayList<InformationObject>();

   public void addInformationObject(InformationObject informationObject) {
      if (informationObject != null) {
         this.informationObjects.add(informationObject);
      }
   }

   public ArrayList<InformationObject> getInformationObjects() {
      return this.informationObjects;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((this.informationObjects == null) ? 0 : this.informationObjects.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {

      if (!super.equals(obj)) {
         return false;
      }
      RSGetResponse other = (RSGetResponse) obj;
      if (this.informationObjects == null) {
         if (other.informationObjects != null) {
            return false;
         }
      } else if (!this.informationObjects.equals(other.informationObjects)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      for (InformationObject informationObject : this.informationObjects) {
         stringBuilder.append("\nInformationObject: \n"
               + DatamodelUtils.toStringInformationObject(informationObject, DatamodelUtils.INDENT));
      }

      return stringBuilder.toString();
   }

   @Override
   public String describe() {
      StringBuffer buf = new StringBuffer("a reply to a request that has ");
      buf.append(this.informationObjects.size());
      buf.append(" Information Object");
      buf.append(this.informationObjects.size() != 1 ? "s" : "");
      buf.append(getErrorMessage() != null ? "; Error: " + getErrorMessage() : "");
      return buf.toString();
   }
}
