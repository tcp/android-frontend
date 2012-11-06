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

import project.cs.lisa.application.MainNetInfActivity;
import project.cs.lisa.file.LisaFileHandler;
import project.cs.lisa.metadata.LisaMetadata;
import android.util.Log;

/**
 * Used to send NetInf GET requests to the OpenNetInf RESTful API.
 * @author Linus Sunde
 */
public class NetInfGet extends NetInfRequest {

    /** Debug tag. **/
    public static final String TAG = "NetInfGet";
    
    /**
     * Creates a new asynchronous NetInf GET.
     * @param activity     Activity creating this object
     * @param host         Target host of the message
     * @param port         Target port
     * @param hashAlg      Hash algorithm used
     * @param hash         Hash
     */
    public NetInfGet(MainNetInfActivity activity, String host, String port,
            String hashAlg, String hash) {
        
        super(activity, host, port, hashAlg, hash);
        
        // TODO make this beautiful
        setPathPrefix("bo");
        addQuery("METHOD", "GET");
        
    }

    /**
     * Handles the response to the sent NetInf GET message.
     * @param jsonResponse     The JSON response.
     */
    @Override
    protected void onPostExecute(String jsonResponse) { 	
        Log.d(TAG, "onPostExecute()");
        Log.d(TAG, "jsonString = " + jsonResponse);
        
        try {
            // Parse the JSON
            LisaMetadata json = new LisaMetadata(jsonResponse);
            String filePath = json.get("filePath");
            String contentType = json.get("contentType");
            Log.d(TAG, "contentType = " + contentType);
            Log.d(TAG, "filePath = " + filePath);
            
            // Try to display the file
            int code = LisaFileHandler.displayContent(getActivity(), filePath, contentType);
            Log.d(TAG, "code = " + code);
            switch (code) {
                case LisaFileHandler.OK:
                    break;
                default:
                    getActivity().showToast("Opening file failed.");
                    break;
            }
            
        } catch (NullPointerException e) {
            e.printStackTrace();
            getActivity().showToast(
                    "Getting file failed. Check your Internet and Bluetooth connections");
        }
    }

}
