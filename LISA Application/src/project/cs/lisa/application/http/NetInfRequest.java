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

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import project.cs.lisa.R;
import project.cs.lisa.file.LisaFileHandler;
import project.cs.lisa.metadata.LisaMetadata;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// TODO: Change variable names: 'response' to 'JSONString'

/**
 * Used to send requests to the OpenNetInf RESTful API.
 * 
 * TODO: Split into two classes: one for get, one for publish.
 * 
 * @author Linus Sunde
 * @author Harold Martinez
 * @author Thiago Costa Porto
 *
 */

public class NetInfRequest extends AsyncTask<String, Void, String> {
    public static enum RequestType {
        /** Represents a NetInf PUBLISH request. **/
        PUBLISH,
        /** Represents a NetInf GET request. **/
        GET;
    }

    /** Debug Log Tag. **/
    private static final String TAG = "NetInfRequest";

    /** HTTP Scheme. **/
    private static final String HTTP = "http://";

    // TODO add to properties file
    /** HTTP Timeout. **/
    private static final int TIMEOUT = 6000000;

    /** Publish Message Type String Representation. **/
    private static final String PUBLISH = "PUT";

    /** Get Message Type String Representation. **/
    private static final String GET = "GET";

    /** Calling Activity. **/
    private Activity mActivity;

    /** Target Host. **/
    private String mHost;

    /** Target Port. **/
    private int mPort;

    /** Message Type. **/
    private RequestType mMessageType;

    /** Hash Algorithm. **/
    private String mHashAlg;

    /** Hash. **/
    private String mHash;

    /** The rest of the URI. **/
    private String mQuery;
    
    /** Toast **/
    private Toast mToast;

    /**
     * Create a new asynchronous NetInf message sent using HTTP GET.
     * @param activity     Activity creating this object
     * @param host         Target host of the message
     * @param port         Target port
     * @param messageType  Type of the message, publish, get, etc.
     * @param hashAlg      Hash algorithm used
     * @param hash         Hash
     */

    public NetInfRequest(Activity activity, String host, int port,
            RequestType messageType, String hashAlg, String hash) {
        mActivity = activity;
        mHost = host;
        mPort = port;
        mMessageType = messageType;
        mHashAlg = hashAlg;
        mHash = hash;
        mQuery = mHashAlg + ";" + mHash;
        mToast = new Toast(activity);

        // Example of full uris
        // http://example.com:80/ni/
        //     sha-256;ABCDEFGHIJKLMNOPQRSTUVWXYZ?METHOD=GET
        // http://example.com:80/ni/
        //     sha-256;ABCDEFGHIJKLMNOPQRSTUVWXYZ?METHOD=PUBLISH&BTMAC=12:34:56:78:90

        // Construct the query depending on message type
        switch (mMessageType) {
            case PUBLISH:
                // Tell user you are publishing
                showToast("Trying to publish content");
                
                // Publish query
                mQuery = "/ni/" + mQuery + "?METHOD=" + PUBLISH;

                // Add locators
                // TODO: Currently only publishes your own bluetooth mac address
                BluetoothAdapter bluetoothDefaultAdapter = BluetoothAdapter.getDefaultAdapter();

                if (bluetoothDefaultAdapter != null) {
                    if (bluetoothDefaultAdapter.isEnabled()) {
                        // Get Bluetooth MAC Address
                        String btMac = BluetoothAdapter.getDefaultAdapter().getAddress();

                        // Add BT MAC Address to query
                        mQuery += "&BTMAC=" + btMac;
                    }
                    else {
                        showToast("Please, turn on Bluetooth");
                        Log.d(TAG, "Bluetooth not enabled");
                    }
                }
                else {
                    showToast("Error connecting Bluetooth. Please, restart your Bluetooth");
                    Log.d(TAG, "Bluetooth adapter is null");		            
                }

                showProgressBar("Publishing file");
                break;

            case GET:
                showToast("Requesting data");

                // Get query with BO request
                mQuery = "/bo/" + mQuery + "?METHOD=" + GET;
                
                showProgressBar("Requesting file");                
                break;

            default:
                showToast("Something nasty happened. Try again");                
                Log.d(TAG, "Unreachable code: Invalid message type");
                break;
        }
    }

