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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;

import org.restlet.resource.Get;

import project.cs.lisa.metadata.LisaMetadata;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.transferdispatcher.TransferDispatcher;
import android.os.Environment;
import android.util.Log;

/**
 * Requests and Retrieves a BO.
 * 
 * @author Miguel Sosa
 * @author Hugo Negrette
 * @author Paolo Boshini
 * @author Kim-Anh Tran
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

    /** The hash algorithm used to generate the hash value . */
    private String mHashAlgorithm;

    /**
     * Initializes the context of a BOResource.
     */
    @Override
    protected void doInit() {
        super.doInit();
        mHashValue = getQuery().getFirstValue("HASH_VALUE", true);
        mHashAlgorithm = getQuery().getFirstValue("HASH_ALG", true);
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
        byte[] fileData = null;
        String filePath = "";
        String contentType = "";

        /* Retrieve a data object from a node (could be an NRS) */
        InformationObject io = retrieveDO();

        // TODO NO THIS NOOOOO
        LisaMetadata lisaMetadata = new LisaMetadata();
        lisaMetadata.insert(CONTENT_TYPE, "k");
        lisaMetadata.insert(FILEPATH, "o");

        return lisaMetadata.convertToString();		

        /* Retrieve the data corresponding to the hash from another device. */
//        if (io != null) {
//
//            /* Store the content type of the requested BO */
//            contentType = io.getIdentifier().getIdentifierLabel(
//                    SailDefinedLabelName.CONTENT_TYPE.getLabelName())
//                    .getLabelValue();
//
//            /* Attempt to transfer the BO from a remote device */
//            TransferDispatcher tsDispatcher = TransferDispatcher.INSTANCE;
//            try {
//                fileData = tsDispatcher.getByteArray(io);
//            } catch (IOException e) {
//                Log.e(TAG, "Couldn't retrieve the requested data.");
//            }
//
//            /* Writes the received data to file */ 
//            if (fileData != null) {
//
//                String hash = io.getIdentifier().getIdentifierLabel(
//                        SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
//
//                filePath = Environment.getExternalStorageDirectory() + "/LISA/" + hash;
//                writeByteStreamToFile(filePath, fileData);
//
//            } else {
//                Log.e(TAG, "No file data to write.");
//            }
//        }
//
//        LisaMetadata lisaMetadata = new LisaMetadata();
//        lisaMetadata.insert(CONTENT_TYPE, contentType);
//        lisaMetadata.insert(FILEPATH, filePath);
//
//        return lisaMetadata.convertToString();
    }

    /**
     * Returns an IO (i.e. DO) containing the list of locators that own the
     * requested BO.
     * 
     * @return The IO that contains the locator list.
     */
    private InformationObject retrieveDO() {
        Log.d(TAG,
                "Retrieve the IO containing the locators from a remote node.");

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
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find file: " + targetPath);
        } catch (IOException e) {
            Log.e(TAG, "Failed while writing data to " + targetPath);
        } finally {

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG,
                            "Failed closing the stream after writing to file.");
                }
            }
        }
    }
}