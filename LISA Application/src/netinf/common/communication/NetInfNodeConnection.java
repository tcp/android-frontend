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
package netinf.common.communication;

import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DeleteMode;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.search.DefinedQueryTemplates;

/**
 * The connection to a NetInfNode, see {@link RemoteNodeConnection} and LocalNodeConnection
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface NetInfNodeConnection {

   /**
    * Fetches all InformationObject instances with the given Identifier (i.e. all versions)
    * 
    * @param identifier
    *           the Identifier
    * @return a list of found InformationObject instances
    * @throws NetInfCheckedException
    */
   ArrayList<InformationObject> getIOs(Identifier identifier) throws NetInfCheckedException;

   /**
    * Fetches all InformationObject instances with the given Identifier (i.e. all versions)
    * 
    * @param identifier
    *           the Identifier
    * @param userName
    *           the username used if middleware functions are required
    * @param privateKey
    *           the private key used if middleware functions are required
    * @return a list of found InformationObject instances
    * @throws NetInfCheckedException
    */
   ArrayList<InformationObject> getIOs(Identifier identifier, String userName, String privateKey) throws NetInfCheckedException;

   /**
    * Fetches the InformationObject instance with the given Identifier
    * 
    * @param identifier
    *           the Identifier
    * @return a found InformationObject instance. If no Information Object could be found <code>null</code> is returned.
    * @throws NetInfCheckedException
    * @throws NetInfDeletedIOException
    *            if the IO which is returned has been marked as deleted by the owner.
    */
   InformationObject getIO(Identifier identifier) throws NetInfCheckedException;

   /**
    * Fetches the InformationObject instance with the given Identifier
    * 
    * @param identifier
    *           the Identifier
    * @param userName
    *           the username used if middleware functions are required
    * @param privateKey
    *           the private key used if middleware functions are required
    * @return a found InformationObject instance. If no Information Object could be found <code>null</code> is returned.
    * @throws NetInfCheckedException
    * @throws NetInfDeletedIOException
    *            if the IO which is returned has been marked as deleted by the owner.
    */
   InformationObject getIO(Identifier identifier, String userName, String privateKey) throws NetInfCheckedException;

   /**
    * Creates a new InformationObject or modifies an existing InformationObject
    * 
    * @param informationObject
    *           the InformationObject
    * @throws NetInfCheckedException
    */
   void putIO(InformationObject informationObject) throws NetInfCheckedException;

   /**
    * Creates a new InformationObject or modifies an existing InformationObject
    * 
    * @param informationObject
    *           the InformationObject
    * @param userName
    *           the username used if middleware functions are required
    * @param privateKey
    *           the private key used if middleware functions are required
    * @throws NetInfCheckedException
    */
   void putIO(InformationObject informationObject, String userName, String privateKey) throws NetInfCheckedException;

   void deleteIO(InformationObject informationObject, DeleteMode deleteMode) throws NetInfCheckedException;

   /**
    * Returns a list of Identifier instances whose InformationObject matches the SPARQL query.
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
    * Example SPARQL query:<br>
    * <code>?id &ltattribute-uri&gt ?blankNode . ?blankNode netinf:attributeValue "String:TestValue."</code>
    * <p>
    * 
    * @param sparqlQuery
    *           the SPARQL query
    * @param desiredTimeout
    *           the timeout that should be used for the search (the actually used timeout may be smaller in case the timeout of
    *           the used search controller is smaller) (in ms)
    * @return a list of Identifier instances
    * @throws NetInfCheckedException
    */
   List<Identifier> performSearch(String sparqlQuery, int desiredTimeout) throws NetInfCheckedException;

   /**
    * @param type
    *           the query template to use
    * @param parameters
    *           the values to use for the parameters of the query template
    * @param desiredTimeout
    *           the timeout that should be used for the search (the actually used timeout may be smaller in case the timeout of
    *           the used search controller is smaller)
    * @return a list of Identifier instances
    * @throws NetInfCheckedException
    */
   List<Identifier> performSearch(DefinedQueryTemplates type, String[] parameters, int desiredTimeout)
   throws NetInfCheckedException;

}
