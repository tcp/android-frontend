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
package netinf.common.datamodel;

import java.util.Collection;
import java.util.List;

import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.ValueUtils;

import org.apache.log4j.Logger;

/**
 * This has to be the superclass of all {@link DatamodelFactory}. It provides generic methods to create and copy instances of the
 * according implementation.
 * 
 * @author PG Augnet 2, University of Paderborn
 * 
 */
public abstract class DatamodelFactoryAbstract implements DatamodelFactory {

   private static final Logger LOG = Logger.getLogger(DatamodelFactoryAbstract.class);

   @SuppressWarnings("unchecked")
   @Override
   public <T> T copyObject(T object) {
      LOG.trace(null);

      if (object instanceof InformationObject) {
         InformationObject informationObject = (InformationObject) object;
         return (T) copyInformationObject(informationObject);
      } else if (object instanceof Attribute) {
         Attribute attribute = (Attribute) object;
         return (T) copyAttribute(attribute);
      } else if (object instanceof Identifier) {
         Identifier identifier = (Identifier) object;
         return (T) copyIdentifier(identifier);
      } else if (object instanceof IdentifierLabel) {
         IdentifierLabel identifierLabel = (IdentifierLabel) object;
         return (T) copyIdentifierLabel(identifierLabel);
      } else {
         throw new NetInfUncheckedException("Could not copy the object, since it is not object of the datamodel");
      }
   }

   private Attribute copyAttribute(Attribute toCopy) {
      Attribute copyAttribute = createAttribute();

      String attributePurpose = toCopy.getAttributePurpose();
      if (attributePurpose != null) {
         copyAttribute.setAttributePurpose(attributePurpose);
      }

      String attributeIdentification = toCopy.getIdentification();
      if (attributeIdentification != null) {
         copyAttribute.setIdentification(attributeIdentification);
      }

      Object attributeValue = toCopy.getValue(Object.class);
      if (attributeValue != null) {
         Object clonedObject = ValueUtils.getObjectFromRaw(toCopy.getValueRaw());
         copyAttribute.setValue(clonedObject);
      }

      // The attributes
      List<Attribute> subattributes = toCopy.getSubattributes();
      for (Attribute attribute : subattributes) {
         Attribute clonedSubAttribute = copyAttribute(attribute);
         copyAttribute.addSubattribute(clonedSubAttribute);
      }

      return copyAttribute;
   }

   private InformationObject copyInformationObject(InformationObject toCopy) {
      // InformationObject copyInformationObject = createInformationObject();
      InformationObject copyInformationObject = createDatamodelObject(toCopy.getClass());

      // The attributes
      List<Attribute> attributes = toCopy.getAttributes();
      for (Attribute attribute : attributes) {
         Attribute copyAttribute = copyAttribute(attribute);
         copyInformationObject.addAttribute(copyAttribute);
      }

      // The identifier
      Identifier identifier = toCopy.getIdentifier();
      if (identifier != null) {
         Identifier clonedIdentifier = copyIdentifier(identifier);
         copyInformationObject.setIdentifier(clonedIdentifier);
      }

      return copyInformationObject;
   }

   private Identifier copyIdentifier(Identifier toCopy) {
      Identifier clonedIdentifier = createIdentifier();

      Collection<IdentifierLabel> values = toCopy.getIdentifierLabels();

      for (IdentifierLabel identifierLabel : values) {
         IdentifierLabel copiedIdentifierLabel = copyIdentifierLabel(identifierLabel);
         clonedIdentifier.addIdentifierLabel(copiedIdentifierLabel);
      }

      return clonedIdentifier;
   }

   private IdentifierLabel copyIdentifierLabel(IdentifierLabel toCopy) {
      IdentifierLabel copiedIdentifierLabel = createIdentifierLabel();

      copiedIdentifierLabel.setLabelName(toCopy.getLabelName());
      copiedIdentifierLabel.setLabelValue(toCopy.getLabelValue());

      return copiedIdentifierLabel;
   }

   @Override
   public Identifier createIdentifierFromString(String identifierString) {
      try {
         List<IdentifierLabel> identifierLabels = DatamodelUtils.getIdentifierLabels(identifierString, this);
         Identifier identifier = createIdentifier();
         for (IdentifierLabel identifierLabel : identifierLabels) {
            identifier.addIdentifierLabel(identifierLabel);
         }

         return identifier;
      } catch (NetInfUncheckedException e) {
         LOG.error("Could not create identifier from '" + identifierString + "'");
         throw e;
      }
   }

   @Override
   public Attribute createAttribute(String identification, Object value) {
      Attribute attribute = createAttribute();

      attribute.setIdentification(identification);
      attribute.setValue(value);
      attribute.setAttributePurpose("");

      return attribute;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T createDatamodelObject(Class<T> clazz) {
      try {
         // this tries to find the correct method dynamically - e.g. createInformationObject for InformationObject
         String methodName = null;
         if (clazz.isInterface()) {
            methodName = "create" + clazz.getSimpleName();
         } else {
            methodName = "create" + clazz.getInterfaces()[0].getSimpleName();
         }
         return (T) this.getClass().getMethod(methodName).invoke(this);
      } catch (Exception e) {
         throw new NetInfUncheckedException("Could not create " + clazz.getSimpleName(), e);
      }
   }
}
