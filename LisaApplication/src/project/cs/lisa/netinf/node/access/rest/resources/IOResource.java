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

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import project.cs.lisa.util.IOBuilder;
import android.util.Log;

/**
 * Handles NetInf Publish and Get requests.
 * @author Linus Sunde
 *
 */
public class IOResource extends LisaServerResource {

    /** Debug tag. **/
    public static final String TAG = "IOResource";

    /** Hash Algorithm. **/
    private String mHashAlg;

    /** Hash. **/
    private String mHash;

    /** Content Type. **/
    private String mContentType;

    /** Bluetooth MAC Address. **/
    private String mBluetoothMac;

    /** Metadata. **/
    private String mMeta;

    /** File path. **/
    private String mFilePath;

    /** Implementation of DatamodelFactory, used to create and edit InformationObjects etc. **/
    private DatamodelFactory mDatamodelFactory;

    /** Node Connection, used to access the local NetInf node. **/
    private NetInfNodeConnection mNodeConnection;

    @Override
    protected void doInit() {
        super.doInit();
        Log.d(TAG, "doInit()");

        mHashAlg           = getQuery().getFirstValue("hashAlg", true);
        mHash              = getQuery().getFirstValue("hash", true);
        mContentType       = getQuery().getFirstValue("ct", true);
        mBluetoothMac      = getQuery().getFirstValue("btmac", true);
        mMeta              = getQuery().getFirstValue("meta", true);
        mFilePath          = getQuery().getFirstValue("filePath", true);

        Log.d(TAG, "mHashAlg = " + mHashAlg);
        Log.d(TAG, "mHash = " + mHash);
        Log.d(TAG, "mContentType = " + mContentType);
        Log.d(TAG, "mBluetoothMac = " + mBluetoothMac);
        Log.d(TAG, "mMeta = " + mMeta);
        Log.d(TAG, "mFilePath = " + mFilePath);

        mDatamodelFactory = getDatamodelFactory();
        mNodeConnection   = getNodeConnection();
    }

    /**
     * Debug.
     */
    @Post
    public void handlePost() {
        Log.e(TAG, "@Post");
        IOBuilder builder = new IOBuilder(mDatamodelFactory);
        builder.addFilePathLocator(mFilePath);
        publish(builder);
    }

    /**
     * Debug.
     */
    @Get
    public void handleGet() {
        Log.e(TAG, "@Get");
    }

    /**
     * Debug.
     */
    @Delete
    public void handleDelete() {
        Log.e(TAG, "@Delete");
    }

    /**
     * Publish an IO.
     * @return      JSON String with key "status" set to "ok" if publish succeeded
     *              or "failed" if publish failed
     */
    @Put
    public String putIO() {
        Log.d(TAG, "putIO()");
        return publish();
    }

    private String publish() {
        return publish(new IOBuilder(mDatamodelFactory));
    }

    private String publish(IOBuilder builder) {

        builder.setHash(mHash).setHashAlgorithm(mHashAlg);

        if (mContentType != null) {
            builder.setContentType(mContentType);
        }

        if (mBluetoothMac != null) {
            builder.addBluetoothLocator(mBluetoothMac);
        }

        if (mMeta == null) {
            // Create empty meta data
            mMeta = "{\"meta\":{}}";
        }
        builder.setMetaData(mMeta);

        //Putting the IO
        try {
            Log.d(TAG, "calling putIO()");
            mNodeConnection.putIO(builder.build());
        } catch (NetInfCheckedException e) {
            Log.e(TAG, e.getMessage());
            return "{\"status\":\"failed\"}";
        } catch (NetInfUncheckedException e) {
            Log.e(TAG, e.getMessage());
            return "{\"status\":\"failed\"}";
        }
        Log.d(TAG, "Publish succeeded.");
        return "{\"status\":\"ok\"}";

    }


}
