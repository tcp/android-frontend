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
package netinf.common.datamodel;

/**
 * A {@link DefinedAttributePurpose} determines how the attribute has to be interpreted. Is it used system internally to handle
 * particular pieces of functionality not directly visible to the user, or is the attribute created by the user? This is
 * determined in this class.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public enum DefinedAttributePurpose {

   USER_ATTRIBUTE("USER_ATTRIBUTE"),
   SYSTEM_ATTRIBUTE("SYSTEM_ATTRIBUTE"),
   LOCATOR_ATTRIBUTE("LOCATOR_ATTRIBUTE");

   private final String attributePurpose;

   public String getAttributePurpose() {
      return attributePurpose;
   }

   private DefinedAttributePurpose(String attributePurpose) {
      this.attributePurpose = attributePurpose;
   }

   public static DefinedAttributePurpose getDefinedAttributePurpose(String attributePurpose) {
      DefinedAttributePurpose result = null;

      for (DefinedAttributePurpose definedAttributePurpose : DefinedAttributePurpose.values()) {
         if (definedAttributePurpose.getAttributePurpose().equals(attributePurpose)) {
            result = definedAttributePurpose;
            break;
         }
      }

      return result;
   }
}
