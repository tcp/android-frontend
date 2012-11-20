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
package project.cs.lisa.application.http;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

import project.cs.lisa.exceptions.NullEntityException;
import project.cs.lisa.metadata.Metadata;
import android.util.Log;

/**
 * Used to send NetInf GET requests to the OpenNetInf RESTful API.
 * @author Linus Sunde
 */
public class NetInfPublish extends NetInfRequest {

    /** Debug tag. **/
    public static final String TAG = "NetInfPublish";

    /** Encoding. **/
    public static final String ENCODING = "UTF-8";

    /** Locators. **/
    private Set<Locator> mLocators;

    /** File. **/
    private File mFile;

    /**
     * Creates a new asynchronous NetInf PUBLISH.
     * @param host         Target host of the message
     * @param port         Target port
     * @param hashAlg      Hash algorithm used
     * @param hash         Hash
     * @param locators     Set of locators to publish
     */
    public NetInfPublish(String host, String port,
            String hashAlg, String hash, Set<Locator> locators) {

        super(host, port, hashAlg, hash);
        Log.d(TAG, "NetInfPublish()");

        mLocators = locators;

        // TODO make this beautiful
        setPathPrefix("publish");

    }

    /**
     * Sends the NetInf PUBLISH request to the local node using HTTP.
     * @param   voids   Nothing.
     * @return          JSON response to the NetInf request sent as HTTP
     *                  or null if the request failed.
     */
    @Override
    protected String doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground()");

        // Don't publish without locators
        if (mLocators == null || mLocators.size() == 0) {
            return null;
        }

        // Add locators
        for (Locator locator : mLocators) {
            addQuery(locator.getQueryKey(), locator.getQueryValue());
        }

        try {
            // TODO break into several methods
            // FullPut or not?
            HttpUriRequest request;
            if (mFile != null) {
                request = new HttpPost(getUri());
            } else {
                request = new HttpPut(getUri());
            }
            // Execute HTTP request
            return execute(request);
        } catch (NullEntityException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Sets the content type to be sent in the NetInf PUBLISH message.
     * @param contentType       The content type.
     */
    public void setContentType(String contentType) {
        Log.d(TAG, "setContentType()");
        addQuery("ct", contentType);
    }

    /**
     * Sets the metadata to be sent in the NetInf PUBLISH message.
     * @param metadata      The JSON string containing the metadata.
     */
    public void setMetadata(Metadata metadata) {
        Log.d(TAG, "setMetadata()");
        String meta = metadata.convertToMetadataString();
        Log.d(TAG, "meta = " + meta);
        addQuery("meta", meta);
    }

    /**
     * Sets the file to publish.
     * @param file
     *      The file to publish.
     */
    public void setFile(File file) {
        Log.d(TAG, "setFile()");
        if (file == null) {
            throw new IllegalArgumentException("setFile() called with null file");
        }
        mFile = file;
        addQuery("filePath", file.getAbsolutePath());
    }

}
