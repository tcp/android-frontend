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

/**
 * This message can be used to search for information objects. The search query has to be specified as a SPARQL query. The
 * requirements towards the SPARQL query can be found below in the description of the <code>request</code> parameter.
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
public class SCGetBySPARQLRequest extends NetInfMessage {
   /**
    * The SPARQL query that should be used for the search.
    * <p>
    * Note the following instructions:
    * <ul>
    * <li>The Select-Clause is hard-coded, so it is only necessary to specify the WHERE-Clause</li>
    * <li>The variable <code>?id</code> points to the identifier within the rdf representation of IOs. Values, which are bound to
    * this variable, are returned.</li>
    * </ul>
    * The following prefixes can be used:
    * <ul>
    * <li>rdf -> http://www.w3.org/1999/02/22-rdf-syntax-ns#</li>
    * <li>netinf -> http://rdf.netinf.org/2009/netinf-rdf/1.0/#</li>
    * </ul>
    * Example SPARQL query: <br>
    * <code>?id &ltattribute-uri&gt ?blankNode . ?blankNode netinf:attributeValue "String:TestValue."</code>
    */
   private String request;
   /**
    * The ID identifying this search request. It has to be requested in advance by sending a
    * {@link SCGetTimeoutAndNewSearchIDRequest}.
    */
   private int searchID;

   public SCGetBySPARQLRequest(String request, int searchID) {
      super();
      this.request = request;
      this.searchID = searchID;
   }

   public String getRequest() {
      return this.request;
   }

   public void setRequest(String request) {
      this.request = request;
   }

   public int getSearchID() {
      return this.searchID;
   }

   public void setSearchID(int searchID) {
      this.searchID = searchID;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((this.request == null) ? 0 : this.request.hashCode());
      result = prime * result + this.searchID;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (!super.equals(obj)) {
         return false;
      }
      SCGetBySPARQLRequest other = (SCGetBySPARQLRequest) obj;
      if (this.request == null) {
         if (other.request != null) {
            return false;
         }
      } else if (!this.request.equals(other.request)) {
         return false;
      }
      if (this.searchID != other.searchID) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      stringBuilder.append("\nRequest: " + this.request);
      stringBuilder.append("\nSearchID: " + this.searchID);

      return stringBuilder.toString();
   }

}
