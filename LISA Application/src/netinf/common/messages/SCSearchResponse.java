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

import netinf.common.datamodel.Identifier;

/**
 * Sent by a SearchController as response to a {@link SCGetBySPARQLRequest} or {@link SCGetByQueryTemplateRequest
 * } message. The
 * message contains the {@link Identifier}s of all information objects that matched the search query.
 * 
 * @see NetInfMessage
 * @author PG Augnet 2, University of Paderborn
 */
public class SCSearchResponse extends NetInfMessage {

   /**
    * list of identifiers of the information objects that matched the search request
    */
   private final List<Identifier> resultIdentifiers = new ArrayList<Identifier>();

   public void addResultIdentifier(Identifier identifier) {
      this.resultIdentifiers.add(identifier);
   }

   public List<Identifier> getResultIdentifiers() {
      return this.resultIdentifiers;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((this.resultIdentifiers == null) ? 0 : this.resultIdentifiers.hashCode());
      return result;
   }

   /**
    * The overridden equals method. It generally holds: For any non-null reference value x, x.equals(null) should return false.
    * That check is implemented in the NetInfMessage class
    * @param obj The object comparing the message to. If it's not of the same type, the superclass method will take care of it
    * @see NetInfMessage
    */
   @Override
   public boolean equals(Object obj) {

      if (!super.equals(obj)) {
         return false;
      }
      SCSearchResponse other = (SCSearchResponse) obj;
      if (this.resultIdentifiers == null) {
         if (other.resultIdentifiers != null) {
            return false;
         }
      } else if (!this.resultIdentifiers.equals(other.resultIdentifiers)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      for (Identifier identifier : this.resultIdentifiers) {
         stringBuilder.append("\nIdentifier: " + identifier);
      }

      return stringBuilder.toString();
   }
}
