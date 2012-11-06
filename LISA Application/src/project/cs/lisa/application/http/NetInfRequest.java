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

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import project.cs.lisa.R;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public abstract class NetInfRequest extends AsyncTask<String, Void, String> {

    /** Debug Log Tag. **/
    private static final String TAG = "NetInfRequest";

    // TODO inject from properties    
    /** HTTP Scheme. **/
    private static final String HTTP = "http://";

    // TODO inject from properties
    /** HTTP Timeout. **/
    private static final int TIMEOUT = 6000000;

    /** Calling Activity. **/
    private Activity mActivity;

    // TODO inject from properties
    /** Target Host. **/
    private String mHost;

    // TODO inject from properties
    /** Target Port. **/
    private String mPort;

    /** Path Prefix. **/
    private String mPathPrefix;
    
    /** Hash Algorithm. **/
    private String mHashAlg;

    /** Hash. **/
    private String mHash;

    /** The rest of the URI. **/
    private HashMap<String, String> mQueryVariables;
    
    /** Toast. **/
    private Toast mToast;

    /**
     * Create a new asynchronous NetInf message sent using HTTP GET.
     * @param activity     Activity creating this object
     * @param host         Target host of the message
     * @param port         Target port
     * @param hashAlg      Hash algorithm used
     * @param hash         Hash
     */

    public NetInfRequest(Activity activity, String host, String port, String hashAlg, String hash) {
        mActivity = activity;
        mHost = host;
        mPort = port;
        mPathPrefix = "";
        mHashAlg = hashAlg;
        mHash = hash;
        // TODO move to activity
        mToast = new Toast(activity);
    }

    /**
     * Runs on the UI thread before doInBackground().
     */
    @Override
    protected abstract void onPreExecute();

    /**
     * Sends the NetInf request to the local node using HTTP.
     * @param params    Content-type, Meta-data
     * @return          JSON response to the NetInf request sent as HTTP.
     */
    @Override
    protected abstract String doInBackground(String... params);

    /**
     * Handles the response to the sent NetInf message.
     * @param   jsonResponse The response to the HTTP request.
     */
    @Override
    protected abstract void onPostExecute(String jsonResponse);

    /**
     * Converts an InputStream into a String.
     * @param input A input stream
     * @return      String representation of the input stream
     */
    protected String streamToString(Readable input) {
        try {
            // Read the stream from beginning to beginning.
            // That is everything into one token.
            return new Scanner(input).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }
    
    /**
     * Adds a key-value pair to the query part of the HTTP URI. 
     * @param key   The query key
     * @param value The value of the query key
     */
    protected void addQuery(String key, String value) {
        mQueryVariables.put(key, value);
    }
    
    /**
     * Gets the query string representation of added query key-value pairs.
     * @return The query string
     */
    protected String getQueryString() {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for (String key : mQueryVariables.keySet()) {
            if (first) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            queryString.append(key);
            queryString.append("=");
            queryString.append(mQueryVariables.get(key));
        }
        return queryString.toString();
    }
    
    /**
     * Creates the HTTP URI to use in the HTTP request in doInBackground.
     * @return The HTTP URI
     */
    protected String getUri() {
        StringBuilder uri = new StringBuilder();
        uri.append(HTTP);
        uri.append(mHost);
        uri.append(":");
        uri.append(mPort);
        uri.append("/");
        uri.append(mPathPrefix);
        uri.append("/");
        uri.append(mHashAlg);
        uri.append(";");
        uri.append(mHash);
        uri.append(getQueryString());
        return uri.toString();
    }
    
    protected void setPathPrefix(String pathPrefix) {
        mPathPrefix = pathPrefix;
    }
    
    // TODO move to activity
    /**
     * Function that hides the ProgressBar associated with the demo_sprint2 view.
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
    
    // TODO move to activity
    /**
     * Function that shows the ProgressBar associated with the demo_sprint2 view.
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

    // TODO move to activity
    /**
     * Function that shows a Toast to the user.
     * @param text String that will be shown in the toast
     */
    private void showToast(String text) {
        mToast.cancel();
        mToast = Toast.makeText(mActivity.getApplicationContext(), text,
                Toast.LENGTH_LONG);
        mToast.show();
    }

    // TODO move to activity
    /**
     * Kill any toast that is on display.
     */
    private void killToast() {
        mToast.cancel();
    }
}
