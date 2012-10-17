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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.rdf.attribute.AttributeRdf;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.exceptions.NetInfWrongAttributeException;
import netinf.common.security.impl.IntegrityImpl;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * This is a {@link InformationObjectRdf}, it might only be used within the rdf-implementation of the datamodel
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class InformationObjectRdf extends NetInfObjectWrapperRdf implements InformationObject {

   private static final Logger LOG = Logger.getLogger(InformationObjectRdf.class);

   private final List<AttributeRdf> attributes = new ArrayList<AttributeRdf>();
   private IdentifierRdf identifier;

   public InformationObjectRdf(Model model, DatamodelFactoryRdf datamodelFactoryRdf) throws NetInfCheckedException {
      super(datamodelFactoryRdf);

      String strNull = null;

      StmtIterator listStatements = model.listStatements(null, DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO),
            strNull);
      if (listStatements.hasNext()) {

         // This must be the one referring to the information object
         Statement next = listStatements.next();

         if (next.getObject().isResource()) {

            // This might lead to an error
            Resource resource = (Resource) next.getObject();
            initFromResource(resource);
         } else {
            throw new NetInfUncheckedException("Could not find resource of information object");
         }
      } else {
         throw new NetInfCheckedException("Could not find the required information object");
      }
   }

   @Override
   public void addAttribute(Attribute attribute) {
      AttributeRdf attributeRdf = (AttributeRdf) attribute;

      // The attribute is bound and has to be removed from its parent (informationobject or parent attribute
      if (attributeRdf.getInformationObject() != null || attributeRdf.getParentAttribute() != null) {
         if (attributeRdf.getParentAttribute() != null) {
            attributeRdf.getParentAttribute().removeSubattribute(attributeRdf);
         } else {
            attributeRdf.getInformationObject().removeAttribute(attributeRdf);
         }
      }

      attributeRdf.setInformationObject(this);

      if (this.getResource() != null) {
         Resource resourceForAttribute = this.getResource().getModel().createResource();
         attributeRdf.bindToResource(resourceForAttribute);
      }

      this.attributes.add(attributeRdf);
   }

   @Override
   public void removeAttribute(Attribute attribute) {
      if (this.attributes.contains(attribute)) {
         AttributeRdf attributeRdf = (AttributeRdf) attribute;
         attributeRdf.setInformationObject(null);
         this.attributes.remove(attribute);

         if (this.getResource() != null) {
            // Thus, the attribute is not attached to any resource anymore
            attributeRdf.bindToResource(null);
         }
      }
   }

   @Override
   public void initFromResource(Resource resource) throws NetInfCheckedException {
      // TODO: Check validity of Resource
      // TODO: remove all the old values from this InformationObjectT

      StmtIterator listProperties = resource.listProperties();
      this.setResource(resource);

      // First update identifier, only in case we have a valid identifier.
      String uri = this.getResource().getURI();
      if (uri != null) {
         IdentifierRdf tmpIdentifier = (IdentifierRdf) getDatamodelFactory().createIdentifier();
         tmpIdentifier.initFromResource(resource);
         setIdentifier(tmpIdentifier);
      }

      while (listProperties.hasNext()) {
         Statement statement = listProperties.next();

         AttributeRdf attribute = (AttributeRdf) getDatamodelFactory().createAttribute();

         // It is important to have the parent
         attribute.setInformationObject(this);

         // Initialize from resource
         Resource tmpResource = (Resource) statement.getObject();

         try {
            attribute.initFromResource(tmpResource);
            this.attributes.add(attribute);

         } catch (NetInfCheckedException e) {
            LOG.debug("Could not initialize Attribute from Resource, ignoring Resource: " + tmpResource);
         }
      }

   }

   @Override
   public List<Attribute> getAttribute(String attributeIdentification) {
      ArrayList<Attribute> result = new ArrayList<Attribute>();

      if (attributeIdentification != null) {

         for (Attribute attribute : this.attributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               result.add(attribute);
            }
         }
      }

      Collections.sort(result);
      return result;
   }

   @Override
   public List<Attribute> getAttributes() {
      ArrayList<Attribute> result = new ArrayList<Attribute>(this.attributes);
      Collections.sort(result);
      return result;
   }

   @Override
   public List<Attribute> getAttributesForPurpose(String attributePurpose) {
      List<Attribute> result = getAttributes();
      Iterator<Attribute> iteratorAttributes = result.iterator();

      while (iteratorAttributes.hasNext()) {
         Attribute attribute = iteratorAttributes.next();

         if (!attribute.getAttributePurpose().equals(attributePurpose)) {
            iteratorAttributes.remove();
         }
      }

      return result;
   }

   @Override
   public Attribute getSingleAttribute(String attributeIdentification) {
      Attribute result = null;

      if (attributeIdentification != null) {

         for (Attribute attribute : this.attributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               result = attribute;
               break;
            }
         }
      }

      return result;
   }

   @Override
   public void removeAttribute(String attributeIdentification) {
      ArrayList<Attribute> toDelete = new ArrayList<Attribute>();

      if (attributeIdentification != null) {
         for (Attribute attribute : this.attributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               toDelete.add(attribute);
            }
         }
      }

      for (Attribute attribute : toDelete) {
         removeAttribute(attribute);
      }
   }

   @Override
   public List<Identifier> getReaderIdentifiers() {

      List<Attribute> readerListProperties = this.getAttribute(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());
      List<Identifier> identifierList = new ArrayList<Identifier>();

      for (Attribute readerList : readerListProperties) {
         List<Attribute> readerProperties = readerList.getSubattribute(DefinedAttributeIdentification.READER.getURI());
         for (Attribute reader : readerProperties) {
            try {
               String readerIdentifierString = reader.getValue(String.class);
               if (readerIdentifierString.indexOf(IntegrityImpl.PATH_SEPERATOR) != -1) {
                  readerIdentifierString = readerIdentifierString.substring(0,
                        readerIdentifierString.indexOf(IntegrityImpl.PATH_SEPERATOR));
               }

               Identifier identifier = getDatamodelFactory().createIdentifierFromString(readerIdentifierString);
               identifierList.add(identifier);
            } catch (Exception e) {
               LOG.warn("Unable to get reader Identifier from String " + reader + ". " + e.getMessage());
            }
         }
      }

      return identifierList;
   }

   @Override
   public List<String> getWriterPaths() {
      List<Attribute> writerProperties = this.getAttribute(DefinedAttributeIdentification.AUTHORIZED_WRITERS.getURI());

      List<String> pathList = new ArrayList<String>();

      Attribute owner = this.getSingleAttribute(DefinedAttributeIdentification.OWNER.getURI());
      if (owner == null) {
         LOG.warn("No owner for IO. Thus, Authorized Writers can't be verified");
         return pathList;
      }

      for (Attribute writer : writerProperties) {
         //
         // each authorized Writer has to be signed by Owner. Check this first
         Attribute writerOfSignature = writer.getSingleSubattribute(DefinedAttributeIdentification.WRITER.getURI());
         if (writerOfSignature != null) {
            if (!writerOfSignature.getValue(String.class).equals(owner.getValue(String.class))) {
               // Authorized Writer is not signed by owner, thus this is not an authorized writer. skipping
               continue;
            }
         } else {
            continue;
         }

         try {
            String writerIdentifierString = writer.getValue(String.class);
            pathList.add(writerIdentifierString);
         } catch (Exception e) {
            LOG.warn("Unable to get writer. " + e.getMessage());
         }
      }

      return pathList;
   }

   @Override
   public void setIdentifier(Identifier givenIdentifier) {
      IdentifierRdf identifierRdf = (IdentifierRdf) givenIdentifier;

      // The Identifier is removed
      if (identifierRdf == null) {
         this.identifier.setInformationObject(null);
         this.identifier = null;
         updateNameOfResource();
      } else {
         InformationObjectRdf informationObject = identifierRdf.getInformationObject();

         // Only add the identifier, if it was not already added previously
         if (informationObject != this) {

            // Already bound, remove the identifier from the other information object.
            if (informationObject != null) {
               informationObject.setIdentifier(null);
            }

            // Unbind old identifier
            if (this.identifier != null) {
               this.identifier.setInformationObject(null);
            }

            // now add the identifier to this information object
            this.identifier = identifierRdf;
            this.identifier.setInformationObject(this);
            updateNameOfResource();
         }
      }
   }

   @Override
   public Identifier getIdentifier() {
      return this.identifier;
   }

   @Override
   public byte[] serializeToBytes() {
      if (this.getResource() != null) {
         StringWriter stringWriter = new StringWriter();
         this.getResource().getModel().write(stringWriter, DefinedRdfNames.NETINF_RDF_STORAGE_FORMAT);
         return Utils.stringToBytes(stringWriter.toString());
      } else {
         throw new NetInfUncheckedException("Could not serialize RDF-Object, which is not bound to an rdf resource");
      }
   }

   @Override
   protected void addToResource(Resource givenResource) {
      this.setResource(givenResource);

      Model model = this.getResource().getModel();
      Resource firstResource = model.createResource();

      model.add(firstResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO), this.getResource());
      model.add(firstResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.IO_TYPE), getClass().getCanonicalName());

      // Now all subattributes
      Iterator<AttributeRdf> subAttributeIterator = this.attributes.iterator();

      while (subAttributeIterator.hasNext()) {
         AttributeRdf next = subAttributeIterator.next();
         Resource resource = model.createResource();
         next.bindToResource(resource);
      }
   }

   @Override
   protected void removeFromResource(Resource resource) {
      if (this.getResource() != resource) {
         throw new NetInfUncheckedException("Trying to unbind from a resource, which was not bound");
      }
      Model model = resource.getModel();

      List<Resource> list = model.listResourcesWithProperty(DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO),
            resource).toList();

      if (list.size() == 1) {
         Resource transportResource = list.get(0);

         model.remove(transportResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO), resource);
         transportResource.removeAll(DatamodelFactoryRdf.getProperty(DefinedRdfNames.IO_TYPE));
      } else {
         throw new NetInfUncheckedException("Could not remove InformationObject from Resource");
      }

      this.setResource(null);

      // Remove the connection to parent
      Iterator<AttributeRdf> subAttributeIterator = this.attributes.iterator();

      while (subAttributeIterator.hasNext()) {
         AttributeRdf next = subAttributeIterator.next();
         next.bindToResource(null);
      }
   }

   /*
    * This method is NOT generated (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      // Check hashCode of sorted list
      result = prime * result + ((this.attributes == null) ? 0 : getAttributes().hashCode());
      result = prime * result + ((this.identifier == null) ? 0 : this.identifier.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      return DatamodelUtils.equalInformationObjects(this, obj);
   }

   // **** Internal Methods *****/

   protected void resetAttribute(String identification, Object value, String purpose) {
      if (getSingleAttribute(identification) == null) {
         AttributeRdf attributeRdf = (AttributeRdf) getDatamodelFactory().createAttribute();
         attributeRdf.setIdentification(identification);
         attributeRdf.setValue(value);
         attributeRdf.setAttributePurpose(purpose);

         addAttribute(attributeRdf);
      } else {
         getSingleAttribute(identification).setValue(value);
         getSingleAttribute(identification).setAttributePurpose(purpose);
      }
   }

   protected void resetAttribute(String identification, Attribute attribute) {
      if (identification.equals((attribute.getIdentification()))) {
         removeAttribute(identification);
         addAttribute(attribute);
      } else {
         throw new NetInfWrongAttributeException("The attribute being set has the identification '"
               + attribute.getIdentification() + "' but expected was '" + identification + "'");
      }
   }

   public void updateNameOfResource() {
      if (this.getResource() != null) {
         if (this.identifier == null) {
            String name = null;

            // Rename it, only when the other resource was not already set to null
            if (this.getResource().getURI() != null) {
               Resource renamedResource = ResourceUtils.renameResource(this.getResource(), name);
               this.setResource(renamedResource);
            }

         } else {
            String name = DatamodelUtils.toStringIdentifier(this.identifier);

            // Rename it, only when the name has changed
            if (!name.equals(this.getResource().getURI())) {
               Resource renamedResource = ResourceUtils.renameResource(this.getResource(), name);
               this.setResource(renamedResource);
            }
         }
      }
   }

   @Override
   public String toString() {
      return DatamodelUtils.toStringInformationObject(this, null);
   }

   @Override
   public String describe() {
      StringBuffer buf = new StringBuffer("a (general) Information Object that ");
      if (getIdentifier() != null) {
         buf.append(getIdentifier().describe());
      } else {
         LOG.warn("Identifier is null, cannot describe IO");
      }

      return buf.toString();
   }

}
