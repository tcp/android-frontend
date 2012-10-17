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

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.List;

import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DatamodelFactoryAbstract;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.EventServiceIdentityObject;
import netinf.common.datamodel.identity.GroupIdentityObject;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.identity.NodeIdentityObject;
import netinf.common.datamodel.identity.PersonIdentityObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.common.datamodel.rdf.attribute.AttributeRdf;
import netinf.common.datamodel.rdf.identity.EventServiceIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.GroupIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.IdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.NodeIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.PersonIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.ResolutionServiceIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.SearchServiceIdentityObjectRdf;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This factory provides all the classes for the rdf-datamodel. Always use this factory to create instances of datamodel classes,
 * and never use the constructor of datamodel classes directly.
 * 
 * @author PG Augnet 2, University of Paderborn
 * 
 */
@Singleton
public class DatamodelFactoryRdf extends DatamodelFactoryAbstract {

   private static final Logger LOG = Logger.getLogger(DatamodelFactoryRdf.class);

   @Inject
   public DatamodelFactoryRdf() {
   }

   @Override
   public Attribute createAttribute() {
      return new AttributeRdf(this);
   }

   @Override
   public DataObject createDataObject() {
      Model netinfModel = createModelForInformationObject(DataObjectRdf.class.getCanonicalName());
      DataObjectRdf informationObject = null;
      try {
         informationObject = new DataObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create DataObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public EventServiceIdentityObject createEventServiceIdentityObject() {
      Model netinfModel = createModelForInformationObject(EventServiceIdentityObjectRdf.class.getCanonicalName());
      EventServiceIdentityObjectRdf informationObject = null;
      try {
         informationObject = new EventServiceIdentityObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create EventServiceIdentityObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public GroupIdentityObject createGroupIdentityObject() {
      Model netinfModel = createModelForInformationObject(GroupIdentityObjectRdf.class.getCanonicalName());
      GroupIdentityObjectRdf informationObject = null;
      try {
         informationObject = new GroupIdentityObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create GroupIdentityObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public Identifier createIdentifier() {
      return new IdentifierRdf(this);
   }

   @Override
   public IdentifierLabel createIdentifierLabel() {
      return new IdentifierLabelRdf(this);
   }

   @Override
   public IdentityObject createIdentityObject() {
      Model netinfModel = createModelForInformationObject(IdentityObjectRdf.class.getCanonicalName());
      IdentityObjectRdf informationObject = null;
      try {
         informationObject = new IdentityObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create IdentityObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public InformationObject createInformationObject() {
      Model netinfModel = createModelForInformationObject(InformationObjectRdf.class.getCanonicalName());
      InformationObjectRdf informationObject = null;
      try {
         informationObject = new InformationObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create InformationObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public NodeIdentityObject createNodeIdentityObject() {
      Model netinfModel = createModelForInformationObject(NodeIdentityObjectRdf.class.getCanonicalName());
      NodeIdentityObjectRdf informationObject = null;
      try {
         informationObject = new NodeIdentityObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create NodeIdentityObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public PersonIdentityObject createPersonIdentityObject() {
      Model netinfModel = createModelForInformationObject(PersonIdentityObjectRdf.class.getCanonicalName());
      PersonIdentityObjectRdf informationObject = null;
      try {
         informationObject = new PersonIdentityObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create PersonIdentityObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public ResolutionServiceIdentityObject createResolutionServiceIdentityObject() {
      Model netinfModel = createModelForInformationObject(ResolutionServiceIdentityObjectRdf.class.getCanonicalName());
      ResolutionServiceIdentityObjectRdf informationObject = null;
      try {
         informationObject = new ResolutionServiceIdentityObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create ResolutionServiceIdentityObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public SearchServiceIdentityObject createSearchServiceIdentityObject() {
      Model netinfModel = createModelForInformationObject(SearchServiceIdentityObjectRdf.class.getCanonicalName());
      SearchServiceIdentityObjectRdf informationObject = null;
      try {
         informationObject = new SearchServiceIdentityObjectRdf(netinfModel, this);
      } catch (NetInfCheckedException e) {
         LOG.error("Could not create SearchServiceIdentityObject, since the build in model is not formatted correctly");
      }
      return informationObject;
   }

   @Override
   public Attribute createAttributeFromBytes(byte[] bytes) {
      LOG.trace(null);

      Model deserializedModel = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(bytes), null);
      Resource nullResource = null;
      List<Statement> statementsPointerToAttribute = deserializedModel.listStatements(nullResource,
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_ATTRIBUTE), nullResource).toList();

      if (statementsPointerToAttribute.size() == 1) {

         Statement statementPointerToAttribute = statementsPointerToAttribute.get(0);
         Resource attributeResource = (Resource) statementPointerToAttribute.getObject();
         deserializedModel.remove(statementPointerToAttribute);

         AttributeRdf attributeRdf = (AttributeRdf) createAttribute();
         try {
            attributeRdf.initFromResource(attributeResource);

            // Remove binding to any resource
            attributeRdf.bindToResource(null);

            LOG.debug("Successfully deserialized attribute");
            return attributeRdf;
         } catch (NetInfCheckedException e) {
            LOG.error("Could not deserialize attribute", e);
            throw new NetInfUncheckedException("Could not deserialize attribute from given model");
         }
      } else {
         LOG.error("Could not deserialize attribute");
         throw new NetInfUncheckedException("Could not deserialize attribute from given model");
      }
   }

   @Override
   public Identifier createIdentifierFromBytes(byte[] bytes) {
      Model deserializedModel = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(bytes), null);
      Resource nullResource = null;
      List<Statement> statementsPointerToIdentifier = deserializedModel.listStatements(nullResource,
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IDENTIFIER), nullResource).toList();

      if (statementsPointerToIdentifier.size() == 1) {
         Statement statementPointerToIdentifier = statementsPointerToIdentifier.get(0);
         Resource identifierResource = (Resource) statementPointerToIdentifier.getObject();
         IdentifierRdf identifier = (IdentifierRdf) createIdentifier();

         try {
            identifier.initFromResource(identifierResource);
            return identifier;
         } catch (NetInfCheckedException e) {
            LOG.error("Could not deserialize identifier", e);
            throw new NetInfUncheckedException("Could not deserialize identifier from given model");
         }
      } else {
         LOG.error("Could not deserialize identifier");
         throw new NetInfUncheckedException("Could not deserialize attribute from given model");
      }
   }

   @Override
   public InformationObject createInformationObjectFromBytes(byte[] bytes) {
      LOG.trace(null);
      Model deserializedModel = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(bytes), null);
      return createInformationObjectFromModel(deserializedModel);
   }

   public InformationObject createInformationObjectFromModel(Model model) {
      InformationObjectRdf informationObject = null;
      try {
         // Determine the correct Type
         String strNull = null;
         List<Statement> listToIoResource = model.listStatements(null,
               DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO), strNull).toList();
         if (listToIoResource.size() == 1) {
            Statement statement = listToIoResource.get(0);
            Resource transportResource = statement.getSubject();
            Statement ioTypeStatement = transportResource.getProperty(DatamodelFactoryRdf.getProperty(DefinedRdfNames.IO_TYPE));
            String ioType = ioTypeStatement.getObject().as(Literal.class).getString();
            Constructor<?>[] constructors = Class.forName(ioType).getConstructors();

            if (constructors.length == 1) {
               Constructor<?> constructor = constructors[0];
               informationObject = (InformationObjectRdf) constructor.newInstance(model, this);
            } else {
               throw new NetInfUncheckedException("Could not find suitable constructor");
            }

         } else {
            throw new NetInfUncheckedException("Could not determine type of information object");
         }

      } catch (Exception e) {
         // Enormously many things might went wrong here, accordingly we do catch everything at once
         LOG.error("Could not create Information Object from given model", e);
         throw new NetInfUncheckedException(e);
      }
      return informationObject;
   }

   /**
    * The type is always the full name of the class e.g netinf.common.datamodel.rdf and so on.
    * 
    * @param type
    * @return
    */
   public Model createModelForInformationObject(String type) {
      // Initialize the first pointer to the InformationObject
      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefix(DefinedRdfNames.NETINF_NAMESPACE_NAME, DefinedRdfNames.NETINF_RDF_SCHEMA_URI);

      Resource ioResource = model.createResource();

      // The pointer-resource
      Resource firstResource = model.createResource();
      model.add(firstResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO), ioResource);
      model.add(firstResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.IO_TYPE), type);

      LOG.debug("Successfully created model for InformationObject");
      return model;
   }

   public Model createModelForAttribute() {
      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefix(DefinedRdfNames.NETINF_NAMESPACE_NAME, DefinedRdfNames.NETINF_RDF_SCHEMA_URI);

      Resource resource = model.createResource();

      // The pointer-resource
      Resource firstResource = model.createResource();
      model.add(firstResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_ATTRIBUTE), resource);

      LOG.debug("Successfully created model for Attribute");
      return model;
   }

   public Model createModelForIdentifier() {
      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefix(DefinedRdfNames.NETINF_NAMESPACE_NAME, DefinedRdfNames.NETINF_RDF_SCHEMA_URI);

      LOG.debug("Successfully created model for Identifier");
      return model;
   }

   @Override
   public NetInfObjectWrapper createFromBytes(byte[] bytes) {
      Model deserializedModel = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(bytes), null);

      // Check IO
      List<Resource> ioResources = deserializedModel.listResourcesWithProperty(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO)).toList();

      if (ioResources.size() > 0) {
         return createInformationObjectFromBytes(bytes);
      }

      // Check Attribute
      List<Resource> attributeResources = deserializedModel.listResourcesWithProperty(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_ATTRIBUTE)).toList();

      if (attributeResources.size() > 0) {
         return createAttributeFromBytes(bytes);
      }

      // Check Identifier
      List<Resource> identifierResources = deserializedModel.listResourcesWithProperty(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IDENTIFIER)).toList();

      if (identifierResources.size() > 0) {
         return createIdentifierFromBytes(bytes);
      }

      throw new NetInfUncheckedException("Could not create Datamodel-object from bytes array");
   }

   private static final Hashtable<String, Property> PROPERTY_STORAGE = new Hashtable<String, Property>();

   public static Property getProperty(String uri) {
      Property property = PROPERTY_STORAGE.get(uri);

      if (property == null) {
         property = ResourceFactory.createProperty(uri);
         PROPERTY_STORAGE.put(uri, property);
      }

      return property;
   }

   public static Property getProperty(DefinedAttributeIdentification definedAttributeIdentification) {
      return getProperty(definedAttributeIdentification.getURI());
   }

   @Override
   public SerializeFormat getSerializeFormat() {
      return SerializeFormat.RDF;
   }
}
