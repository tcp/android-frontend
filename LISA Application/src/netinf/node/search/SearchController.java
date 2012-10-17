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
import java.util.Set;

import netinf.common.datamodel.Identifier;
import netinf.common.messages.NetInfMessage;
import netinf.node.api.NetInfNode;
import netinf.node.search.impl.events.SearchEvent;

/**
 * This is the interface for search controllers. Such a controller manages the {@link SearchService}s of a {@link NetInfNode}.
 * <p>
 * If a search request is handed over to this controller, it hands it over to all managed SearchServices and afterwards collects
 * all the results and returns them collectively.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface SearchController {

   /**
    * @return a list of the supported {@link NetInfMessage}s
    */
   List<Class<? extends NetInfMessage>> getSupportedOperations();

   /**
    * called if a {@link NetInfMessage} shall be processed by this search controller
    * 
    * @param netInfMessage
    *           the NetInfMessage to process
    * @return the according response NetInfMessage or <code>null</code> if the message is not supported
    */
   NetInfMessage processNetInfMessage(NetInfMessage netInfMessage);

   /**
    * One possibility to execute a search. The search is specified by a SPARQL query.
    * <p>
    * The search request is handed over to the
    * {@link SearchService#getBySPARQL(String, int, netinf.common.datamodel.identity.SearchServiceIdentityObject, SearchController)}
    * method of all managed SearchServices.
    * 
    * @param request
    *           SPARQL query specifying the search
    * @param searchID
    *           the reference used. Has to be retrieved beforehand by calling {@link #getTimeoutAndNewSearchID(int)}
    * @return a list of the Identifiers of all InformationObjects which matched the search
    */
   Set<Identifier> getBySPARQL(String request, int searchID);

   /**
    * One possibility to execute a search. {@link SearchService}s can have inbuilt query templates which can be used by calling
    * this method.
    * <p>
    * The search request is handed over to the
    * {@link SearchService #getByQueryTemplate(String, List, int, netinf.common.datamodel.identity.SearchServiceIdentityObject, SearchController)}
    * method of all managed SearchServices.
    * 
    * @param type
    *           query template which shall be used for the search
    * @param parameters
    *           values for the parameters of the chosen template type
    * @param searchID
    *           the reference used. Has to be retrieved beforehand by calling {@link #getTimeoutAndNewSearchID(int)}
    * @return a list of the Identifiers of all InformationObjects which matched the search
    */
   Set<Identifier> getByQueryTemplate(String type, List<String> parameters, int searchID);

   /**
    * This method appropriately handles all kinds of {@link SearchEvent}s.
    * 
    * @param event
    *           the SearchEvent to handle
    */
   void handleSearchEvent(SearchEvent event);

   /**
    * This method must be used to prepare a search request.
    * <p>
    * It expects the timeout the caller would like to use for its search request.
    * <p>
    * It returns a unique searchID for the search request, which must be handed over later on with the actual search request.
    * Furthermore it returns the timeout that will be used within the corresponding search request. It is the minimum of the
    * desiredTimeout of the caller and the maximum internal timeout of the SearchController.
    * 
    * @param desiredTimeout
    *           the desired timeout of the caller in milliseconds
    * @return int[2], pos 0: timeout, pos 1: searchID
    */
   int[] getTimeoutAndNewSearchID(int desiredTimeout);

   /**
    * Adds a {@link SearchService} instance to this SearchController. Adding an instance already managed by the controller will
    * have no effect.
    * 
    * @param searchService
    *           the SearchService instance to add
    */
   void addSearchService(SearchService searchService);

   /**
    * Removes the specified {@link SearchService} instance from this SearchController. Trying to remove an instance which is not
    * managed by the controller will have no effect.
    * 
    * @param searchService
    *           the SearchService instance to remove
    */
   void removeSearchService(SearchService searchService);

}
