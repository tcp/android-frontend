package project.cs.lisa.application.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import project.cs.lisa.application.MainNetInfActivity;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Used to send requests to the OpenNetInf RESTful API.
 * 
 * @author Linus Sunde
 * @author Harold Martinez
 * @author Thiago Costa Porto
 *
 */

public abstract class NetInfRequest extends AsyncTask<Void, Void, String> {

    /** Debug Log Tag. **/
    private static final String TAG = "NetInfRequest";

    // TODO inject from properties    
    /** HTTP Scheme. **/
    private static final String HTTP = "http://";

    // TODO inject from properties
    /** HTTP Timeout. **/
    private static final int TIMEOUT = 6000000;

    /** Calling Activity. **/
    private MainNetInfActivity mActivity;

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
    private HashMap<String, String> mQueryVariables = new HashMap<String, String>();

    /**
     * Create a new asynchronous NetInf message sent using HTTP GET.
     * @param activity     Activity creating this object
     * @param host         Target host of the message
     * @param port         Target port
     * @param hashAlg      Hash algorithm used
     * @param hash         Hash
     */

    public NetInfRequest(MainNetInfActivity activity, String host, String port,
            String hashAlg, String hash) {
        Log.d(TAG, "NetInfRequest()");
        mActivity = activity;
        mHost = host;
        mPort = port;
        mPathPrefix = "";
        mHashAlg = hashAlg;
        mHash = hash;
    }

    /**
     * Sends the NetInf request to the local node using HTTP.
     * @param   voids   Nothing.
     * @return          JSON response to the NetInf request sent as HTTP
     *                  or null if the request failed.
     */
    @Override
    protected String doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground()");

        // HTTP client with a timeout
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
        HttpClient client = new DefaultHttpClient(httpParams);

        // HTTP GET and response
        HttpGet get = new HttpGet(getUri());

        Log.d(TAG, "doInBackground(), Executing Http Get: " + getUri());

        // Execute the HTTP GET
        HttpResponse response = null;
        try {
            response = client.execute(get);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String jsonResponse = null;
        try {
            HttpEntity entity = response.getEntity();
            InputStream input = entity.getContent();
            jsonResponse = streamToString(input);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "Could not read json from HTTP response.");
            e.printStackTrace();
        }
        
        return jsonResponse;  
    }

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
    protected String streamToString(InputStream input) {
        Log.d(TAG, "streamToString()");
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
        Log.d(TAG, "addQuery()");
        Log.d(TAG, "key = " + key);
        Log.d(TAG, "value = " + value);
        mQueryVariables.put(key, value);
    }
    
    /**
     * Gets the query string representation of added query key-value pairs.
     * @return The query string
     */
    protected String getQueryString() {
        Log.d(TAG, "getQueryString()");
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for (String key : mQueryVariables.keySet()) {
            if (first) {
                queryString.append("?");
                first = false;
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
        Log.d(TAG, "getUri()");
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
    
    /**
     * Gets the creating activity.
     * @return      The creating activity.
     */
    protected MainNetInfActivity getActivity() {
        Log.d(TAG, "getActivity()");
        return mActivity;
    }
    
    /**
     * Sets the HTTP path prefix for the HTTP GET request send to the local NetInf node.
     * Should be set before do in background is called.
     * At time of writing should be either "ni" or "bo".
     * "ni" for a NetInf PUBLISH request
     * "bo" for a NetInf GET request
     *  // TODO straighten this interface out 
     * @param pathPrefix    "ni" or "bo"
     */
    protected void setPathPrefix(String pathPrefix) {
        Log.d(TAG, "setPathPrefix()");
        Log.d(TAG, "pathPrefix = " + pathPrefix);
        mPathPrefix = pathPrefix;
    }

}
