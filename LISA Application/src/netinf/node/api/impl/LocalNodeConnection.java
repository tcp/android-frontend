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
package netinf.node.api.impl;

import java.util.ArrayList;
import java.util.List;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DeleteMode;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.search.DefinedQueryTemplates;
import netinf.node.api.NetInfNode;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Class LocalNodeConnection.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class LocalNodeConnection implements NetInfNodeConnection {

   private static final Logger LOG = Logger.getLogger(LocalNodeConnection.class);

   private final NetInfNode node;

   private final DatamodelFactory datamodelFactory;

   @Inject
   public LocalNodeConnection(NetInfNode node, DatamodelFactory datamodelFactory) {
      this.node = node;
      this.datamodelFactory = datamodelFactory;
   }

   @Override
   public void deleteIO(InformationObject informationObject, DeleteMode deleteMode) throws NetInfCheckedException {
      Attribute attr = this.datamodelFactory
            .createAttribute(DefinedAttributeIdentification.DELETE.getURI(), deleteMode.getMode());
      informationObject.addAttribute(attr);

      LOG.log(DemoLevel.DEMO, "(CCOMM) Deleting " + informationObject.describe() + " by sending a special IO to the node");
      putIO(informationObject);
   }

   @Override
   public InformationObject getIO(Identifier identifier) throws NetInfCheckedException {
      return this.node.getResolutionController().get(identifier);
   }

   @Override
   public InformationObject getIO(Identifier identifier, String userName, String privateKey) throws NetInfCheckedException {
      return this.node.getResolutionController().get(identifier, userName, privateKey);
   }

   @Override
   public ArrayList<InformationObject> getIOs(Identifier identifier) throws NetInfCheckedException {
      InformationObject io = this.node.getResolutionController().get(identifier);
      if (io != null) {
         ArrayList<InformationObject> ios = new ArrayList<InformationObject>();
         ios.add(io);
         return ios;
      } else {
         return null;
      }
   }

   @Override
   public ArrayList<InformationObject> getIOs(Identifier identifier, String userName, String privateKey)
         throws NetInfCheckedException {
      InformationObject io = this.node.getResolutionController().get(identifier, userName, privateKey);
      if (io != null) {
         ArrayList<InformationObject> ios = new ArrayList<InformationObject>();
         ios.add(io);
         return ios;
      } else {
         return null;
      }
   }

   @Override
   public List<Identifier> performSearch(String sparqlQuery, int desiredTimeout) throws NetInfCheckedException {
      int[] searchID = this.node.getSearchController().getTimeoutAndNewSearchID(desiredTimeout);
      return new ArrayList<Identifier>(this.node.getSearchController().getBySPARQL(sparqlQuery, searchID[1]));
   }

   @Override
   public List<Identifier> performSearch(DefinedQueryTemplates type, String[] parameters, int desiredTimeout)
         throws NetInfCheckedException {
      int[] searchID = this.node.getSearchController().getTimeoutAndNewSearchID(desiredTimeout);
      List<String> params = new ArrayList<String>();
      for (String p : parameters) {
         params.add(p);
      }
      return new ArrayList<Identifier>(this.node.getSearchController().getByQueryTemplate(type.getQueryTemplateName(), params,
            searchID[1]));
   }

   @Override
   public void putIO(InformationObject informationObject) throws NetInfCheckedException {
      this.node.getResolutionController().put(informationObject);
   }

   @Override
   public void putIO(InformationObject informationObject, String userName, String privateKey) throws NetInfCheckedException {
      this.node.getResolutionController().put(informationObject, userName, privateKey);
   }

}
