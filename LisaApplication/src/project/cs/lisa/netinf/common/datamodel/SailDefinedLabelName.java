/* Modified by Linus and Harold
 * Based on netinf.common.datamodel.DefinedLabelName
 * added CONTENT_TYPE ("CONTENT_TYPE",6);
 *     a label that contains the content type
 * added constructors to utilise this label
 */

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

import java.util.ArrayList;
import java.util.List;

/**
 * This enumeration type contains all the defined label names. The string of each defined label name is the user-readable
 * labelname within identifiers. The number of each defined label name defines the ordering among the label names. This guarantees
 * uniqueness among the labelnames.
 *
 * @author PG Augnet 2, University of Paderborn
 */
public enum SailDefinedLabelName {

   AUTHORITY    ("AUTHORITY", 1),
   HASH_ALG     ("HASH_ALG", 2),
   HASH_CONTENT ("HASH_CONTENT", 3),
   FILE_LOCATOR ("FILE_LOCATOR", 4),
   TTL          ("TTL", 5),
   CONTENT_TYPE ("CONTENT_TYPE", 6),
   META_DATA    ("META_DATA", 7);

   private final String labelName;
   private final int order;

   private SailDefinedLabelName(String labelName, int order) {
      this.labelName = labelName;
      this.order = order;
   }

   public String getLabelName() {
      return this.labelName;
   }

   public int getOrder() {
      return this.order;
   }

   public static SailDefinedLabelName getDefinedLabelNameByString(String labelName) {
      SailDefinedLabelName result = null;

      for (SailDefinedLabelName definedLabelName : SailDefinedLabelName.values()) {
         if (definedLabelName.getLabelName().equals(labelName)) {
            result = definedLabelName;
            break;
         }
      }

      return result;
   }

   public static List<String> valueStrings() {
      ArrayList<String> list = new ArrayList<String>();
      for (SailDefinedLabelName defLabel : values()) {
         list.add(defLabel.getLabelName());
      }
      return list;
   }

   public static boolean isDefined(String string) {
      return valueStrings().contains(string);
   }

}
