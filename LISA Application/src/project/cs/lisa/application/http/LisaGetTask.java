package project.cs.lisa.application.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
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
import project.cs.lisa.application.MainActivity;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

public class LisaGetTask extends AsyncTask<Void, Void, HttpResponse> {

    /** Type of NetInf message **/
	public static enum MessageType {
	    PUBLISH, GET;
	}
    
	/** Debug Log Tag. **/
	private static final String TAG = "LisaGetTask";
	/** HTTP Scheme. **/
	private static final String HTTP = "http://";
	// TODO add to properties file
	/** HTTP Timeout **/
	private static final int TIMEOUT = 2000;
	
	/** Publish Message Type String Representation **/
	private static final String PUBLISH = "PUBLISH";
	/** Get Message Type String Representation **/
	private static final String GET = "GET";
	
	
	/** Calling Activity. **/
	private MainActivity mActivity;
	/** Target Host. **/
	private String mHost;
	/** Target Port. **/
	private int mPort;
	/** Message Type **/
    private MessageType mMessageType;
	/** Hash Algorithm **/
    private String mHashAlg;
    /** Hash **/
    private String mHash;
	/** The rest of the URI. **/
	private String mQuery;
	
	public LisaGetTask(MainActivity activity, String host, int port, MessageType messageType, String hashAlg, String hash) {
		mActivity = activity;
		mHost = host;
		mPort = port;
        mMessageType = messageType;
		mHashAlg = hashAlg;
		mHash = hash;
		mQuery = "/ni/" + mHashAlg + ";" + mHash + "?METHOD=";
		
		// Example of full uris
		// http://example.com:80/ni/sha-256;ABCDEFGHIJKLMNOPQRSTUVWXYZ?METHOD=GET
		// http://example.com:80/ni/sha-256;ABCDEFGHIJKLMNOPQRSTUVWXYZ?METHOD=PUBLISH&BTMAC=12:34:56:78:90
		
		// Construct the query depending on message type
		switch (mMessageType) {
		    case PUBLISH:
		        mQuery += PUBLISH;
		        // Add locators
		        // TODO Currently only publishes your own bluetooth mac address
		        BluetoothAdapter bluetoothDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
		        if(bluetoothDefaultAdapter != null) {
		            if(bluetoothDefaultAdapter.isEnabled()) {
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
		        mQuery += GET;
		        break;
		    default:
		        Log.d(TAG, "Unreachable code: Invalid message type");
		        break;
		}

	}
	
	
	@Override
    protected void onPreExecute() {
		Log.d(TAG, "onPreExecute()");
    }
	
	@Override
	protected HttpResponse doInBackground(Void... params) {
		Log.d(TAG, "doInBackground()");
		
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
    	
    	Log.d(TAG, "doInBackground()\tExecuting Http Get");
    	
    	// Try to execute the http get
    	try {
    		response = client.execute(get);
    	} catch (ClientProtocolException e) {
     		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		//TODO REMOVE
    		e.printStackTrace();
    		Log.d(TAG, e.toString());
    	}
    	
    	Log.d(TAG, "doInBackground()\tReading Http Get Response");
    	
    	return response;

	}
	
    @Override
    protected void onPostExecute(HttpResponse response) { 	
    	Log.d(TAG, "onPostExecute()");
    	Log.d(TAG, "Http response code: " + response.getStatusLine().getStatusCode());
    	
    	switch (mMessageType) {
    	    case PUBLISH:
    	        handlePublishResponse(response);
    	        break;
    	    case GET:
    	        handleGetResponse(response);
    	        break;
    	    default:
    	        Log.d(TAG, "Unreachable code: Invalid message type");
    	        break;
    	}
    	
    	EditText log = (EditText) mActivity.findViewById(R.id.editText1);
    	log.setText(response);
    	Log.d(TAG, response);
    	
    }
	
    private void handlePublishResponse(HttpResponse response) {
        Log.d(TAG, "handlePublishResponse()");
    }
    
    private void handleGetResponse(HttpResponse response) {
        Log.d(TAG, "handleGetResponse()");
        try {
            InputStream content = response.getEntity().getContent(); 
            ObjectInputStream object = new ObjectInputStream(content);
            HashMap<String, String> responseMap = (HashMap<String, String>) object.readObject();
            // TODO Do something with the file and content type
            // HashMap<String, String>
            // "contentType"
            // "filePath"
            Log.d(TAG, "response map:");
            for(String key : responseMap.keySet()) {
                Log.d(TAG, key + " => " + responseMap.get(key));
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.toString(), e);
        }
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
