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
package project.cs.lisa.netinf.common.datamodel;

import netinf.common.datamodel.rdf.DefinedRdfNames;

/**
 * This class contains a list of all predefined attribute identifications. With the help of the
 * {@link SailDefinedAttributeIdentification#getURI()} method, it is possible to access the underlying URI of the according defined
 * attribute.
 *
 * @author PG Augnet 2, University of Paderborn
 */
public enum SailDefinedAttributeIdentification {

   LOCATOR_PRIORITY("locator_priority", false), // specifies the priority for a locator
   BLUETOOTH_MAC("bluetooth_mac", false),
   WIFI_MAC("wifi_mac", false),
   WIFI_IP("wifi_ip", false),
   NCS_URL("ncs_url", false),
   FILE_PATH("file_path", false);

   private final String uri;

   private SailDefinedAttributeIdentification(String uri, boolean fromNetInf) {
      if (fromNetInf) {
         // TODO: This is currently a little bit ugly, since we are using RDF in an non-RDF part of the code.
         this.uri = DefinedRdfNames.NETINF_RDF_SCHEMA_URI + uri;
      } else {
         this.uri = uri;
      }
   }

   public String getURI() {
      return uri;
   }

   public static SailDefinedAttributeIdentification getDefinedAttributeIdentificationByURI(String uri) {
      SailDefinedAttributeIdentification result = null;

      for (SailDefinedAttributeIdentification definedAttributeIdentification : SailDefinedAttributeIdentification.values()) {
         if (definedAttributeIdentification.getURI().equals(uri)) {
            result = definedAttributeIdentification;
            break;
         }
      }

      return result;
   }

   public static String getURIByAttributeIdentification(SailDefinedAttributeIdentification definedAttributeIdentification) {
      return definedAttributeIdentification.getURI();
   }
}