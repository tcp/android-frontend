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
package project.cs.lisa.bluetooth;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import project.cs.lisa.R;
import project.cs.lisa.application.MainNetInfActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The BluetoothServer listens for incoming Bluetooth pairing
 * requests. As soon as the local device has been successfully paired
 * with a remote device, the BluetoothServer waits for a file request
 * containing the hash. If the specified file is existing, the file will
 * be transferred to the remote device.
 * 
 * @author Kim-Anh Tran
 *
 */
public class BluetoothServer extends Thread {
	
	/** Debug Tag. */
	private static final String TAG = "BluetoothServer";
	
	/** Unique UUID. For more information see {@link project.cs.lisa.bluetooth.provider#MY_UUID} */
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    /** Sending macro. */
    private static final int SENDING = 0;
    
    /** Done macro. */
    private static final int DONE = 1;
    
    /** The buffer for reading in the hash out of a file request message. */
	private static final int BUFFER_SIZE = 1024;
	
    /** Flag determining how long to listen for incoming pairing requests. */
    private boolean mServerListens;
    
    /** The Bluetooth Server Socket that is created as soon as a connection is established. */
	private BluetoothServerSocket mBtServerSocket;
	
	/** Device's Bluetooth Adapter. */
	private BluetoothAdapter mBtAdapter;
	
	/** The input stream used for reading in the hash. */
	private DataInputStream mInStream;
	
	/** The output stream used for writing the file to the remote device. */
	private DataOutputStream mOutStream;
	
    /** The directory containing the published files. */
    private String mSharedFolder = 
    		Environment.getExternalStorageDirectory() + "/DCIM/Shared/";
	
	/**
	 * Creates a new BluetoothServer that waits for incoming
	 * bluetooth requests and handles file requests.
	 */
	
    public BluetoothServer() {
	    // Creates shared folder
	    createSharedFolder();

	    // Get default adapter from Bluetooth
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		/* Start listening for incoming pairing requests. No authorization from the user
		 * is needed in order to pair with another device. */
		BluetoothServerSocket tmp = null;
		
		try {
			tmp = mBtAdapter.listenUsingRfcommWithServiceRecord(TAG, MY_UUID);
		}
		catch (IOException e) {
			Log.e(TAG, "Bluetooth Server Socket couldn't be initialized.");
		}
		
		mBtServerSocket = tmp;
		mServerListens = true;
	}

	@Override
	public void run() {
		Log.d(TAG, "Start Listening..");
		BluetoothSocket socket = null;
		
		while (mServerListens) {
			try {
				/* Accept an incoming pairing request */
				socket = mBtServerSocket.accept();
				Log.d(TAG, "Accepted Request");
			}
			catch (IOException e) {
				Log.e(TAG, "Error occured during wating for an incoming pairing request.");
				e.printStackTrace();
				break;
			}
			
			if (socket != null) {
				setUpIoStreams(socket);
				handleIncomingRequest(socket);
				cleanUp(socket);
			}
		}	
	}
	
	/**
	 * Shuts down the current server client connection.
	 */
	
	public void cancel() {
		try {
			Log.d(TAG, "Close BluetoothServerSocket. Stop listening.");
			mServerListens = false;
			mBtServerSocket.close();
		}
		catch (IOException e) {
			Log.e(TAG, "Error while closing Bluetooth socket");
		}
	}
	
	/**
	 * Creates the folder that contains the files to be shared with other phones.
	 */
	
	private void createSharedFolder() {
		File folder = new File(mSharedFolder);
		
		if (!folder.exists()) {
			Log.d(TAG, "Creating shared folder " + mSharedFolder);
			boolean created = folder.mkdir();
			
			if (!created) {
				Log.e(TAG, "Failed creating the shared folder. Set shared folder to DCIM/");
				mSharedFolder = Environment.getExternalStorageDirectory() + "/DCIM/";
			}
		}
	}

	/**
	 * Cleans up the openend socket and corresponding streams.
	 * 
	 * @param socket	The socket used for the communication to the remote device.
	 */
	
	private void cleanUp(BluetoothSocket socket) {
		try {
			/* Clean up open streams and sockets. */
			mOutStream.close();
			mInStream.close();
			socket.close();
		}
		catch (IOException e) {
			Log.e(TAG, "Closing the bluetooth socket failed.");
		}
	}
	
