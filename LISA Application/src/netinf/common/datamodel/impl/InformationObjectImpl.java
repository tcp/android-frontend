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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.impl.attribute.AttributeImpl;
import netinf.common.exceptions.NetInfWrongAttributeException;
import netinf.common.security.impl.IntegrityImpl;
import netinf.common.utils.DatamodelUtils;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * This class represents an {@link InformationObject}. It is a simple implementation, basing on dump java classes, which do not
 * have many pieces of functionality. They are only containers for data.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class InformationObjectImpl extends NetInfObjectWrapperImpl implements InformationObject {

   private static final long serialVersionUID = 8888324979173072386L;

   private static final Logger LOG = Logger.getLogger(InformationObjectImpl.class);
   private final LinkedList<AttributeImpl> attributes;
   private IdentifierImpl identifier;

   @Inject
   public InformationObjectImpl(DatamodelFactoryImpl datamodelFactory) {
      super(datamodelFactory);
      this.attributes = new LinkedList<AttributeImpl>();
   }

   @Override
   public Identifier getIdentifier() {
      return this.identifier;
   }

   @Override
   public void setIdentifier(Identifier givenIdentifier) {
      IdentifierImpl identifierImpl = (IdentifierImpl) givenIdentifier;

      // The Identifier is removed
      if (identifierImpl == null) {
         this.identifier.setInformationObject(null);
         this.identifier = null;
      } else {
         InformationObjectImpl informationObject = identifierImpl.getInformationObject();

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
            identifierImpl.setInformationObject(this);
            this.identifier = identifierImpl;
         }
      }
   }

   @Override
   public void addAttribute(Attribute attribute) {
      AttributeImpl attributeImpl = (AttributeImpl) attribute;

      // The attribute is bound and has to be removed from its parent
      // (informationobject or parent attribute
      if (attributeImpl.getInformationObject() != null || attributeImpl.getParentAttribute() != null) {
         if (attributeImpl.getParentAttribute() != null) {
            attributeImpl.getParentAttribute().removeSubattribute(attributeImpl);
         } else {
            attributeImpl.getInformationObject().removeAttribute(attributeImpl);
         }
      }

      attributeImpl.setInformationObject(this);

      int index = Collections.binarySearch(this.attributes, attributeImpl);
      if (index < 0) {
         this.attributes.add(-index - 1, attributeImpl);
      } else {
         this.attributes.add(index, attributeImpl);
      }
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
   public List<Attribute> getAttributes() {
      return new ArrayList<Attribute>(this.attributes);
   }

   @Override
   public void removeAttribute(Attribute attribute) {
      if (this.attributes.contains(attribute)) {
         AttributeImpl attributeImpl = (AttributeImpl) attribute;
         attributeImpl.setInformationObject(null);
         this.attributes.remove(attribute);
      }
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
   public List<Attribute> getAttributesForPurpose(String attributePurpose) {
      ArrayList<Attribute> result = new ArrayList<Attribute>();

      for (Attribute attribute : this.attributes) {
         if (attribute.getAttributePurpose().equals(attributePurpose)) {
            result.add(attribute);
         }
      }

      return result;
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

   @Override
   public List<Identifier> getReaderIdentifiers() {

      List<Attribute> readerListProperties = getAttribute(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());
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

               Identifier identifier = this.datamodelFactory.createIdentifierFromString(readerIdentifierString);
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
      List<Attribute> writerProperties = getAttribute(DefinedAttributeIdentification.AUTHORIZED_WRITERS.getURI());

      List<String> pathList = new ArrayList<String>();

      Attribute owner = getSingleAttribute(DefinedAttributeIdentification.OWNER.getURI());
      if (owner == null) {
         LOG.warn("No owner for IO. Thus, Authorized Writers can't be verified");
         return pathList;
      }

      for (Attribute writer : writerProperties) {

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
   public List<Attribute> getAttribute(String identification) {
      ArrayList<Attribute> result = new ArrayList<Attribute>();

      if (identification != null) {

         for (Attribute attribute : this.attributes) {
            if (identification.equals(attribute.getIdentification())) {
               result.add(attribute);
            }
         }
      }

      return result;
   }

   protected void resetAttribute(String identification, Object value, String purpose) {
      if (getSingleAttribute(identification) == null) {
         AttributeImpl attributeImpl = (AttributeImpl) this.datamodelFactory.createAttribute();
         attributeImpl.setIdentification(identification);
         attributeImpl.setValue(value);
         attributeImpl.setAttributePurpose(purpose);

         addAttribute(attributeImpl);
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

   public void resortAttributes() {
      Collections.sort(this.attributes);
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
