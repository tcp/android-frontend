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
package netinf.common.datamodel.creator;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.security.SignatureAlgorithm;
import netinf.common.security.impl.IntegrityImpl;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The ValidCreator is a Utility Class to create valid Attributes, InformationObjects, IdentityObjects, ... conveniently.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ValidCreator {

   private static final Logger log = Logger.getLogger(ValidCreator.class);

   private static String defaultHashAlgorithm = "SHA1";

   private static DatamodelFactory dmFactory;

   private static SignatureAlgorithm signatureAlgorithm;

   public static Identifier createValidIdentifier(DefinedVersionKind versionKind, PublicKey publicKey) {
      Identifier identifier = dmFactory.createIdentifier();
      String pkToHash = "String" + DatamodelUtils.TYPE_VALUE_SEPARATOR + Utils.objectToString(publicKey);

      IdentifierLabel label = dmFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.HASH_OF_PK.getLabelName());

      // hash and compare hashed public keys
      String pkHashed = null;
      try {
         pkHashed = signatureAlgorithm.hash(pkToHash, defaultHashAlgorithm);
      } catch (NoSuchAlgorithmException e) {
         log.error("Unable to hash public key. " + e.getMessage());
      }
      label.setLabelValue(pkHashed);
      identifier.addIdentifierLabel(label);

      label = dmFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.HASH_OF_PK_IDENT.getLabelName());
      label.setLabelValue(defaultHashAlgorithm);
      identifier.addIdentifierLabel(label);

      label = dmFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.VERSION_KIND.getLabelName());
      label.setLabelValue(versionKind.name());
      identifier.addIdentifierLabel(label);

      return identifier;
   }

   public static Identifier createValidIdentifier(DefinedVersionKind versionKind, PublicKey publicKey, String versionNumber) {
      Identifier identifier = createValidIdentifier(versionKind, publicKey);
      IdentifierLabel label = dmFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.VERSION_NUMBER.getLabelName());
      label.setLabelValue(versionNumber);
      identifier.addIdentifierLabel(label);
      return identifier;
   }

   @Inject
   public static void setDatamodelFactory(DatamodelFactory factory) {
      dmFactory = factory;
   }

   @Inject
   public static void setSignatureAlgorithm(SignatureAlgorithm algo) {
      signatureAlgorithm = algo;
   }

   public static IdentityObject createValidIdentityObject(PublicKey key) {
      IdentityObject newIdentity = dmFactory.createIdentityObject();
      newIdentity.setPublicMasterKey(key);
      Identifier identifier = ValidCreator.createValidIdentifier(DefinedVersionKind.UNVERSIONED, key);
      newIdentity.setIdentifier(identifier);
      return newIdentity;
   }

   public static InformationObject createValidInformationObject(String type, IdentityObject owner,
         DefinedVersionKind versionKind, String uniqueLabelValue) {
      InformationObject io = null;
      try {
         Class<InformationObject> getClass = (Class<InformationObject>) Class.forName(type);
         io = dmFactory.createDatamodelObject(getClass);
      } catch (Exception e) {
         e.printStackTrace();
         log.error("Something bad happened while creating the IO: " + e.getMessage());
      }

      if (io == null) {
         throw new NetInfUncheckedException("Could not create IO");
      }

      Identifier identifier = createValidIdentifier(versionKind, owner.getPublicMasterKey());
      IdentifierLabel label = dmFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.UNIQUE_LABEL.getLabelName());
      label.setLabelValue(uniqueLabelValue);
      identifier.addIdentifierLabel(label);
      io.setIdentifier(identifier);

      Attribute attribute = dmFactory.createAttribute(DefinedAttributeIdentification.PUBLIC_KEY.getURI(),
            Utils.objectToString(owner.getPublicMasterKey()));
      attribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      io.addAttribute(attribute);

      String ownerPath = getIdentityPath(owner);

      attribute = dmFactory.createAttribute(DefinedAttributeIdentification.OWNER.getURI(), ownerPath);
      attribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      io.addAttribute(attribute);

      ValidCreator.addAuthorizedWriterToIO(io, ownerPath);

      // if (!DatamodelUtils.isSyntacticallyValidIO(io)) {
      // throw new NetInfUncheckedException("Sad trombone here");
      // }

      return io;
   }

   public static InformationObject createValidInformationObject(IdentityObject owner, DefinedVersionKind versionKind,
         String uniqueLabelValue) {
      return createValidInformationObject(dmFactory.createInformationObject().getClass().getName(), owner, versionKind,
            uniqueLabelValue);
   }

   /**
    * Appends a new Authorized Writers List to IO. Authorized Writers List contains owner from the beginning.
    * 
    * @param io
    *           IO to add authorized writers list to.
    * @param owner
    *           Owner of the IO
    */
   private static void appendAuthorizedWritersListToIO(InformationObject io, String ownerPath) {
      List<String> writersList = new ArrayList<String>();
      writersList.add(ownerPath);

      Attribute authorizedWritersList = dmFactory.createAttribute(DefinedAttributeIdentification.AUTHORIZED_WRITERS.getURI(),
            Utils.objectToString(writersList));
      authorizedWritersList.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      secureAttributeItself(authorizedWritersList, ownerPath);
   }

   private static String getIdentityPath(IdentityObject ido) {
      return ido.getIdentifier().toString() + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI();
   }

   public static void secureAttributeItself(Attribute attribute, String pathToKey) {
      Attribute subattribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), pathToKey);
      subattribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      attribute.addSubattribute(subattribute);
   }

   public static void secureAttributeInOverall(Attribute attribute, String pathToKey) {
      Attribute subattribute = dmFactory
            .createAttribute(DefinedAttributeIdentification.SECURED_IN_OVERALL.getURI(), Boolean.TRUE);
      subattribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      attribute.getInformationObject().removeAttribute(DefinedAttributeIdentification.WRITER.getURI());
      secureIO(attribute.getInformationObject(), pathToKey);
      attribute.addSubattribute(subattribute);
   }

   private static void secureIO(InformationObject io, String pathToKey) {
      Attribute subattribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), pathToKey);
      subattribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      io.addAttribute(subattribute);
   }

   public static boolean isAttributeSigned(Attribute attribute) {
      if (isAttributeSignedInOverall(attribute) || isAttributeSignedItself(attribute)) {
         return true;
      }
      return false;
   }

   private static boolean isAttributeSignedItself(Attribute attribute) {
      if ((attribute.getSingleSubattribute(DefinedAttributeIdentification.WRITER.getURI()) != null)
            && (attribute.getSingleSubattribute(DefinedAttributeIdentification.SIGNATURE.getURI()) != null)
            && (attribute.getSingleSubattribute(DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI()) == null)
            && (attribute.getSingleSubattribute(DefinedAttributeIdentification.IDENTITY_VERIFICATION_FAILED.getURI()) == null)) {
         return true;
      }
      return false;
   }

   private static boolean isAttributeSignedInOverall(Attribute attribute) {
      if ((attribute.getSingleSubattribute(DefinedAttributeIdentification.SECURED_IN_OVERALL.getURI()) != null)
            && (attribute.getInformationObject().getSingleAttribute(DefinedAttributeIdentification.WRITER.getURI()) != null)
            && (attribute.getInformationObject().getSingleAttribute(DefinedAttributeIdentification.SIGNATURE.getURI()) != null)
            && (attribute.getInformationObject().getSingleAttribute(
                  DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI()) == null)
            && (attribute.getInformationObject().getSingleAttribute(
                  DefinedAttributeIdentification.IDENTITY_VERIFICATION_FAILED.getURI()) == null)) {
         return true;
      }
      return false;
   }

   public static boolean isAttributeSignedIncorrectly(Attribute attribute) {
      if (isAttributeSignedIncorrectlyItself(attribute) || isAttributeSignedIncorrectlyInOverall(attribute)) {
         return true;
      }

      return false;

   }

   private static boolean isAttributeSignedIncorrectlyItself(Attribute attribute) {
      if ((attribute.getSingleSubattribute(DefinedAttributeIdentification.SIGNATURE.getURI()) != null)
            && (attribute.getSingleSubattribute(DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI()) != null || attribute
                  .getSingleSubattribute(DefinedAttributeIdentification.IDENTITY_VERIFICATION_FAILED.getURI()) != null)) {
         return true;
      }

      return false;
   }

   private static boolean isAttributeSignedIncorrectlyInOverall(Attribute attribute) {
      if ((attribute.getSingleSubattribute(DefinedAttributeIdentification.SECURED_IN_OVERALL.getURI()) != null)
            && (attribute.getInformationObject().getSingleAttribute(
                  DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI()) == null || attribute
                  .getInformationObject().getSingleAttribute(
                        DefinedAttributeIdentification.SIGNATURE_VERIFICATION_FAILED.getURI()) == null)) {
         return true;
      }

      return false;
   }

   public static void addAuthorizedWriterToIO(InformationObject io, IdentityObject additionalWriter) {
      addAuthorizedWriterToIO(io, getIdentityPath(additionalWriter));
   }

   public static void addAuthorizedWriterToIO(InformationObject io, String additionalWriterPath) {
      Attribute newWriter = createValidAttribute(DefinedAttributeIdentification.AUTHORIZED_WRITERS, additionalWriterPath,
            DefinedAttributePurpose.SYSTEM_ATTRIBUTE);
      io.addAttribute(newWriter);

      Attribute owner = io.getSingleAttribute(DefinedAttributeIdentification.OWNER.getURI());
      if (owner == null) {
         throw new NetInfUncheckedException("IO has no owner! Thus, can't sign new authorized writer.");
      }
      ValidCreator.secureAttributeItself(newWriter, owner.getValue(String.class));
   }

   public static void addAuthorizedReader(InformationObject io, IdentityObject readerIdentity) {
      addAuthorizedReader(io, getIdentityPath(readerIdentity));
   }

   public static void addAuthorizedReader(InformationObject io, String readerPath) {
      io.addAttribute(createAuthorizedReaderAttribute(readerPath));
   }

   public static void addAuthorizedReader(Attribute attribute, IdentityObject readerIdentity) {
      addAuthorizedReader(attribute, getIdentityPath(readerIdentity));
   }

   public static void addAuthorizedReader(Attribute attribute, String readerPath) {
      attribute.addSubattribute(createAuthorizedReaderAttribute(readerPath));
   }

   private static Attribute createAuthorizedReaderAttribute(String readerPath) {
      return createValidAttribute(DefinedAttributeIdentification.AUTHORIZED_READERS, readerPath,
            DefinedAttributePurpose.SYSTEM_ATTRIBUTE);
   }

   public static Attribute createValidAttribute(DefinedAttributeIdentification defAttrIdent, Object value,
         DefinedAttributePurpose defAttrPurpose) {
      Attribute attribute = dmFactory.createAttribute(defAttrIdent.getURI(), value);
      attribute.setAttributePurpose(defAttrPurpose.getAttributePurpose());
      return attribute;
   }
}
