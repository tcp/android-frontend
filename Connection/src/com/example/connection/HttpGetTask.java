package com.example.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.demosprint1.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

/**
 * HttpGetTask represent an asynchronous HTTP get request.
 * @author Linus Sunde
 */
// TODO change to AsyncTask<Void, Void, HttpResponse>?
public class HttpGetTask extends AsyncTask<Void, Void, String> {

	//TODO if using broadcast
//	/** Broadcast when GET received. **/
//	public static final String GET_RECEIVED = "DemoSprint1.GET_RECEIVED";
	
	/** Debug Log Tag. **/
	private static final String TAG = "HttpGetTask";
	/** HTTP Scheme. **/
	private static final String HTTP = "http://";
	
	/** Calling Activity. **/
	private Activity mActivity;
	/** Target Host. **/
	private String mHost;
	/** Target Port. **/
	private int mPort;
	/** The rest of the URI. **/
	private String mMessage;
	
	/**
	 * Constructs a HTTP GET.
	 * @param activity	The calling Activity
	 * @param host		Target Host
	 * @param port		Target Port
	 * @param message	Rest of the URI
	 */
	public HttpGetTask(Activity activity, String host, int port, String message) {
		mActivity = activity;
		mHost = host;
		mPort = port;
		mMessage = message;
	}
	
	@Override
    protected void onPreExecute() {
		Log.d(TAG, "onPreExecute()");
    }
    
    @Override
    protected String doInBackground(Void... nothing) {	
    	Log.d(TAG, "doInBackground()");
    	
//    	requestUri = HTTP + TARGETHOST + ":"+TARGETPORT+"/.well-known/ni/"
//    		+HASH_ALGORITHM+";"+hash +"?METHOD=" + methodType;

    	
    	String uri = HTTP + mHost + ":" + Integer.toString(mPort) + "/" + mMessage;
    	HttpClient client = new DefaultHttpClient();
    	HttpGet get = new HttpGet(uri);
    	HttpResponse response = null;
    	
    	Log.d(TAG, "doInBackground()\tExecute");
    	
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
    	
    	Log.d(TAG, "doInBackground()\tParse");
    	
    	try {
    		return streamToString(response.getEntity().getContent());
    	} catch (IOException e) {
    		Log.d(TAG, e.toString());
    		return e.toString();
    	}  catch (Exception e) {
    		//TODO REMOVE
    		Log.d(TAG, e.toString());
    		return uri + "\n" + e.toString();
    	}
    }
    
    @Override
    protected void onProgressUpdate(Void... nothing) {   	
    }
    
    @Override
    protected void onPostExecute(String response) { 	
    	Log.d(TAG, "onPostExecute()");
    	
    	//TODO Perhaps use a message handler, BroadcastReceiver or something?
    	// Change return type?
    	// Simple example http://stackoverflow.com/questions/5457493/asynctask-return-value
    	EditText log = (EditText) mActivity.findViewById(R.id.textLog);
    	log.setText(response);
    	Log.d(TAG, response);
    	
    	//TODO fixa
//    	Intent intent = new Intent();
//    	intent.setAction(GET_RECEIVED);
//    	intent.putExtra("response", response);
//    	mActivity.getApplication().sendBroadcast(intent);
    	
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