    /**
     * Pre-execute method.
     */

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute()");
    }

    // TODO: Separate PUBLISH and GET processes from this function
    /**
     * Sends the HTTP GET request containing the netInf message to the target.
     * @param params   Either null or two strings with content type and meta data
     */
    
    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "doInBackground()");

        // If it is a publish, try to get the content type and meta data
        if (mMessageType == RequestType.PUBLISH && params.length == 2) {
            mQuery += "&CT=" + params[0];
            mQuery += "&META=" + params[1];
        }
        else {
            Log.d(TAG, "Content type and meta data not provided");
        }

        // Create uri
        String uri = HTTP + mHost + ":" + Integer.toString(mPort) + mQuery;

        // Create http client with a timeout
        HttpParams httpParams = new BasicHttpParams();

        // Set Connection timeout
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);

        // Set SO timeout
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
        HttpClient client = new DefaultHttpClient(httpParams);

        // Create http get
        HttpGet get = new HttpGet(uri);

        // Response obj
        HttpResponse response = null;

        Log.d(TAG, "doInBackground(), Executing Http Get: " + uri);

        // TODO: Refactor and figure out a way of fixing the exceptions!
        // Try to execute the http get
        try {
            response = client.execute(get);
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        catch (SocketTimeoutException e) {
            Log.d(TAG, "TimeoutException");
            response = null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground(), Extracting Http Get Response Content");

        switch (mMessageType) {
            case PUBLISH:
                Log.d(TAG, "NetInf Publish response, returning null");
                return null;
            case GET:
                Log.d(TAG, "NetInf Get response, returning JSON String");
                return readGetResponse(response);
            default:
                Log.d(TAG, "Unreachable code: Invalid message type, returning null");
                return null;
        }
    }

    /**
     * Handles the response to the sent NetInf message.
     * @param response     The HTTP Response
     */
    
    @Override
    protected void onPostExecute(String _JSONString) { 	
        Log.d(TAG, "onPostExecute()");

        switch (mMessageType) {
            case PUBLISH:
                logPublishResponse();
                break;
            case GET:
                logGetResponse(_JSONString);
                handleGetResponse(_JSONString);
                break;
            default:
                Log.d(TAG, "Unreachable code: Invalid message type");
                break;
        }
    }

    /**
     * Handles the response to a NetInf Get message.
     * @param response  HashMap containing filename and content type
     */
    
    private void logGetResponse(String response) {
        Log.d(TAG, "handleGetResponse()");
        Log.d(TAG, "string response " + response);
    }

    /**
     * Extracts the filePath and the contentType from the response.
     * 
     * The filePath is generated after the file has been transmitted
     * from a remote device and stored on the local device.
     * 
     * The contentType came from the NRS bounded with the name of the file.
     * 
     * @param response  The response from this background thread. 
     */
    
    private void handleGetResponse(String _JSONString) {
        Log.d(TAG, "handleGetResponse()");
        
        if (_JSONString != null) {
            LisaMetadata lM = new LisaMetadata(_JSONString);
            String filePath = lM.get("filePath");
            String contentType = lM.get("contentType");
            Log.d(TAG, "contentType = " + contentType);
            Log.d(TAG, "filePath = " + filePath);
            
            // Display the file according to the file type.
            // TODO: Make this an exception!
            // TODO: Clean this
            int display = LisaFileHandler.displayContent(mActivity, filePath, contentType);
            if (display == LisaFileHandler.ERR_NULL_PATH_RECEIVED) {
                Toast toast = new Toast(mActivity);
                toast.cancel();
                toast = Toast.makeText(mActivity, "Could not find this file", Toast.LENGTH_LONG);
                toast.show();
            }
            
            // Arrange View
            hideProgressBar();
        }
        else {
            Log.d(TAG, "_JSONSTring null, probably TimeoutException happened somewhere");
            
            // Message the user
            showToast("We could not get the content. Check your Internet " +
            		"and your Bluetooth connection");
            
            // Modify View
            hideProgressBar();
        }
    }

    /**
     * Handles the response to a NetInf Publish message.
     */
    
    private void logPublishResponse() {
        Log.d(TAG, "handlePublishResponse()");
        
        // Dialog
        final AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setTitle("Hash");
        b.setMessage("You have published this hash: " + mHash);
        b.setCancelable(false);
        b.setPositiveButton("OK", null);
        b.show();
        
        // hide ProgressBar
        hideProgressBar();
        killToast();
    }

    /**
     * Extracts the HashMap from a NetInf Get response.
     * @param response  the HTTP response
     * @return          a HashMap containing the keys "filePath" and "contentType"
     *                  with their respective values set appropriately
     */
    
    private String readGetResponse(HttpResponse response) {
        Log.d(TAG, "readGetResponse()");

        if (response == null) {
            Log.d(TAG, "Response is null");
            return null;
        }

        String _JSONString = null;
        
        // TODO: Fix the exception/return values. Make this less hacked.
        try {
            // Get content
            InputStream content = response.getEntity().getContent();
            _JSONString = streamToString(content);
            Log.d(TAG, _JSONString);
        }
        catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }
        catch (NullPointerException e) {
            Log.d(TAG, "Content is null");
        }

        return _JSONString;
    }

    /**
     * Converts an InputStream into a String.
     * TODO Only here because of the ugly implementation?
     * @param input A input stream
     * @return String representation of the input stream
     */
    
    private String streamToString(InputStream input) {
        try {
            return new Scanner(input).useDelimiter("\\A").next();
        }
        catch (NoSuchElementException e) {
            return "";
        }
    }
    
    /**
     * Function that hides the ProgressBar associated with the demo_sprint2 view
     */
    
    private void hideProgressBar() {
        Log.d(TAG, "hideProgressBar()");
        ProgressBar pb = (ProgressBar) mActivity.findViewById(R.id.progressBar1);
        pb.setVisibility(ProgressBar.INVISIBLE);
        ProgressBar pb1 = (ProgressBar) mActivity.findViewById(R.id.progressbar_Horizontal);
        pb1.setVisibility(ProgressBar.INVISIBLE);
        TextView tv = (TextView) mActivity.findViewById(R.id.ProgressBarText);
        tv.setVisibility(TextView.INVISIBLE);
    }
    
    /**
     * Function that shows the ProgressBar associated with the demo_sprint2 view
     * @param text String with the text to show to the user. Normally informs
     *             if we are publishing, searching or requesting content.
     */
    
    private void showProgressBar(String text) {
        ProgressBar pb = (ProgressBar) mActivity.findViewById(R.id.progressBar1);
        pb.setVisibility(ProgressBar.VISIBLE);
        TextView tv = (TextView) mActivity.findViewById(R.id.ProgressBarText);
        tv.setVisibility(TextView.VISIBLE);
        tv.setText(text);
    }

    /**
     * Function that shows a Toast to the user
     * @param text String that will be shown in the toast
     */
    
    private void showToast(String text) {
        mToast.cancel();
        mToast = Toast.makeText(mActivity.getApplicationContext(), text,
                Toast.LENGTH_LONG);
        mToast.show();
    }

    /**
     * Kill any toast that is on display
     */
    
    private void killToast() {
        mToast.cancel();
    }
}
