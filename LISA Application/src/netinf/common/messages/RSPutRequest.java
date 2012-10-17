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

import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.utils.DatamodelUtils;

/**
 * Stores an {@link InformationObject} in a ResolutionService. Can be used for creating new {@link InformationObject} instances as
 * well as modifying existing {@link InformationObject} instances. The latter case occurs if an {@link InformationObject} with the
 * same {@link Identifier} already exists.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class RSPutRequest extends NetInfMessage {

   /**
    * The {@link InformationObject} that should be created or modified.
    */
   private InformationObject informationObject;

   /**
    * Used to optionally specify that one wants to store the {@link InformationObject} in specific ResolutionServices of a
    * ResolutionController.
    */
   private List<ResolutionServiceIdentityObject> resolutionServicesToUse;

   public RSPutRequest(InformationObject informationObject, List<ResolutionServiceIdentityObject> resolutionServicesToUse) {
      super();
      this.informationObject = informationObject;
      this.resolutionServicesToUse = resolutionServicesToUse;
   }

   public RSPutRequest(InformationObject informationObject) {
      this.informationObject = informationObject;
   }

   public void setInformationObject(InformationObject informationObject) {
      this.informationObject = informationObject;
   }

   public InformationObject getInformationObject() {
      return this.informationObject;
   }

   public List<ResolutionServiceIdentityObject> getResolutionServicesToUse() {
      return this.resolutionServicesToUse;
   }

   public void setResolutionServicesToUse(List<ResolutionServiceIdentityObject> resolutionServicesToUse) {
      this.resolutionServicesToUse = resolutionServicesToUse;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((this.informationObject == null) ? 0 : this.informationObject.hashCode());
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
      RSPutRequest other = (RSPutRequest) obj;
      if (this.informationObject == null) {
         if (other.informationObject != null) {
            return false;
         }
      } else if (!this.informationObject.equals(other.informationObject)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();

      stringBuilder.append(superString);

      stringBuilder.append("\nInformationObject: \n"
            + DatamodelUtils.toStringInformationObject(this.informationObject, DatamodelUtils.INDENT));

      if (this.resolutionServicesToUse != null) {
         for (ResolutionServiceIdentityObject resolutionServiceIdentityObject : this.resolutionServicesToUse) {
            stringBuilder.append("\nResolutionServiceIdentityObject: \n"
                  + DatamodelUtils.toStringInformationObject(resolutionServiceIdentityObject, DatamodelUtils.INDENT));
         }
      }

      return stringBuilder.toString();
   }

   @Override
   public String describe() {
      StringBuffer buf = new StringBuffer("a request for storing ");
      buf.append(this.informationObject.describe());
      buf.append(getErrorMessage() != null ? "; Error: " + getErrorMessage() : "");
      return buf.toString();
   }
}
