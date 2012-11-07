/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
/**
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
package project.cs.lisa.netinf.node.access.rest.resources;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;

import org.restlet.resource.ServerResource;

import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.netinf.node.access.rest.RESTApplication;

/**
 * Abstract resource that provides a NetInfNodeConnection and a DatamodelFactory.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public abstract class LisaServerResource extends ServerResource {

    /**
     * Yields a connection to a NetInfNode.
     * 
     * @return A NetInfNodeConnection of the parent application
     */

    protected NetInfNodeConnection getNodeConnection() {
        return ((RESTApplication) getApplication()).getNodeConnection();
    }

    /**
     * Yields an implementation of a DatamodelFactory.
     * 
     * @return A concrete DatamodelFactory
     */
    protected DatamodelFactory getDatamodelFactory() {
        return ((RESTApplication) getApplication()).getDatamodelFactory();
    }

    /**
     * Creates a NetInf Identifier given a number of plain Strings.
     * 
     * @param hashAlg
     * 			Hash Algorithm
     * @param hash
     * 			Hash
     * @param contentType
     *          Content Type
     * @return NetInf Identifier
     */
    protected Identifier createIdentifier(String hashAlg, String hash, String contentType) {
      
        Identifier identifier = getDatamodelFactory().createIdentifier();
      
        //Creating the HASH_ALG label
        IdentifierLabel identifierLabel = getDatamodelFactory().createIdentifierLabel();
        identifierLabel.setLabelName(SailDefinedLabelName.HASH_ALG.getLabelName());
        identifierLabel.setLabelValue(hashAlg);
        identifier.addIdentifierLabel(identifierLabel);
    
        //Creating the HASH_CONTENT label
        IdentifierLabel identifierLabel2 = getDatamodelFactory().createIdentifierLabel();
        identifierLabel2.setLabelName(SailDefinedLabelName.HASH_CONTENT.getLabelName());
        identifierLabel2.setLabelValue(hash);
        identifier.addIdentifierLabel(identifierLabel2);
        
      //Creating the HASH_CONTENT label
        IdentifierLabel identifierLabel3 = getDatamodelFactory().createIdentifierLabel();
        identifierLabel3.setLabelName(SailDefinedLabelName.CONTENT_TYPE.getLabelName());
        identifierLabel3.setLabelValue(contentType);
        identifier.addIdentifierLabel(identifierLabel3);
        
        return identifier;
   }
    
    /**
     * Creates a NetInf Identifier given a number of plain Strings.
     * 
     * @param hashAlg
     *          Hash Algorithm
     * @param hash
     *          Hash
     * @return NetInf Identifier
     */
    protected Identifier createIdentifier(String hashAlg, String hash) {
      
        Identifier identifier = getDatamodelFactory().createIdentifier();
      
        //Creating the HASH_ALG label
        IdentifierLabel identifierLabel = getDatamodelFactory().createIdentifierLabel();
        identifierLabel.setLabelName(SailDefinedLabelName.HASH_ALG.getLabelName());
        identifierLabel.setLabelValue(hashAlg);
        identifier.addIdentifierLabel(identifierLabel);
    
        //Creating the HASH_CONTENT label
        IdentifierLabel identifierLabel2 = getDatamodelFactory().createIdentifierLabel();
        identifierLabel2.setLabelName(SailDefinedLabelName.HASH_CONTENT.getLabelName());
        identifierLabel2.setLabelValue(hash);
        identifier.addIdentifierLabel(identifierLabel2);
        
        return identifier;
   }

}
