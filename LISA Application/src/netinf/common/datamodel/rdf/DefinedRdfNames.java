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
package netinf.common.datamodel.rdf;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileUtils;

/**
 * RDF Vocabulary for NetInf
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class DefinedRdfNames {

   public static final String NETINF_RDF_SCHEMA_URI = "http://rdf.netinf.org/2009/netinf-rdf/1.0/#";
   public static final String NETINF_NAMESPACE_NAME = "netinf";
   public static final String NETINF_IO_NAMESPACE_NAME = "io";
   public static final String NETINF_URI_SCHEMA = "ni:";
   public static final String NETINF_RDF_STORAGE_FORMAT = "RDF/XML-ABBREV";

   public static final Resource netinfResource(String local) {
      return ResourceFactory.createResource(NETINF_RDF_SCHEMA_URI + local);
   }

   public static final Property netinfProperty(String local) {
      return ResourceFactory.createProperty(NETINF_RDF_SCHEMA_URI, local);
   }

   // public static final String uriFromIO(InformationObject informationObject) {
   // return NETINF_URI_SCHEMA + informationObject.getIdentifier().toString();
   // }

   public static final String namespaceFromIO(InformationObject informationObject) {
      // return uriFromIO(informationObject) + "#";
      return informationObject + "#";
   }

   public static final Property ioProperty(InformationObject informationObject, String local) {
      return ResourceFactory.createProperty(namespaceFromIO(informationObject), local);
   }

   public static final Property ioProperty(Attribute attribute) {
      if (FileUtils.isURI(attribute.getIdentification())) {
         return ResourceFactory.createProperty(attribute.getIdentification());
      } else {
         return netinfProperty(attribute.getIdentification());
      }
   }

   public static Property li(int i) {
      return netinfProperty("_" + i);
   }

   // GENERAL PROPERTIES
   public static final String IO_TYPE = NETINF_RDF_SCHEMA_URI + "ioType";
   public static final String ATTRIBUTE_VALUE = NETINF_RDF_SCHEMA_URI + "attributeValue";
   public static final String ATTRIBUTE_PURPUSE = NETINF_RDF_SCHEMA_URI + "attributePurpose";

   // TRANSPORT RELATED PROPERTIES
   public static final String POINTER_TO_IO = NETINF_RDF_SCHEMA_URI + "transportedIO";
   public static final String POINTER_TO_ATTRIBUTE = NETINF_RDF_SCHEMA_URI + "transportedAttribute";
   public static final String POINTER_TO_IDENTIFIER = NETINF_RDF_SCHEMA_URI + "transportedIdentifier";
}
