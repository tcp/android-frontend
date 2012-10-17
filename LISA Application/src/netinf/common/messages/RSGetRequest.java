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

import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.utils.DatamodelUtils;

/**
 * Fetches one or multiple {@link InformationObject} instances from a ResolutionController.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class RSGetRequest extends NetInfMessage {
   /**
    * The {@link Identifier} of the {@link InformationObject} that should be fetched. If the {@link IdentifierLabel}
    * {@link DefinedLabelName#VERSION_NUMBER} is omitted then the ResolutionController will return the most recent version.
    */
   private Identifier identifier;

   /**
    * If the client wants to fetch all versions (and not just the most recent one) of the request {@link InformationObject} then
    * this flag has to be set to <code>true</code>. The default is <code>false</code>
    */
   private boolean fetchAllVersions;

   /**
    * Used to optionally specify that one wants to query specific ResolutionServices of a ResolutionController.
    */
   private List<ResolutionServiceIdentityObject> resolutionServicesToUse;

   /**
    * If this flag is set to <code>true</code> and the requested {@link InformationObject} has at least one {@link Attribute}
    * whose identification is {@link DefinedAttributeIdentification#HTTP_URL}, then prior to sending its response the
    * ResolutionService will download the linked binary object and add itself as a mirror (by means of adding a new attribute with
    * identification {@link DefinedAttributeIdentification#HTTP_URL} that points to a local HTTP server).
    */
   private boolean downloadBinaryObject = false;

   public RSGetRequest(Identifier identifier, List<ResolutionServiceIdentityObject> resolutionServicesToUse) {
      super();
      this.identifier = identifier;
      this.resolutionServicesToUse = resolutionServicesToUse;
      this.fetchAllVersions = false;
   }

   public RSGetRequest(Identifier identifier) {
      this.identifier = identifier;
      this.fetchAllVersions = false;
   }

   public void setIdentifier(Identifier identifier) {
      this.identifier = identifier;
   }

   public Identifier getIdentifier() {
      return this.identifier;
   }

   public void setFetchAllVersions(boolean fetchAllVersions) {
      this.fetchAllVersions = fetchAllVersions;
   }

   public boolean isFetchAllVersions() {
      return this.fetchAllVersions;
   }

   public List<ResolutionServiceIdentityObject> getResolutionServicesToUse() {
      return this.resolutionServicesToUse;
   }

   public void setResolutionServicesToUse(List<ResolutionServiceIdentityObject> resolutionServicesToUse) {
      this.resolutionServicesToUse = resolutionServicesToUse;
   }

   public void setDownloadBinaryObject(boolean downloadBinaryObject) {
      this.downloadBinaryObject = downloadBinaryObject;
   }

   public boolean isDownloadBinaryObject() {
      return this.downloadBinaryObject;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + (this.downloadBinaryObject ? 1231 : 1237);
      result = prime * result + (this.fetchAllVersions ? 1231 : 1237);
      result = prime * result + ((this.identifier == null) ? 0 : this.identifier.hashCode());
      result = prime * result + ((this.resolutionServicesToUse == null) ? 0 : this.resolutionServicesToUse.hashCode());
      return result;
   }

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
      RSGetRequest other = (RSGetRequest) obj;
      if (this.downloadBinaryObject != other.downloadBinaryObject) {
         return false;
      }
      if (this.fetchAllVersions != other.fetchAllVersions) {
         return false;
      }
      if (this.identifier == null) {
         if (other.identifier != null) {
            return false;
         }
      } else if (!this.identifier.equals(other.identifier)) {
         return false;
      }
      if (this.resolutionServicesToUse == null) {
         if (other.resolutionServicesToUse != null) {
            return false;
         }
      } else if (!this.resolutionServicesToUse.equals(other.resolutionServicesToUse)) {
         return false;
      }
      if (!super.equals(obj)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      stringBuilder.append("\nIdentifier: " + this.identifier);
      stringBuilder.append("\nFetchAllVersions: " + this.fetchAllVersions);

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
      StringBuffer buf = new StringBuffer("a request for any IO that ");
      buf.append(this.identifier.describe());
      buf.append(getErrorMessage() != null ? "; Error: " + getErrorMessage() : "");
      return buf.toString();
   }
}
