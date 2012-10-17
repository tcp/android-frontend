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
package netinf.common.datamodel.attribute;

import netinf.common.datamodel.rdf.DefinedRdfNames;

/**
 * This class contains a list of all predefined attribute identifications. With the help of the
 * {@link DefinedAttributeIdentification#getURI()} method, it is possible to access the underlying URI of the according defined
 * attribute.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public enum DefinedAttributeIdentification {

   // new by PG NetInf 3
   REFERENCED_DO("referenced_do", true),  // reference to a DO (inside an IO)
   CONTENT_TYPE("content_type", true),    // content-type of a DO (added as subattribute of referenced-do)
   PERSON_PHONE("person_phone", true),
   PERSON_ADDRESS("person_address", true),
   PERSON_BIRTHDAY("person_birthday", true),
   PERSON_HOMEPAGE("person_homepage", true),
   MDHT_LEVEL("mdht_level", true),        // specifies in which level the IO will be published (only mdht related)
   LOCATOR_PRIORITY("locator_priority", true), // specifies the priority for a locator
   
   // for chunking
   CHUNKED("chunked", true),              // indicates whether a locator is chunk/range enabled or not 
   CHUNKS("chunks", true),                // represents a list of chunks/ranges
   CHUNK("chunk", true),                  // represents a single chunk/range
   HASH_OF_CHUNK("hash_of_chunk", true),  // the hash value of that chunk/range
   
   // TODO: The following lines should be URIs one day
   OWNER("owner", true),
   PARENT_VERSION("parent_version", true),
   ORIGIN("origin", true),
   BRANCH_NAME("branch_name", true),
   TIME_TO_LIVE("time_to_live", true),
   AUTHORIZED_WRITERS("authorized_writers", true),
   AUTHORIZED_READERS("authorized_readers", true),
   READER("reader", true),
   WRITER("writer", true),
   WRITER_OF_GROUP("writer_of_group", true),
   SIGNATURE("signature", true),
   SIGNATURE_IDENTIFICATION("signature_identication", true),
   SIGNATURE_VERIFICATION_FAILED("signature_verification_failed", true),
   HASH("hash", true),
   HASH_IDENTIFICATION("hash_identification", true),
   HASH_OF_DATA("hash_of_data", true),
   PERSON_NAME("person_name", true),
   PERSON_AGE("person_age", true),
   E_MAIL_ADDRESS("e_mail_address", true),
   MEMBERS_OF_GROUP("members_of_group", true),
   ENCRYPTED_GROUP_KEYS("encrypted_group_keys", true),
   CACHE("cache", true), // Represents the encrypted private key for the group, with the
   // according
   // public
   // key of the user.
   IDENTITY_REVOKED("identity_revoked", true),
   THIRD_PARTY_SIGNATURE("third_party_signature", true),
   SINGLE_WRITER_REQUIRED("single_writer_required", true),
   IDENTIFIER("identifier", true),
   ENCRYPTED_INFORMATION_OBJECT("encrypted_information_object", true),
   ENCRYPTED_CONTENT("encrypted_content", true),
   ENCRYPTED_READER_KEY_LIST("encrypted_reader_key_list", true),
   ENCRYPTED_READER_KEY_ENTRY("encrypted_reader_key_entry", true),
   ENCRYPTED_READER_KEY("encrypted_reader_key", true),
   ENCRYPTION_ALGORITHM("encryption_algorithm", true),
   PUBLIC_KEY("public_key", true),
   PREFERRED_READER_KEY("preferred_reader_key", true),
   SECURED_IN_OVERALL("secured_in_overall", true),
   DESCRIPTION("description", true),
   IO_TYPE("ioType", true),
   EVENT("event", true),
   DEFAULT_PRIORITY("default_priority", true),
   DELETE("delete", true),
   NAME("name", true),
   IDENTITY_VERIFICATION_FAILED("identity_verification_failed", true),
   HTTP_URL("http_url", true),
   REPRESENTS("represents", true),
   PRODUCT("product", true),
   AMOUNT("amount", true),
   GEO_LONG("http://www.w3.org/2003/01/geo/wgs84_pos#long", false),
   GEO_LAT("http://www.w3.org/2003/01/geo/wgs84_pos#lat", false);
   
   
   private final String uri;

   private DefinedAttributeIdentification(String uri, boolean fromNetInf) {
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

   public static DefinedAttributeIdentification getDefinedAttributeIdentificationByURI(String uri) {
      DefinedAttributeIdentification result = null;

      for (DefinedAttributeIdentification definedAttributeIdentification : DefinedAttributeIdentification.values()) {
         if (definedAttributeIdentification.getURI().equals(uri)) {
            result = definedAttributeIdentification;
            break;
         }
      }

      return result;
   }

   public static String getURIByAttributeIdentification(DefinedAttributeIdentification definedAttributeIdentification) {
      return definedAttributeIdentification.getURI();
   }
}
