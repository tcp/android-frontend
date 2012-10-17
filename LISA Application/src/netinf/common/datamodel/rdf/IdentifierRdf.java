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
package netinf.common.datamodel.rdf;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This is a {@link IdentifierRdf}, it might only be used within the rdf-implementation of the datamodel
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IdentifierRdf extends NetInfObjectWrapperRdf implements Identifier {

   private static final Logger LOG = Logger.getLogger(IdentifierRdf.class);
   private InformationObjectRdf informationObjectRdf;

   private final Hashtable<String, IdentifierLabel> identifierLabels;

   public IdentifierRdf(DatamodelFactoryRdf datamodelFactoryRdf) {
      super(datamodelFactoryRdf);
      this.identifierLabels = new Hashtable<String, IdentifierLabel>();
   }

   @Override
   public void addIdentifierLabel(IdentifierLabel identifierLabel) {
      IdentifierLabelRdf identifierLabelRdf = (IdentifierLabelRdf) identifierLabel;
      Identifier identifier = identifierLabelRdf.getIdentifier();

      // Remove from old identifier
      if (identifier != null && identifier != this) {
         identifier.removeIdentifierLabel(identifierLabelRdf);
      }

      identifierLabelRdf.setIdentifier(this);
      this.identifierLabels.put(identifierLabel.getLabelName(), identifierLabel);
      renameResourceIfBound();
   }

   @Override
   public IdentifierLabel getIdentifierLabel(String labelName) {
      return this.identifierLabels.get(labelName);
   }

   @Override
   public List<IdentifierLabel> getIdentifierLabels() {
      ArrayList<IdentifierLabel> list = new ArrayList<IdentifierLabel>(this.identifierLabels.values());
      Collections.sort(list);
      return list;
   }

   @Override
   @Deprecated
   public void initFromString(String string) {
      // First remove all identifierLabels
      Collection<IdentifierLabel> values = this.identifierLabels.values();

      for (IdentifierLabel identifierLabel : values) {
         removeIdentifierLabel(identifierLabel);
      }

      try {
         List<IdentifierLabel> labels = DatamodelUtils.getIdentifierLabels(string, this.getDatamodelFactory());

         for (IdentifierLabel identifierLabel : labels) {
            addIdentifierLabel(identifierLabel);
         }

      } catch (NetInfUncheckedException e) {
         LOG.error("Could not use given String '" + string + "' as identifier");
      }
   }

   @Override
   public boolean isVersioned() {
      return DatamodelUtils.isIdentifierVersioned(this);
   }

   @Override
   public void removeIdentifierLabel(IdentifierLabel identifierLabel) {
      if (identifierLabel != null) {
         if (this.identifierLabels.contains(identifierLabel)) {
            ((IdentifierLabelRdf) identifierLabel).setIdentifier(null);
            this.identifierLabels.remove(identifierLabel.getLabelName());
            renameResourceIfBound();
         }
      }
   }

   @Override
   public void removeIdentifierLabel(String labelName) {
      removeIdentifierLabel(this.identifierLabels.get(labelName));
   }

   @Override
   public byte[] serializeToBytes() {

      DatamodelFactoryRdf datamodelFactoryRdf = (DatamodelFactoryRdf) this.getDatamodelFactory();
      Model identifierModel = datamodelFactoryRdf.createModelForIdentifier();

      Resource carrierResource = identifierModel.createResource();
      Resource resourceWithName = identifierModel.createResource(DatamodelUtils.toStringIdentifier(this));

      identifierModel.add(carrierResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IDENTIFIER),
            resourceWithName);

      StringWriter stringWriter = new StringWriter();
      identifierModel.write(stringWriter, DefinedRdfNames.NETINF_RDF_STORAGE_FORMAT);
      return Utils.stringToBytes(stringWriter.toString());
   }

   @Override
   public String toString() {
      return DatamodelUtils.toStringIdentifier(this);
   }

   /*
    * NOT generated (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.identifierLabels == null) ? 0 : this.identifierLabels.hashCode());
      return result;
   }

   /*
    * NOT generated (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      return DatamodelUtils.equalIdentifiers(this, obj);
   }

   // ****** Internal methods ********//

   private boolean isBound() {
      return this.informationObjectRdf != null && this.informationObjectRdf.getResource() != null;
   }

   public void renameResourceIfBound() {
      if (isBound()) {
         this.informationObjectRdf.updateNameOfResource();
      }
   }

   public void setInformationObject(InformationObjectRdf informationObjectRdf) {
      this.informationObjectRdf = informationObjectRdf;
   }

   public InformationObjectRdf getInformationObject() {
      return this.informationObjectRdf;
   }

   @Override
   protected void addToResource(Resource givenResource) {
      throw new UnsupportedOperationException("This is not possible from an Identifier");
   }

   @Override
   protected void removeFromResource(Resource resource) {
      throw new UnsupportedOperationException("This is not possible from an Identifier");
   }

   @Override
   public void initFromResource(Resource resource) throws NetInfCheckedException {
      String uri = resource.getURI();
      if (uri != null) {
         initFromString(uri);
      }
   }

   @Override
   public String describe() {
      ArrayList<String> descriptions = new ArrayList<String>();
      if (getIdentifierLabel(DefinedLabelName.VERSION_KIND.getLabelName()) != null) {
         if (getIdentifierLabel(DefinedLabelName.VERSION_KIND.getLabelName()).getLabelValue().equals(
               DefinedVersionKind.VERSIONED.name())) {
            descriptions.add("is versioned");
         }
         if (getIdentifierLabel(DefinedLabelName.VERSION_KIND.getLabelName()).getLabelValue().equals(
               DefinedVersionKind.UNVERSIONED.name())) {
            descriptions.add("is unversioned");
         }
         if (getIdentifierLabel(DefinedLabelName.VERSION_KIND.getLabelName()).getLabelValue().equals(
               DefinedVersionKind.UNKNOWN.name())) {
            descriptions.add("might be versioned or not");
         }
      }
      if (getIdentifierLabel(DefinedLabelName.VERSION_NUMBER.getLabelName()) != null) {
         descriptions.add("has version number "
               + getIdentifierLabel(DefinedLabelName.VERSION_NUMBER.getLabelName()).getLabelValue());
      }
      if (getIdentifierLabel(DefinedLabelName.HASH_OF_PK.getLabelName()) != null) {
         descriptions.add("whose PK-hash is "
               + getIdentifierLabel(DefinedLabelName.HASH_OF_PK.getLabelName()).getLabelValue().substring(0, 5) + "...");
      }
      if (getIdentifierLabel(DefinedLabelName.HASH_OF_PK_IDENT.getLabelName()) != null) {
         descriptions.add("whose PK is hashed with "
               + getIdentifierLabel(DefinedLabelName.HASH_OF_PK_IDENT.getLabelName()).getLabelValue());
      }
      if (getIdentifierLabel(DefinedLabelName.UNIQUE_LABEL.getLabelName()) != null) {
         descriptions.add("has unique label " + getIdentifierLabel(DefinedLabelName.UNIQUE_LABEL.getLabelName()).getLabelValue());
      }
      for (IdentifierLabel label : getIdentifierLabels()) {

         if (!DefinedLabelName.isDefined(label.getLabelName())) {
            descriptions.add("whose value of " + label.getLabelName() + " is " + label.getLabelValue());

         }
      }

      if (descriptions.isEmpty()) {
         return "";
      }
      if (descriptions.size() == 1) {
         return descriptions.get(0);
      }

      StringBuffer buf = new StringBuffer(descriptions.get(0));
      for (int i = 1; i < (descriptions.size() - 1); i++) {
         buf.append(", ");
         buf.append(descriptions.get(i));
      }
      buf.append(" and ");
      buf.append(descriptions.get(descriptions.size() - 1));

      return buf.toString();
   }
}
