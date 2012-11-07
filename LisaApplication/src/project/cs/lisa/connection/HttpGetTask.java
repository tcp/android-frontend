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
package project.cs.lisa.connection;

import project.cs.lisa.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
