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
package project.cs.lisa.util;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import project.cs.lisa.metadata.Metadata;
import project.cs.lisa.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;

/**
 * A Builder that makes it easier to create information objects.
 *
 * @author Kim-Anh Tran
 *
 */
public class IOBuilder {

	/**
	 * The label for identifying content types.
	 */
	private static final String CONTENT_TYPE_LABEL =
			SailDefinedLabelName.CONTENT_TYPE.getLabelName();

	/**
	 * The label for identifying the hash contents.
	 */
	private static final String HASH_LABEL =
			SailDefinedLabelName.HASH_CONTENT.getLabelName();

	/**
	 * The label for identifying the hash algorithm.
	 */
	private static final String HASH_ALG_LABEL = SailDefinedLabelName.HASH_ALG.getLabelName();

	/**
	 * The label for identifying the meta data.
	 */
	private static final String META_LABEL = SailDefinedLabelName.META_DATA.getLabelName();

	/** The datamodel factory that is needed to create information objects. */
	private DatamodelFactory mFactory;

	/** The identifier of the information object we are creating. */
	private Identifier mIdentifier;

	/** The meta data. */
	private Metadata mMetadata;

	/** The Information Object. **/
	private InformationObject mIo;

	/**
	 * Creates a new Builder.
	 *
	 * @param factory	The factory for creating the information object
	 */
	public IOBuilder(DatamodelFactory factory) {
		mFactory = factory;
		mIdentifier = mFactory.createIdentifier();
		mMetadata = new Metadata();
		mIo = mFactory.createInformationObject();

	}

	/**
	 * Creates a new Builder with the given meta data.
	 * @param factory		The datamodel factory
	 * @param jsonMetadata	The metadata string encoded in json
	 */
	public IOBuilder(DatamodelFactory factory, String jsonMetadata) {
	    this(factory);
	    mMetadata = new Metadata(jsonMetadata);
	}

	/**
	 * Sets the hash value.
	 *
	 * @param hash	The hash value of the information object.
	 * @return		Returns this Builder.
	 */
	public IOBuilder setHash(String hash) {
		addIdentifierLabel(mIdentifier, HASH_LABEL, hash);
		return this;
	}

	/**
	 * Sets the hash algorithm.
	 *
	 * @param hashAlgorithm	The hash algorithm of the information object.
	 * @return		Returns this Builder.

	 */
	public IOBuilder setHashAlgorithm(String hashAlgorithm) {
		addIdentifierLabel(mIdentifier, HASH_ALG_LABEL, hashAlgorithm);
		return this;
	}

	/**
	 * Sets the content type.
	 *
	 * @param contentType	The content type of the information object.
	 * @return		Returns this Builder.

	 */
	public IOBuilder setContentType(String contentType) {
		addIdentifierLabel(mIdentifier, CONTENT_TYPE_LABEL, contentType);
		return this;
	}

	/**
	 * Adds a meta data key value pair to the information object.
	 *
	 * @param key	The key of the metadata
	 * @param value	The value of the metadata
	 * @return		Returns this Builder.
	 */
	public IOBuilder addMetaData(String key, String value) {
		mMetadata.insert(key, value);
		return this;
	}

	/**
	 * Sets the meta data (overwriting any previously added) of the information object.
	 * @param jsonMetadata
	 *     the metadata as a json string
	 * @return
	 *     the builder
	 */
	public IOBuilder setMetaData(String jsonMetadata) {
	    mMetadata = new Metadata(jsonMetadata);
	    return this;
	}

	/**
	 * Adds a bluetooth locator to the information object.
	 * @param bluetoothMac
	 *     The bluetooth MAC address
	 * @return
	 *     The builder
	 */
	public IOBuilder addBluetoothLocator(String bluetoothMac) {
	    addAttribute(
	            DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString(),
	            SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI(),
	            bluetoothMac);
	    return this;
	}

    /**
     * Adds a file path locator to the information object.
     * @param bluetoothMac
     *     The file path locator
     * @return
     *     The builder
     */
    public IOBuilder addFilePathLocator(String bluetoothMac) {
        addAttribute(
                DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString(),
                SailDefinedAttributeIdentification.FILE_PATH.getURI(),
                bluetoothMac);
        return this;
    }

	/**
	 * Creates an information object based on the Builder object.
	 *
	 * @return	The information object that was created.
	 */
	public InformationObject build() {
		String metaString;
		if (mMetadata.convertToString().contains("meta")) {
			// The metadata was setted and not created from scratch
			metaString = mMetadata.convertToString();
		} else {
			metaString = mMetadata.convertToMetadataString();
		}
		
		addIdentifierLabel(mIdentifier, META_LABEL, metaString);
		mIo.setIdentifier(mIdentifier);
		return mIo;
	}

	/**
	 * Adds an identifier label for the specified identifier for the passed label properties.
	 *
	 * @param identifier	The identifier to modify
	 * @param labelName		The label name
	 * @param labelValue	The label value
	 */
	private void addIdentifierLabel(Identifier identifier, String labelName, String labelValue) {
		 IdentifierLabel hashLabel = mFactory.createIdentifierLabel();
         hashLabel.setLabelName(labelName);
         hashLabel.setLabelValue(labelValue);
         identifier.addIdentifierLabel(hashLabel);
	}

	/**
	 * Adds an an Attribute to the current information object.
	 * 
	 * @param attributePurpose			The purpose of the attribute
	 * @param attributeIdentification	The identification 
	 * @param attributeValue			The actual attribute
	 */
	private void addAttribute(
	        String attributePurpose,
	        String attributeIdentification,
	        String attributeValue) {

        Attribute attribute = mFactory.createAttribute();
        attribute.setAttributePurpose(attributePurpose);
        attribute.setIdentification(attributeIdentification);
        attribute.setValue(attributeValue);
        mIo.addAttribute(attribute);
	}
}