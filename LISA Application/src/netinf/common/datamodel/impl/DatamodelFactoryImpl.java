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
package netinf.common.datamodel.impl;

import java.io.Serializable;
import java.util.List;

import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DatamodelFactoryAbstract;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.identity.EventServiceIdentityObject;
import netinf.common.datamodel.identity.GroupIdentityObject;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.identity.NodeIdentityObject;
import netinf.common.datamodel.identity.PersonIdentityObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.common.datamodel.impl.attribute.AttributeImpl;
import netinf.common.datamodel.impl.identity.EventServiceIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.GroupIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.IdentityObjectImpl;
import netinf.common.datamodel.impl.identity.NodeIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.PersonIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.ResolutionServiceIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.SearchServiceIdentityObjectImpl;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;

/**
 * The {@link DatamodelFactory} for the impl-classes. Provides instances of classes belonging to the impl-implementation of the
 * datamodel.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
@Singleton
public class DatamodelFactoryImpl extends DatamodelFactoryAbstract implements Serializable {

   private static final long serialVersionUID = 6100474199206553951L;
   private static final Logger LOG = Logger.getLogger(DatamodelFactoryImpl.class);

   @Override
   public DataObject createDataObject() {
      return new DataObjectImpl(this);
   }

   @Override
   public EventServiceIdentityObject createEventServiceIdentityObject() {
      return new EventServiceIdentityObjectImpl(this);
   }

   @Override
   public GroupIdentityObject createGroupIdentityObject() {
      return new GroupIdentityObjectImpl(this);
   }

   @Override
   public Identifier createIdentifier() {
      return new IdentifierImpl(this);
   }

   @Override
   public IdentifierLabel createIdentifierLabel() {
      return new IdentifierLabelImpl(this);
   }

   @Override
   public IdentityObject createIdentityObject() {
      return new IdentityObjectImpl(this);
   }

   @Override
   public InformationObject createInformationObject() {
      return new InformationObjectImpl(this);
   }

   @Override
   public NodeIdentityObject createNodeIdentityObject() {
      return new NodeIdentityObjectImpl(this);
   }

   @Override
   public PersonIdentityObject createPersonIdentityObject() {
      return new PersonIdentityObjectImpl(this);
   }

   @Override
   public Attribute createAttribute() {
      return new AttributeImpl(this);
   }

   @Override
   public ResolutionServiceIdentityObject createResolutionServiceIdentityObject() {
      return new ResolutionServiceIdentityObjectImpl(this);
   }

   @Override
   public SearchServiceIdentityObject createSearchServiceIdentityObject() {
      return new SearchServiceIdentityObjectImpl(this);
   }

   @Override
   public InformationObject createInformationObjectFromBytes(byte[] bytes) {
      try {
         NetInfObjectWrapper createFromBytes = createFromBytes(bytes);
         if (createFromBytes instanceof InformationObject) {
            InformationObject result = (InformationObject) createFromBytes;
            return result;
         } else {
            throw new NetInfUncheckedException("Wrong type");
         }
      } catch (NetInfUncheckedException e) {
         LOG.error("The given object is not of the type Information Object");
         throw new NetInfUncheckedException("The given object is not of the type Information Object");
      }
   }

   @Override
   public Attribute createAttributeFromBytes(byte[] bytes) {
      try {
         NetInfObjectWrapper createFromBytes = createFromBytes(bytes);
         if (createFromBytes instanceof Attribute) {
            Attribute result = (Attribute) createFromBytes;
            return result;
         } else {
            throw new NetInfUncheckedException("Wrong type");
         }
      } catch (NetInfUncheckedException e) {
         LOG.error("The given object is not of the type Attribute");
         throw new NetInfUncheckedException("The given object is not of the type Attribute");
      }
   }

   @Override
   public Identifier createIdentifierFromBytes(byte[] bytes) {
      try {
         NetInfObjectWrapper createFromBytes = createFromBytes(bytes);
         if (createFromBytes instanceof Identifier) {
            Identifier result = (Identifier) createFromBytes;
            return result;
         } else {
            throw new NetInfUncheckedException("Wrong type");
         }
      } catch (NetInfUncheckedException e) {
         LOG.error("The given object is not of the type Identifier");
         throw new NetInfUncheckedException("The given object is not of the type Identifier");
      }
   }

   @Override
   public NetInfObjectWrapper createFromBytes(byte[] bytes) {
      Object readObject = Utils.unserializeJavaObject(bytes);

      if (readObject instanceof NetInfObjectWrapperImpl) {
         NetInfObjectWrapperImpl netInfObjectWrapperImpl = (NetInfObjectWrapperImpl) readObject;
         assignDatamodelFactory(netInfObjectWrapperImpl);
         wireParentCorrectly(netInfObjectWrapperImpl);

         return netInfObjectWrapperImpl;
      } else {
         LOG.error("The given object '" + readObject + "' is not of the type NetInfObjectWrapper");
         throw new NetInfUncheckedException("The given object is not of the type NetInfObjectWrapper");
      }
   }

   private void assignDatamodelFactory(NetInfObjectWrapperImpl netInfObjectWrapperImpl) {
      if (netInfObjectWrapperImpl == null) {
         return;
      }

      netInfObjectWrapperImpl.setDatamodelFactory(this);

      // Information Object
      if (netInfObjectWrapperImpl instanceof InformationObjectImpl) {
         InformationObjectImpl informationObjectImpl = (InformationObjectImpl) netInfObjectWrapperImpl;
         List<Attribute> attributes = informationObjectImpl.getAttributes();
         for (Attribute attribute : attributes) {
            AttributeImpl attributeImpl = (AttributeImpl) attribute;
            assignDatamodelFactory(attributeImpl);
         }

         // Assign datamodelFactory to identifier
         assignDatamodelFactory((NetInfObjectWrapperImpl) informationObjectImpl.getIdentifier());
      }
      // Attributes
      if (netInfObjectWrapperImpl instanceof AttributeImpl) {
         AttributeImpl informationObjectImpl = (AttributeImpl) netInfObjectWrapperImpl;
         List<Attribute> attributes = informationObjectImpl.getSubattributes();
         for (Attribute attribute : attributes) {
            AttributeImpl attributeImpl = (AttributeImpl) attribute;
            assignDatamodelFactory(attributeImpl);
         }
      }
      // Identifier (IdentifierLabels
      if (netInfObjectWrapperImpl instanceof IdentifierImpl) {
         IdentifierImpl identifierImpl = (IdentifierImpl) netInfObjectWrapperImpl;
         List<IdentifierLabel> identifierLabels = identifierImpl.getIdentifierLabels();

         for (IdentifierLabel identifierLabel : identifierLabels) {
            IdentifierLabelImpl identifierLabelImpl = (IdentifierLabelImpl) identifierLabel;
            identifierLabelImpl.setDatamodelFactory(this);
         }
      }
   }

   private void wireParentCorrectly(NetInfObjectWrapperImpl readObject) {
      if (readObject instanceof InformationObjectImpl) {
         InformationObjectImpl informationObjectImpl = (InformationObjectImpl) readObject;

         IdentifierImpl identifier = (IdentifierImpl) informationObjectImpl.getIdentifier();
         if (identifier != null) {
            identifier.setInformationObject(informationObjectImpl);
         } else {
            LOG.error("Found Information object without identifier... this is not allowed to happen.");
         }

         List<Attribute> attributes = informationObjectImpl.getAttributes();

         for (Attribute attribute : attributes) {
            AttributeImpl attributeImpl = (AttributeImpl) attribute;
            attributeImpl.setInformationObject(informationObjectImpl);
            wireParentCorrectly(attributeImpl);
         }
      }

      if (readObject instanceof IdentifierImpl) {
         IdentifierImpl identifierImpl = (IdentifierImpl) readObject;

         List<IdentifierLabel> identifierLabels = identifierImpl.getIdentifierLabels();

         for (IdentifierLabel identifierLabel : identifierLabels) {
            IdentifierLabelImpl identifierLabelImpl = (IdentifierLabelImpl) identifierLabel;
            identifierLabelImpl.setIdentifier(identifierImpl);
         }
      }

      if (readObject instanceof AttributeImpl) {
         AttributeImpl attributeImpl = (AttributeImpl) readObject;

         List<Attribute> subattributes = attributeImpl.getSubattributes();

         for (Attribute attribute : subattributes) {
            AttributeImpl subattributeImpl = (AttributeImpl) attribute;
            subattributeImpl.setParentAttribute(attributeImpl);
            wireParentCorrectly(subattributeImpl);
         }
      }
   }

   @Override
   public SerializeFormat getSerializeFormat() {
      return SerializeFormat.JAVA;
   }

}
