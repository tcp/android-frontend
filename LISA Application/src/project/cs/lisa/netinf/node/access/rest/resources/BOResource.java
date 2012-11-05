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
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */

package project.cs.lisa.netinf.node.access.rest.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;

import org.restlet.resource.Get;

import project.cs.lisa.application.MainApplication;
import project.cs.lisa.metadata.LisaMetadata;
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

        mHashValue = getQuery().getFirstValue("HASH", true);
        mHashAlgorithm = getQuery().getFirstValue("HASH_ALG", true);

        createSharedFolder();
    }

    /**
     * Responds to an HTTP get request. Returns a Map describing the retrieved
     * file.
     * 
     * @return The Map that contains the information about the file: First key:
     *         the file path Second key: the content type of the file
     */

    @Get
    public String retrieveBO() {
        Log.d(TAG, "Trying to retrieve the BO.");

        byte[] fileData = null;
        String filePath = "";
        String contentType = "";
        String returnString = null;

        /* Retrieve a data object from a node (could be an NRS) */
        InformationObject io = retrieveDO();	

        /* Retrieve the data corresponding to the hash from another device. */
        if (io != null) {
            /* Store the content type of the requested BO */
            contentType = io.getIdentifier().getIdentifierLabel(
                    SailDefinedLabelName.CONTENT_TYPE.getLabelName())
                    .getLabelValue();

            Log.d(TAG, "Trying to receive file with the following content type: " + contentType);

            /* Attempt to transfer the BO from a remote device */
            TransferDispatcher tsDispatcher = TransferDispatcher.INSTANCE;

            try {
                fileData = tsDispatcher.getByteArray(io);
            }
            catch (IOException e) {
                Log.e(TAG, "Couldn't retrieve the requested data.");
            }

            /* Writes the received data to file */ 
            if (fileData != null) {
                // Fetch metadata from IO
                String metaData = 
                        io.getIdentifier().getIdentifierLabel("metadata").getLabelValue();
                LisaMetadata metaLabel = new LisaMetadata(metaData);

                // Set saving filename to the same filename as in metadata
                filePath = mSharedFolder + metaLabel.get("filename");
                Log.d(TAG, "Filepath is: " + filePath);

                // Write it to file
                writeByteStreamToFile(filePath, fileData);
                makeFileVisibleToPhone(filePath, contentType);

                // Make a new metadata to pass along the content_type and filepath
                LisaMetadata lisaMetadata = new LisaMetadata();
                lisaMetadata.insert(CONTENT_TYPE, contentType);
                lisaMetadata.insert(FILEPATH, filePath);

                returnString = lisaMetadata.convertToString();
            }
            else {
                Log.e(TAG, "No file data to write.");
            }

            return returnString;
        }
        else {
            // TODO: Think about an exception to be thrown here. Maybe handle the return value
            // TODO: and throw an exception if that happens.
            Log.d(TAG, "InformationObject is null. Nothing was done here.");
            return returnString;
        }
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
        }
        catch (NetInfCheckedException e) {
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

    /**
     * Creates a new file containing the specified fileData at the specified
     * targetPath.
     * 
     * @param targetPath
     *            The location to create the file
     * @param fileData
     *            The data to write at the specified path
     */

    private void writeByteStreamToFile(String targetPath, byte[] fileData) {
        Log.d(TAG, "Writing received data to " + targetPath);

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(targetPath);
            fos.write(fileData);
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find file: " + targetPath);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed while writing data to " + targetPath);
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Failed closing the stream after writing to file.");
                }
            }
        }
    }
}