	/**
	 * Set up the streams used for reading in and writing to
	 * a socket that connects this device to a remote device.
	 * 
	 * @param socket	The socket for reading and writing.
	 */

	private void setUpIoStreams(BluetoothSocket socket) {
		try {
			mInStream = new DataInputStream(socket.getInputStream());
			mOutStream = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException e) {
			Log.d(TAG, "Failed creating the streams for communicating.");
		}
	}

	/**
	 * Extracts the hash, searches for the file requested and sends the
	 * corresponding file to the remote device.
	 * 
	 * @param socket The bluetooth socket used for communicating with the remote device
	 */
	
	private void handleIncomingRequest(BluetoothSocket socket) {
		/* Receive the hash */
		String hash = readHash(socket);
		Log.d(TAG, "Received hash: " + hash);
		
		/* Find the file on the device */
		File file = getFileByHash(hash);
		
		/* Create a byte array representation of the file */
		byte[] fileData = toByteArray(file);
		
		/* Send the data to the remote device */
		writeFile(fileData);
	}

	/**
	 * Writes the specified buffer to the current stream.
	 * 
	 * @param buffer The data to be send.
	 */
	
	private void writeFile(byte[] buffer) {
		Log.d(TAG, "Sending file of size: " + buffer.length); 

		try {
			mOutStream.writeInt(buffer.length);
			onBufferSend(SENDING); // updates UI to inform that file is being sent
			mOutStream.write(buffer, 0, buffer.length);
			mOutStream.flush();
			onBufferSend(DONE); // updates UI to hide the sending file box
			Log.d(TAG, "Done writing file to remote device.");
		}
		catch (IOException e) {
			Log.e(TAG, "Exception occured during writing", e);
		}		
	}

	/**
	 * Function that updates view to display that phone is sending file
	 * @param done 0 for Sending
	 *             1 for Sent
	 */
	
	public void onBufferSend(final int done) {
	    final Activity activity = (Activity) MainNetInfActivity.getContext();
	    
	    activity.runOnUiThread(new Runnable() {
	       public void run() {
	           // Set Views
	           TextView tv = (TextView) activity.findViewById(R.id.ProgressBarText);
	           ProgressBar pb = (ProgressBar) activity.findViewById(R.id.progressBar1);
	           
	           // If done sending file, make it invisible
	           if (done == 0) {
	               tv.setText("Sending file");
	               tv.setVisibility(TextView.VISIBLE);
	               pb.setVisibility(ProgressBar.VISIBLE);
	           }
	           else {
	               tv.setVisibility(TextView.INVISIBLE);
	               pb.setVisibility(ProgressBar.INVISIBLE);
	           }
	       }
	    });
	}

	/**
	 * Converts a file into a byte array.
	 * 
	 * @param file	The file we want to convert.
	 * @return		The byte array that corresponds to the file.
	 */


	private byte[] toByteArray(File file) {
		byte[] fileByteArray = new byte[(int) file.length()];
		FileInputStream fis;
		
		try {
			
			fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(fileByteArray, 0, fileByteArray.length);
						
			fis.close();
			bis.close();
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "The file " + file.getName() + " could not be found.");
			
		} catch (IOException e) {
			Log.e(TAG, "Exception occured during file to byte array conversion.", e);
		}
		
		return fileByteArray;
	}

	/**
	 * Returns the file specified by the hash.
	 * 
	 * @param hash	The identfier of the file we request.
	 * @return		The file that is identified with the specified hash
	 */

	private File getFileByHash(String hash) {
		
		String filepath = mSharedFolder + hash;
		File requestedFile = new File(filepath);
	
		return requestedFile;
	}

	/**
	 * Reads and returns the hash that is received through the current socket.
	 * 
	 * @param socket	The bluetooth socket used for communicating with the remote device
	 * @return			The hash that is read from the socket.
	 */

	private String readHash(BluetoothSocket socket) {
		
		byte[] buffer = new byte[BUFFER_SIZE];
		int length;
		
		String readHash = "";
		
		try {
	
			length = mInStream.read(buffer);
			readHash = new String(buffer, 0, length);
			
		}
		catch (IOException e) {
			Log.e(TAG, "Couldn't extract streams for Bluetooth transmission.");
		}
		
		return readHash;		
	}
}
