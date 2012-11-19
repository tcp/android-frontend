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

/**
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
package project.cs.lisa.netinf.node.access.rest.resources;

import java.io.File;
import java.io.IOException;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;

import org.apache.commons.io.FileUtils;
import org.restlet.resource.Get;

import project.cs.lisa.application.MainApplication;
import project.cs.lisa.metadata.Metadata;
import project.cs.lisa.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.transferdispatcher.TransferDispatcher;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

/**
 * Requests and Retrieves a BO.
 *
 * @author Miguel Sosa
 * @author Hugo Negrette
 * @author Paolo Boschini
 * @author Kim-Anh Tran
 * @author Linus Sunde
 * @author Thiago Costa Porto
 *
 */
public class BOResource extends LisaServerResource {

    /** Debugging Tag. */
    private static final String TAG = "BOResource";

    /** HashMap Key: Filepath. */
    private static final String FILEPATH = "filePath";

    /** HashMap Key: Content type. */
    private static final String CONTENT_TYPE = "contentType";

    /** The hash value of the requested BO. */
    private String mHashValue;

    /** The hash algorithm used to generate the hash value. */
    private String mHashAlgorithm;

    /** The directory containing the published files. */
    private String mSharedFolder =
            Environment.getExternalStorageDirectory() + "/DCIM/Shared/";

    /**
     * Initializes the context of a BOResource.
     */
    @Override
    protected void doInit() {
        super.doInit();

        mHashValue = getQuery().getFirstValue("hash", true);
        mHashAlgorithm = getQuery().getFirstValue("hashAlg", true);

        createSharedFolder();
    }

    /**
     * Responds to an HTTP get request. Returns a String representing the meta-data
     * that describes the retrieved file.
     *
     * @return The Map that contains the information about the file: First key:
     *         the file path Second key: the content type of the file.
     *         If the object couldn't be retrieved the function returns null.
     */
    @Get
    public String retrieveBO() {
        Log.d(TAG, "Trying to retrieve the BO.");

        byte[] fileData = null;

        // Retrieve a data object from a node (could be an NRS)
        InformationObject io = retrieveDO();

        // If the NetInf GET got the file data we are done!
        String filePath = io.getSingleAttribute(SailDefinedAttributeIdentification.FILE_PATH.getURI()).getValueRaw();
        if (filePath != null) {
            Log.d(TAG, "The NetInf GET contained the file");
            String contentType = io.getIdentifier().getIdentifierLabel(
                    SailDefinedLabelName.CONTENT_TYPE.getLabelName())
                    .getLabelValue();
            Metadata metadata = new Metadata();
            metadata.insert(CONTENT_TYPE, contentType);
            metadata.insert(FILEPATH, filePath);
            return metadata.convertToString();
        }
        Log.d(TAG, "The NetInf GET didn't contain the file");

        // Retrieve the data corresponding to the hash from another device.
        if (io != null) {
            TransferDispatcher tsDispatcher = TransferDispatcher.INSTANCE;

            try {
                fileData = tsDispatcher.getByteArray(io);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't retrieve the requested data.");
                return null;
            }

            if (fileData != null) {
                String metaDataString = saveBO(io, fileData);
                return metaDataString;

            } else {
                Log.e(TAG, "No file data to write.");
                return null;
            }

        } else {
            Log.e(TAG, "InformationObject is null. Nothing was done here.");
            return null;
        }
    }

    /**
     * Saves the file data corresponding to the specified io and
     * returns a String representation of the related meta-data.
     *
     * @param io		The Information Object describing the file data
     * @param fileData	The file data corresponding to the io
     * @return			Returns a String representation of the meta data.
     */
    private String saveBO(InformationObject io, byte[] fileData) {
        // Store the content type of the requested BO
        String contentType = io.getIdentifier().getIdentifierLabel(
                SailDefinedLabelName.CONTENT_TYPE.getLabelName())
                .getLabelValue();

        // Fetch metadata from IO
        String metaData =
                io.getIdentifier().getIdentifierLabel("metadata").getLabelValue();
        Metadata metaLabel = new Metadata(metaData);

        // Set saving filename to the same filename as in metadata
        String filePath = mSharedFolder + metaLabel.get("filename");
        Log.d(TAG, "Filepath is: " + filePath);

        // Write it to file
        try {
            FileUtils.writeByteArrayToFile(new File(filePath), fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        makeFileVisibleToPhone(filePath, contentType);

        // Make a new metadata to pass along the content_type and filepath
        Metadata metadata = new Metadata();
        metadata.insert(CONTENT_TYPE, contentType);
        metadata.insert(FILEPATH, filePath);

        return metadata.convertToString();

    }

    /**
     * Returns an IO (i.e. DO) containing the list of locators that own the
     * requested BO.
     *
     * @return The IO that contains the locator list.
     */
    private InformationObject retrieveDO() {
        Log.d(TAG, "Retrieve the IO containing the locators from a remote node.");

        Identifier identifier = createIdentifier(mHashAlgorithm, mHashValue);
        InformationObject io = null;

        try {
            io = getNodeConnection().getIO(identifier);
        } catch (NetInfCheckedException e) {
            Log.e(TAG, "Failed retrieving the IO from the NRS. Hash value: "
                    + mHashValue);
        }

        return io;
    }

    /**
     * Creates the folder that contains the files to be shared with other phones.
     */
    private void createSharedFolder() {
        File folder = new File(mSharedFolder);

        if (!folder.exists()) {
            Log.d(TAG, "Creating shared folder " + mSharedFolder);
            boolean created = folder.mkdir();

            if (!created) {
                Log.e(TAG, "Failed creating the shared folder. Set shared folder to DCIM/");
                mSharedFolder = Environment.getExternalStorageDirectory() + "/DCIM/";
            }
        }
    }

    /**
     * Makes the file specified by file path visible to the user.
     *
     * @param filePath		The file path pointing to the file.
     * @param contentType	The content type of the file.
     */
    private void makeFileVisibleToPhone(String filePath, String contentType) {
        String[] paths = {filePath};
        String[] mediaType = {contentType};
        MediaScannerConnection.scanFile(MainApplication.getAppContext(), paths, mediaType, null);
    }
}