package project.cs.lisa.search;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import netinf.common.datamodel.DatamodelFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.resource.Get;

import project.cs.lisa.exceptions.InvalidResponseException;
import project.cs.lisa.netinf.node.access.rest.resources.LisaServerResource;
import project.cs.lisa.util.UProperties;

import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SearchRequest extends LisaServerResource {
    /** Debug Tag. */
    private final String TAG = "SearchRequest"; 
    
    /** NRS IP address. **/
    private String mHost;
    
    /** NRS port. **/
    private String mPort;
    
    /** HTTP connection timeout. **/
    private static final int TIMEOUT = 5000;
    
    /** HTTP Client **/
    private HttpClient mClient;
    
    /** Keywords string **/
    private String mTokens;
    
    // TODO: Verify if Ext should be JSON
    /** Ext string **/
    private String mExt;
    
    /** Message ID string **/
    private String mMsgId;

    // TODO: Remove search into a better place.
    // TODO: Hey, I said 'throw away netinf model'. Bad sentence.
    @Override
    /**
     * Initializes a Search Service
     * @param host The NRS IP Address
     * @param port The NRS Port
     */
    protected void doInit() {
        Log.d(TAG, "doInit() search");
        mHost = UProperties.INSTANCE.getPropertyWithName("nrs.http.host");
        mPort = UProperties.INSTANCE.getPropertyWithName("nrs.http.port");
        // Setup HTTP client
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        mClient = new DefaultHttpClient(params);
    }

    /**
     * Creates the search URI that is going to be sent to the NRS.
     * @param msgId    Unique message-id
     * @param tokens   Keywords that are going to be searched for
     * @param ext      Extensions
     * @return         HTTP Post with host, port and URI
     * @throws UnsupportedEncodingException
     */
    private HttpPost createSearch(String msgId, String tokens, String ext)
            throws UnsupportedEncodingException {
        Log.d(TAG, "createSearch()");      
        Log.d(TAG, "Creating search to send to " + mHost + ":" + mPort);
        
        // POST
        HttpPost post = new HttpPost(mHost + ":" + mPort + "/netinfproto/search");

        // URI
        String completeUri = "?msgid=" + msgId  + "&tokens=" + tokens + "&ext=" + ext;
        
        // Logs URI
        Log.d(TAG, "createSearch() URI:\n" + completeUri);
        
        // Encode the URL
        String encodeUrl = null;
        encodeUrl = URLEncoder.encode(completeUri, "UTF-8");

        // create new entity
        HttpEntity newEntity =
                new InputStreamEntity(fromString(encodeUrl), encodeUrl.getBytes().length);
        
        // Add header
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        
        // set post entity
        post.setEntity(newEntity);

        return post;
    }

    /**
     * Handles the HTTP response from the server
     * @param response  HTTP Response from search request
     * @return          JSON Object
     * @throws InvalidResponseException
     */
    private JSONObject handleResponse(HttpResponse response)
            throws InvalidResponseException {
        Log.d(TAG, "handleResponse() [search]");
        
        // HTTP Status Response from the HTTP request response
        int statusCode = response.getStatusLine().getStatusCode();
        Log.d(TAG, "statusCode = " + statusCode);
        
        // Return object
        JSONObject json = null;
        
        // Temp string
        String jsonString = null;
        
        switch (statusCode) {
            // Status Code OK: 200
            case HttpStatus.SC_OK:
                // 200 returns a JSON
                jsonString = readJson(response);
                json = parseJson(jsonString);
                return json;
            
            // Status Code NOT FOUND: 404
            case HttpStatus.SC_NOT_FOUND:
                // Returns null if nothing was found on the server
                json = new JSONObject();
                return json;
            
            // Everything else
            default:
                // Something unhandled
                throw new InvalidResponseException("Unexpected Response Code = " + statusCode);
        }
    }

    /**
     * Search function. Build and send a HTTP search request to the NRS and
     * may receive either a JSON Object or null.
     * @return  JSON Object if search was successful.
     *          null        if search raised an exception.
     */
    @Get
    public JSONObject search() {
        Log.d(TAG, "search()");
        
        // Search request requires three fields: tokens (keywords), message-id and ext.
        mTokens = getQuery().getFirstValue("tokens", true);
        mMsgId = getQuery().getFirstValue("msgId", true);
        mExt = getQuery().getFirstValue("ext", true);
        
        try {
            // Create NetInf SEARCH request
            Log.d(TAG, "Creating HTTP POST");
            HttpPost searchRequest = createSearch(mMsgId, mTokens, mExt);
            Log.d(TAG, searchRequest.toString());

            // Execute NetInf SEARCH request
            Log.d(TAG, "Executing HTTP POST");
            HttpResponse response = mClient.execute(searchRequest);

            // Print all response headers
            Log.d(TAG, "HTTP POST Response Headers:");
            for (Header header : response.getAllHeaders()) {
                Log.d(TAG, "\t" + header.getName() + " = " + header.getValue());
            }

            // Handle the response
            Log.d(TAG, "Handling HTTP POST Response");
            JSONObject json = handleResponse(response);
            Log.d(TAG, "search() succeeded. Returning JSON Object");
            
            // Returns JSON Object
            return json;
        } catch (InvalidResponseException e) {
            Log.e(TAG, e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        
        Log.e(TAG, "search() failed. Returning null");
        return null;
    }
 
    /**
     * Converts a string to a type ByteArrayInputStream.
     *
     * @param str string to be converted
     *
     * @return ByteArrayInputStream
     */
    private static InputStream fromString(String str) {
        byte[] bytes = str.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Reads the next content stream from a HTTP response, expecting it to be JSON.
     * @param response                     The HTTP response
     * @return                             The read JSON
     * @throws InvalidResponseException    In case reading the JSON failed
     */
    private String readJson(HttpResponse response) throws InvalidResponseException {
        Log.d(TAG, "readJson()");
        if (response == null) {
            throw new InvalidResponseException("Response is null.");
        } else if (response.getEntity() == null) {
            throw new InvalidResponseException("Entity is null.");
        } else if (response.getEntity().getContentType() == null) {
            throw new InvalidResponseException("Content-Type is null.");
        } else if (!response.getEntity().getContentType().getValue().equals("application/json")) {
            throw new InvalidResponseException("Content-Type is "
                    + response.getEntity().getContentType().getValue()
                    + ", expected \"application/json\"");
        }
        try {
            String jsonString = streamToString(response.getEntity().getContent());
            Log.d(TAG, "jsonString = " + jsonString);
            return jsonString;
        } catch (IOException e)  {
            throw new InvalidResponseException("Failed to convert stream to string.", e);
        }
    }

    /**
     * Converts the JSON String returned in the HTTP response into a JSONObject.
     * @param jsonString                   The JSON String from the HTTP response
     * @return                             The JSONObject
     * @throws InvalidResponseException    In case the JSON String is invalid
     */
    private JSONObject parseJson(String jsonString) throws InvalidResponseException {
        Log.d(TAG, "parseJson()");
        JSONObject json = (JSONObject) JSONValue.parse(jsonString);
        if (json == null) {
            Log.e(TAG, "Unable to parse JSON");
            Log.e(TAG, "jsonString = " + jsonString);
            throw new InvalidResponseException("Unable to parse JSON.");
        }
        Log.d(TAG, "json = " + json.toJSONString());
        return json;
    }

    /**
     * Converts an InputStream into a String.
     * TODO Move to Util class, probably use the better commented version from NetInfRequest
     * @param input A input stream
     * @return String representation of the input stream
     */
    private String streamToString(InputStream input) {
        try {
            return new Scanner(input).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}
