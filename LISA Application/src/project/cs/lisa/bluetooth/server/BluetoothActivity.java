/**

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
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */
package project.cs.lisa.bluetooth.server;

import java.io.File;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.transmission.R;

/**
 * The BluetoothActivity is the main activity that
 * represents the server side.
 * 
 * @author ktran
 *
 */
public class BluetoothActivity extends Activity {

	/**
	 * Debug Tag.
	 */
    private static final String TAG = "BluetoothActivity";
	
    /**
     * Constants indicating the context of a message (Handler).
     */
	private static final int REQUEST_ENABLE_BT = 1;
	
	/** The Constant REQUEST_DISCOVERABLE. */
	private static final int REQUEST_DISCOVERABLE = 2;
	
	/** The Constant MESSAGE_READ. */
	public static final int MESSAGE_READ = 3;
	
	/** The Constant MESSAGE_WRITE. */
	public static final int MESSAGE_WRITE = 4;

	/**
	 * Device's bluetooth adapter.
	 */
	private BluetoothAdapter mBluetoothAdapter;
	
	/**
	 * The Bluetooth Service class.
	 */
	private BluetoothService mBluetoothService;
	
	//DEBUG: it was private -> public static
	/** 
	 * Filepath pointing to the file to be send to client.
	 * Android
	 */
	public static final String mFilePath = 
			Environment.getExternalStorageDirectory() 
			+ "/DCIM/grand_teton_sunset.jpg";
	
	/**/
	
	/**
	 * Filepath pointing to the file to be send to client.
	 * Android
	 * 
	 * public static final String mFilePath =
	 * Environment.getExternalStorageDirectory()
	 * + "/DCIM/Camera/file.jpg";
	 * 
	 * /*
	 *
	 * @param savedInstanceState the saved instance state
	 */

	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        	Toast.makeText(getApplicationContext(), 
        			"No Bluetooth available", Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        ensureDiscoverable();
        
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    public void onStart() {
    	super.onStart();
    	Log.d(TAG, "Starting the app.");
    	
        if (!mBluetoothAdapter.isEnabled()) {
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
        	setUpBtServer();
        }
    }
   

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) {
    	case REQUEST_ENABLE_BT:
    		if (resultCode == Activity.RESULT_OK) {
    			setUpBtServer();
    		} else {
    			Toast.makeText(getApplicationContext(), 
    					"Bluetooth can't be enabled", Toast.LENGTH_SHORT).show();
    			finish();
    		}
    		
    		break;
    		
    	case REQUEST_DISCOVERABLE:
    		if (resultCode == Activity.RESULT_CANCELED) {
    			Toast.makeText(getApplicationContext(), 
    					"Device isn't discoverable", Toast.LENGTH_SHORT).show();
    			finish();
    		}
    		break;
    		
    	default:
    		Toast.makeText(getApplicationContext(), 
    				"Error occured: Undefined request response.", Toast.LENGTH_SHORT).show();
    	}
    	
    	
    }
    
	/**
	 * Sends a request to the user enabling the device to be always discoverable.
	 */
    private void ensureDiscoverable() {
    	Log.d(TAG, "Send discoverability request to user.");
    	Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    	discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
    	startActivity(discoverableIntent);
    }
    
    /**
     * Starts up the Bluetooth server.
     */
    private void setUpBtServer() {
    	Log.d(TAG, "setupBtServer()");
		mBluetoothService = new BluetoothService(mHandler);
		mBluetoothService.start();
	}
    
    
    /**
     * The Handler handling the incoming messages.
     */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String key;
			
			switch (msg.what) {
			
			case MESSAGE_READ:
				Log.d(TAG, "Message read");
				
				Bundle data = msg.getData();
				byte[] readBuf = data.getByteArray("data");
				int bytes = data.getInt("size");
				key = data.getString("client");
						
				String readMessage = new String(readBuf, 0, bytes);
				Log.d(TAG, "Received Message: " + readMessage);
				
				TextView displayMsgField = (TextView) findViewById(R.id.msg_received);
				displayMsgField.setText(readMessage);
				sendFile(key);
				
				break;
				
			case MESSAGE_WRITE:
				Log.d(TAG, "Message write");
				
				Transmission result = Transmission.values()[msg.arg1];
				key = (String) msg.obj;
						
				if (result == Transmission.SUCCESS) {
					Log.d(TAG, "Sending file succeeded.");
				} else if (result == Transmission.FAILED) {
					Log.d(TAG, "Sending file failed.");
				}
				
				mBluetoothService.cancelClientConnection(key);		
				
				break;
			default:
				Log.d(TAG, "Unexpected message retrieved.");
			}
		
		}
		
		/**
		 * Prepares and sends the file to the client.
		 */
		private void sendFile(String key) {
			File file = new File(mFilePath);
			mBluetoothService.write(file, key);
		}
	};
    
}
