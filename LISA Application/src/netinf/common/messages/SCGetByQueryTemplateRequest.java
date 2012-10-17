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

import netinf.common.search.DefinedQueryTemplates;

/**
 * This message can be used to search for information objects. The search is performed based on predefined queries. The sender has
 * to specify the query template that should be used and the values that should be used for the parameters. The available query
 * templates and their parameters can be looked up in {@link DefinedQueryTemplates}.
 * <p>
 * This message is handled by a SearchController. The controller answers with a {@link SCSearchResponse} message which contains
 * the result of the search.
 * <p>
 * Before sending this message, the search request has to be initialized by sending a {@link SCGetTimeoutAndNewSearchIDRequest}.
 * The searchID that is returned by that message has to be specified as a parameter in this message.
 * 
 * @see NetInfMessage
 * @author PG Augnet 2, University of Paderborn
 */
public class SCGetByQueryTemplateRequest extends NetInfMessage {

   /**
    * The type of the query template that should be used. Has to be one of the types defined in {@link DefinedQueryTemplates}.
    */
   private String type;

   /**
    * The values for the parameters of the chosen query type. The parameters and their underlying needed data type can be looked
    * up in {@link DefinedQueryTemplates}.
    */
   private final List<String> parameters = new ArrayList<String>();

   /**
    * The ID identifying this search request. It has to be requested in advance by sending a
    * {@link SCGetTimeoutAndNewSearchIDRequest}.
    */
   private int searchID;

   public SCGetByQueryTemplateRequest(String type, int searchID) {
      super();
      this.type = type;
      this.searchID = searchID;
   }

   public int getSearchID() {
      return this.searchID;
   }

   public void setSearchID(int searchID) {
      this.searchID = searchID;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public List<String> getParameters() {
      return this.parameters;
   }

   public void addParameter(String parameter) {
      this.parameters.add(parameter);
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
      result = prime * result + this.searchID;
      result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {

      if (!super.equals(obj)) {
         return false;
      }
      SCGetByQueryTemplateRequest other = (SCGetByQueryTemplateRequest) obj;
      if (this.parameters == null) {
         if (other.parameters != null) {
            return false;
         }
      } else if (!this.parameters.equals(other.parameters)) {
         return false;
      }
      if (this.searchID != other.searchID) {
         return false;
      }
      if (this.type == null) {
         if (other.type != null) {
            return false;
         }
      } else if (!this.type.equals(other.type)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      stringBuilder.append("\nType: " + this.type);
      for (String parameter : this.parameters) {
         stringBuilder.append("\nParameter: " + parameter);
      }
      stringBuilder.append("\nSearchID: " + this.searchID);

      return stringBuilder.toString();
   }

}
