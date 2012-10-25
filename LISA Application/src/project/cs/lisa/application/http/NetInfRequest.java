package project.cs.lisa.application.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import netinf.common.datamodel.InformationObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import project.cs.lisa.file.LisaFileHandler;
import project.cs.lisa.metadata.LisaMetadata;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

// TODO: Change variable names: 'response' to 'JSONString'

/**
 * Used to send requests to the OpenNetInf RESTful API.
 * 
 * TODO: Split into two classes: one for get, one for publish.
 * 
 * @author Linus, Harold
 *
 */
public class NetInfRequest extends AsyncTask<String, Void, String> {

    /**
     * Respresent different NetInf requests.
     * @author Linus
     *
     */
    public static enum RequestType {
        /** Represents a NetInf publish request. **/
        PUBLISH,
        /** Represents a NetInf get request. **/
        GET;
    }

    /** Debug Log Tag. **/
    private static final String TAG = "NetInfRequest";
    /** HTTP Scheme. **/
    private static final String HTTP = "http://";
    // TODO add to properties file
    /** HTTP Timeout. **/
    private static final int TIMEOUT = 60000;

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

        // Example of full uris
        // http://example.com:80/ni/
        //     sha-256;ABCDEFGHIJKLMNOPQRSTUVWXYZ?METHOD=GET
        // http://example.com:80/ni/
        //     sha-256;ABCDEFGHIJKLMNOPQRSTUVWXYZ?METHOD=PUBLISH&BTMAC=12:34:56:78:90

        // Construct the query depending on message type
        switch (mMessageType) {
        case PUBLISH:
            mQuery = "/ni/" + mQuery + "?METHOD=" + PUBLISH;
            // Add locators
            // TODO Currently only publishes your own bluetooth mac address
            BluetoothAdapter bluetoothDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothDefaultAdapter != null) {
                if (bluetoothDefaultAdapter.isEnabled()) {
                    String btMac = BluetoothAdapter.getDefaultAdapter().getAddress();
                    mQuery += "&BTMAC=" + btMac;
                } else {
                    Log.d(TAG, "Bluetooth not enabled");
                }
            } else {
                Log.d(TAG, "Bluetooth adapter is null");		            
            }
            break;
        case GET:
            mQuery = "/bo/" + mQuery + "?METHOD=" + GET;
            //            mQuery += GET;
            break;
        default:
            Log.d(TAG, "Unreachable code: Invalid message type");
            break;
        }

    }

    /**
     * Does nothing.
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
        } else {
            Log.d(TAG, "Content type and meta data not provided");
        }

        // Create uri
        String uri = HTTP + mHost + ":" + Integer.toString(mPort) + mQuery;

        // Create http client with a timeout
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
        HttpClient client = new DefaultHttpClient(httpParams);

        // Create http get
        HttpGet get = new HttpGet(uri);
        HttpResponse response = null;

        Log.d(TAG, "doInBackground(), Executing Http Get: " + uri);

        // TODO: Refactor and figure out a way of fixing the exceptions!
        // Try to execute the http get
        try {
            response = client.execute(get);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "TimeoutException");
            response = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            //TODO REMOVE
            e.printStackTrace();
            Log.d(TAG, e.toString());
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

        //    	EditText log = (EditText) mActivity.findViewById(R.id.editText1);
        //    	log.setText(response.toString());
        //    	Log.d(TAG, response);

    }

    /**
     * Handles the response to a NetInf Get message.
     * @param response  HashMap containing filename and content type
     */
    private void logGetResponse(String response) {
        Log.d(TAG, "handleGetResponse()");

        Log.d(TAG, "string response " + response);/*
        if (response != null) {
            for (String key : response.keySet()) {
                Log.d(TAG, "\t" + key + " => " + response.get(key));
            }
        } 
        else {
            Log.e(TAG, "Hash map is null");
        }*/
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
            /* Display the file according to the file type. */
            LisaFileHandler.displayContent(mActivity, filePath, contentType);
            //        Intent intent = new Intent(Intent.ACTION_VIEW);
            //        File file = new File(filePath);
            //        intent.setDataAndType(Uri.fromFile(file), contentType);
            //        mActivity.startActivity(intent);
        }
        else {
            Log.d(TAG, "_JSONSTring null, probably TimeoutException happened... HAHAHAHA.");
            Toast.makeText(mActivity, "We could not get the content. Check your internet "
                    + "connection/Bluetooth connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles the response to a NetInf Publish message.
     */
    private void logPublishResponse() {
        Log.d(TAG, "handlePublishResponse()");
        final AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setTitle("Hash");
        b.setMessage("You have published this hash: " + mHash);
        b.setCancelable(false);
        b.setPositiveButton("OK", null);
        b.show();
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
            InputStream content = response.getEntity().getContent();
            _JSONString = streamToString(content);
            Log.d(TAG, _JSONString);
            //ObjectInputStream object = new ObjectInputStream(content);
            //InformationObject io = (InformationObject) object.readObject();
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        } catch (NullPointerException e) {
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
        } catch (NoSuchElementException e) {
            return "";
        }
    }

}
