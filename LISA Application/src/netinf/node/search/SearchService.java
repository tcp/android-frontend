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
package netinf.node.search;

import java.util.List;

import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.common.search.DefinedQueryTemplates;
import netinf.node.search.impl.events.SearchServiceErrorEvent;
import netinf.node.search.impl.events.SearchServiceResultEvent;

/**
 * A SearchService allows to search for InformationObjects regarding their Attributes. The result of a search request is a list of
 * Identifiers of all InformationObjects which matched the search request.
 * <p>
 * SearchService implementations need to implement this interface.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface SearchService {

   /**
    * One possibility to execute a search. The search is specified by a SPARQL query.
    * <p>
    * The method has to inform the responsible {@link SearchController} about either its search results or an occurred error by
    * calling {@link SearchController#handleSearchEvent(netinf.node.search.impl.events.SearchEvent)} with a
    * {@link SearchServiceResultEvent} respectively a {@link SearchServiceErrorEvent}.
    * 
    * @param request
    *           SPARQL query specifying the search
    * @param searchID
    *           the search ID to pass back on callback
    * @param searchServiceIdO
    *           the SearchServiceIdentityObject to pass back on callback
    * @param callback
    *           the controller that is responsible (to which the SearchService*Event is sent)
    */
   void getBySPARQL(final String request, final int searchID, final SearchServiceIdentityObject searchServiceIdO,
         final SearchController callback);

   /**
    * One possibility to execute a search. A SearchService should have inbuilt query templates regarding some common search
    * requests for which the caller only needs to specify the actual values for some parameters.
    * <p>
    * This way of initiating a search is more user-friendly than using the getBySPARQL() method.
    * <p>
    * Supported query templates should be added to the {@link DefinedQueryTemplates} enumeration.
    * <p>
    * The method has to inform the responsible {@link SearchController} about either its search results or an occurred error by
    * calling {@link SearchController#handleSearchEvent(netinf.node.search.impl.events.SearchEvent)} with a
    * {@link SearchServiceResultEvent} respectively a {@link SearchServiceErrorEvent}.
    * 
    * @param type
    *           query template which shall be used for the search
    * @param parameters
    *           values for the parameters of the chosen template type
    * @param searchID
    *           the search ID to pass back on callback
    * @param searchServiceIdO
    *           the SearchServiceIdentityObject to pass back on callback
    * @param callback
    *           the controller that is responsible (to which the SearchService*Event is sent)
    */
   void getByQueryTemplate(final String type, final List<String> parameters, final int searchID,
         final SearchServiceIdentityObject searchServiceIdO, final SearchController callback);

   /**
    * @return true iff the service is ready to process search requests
    */
   boolean isReady();

   /**
    * @return the SearchServiceIdentityObject which represents the identity of this service
    */
   SearchServiceIdentityObject getIdentityObject();

   /**
    * Returns a textual description ("I can search via "+describe())
    * 
    * @return textual description
    */
   String describe();

}
