package project.cs.lisa.netinf;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import project.cs.lisa.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

public class LisaGETTask extends AsyncTask<Void, Void, String> {

	
	/** Debug Log Tag. **/
	private static final String TAG = "LisaGETTask";
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
	
	public LisaGETTask(Activity activity, String host, int port) {
		mActivity = activity;
		mHost = host;
		mPort = port;
		BluetoothAdapter bluetoothDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
		String mac = "";
		if ((bluetoothDefaultAdapter != null) && (bluetoothDefaultAdapter.isEnabled())) 
		    mac = BluetoothAdapter.getDefaultAdapter().getAddress();
		mMessage = "ni/sha-256;644mOZ-DTu66fQt415_kXE?CT=text/plain&METHOD=PUT&BTMAC=" + mac;
	}
	
	
	@Override
    protected void onPreExecute() {
		Log.d(TAG, "onPreExecute()");
    }
	
	@Override
	protected String doInBackground(Void... params) {
		Log.d(TAG, "doInBackground()");
		
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
    protected void onPostExecute(String response) { 	
    	Log.d(TAG, "onPostExecute()");

    	EditText log = (EditText) mActivity.findViewById(R.id.editText1);
    	log.setText(response);
    	Log.d(TAG, response);
    	

    	
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
