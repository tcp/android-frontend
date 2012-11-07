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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import project.cs.lisa.application.MainNetInfActivity;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

/**
 * Used to send NetInf GET requests to the OpenNetInf RESTful API.
 * @author Linus Sunde
 */
public class NetInfPublish extends NetInfRequest {

    /** Debug tag. **/
    public static final String TAG = "NetInfPublish";
    
    /**
     * Creates a new asynchronous NetInf PUBLISH.
     * @param activity     Activity creating this object
     * @param host         Target host of the message
     * @param port         Target port
     * @param hashAlg      Hash algorithm used
     * @param hash         Hash
     */
    public NetInfPublish(MainNetInfActivity activity, String host, String port,
            String hashAlg, String hash) {
        super(activity, host, port, hashAlg, hash);
        Log.d(TAG, "NetInfPublish()");
        
        // TODO make this beautiful
        setPathPrefix("ni");
        addQuery("METHOD", "PUT"); 
        
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
        
        // Try to add the Bluetooth MAC, if success run superclass method.
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        String jsonResponse = null;
        if (adapter == null) {
            getActivity().showToast("Error: Bluetooth not supported");
        } else if (!adapter.isEnabled()) {
            getActivity().showToast("Error: Bluetooth not enabled");
        } else {
            String btMac = adapter.getAddress();
            addQuery("BTMAC", btMac);
            jsonResponse = super.doInBackground(voids);
        }
        
        return jsonResponse;
    }
    
    /**
     * Handles the response to the sent NetInf PUBLISH message.
     * @param jsonResponse     The JSON response.
     */
    @Override
    protected void onPostExecute(String jsonResponse) { 	
        Log.d(TAG, "onPostExecute()");
        Log.d(TAG, "jsonString = " + jsonResponse);
    }

    /**
     * Sets the content type to be sent in the NetInf PUBLISH message.
     * @param contentType       The content type.
     */
    public void setContentType(String contentType) {
        Log.d(TAG, "setContentType()");
        addQuery("CT", contentType);
    }
    
    /**
     * Sets the metadata to be sent in the NetInf PUBLISH message.
     * @param metadata      The JSON string containing the metadata.
     */
    public void setMetadata(String metadata) {
        Log.d(TAG, "setMetadata()");
        Log.d(TAG, "metadata = " + metadata);
        try {
            String encodedMetadata = URLEncoder.encode(metadata, "UTF-8");
            Log.d(TAG, "encoded = " + encodedMetadata);
            addQuery("META", encodedMetadata);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 not supported");
            e.printStackTrace();
        }
    }
    
}