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
package netinf.common.utils;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.impl.attribute.AttributeImpl;
import netinf.common.datamodel.rdf.DefinedRdfNames;
import netinf.common.datamodel.rdf.attribute.AttributeRdf;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.security.impl.IntegrityImpl;

import org.apache.log4j.Logger;

/**
 * The Class DatamodelUtils.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class DatamodelUtils {

   public static final String INDENT = "   ";

   private static final Logger LOG = Logger.getLogger(DatamodelUtils.class);

   public static final char LABEL_SEPARATOR = '~';
   public static final String TYPE_VALUE_SEPARATOR = ":";
   public static final char LABEL_ASSIGNER = '=';

   /**
    * The attributes adhere to the following order:
    * <ul>
    * <li>1) lexicographic order of identification</li>
    * <li>2) lexicographic order of value</li>
    * <li>3) sort by subattributes
    * <ul>
    * <li>3a) attributes are equal if their subattribute lists are equal</li>
    * <li>3b) the attribute with more subattributes comes first</li>
    * <li>3c) the first difference in equal-length subattribute lists decides</li>
    * </ul>
    * </ul>
    * (recursive comparison of subattributes)
    * 
    * @param one
    * @param two
    * @return
    */
   public static int compareAttributes(Attribute one, Attribute two) {
      if (two == null) {
         throw new NullPointerException();
      }
      int firstLevelComparison = one.getIdentification().compareTo(two.getIdentification());
      if (firstLevelComparison == 0) {
         int secondLevelComparison = one.getValueRaw().compareTo(two.getValueRaw());
         if (secondLevelComparison == 0) {
            List<Attribute> subAttributeList1 = one.getSubattributes();
            List<Attribute> subAttributeList2 = two.getSubattributes();
            if (subAttributeList1.equals(subAttributeList2)) {
               return 0;
            }
            if (subAttributeList1.size() == subAttributeList2.size()) {
               for (int i = 0; i < subAttributeList1.size(); i++) {
                  int thirdLevelComparison = subAttributeList1.get(i).compareTo(subAttributeList2.get(i));
                  if (thirdLevelComparison != 0) {
                     return thirdLevelComparison;
                  }
               }
               // should never reach this
               return 0;
            }
            return subAttributeList1.size() >= subAttributeList2.size() ? 1 : -1;
         }
         return secondLevelComparison;
      }
      return firstLevelComparison;
   }

   public static int compareIdentifierLabels(IdentifierLabel one, IdentifierLabel two) {
      // Step 1 compare according to ordering within DefinedLabelName
      DefinedLabelName oneLabelName = DefinedLabelName.getDefinedLabelNameByString(one.getLabelName());
      DefinedLabelName twoLabelName = DefinedLabelName.getDefinedLabelNameByString(two.getLabelName());

      if (oneLabelName != null && twoLabelName != null) {
         int oneOrder = oneLabelName.getOrder();
         int twoOrder = twoLabelName.getOrder();

         if (oneOrder == twoOrder) {
            return 0;
         }
         if (oneOrder < twoOrder) {
            return -1;
         } else {
            return 1;
         }
      }

      if (oneLabelName != null && twoLabelName == null) {
         return -1;
      }

      if (oneLabelName == null && twoLabelName != null) {
         return 1;
      }

      // Else, both are user defined, compare according to string values
      int comparedNames = one.getLabelName().compareTo(two.getLabelName());

      if (comparedNames != 0) {
         return comparedNames;
      } else {
         int comparedValues = one.getLabelValue().compareTo(two.getLabelValue());
         return comparedValues;
      }
   }

   /**
    * This method might even compare {@link AttributeImpl} and {@link AttributeRdf} for equality.
    * 
    * @param one
    * @param two
    * @return
    */
   public static boolean equalAttributes(Object oneObject, Object twoObject) {
      // Some special cases
      if (oneObject == twoObject) {
         return true;
      }
      if (oneObject == null && twoObject == null) {
         return true;
      }
      if (oneObject != null && twoObject == null || twoObject != null && oneObject == null) {
         return false;
      }
      if (!(oneObject instanceof Attribute) || !(twoObject instanceof Attribute)) {
         return false;
      }

      Attribute one = (Attribute) oneObject;
      Attribute two = (Attribute) twoObject;

      // The identification
      String oneIdentification = one.getIdentification();
      String twoIdentification = two.getIdentification();

      if (oneIdentification == null) {
         if (twoIdentification != null) {
            return false;
         }
      } else if (!oneIdentification.equals(twoIdentification)) {
         return false;
      }

      // The purpose
      String onePurpose = one.getAttributePurpose();
      String twoPurpose = two.getAttributePurpose();

      if (onePurpose == null) {
         if (twoPurpose != null) {
            return false;
         }
      } else if (!onePurpose.equals(twoPurpose)) {
         return false;
      }

      // The value
      Object oneValue = one.getValue(Object.class);
      Object twoValue = two.getValue(Object.class);

      if (oneValue == null) {
         if (twoValue != null) {
            return false;
         }
      } else if (!oneValue.equals(twoValue)) {
         return false;
      }

      // The subattributes
      List<Attribute> oneSubattributes = one.getSubattributes();
      List<Attribute> twoSubattributes = two.getSubattributes();

      if (oneSubattributes == null) {
         if (twoSubattributes != null) {
            return false;

         }
         // Check equality of sorted list
      } else if (!oneSubattributes.equals(twoSubattributes)) {
         return false;
      }

      return true;
   }

   public static boolean equalIdentifiers(Object oneObject, Object twoObject) {
      // Some special cases
      if (oneObject == twoObject) {
         return true;
      }
      if (oneObject == null && twoObject == null) {
         return true;
      }
      if (oneObject != null && twoObject == null || twoObject != null && oneObject == null) {
         return false;
      }
      if (!(oneObject instanceof Identifier) || !(twoObject instanceof Identifier)) {
         return false;
      }

      Identifier one = (Identifier) oneObject;
      Identifier two = (Identifier) twoObject;

      List<IdentifierLabel> oneLabels = one.getIdentifierLabels();
      List<IdentifierLabel> twoLabels = two.getIdentifierLabels();

      if (oneLabels == null) {
         if (twoLabels != null) {
            return false;
         }
      } else if (!oneLabels.equals(twoLabels)) {
         return false;
      }
      return true;
   }

   public static boolean equalIdentifierLabels(Object oneObject, Object twoObject) {
      // Some special cases
      if (oneObject == twoObject) {
         return true;
      }
      if (oneObject == null && twoObject == null) {
         return true;
      }
      if (oneObject != null && twoObject == null || twoObject != null && oneObject == null) {
         return false;
      }
      if (!(oneObject instanceof IdentifierLabel) || !(twoObject instanceof IdentifierLabel)) {
         return false;
      }

      IdentifierLabel one = (IdentifierLabel) oneObject;
      IdentifierLabel two = (IdentifierLabel) twoObject;

      // The labelName
      String oneLabelName = one.getLabelName();
      String twoLabelName = two.getLabelName();

      if (oneLabelName == null) {
         if (twoLabelName != null) {
            return false;
         }
      } else if (!oneLabelName.equals(twoLabelName)) {
         return false;
      }

      // The labelValue
      String oneLabelValue = one.getLabelValue();
      String twoLabelValue = two.getLabelValue();

      if (oneLabelValue == null) {
         if (twoLabelValue != null) {
            return false;
         }
      } else if (!oneLabelValue.equals(twoLabelValue)) {
         return false;
      }

      return true;
   }

   public static boolean equalInformationObjects(Object oneObject, Object twoObject) {
      // Some special cases
      if (oneObject == twoObject) {
         return true;
      }
      if (oneObject == null && twoObject == null) {
         return true;
      }
      if (oneObject != null && twoObject == null || twoObject != null && oneObject == null) {
         return false;
      }
      if (!(oneObject instanceof InformationObject) || !(twoObject instanceof InformationObject)) {
         return false;
      }

      InformationObject one = (InformationObject) oneObject;
      InformationObject two = (InformationObject) twoObject;

      // The identifier
      Identifier oneIdentifier = one.getIdentifier();
      Identifier twoIdentifier = two.getIdentifier();

      if (oneIdentifier == null) {
         if (twoIdentifier != null) {
            return false;
         }
      } else if (!oneIdentifier.equals(twoIdentifier)) {
         return false;
      }

      // The attributes
      List<Attribute> oneAttributes = one.getAttributes();
      List<Attribute> twoAttributes = two.getAttributes();

      if (oneAttributes == null) {
         if (twoAttributes != null) {
            return false;
         }
         // Check equality of sorted list
      } else if (!oneAttributes.equals(twoAttributes)) {
         return false;
      }
      return true;
   }

   public static String getValueType(Object attributeValue) {
      if (attributeValue.getClass().getPackage().equals(Package.getPackage("java.lang"))) {
         return attributeValue.getClass().getSimpleName();
      } else {
         return attributeValue.getClass().getCanonicalName();
      }
   }

   public static boolean isSyntacticallyValidIdentifier(Identifier identifier) {

      // null is invalid
      if (identifier == null) {
         return false;
      }

      // no labels is invalid
      if (identifier.getIdentifierLabels().isEmpty()) {
         return false;
      }

      // check for needed labels and cross-relations
      String[] neededLabels = { DefinedLabelName.HASH_OF_PK.getLabelName(), DefinedLabelName.HASH_OF_PK_IDENT.getLabelName(),
            DefinedLabelName.VERSION_KIND.getLabelName() };
      for (String label : neededLabels) {
         if (identifier.getIdentifierLabel(label) == null) {
            return false;
         }
      }
      if (DefinedVersionKind.VERSIONED.name().equals(identifier.getIdentifierLabel(DefinedLabelName.VERSION_KIND.getLabelName()))) {
         if (!identifier.getIdentifierLabels().contains(DefinedLabelName.VERSION_NUMBER.getLabelName())) {
            return false;
         }
      }

      // empty label names/values or such that contain separators/assigners are invalid
      for (IdentifierLabel label : identifier.getIdentifierLabels()) {
         if (label.getLabelName().isEmpty() || label.getLabelName().contains(Character.toString(LABEL_ASSIGNER))
               || label.getLabelName().contains(Character.toString(LABEL_SEPARATOR))) {
            return false;
         }
         if (label.getLabelValue().isEmpty() || label.getLabelValue().contains(Character.toString(LABEL_ASSIGNER))
               || label.getLabelValue().contains(Character.toString(LABEL_SEPARATOR))) {
            return false;
         }
      }

      return true;
   }

   public static boolean isSyntacticallyValidAttribute(Attribute attribute, Attribute parentAttribute,
         InformationObject informationObject) {

      // null is invalid
      if (attribute == null) {
         return false;
      }

      // we need an attribute purpose
      if (attribute.getAttributePurpose() == null || attribute.getAttributePurpose().isEmpty()) {
         return false;
      }

      // we need a value
      if (attribute.getValue(Object.class) == null) {
         return false;
      }

      // we need an identification
      if (attribute.getIdentification() == null || attribute.getIdentification().isEmpty()) {
         return false;
      }

      // check for correct IO
      if (informationObject != null && attribute.getInformationObject() != informationObject) {
         return false;
      }

      // check for correct parent Attribute
      if (parentAttribute != null && attribute.getParentAttribute() != parentAttribute) {
         return false;
      }

      boolean subAttributesValid = true;
      for (Attribute subattribute : attribute.getSubattributes()) {
         if (subattribute == null) {
            return false;
         }
         subAttributesValid &= isSyntacticallyValidAttribute(subattribute, attribute, informationObject);
      }

      return subAttributesValid;
   }

   /**
    * If the {@link DatamodelFactory} is set to {@code null} then an empty list is returned. Nevertheless, the method can then be
    * used for verification, whether or not the identifier is formatted correctly.
    * 
    * @param identifierString
    * @param datamodelFactory
    * @return correctly sorted list of the {@link IdentifierLabel}.
    * @throws NetInfCheckedException
    */
   public static List<IdentifierLabel> getIdentifierLabels(String givenIdentifierString, DatamodelFactory datamodelFactory) {
      class InternalIdentifierLabel implements IdentifierLabel {

         private String labelName;
         private String labelValue;

         @Override
         public String getLabelName() {
            return labelName;
         }

         @Override
         public void setLabelName(String labelName) {
            this.labelName = labelName;
         }

         @Override
         public String getLabelValue() {
            return labelValue;
         }

         @Override
         public void setLabelValue(String labelValue) {
            this.labelValue = labelValue;
         }

         @Override
         public DatamodelFactory getDatamodelFactory() {
            throw new UnsupportedOperationException("This is a dummy class that should not be used");
         }

         @Override
         public Object clone() {
            throw new UnsupportedOperationException("This is a dummy class that should not be used");
         }

         @Override
         public int compareTo(IdentifierLabel arg0) {
            throw new UnsupportedOperationException("This is a dummy class that should not be used");
         }

      }

      String labelSeparatorString = Character.toString(LABEL_SEPARATOR);
      String labelAssignerString = Character.toString(LABEL_ASSIGNER);

      if (givenIdentifierString == null || givenIdentifierString.isEmpty()
            || !givenIdentifierString.contains(labelAssignerString)) {
         throw new NetInfUncheckedException("Identifier not valid");
      }

      // remove the schema definition at the beginning.
      String identifierString = givenIdentifierString;
      if (givenIdentifierString.startsWith(DefinedRdfNames.NETINF_URI_SCHEMA)) {
         identifierString = givenIdentifierString.substring(DefinedRdfNames.NETINF_URI_SCHEMA.length());
      } else {
         LOG.error("The given identifier '" + identifierString + "' does NOT start with '" + DefinedRdfNames.NETINF_URI_SCHEMA
               + "'. Nevertheless, tryping to parse.");
      }

      Hashtable<String, IdentifierLabel> temporaryLabelList = new Hashtable<String, IdentifierLabel>();
      boolean allValid = true;

      String[] nameValuePairs = identifierString.split(labelSeparatorString);
      for (String nameValuePair : nameValuePairs) {

         if (nameValuePair.contains(labelAssignerString)) {
            String[] nameAndValue = nameValuePair.split(labelAssignerString, 2);
            if (nameAndValue[0].length() > 0 && nameAndValue[1].length() > 0 && !nameAndValue[0].contains(labelAssignerString)
                  && !temporaryLabelList.containsKey(nameAndValue[0])) {

               IdentifierLabel newLabel;
               if (datamodelFactory != null) {
                  newLabel = datamodelFactory.createIdentifierLabel();
               } else {
                  newLabel = new InternalIdentifierLabel();
               }

               newLabel.setLabelName(nameAndValue[0]);
               newLabel.setLabelValue(nameAndValue[1]);
               temporaryLabelList.put(nameAndValue[0], newLabel);

            } else {
               allValid = false;
            }
         } else {
            allValid = false;
         }
      }

      if (allValid) {
         if (datamodelFactory == null) {
            return new ArrayList<IdentifierLabel>();
         } else {
            ArrayList<IdentifierLabel> list = new ArrayList<IdentifierLabel>(temporaryLabelList.values());
            Collections.sort(list);
            return list;
         }
      } else {
         throw new NetInfUncheckedException("Identifier not valid");
      }
   }

   public static boolean isIdentifierVersioned(Identifier identifier) {
      IdentifierLabel versionKindLabel = identifier.getIdentifierLabel(DefinedLabelName.VERSION_KIND.getLabelName());
      if (versionKindLabel == null) {
         // TODO handle this case correctly! (throw and catch exception?)
         return false;
      }
      String labelValue = versionKindLabel.getLabelValue();

      return !DefinedVersionKind.UNVERSIONED.equals(DefinedVersionKind.getVersionKind(labelValue));
   }

   public static boolean isSyntacticallyValidIO(InformationObject informationObject) {
      if (informationObject == null) {
         return false;
      }

      if (!isSyntacticallyValidIdentifier(informationObject.getIdentifier())) {
         return false;
      }

      boolean attributesValid = true;
      for (Attribute attribute : informationObject.getAttributes()) {
         if (attribute == null) {
            return false;
         }
         attributesValid &= isSyntacticallyValidAttribute(attribute, null, informationObject);
      }

      // OWNER checks
      if (!(informationObject instanceof IdentityObject)) {
         List<Attribute> owner = informationObject.getAttribute(DefinedAttributeIdentification.OWNER.getURI());
         if (owner.size() != 1) {
            // no or more than one owner
            attributesValid = false;
         }

         Object ownerIdentification = ValueUtils.getObjectFromRaw(owner.get(0).getValueRaw());
         if (!(ownerIdentification instanceof String) || !(isValidIdentifierString((String) ownerIdentification))) {
            // invalid owner
            attributesValid = false;
         }

         // TODO String owner =
      }

      // PUBLIC_KEY checks
      List<Attribute> pubkey = informationObject.getAttribute(DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      if (pubkey.size() != 1) {
         // no or more than one pubkey
         attributesValid = false;
      }

      Object pubkeyValue = ValueUtils.getObjectFromRaw(pubkey.get(0).getValueRaw());
      if (!(pubkeyValue instanceof String) || (Utils.stringToObject((String) pubkeyValue) == null)
            || !(Utils.stringToObject((String) pubkeyValue) instanceof PublicKey)) {
         // invalid pubkey
         attributesValid = false;
      }

      return attributesValid;
   }

   public static boolean isValidIdentifierString(String string) {
      try {
         getIdentifierLabels(string, null);
      } catch (NetInfUncheckedException e) {
         return false;
      }
      return true;
   }

   /**
    * calculates the identifier of an IO from an identity description as used in integrity
    * 
    * @param identity
    *           string as received from private key file
    * @return identifier String
    */
   public static String identifierFromIdentity(String identity) {
      String separator = java.util.regex.Pattern.quote(IntegrityImpl.PATH_SEPERATOR);
      String possibleIdentifier = identity.split(separator)[0];
      if (isValidIdentifierString(possibleIdentifier)) {
         return possibleIdentifier;
      }
      return null;
   }

   public static String toStringInformationObject(InformationObject informationObject, String indent) {
      if (informationObject != null) {
         String realIdent = indent;

         if (realIdent == null) {
            realIdent = "";
         }

         StringBuilder stringBuilder = new StringBuilder();

         stringBuilder.append(realIdent + "Type: " + informationObject.getClass().getSimpleName());
         stringBuilder.append("\n" + realIdent + "Identifier: " + informationObject.getIdentifier());

         List<Attribute> attributes = informationObject.getAttributes();

         for (Attribute subAttribute : attributes) {
            stringBuilder.append("\n" + realIdent + "Attribute: \n" + toStringAttribute(subAttribute, realIdent + INDENT));
         }

         return stringBuilder.toString();
      } else {
         return null;
      }
   }

   public static String toStringAttribute(Attribute attribute, String indent) {
      String realIdent = indent;

      if (realIdent == null) {
         realIdent = "";
      }

      StringBuilder stringBuilder = new StringBuilder();

      stringBuilder.append(realIdent + "Identification: " + attribute.getIdentification());
      stringBuilder.append("\n" + realIdent + "AttributePurpose: " + attribute.getAttributePurpose());
      stringBuilder.append("\n" + realIdent + "Value: " + attribute.getValueRaw());

      List<Attribute> subattributes = attribute.getSubattributes();

      for (Attribute subAttribute : subattributes) {
         stringBuilder.append("\n" + realIdent + "Attribute: \n" + toStringAttribute(subAttribute, realIdent + INDENT));
      }

      return stringBuilder.toString();
   }

   public static String toStringIdentifier(Identifier identifier) {
      if (identifier != null) {
         StringBuilder sb = new StringBuilder();

         // The labels are already sorted!
         List<IdentifierLabel> identifierLabelsList = identifier.getIdentifierLabels();

         for (IdentifierLabel label : identifierLabelsList) {
            if (sb.length() != 0) {
               sb.append(LABEL_SEPARATOR);
            }

            sb.append(label.getLabelName() + LABEL_ASSIGNER + label.getLabelValue());
         }

         // Add the schema definition at the beginning
         if (sb.length() != 0) {
            sb.insert(0, DefinedRdfNames.NETINF_URI_SCHEMA);
         }

         return sb.toString();
      } else {
         return null;
      }
   }

   /**
    * Gets the content type out of the IO
    * 
    * @param io
    *           the Information-/DataObject
    * @return the content type in string format
    */
   public static String getContentType(InformationObject io) {
      List<Attribute> contentType = io.getAttribute(DefinedAttributeIdentification.CONTENT_TYPE.getURI());
      for (Attribute attr : contentType) { // should be one entry
         return attr.getValue(String.class);
      }

      // undefined content type
      return "application/octet-stream";
   }
   
   /**
    * Gets the hash-value of a DataObject
    * 
    * @param dataO
    *           the DataObject
    * @return hash-value of the DO or null
    */
   public static String getHash(InformationObject io) {
      List<Attribute> attributes = io.getAttribute(DefinedAttributeIdentification.HASH_OF_DATA.getURI());
      for (Attribute attr : attributes) {
         return attr.getValue(String.class);
      }
      return null;
   }
}
