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
package project.cs.lisa.bluetooth;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

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
	private static final String TAG = "AcceptBluetoothThread";
	
	/** Unique UUID. For more information see {@link project.cs.lisa.bluetooth.provider#MY_UUID} */
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    
    /** The directory containing the published files. */
    private static final String SHARED_FILES_DIR = 
    		Environment.getExternalStorageDirectory() + "/LISA/";

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
	
	/**
	 * Creates a new BluetoothServer that waits for incoming
	 * bluetooth requests and handles file requests.
	 */
	public BluetoothServer() {
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		/* Start listening for incoming pairing requests. No authorization from the user
		 * is needed in order to pair with another device. */
		BluetoothServerSocket tmp = null;
		try {
			
			tmp = 
				mBtAdapter.listenUsingInsecureRfcommWithServiceRecord("Request Listener", MY_UUID);
			
		} catch (IOException e) {
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
				
			} catch (IOException e) {
				Log.e(TAG, "Error occured during wating for an incoming pairing request.");
				break;
			}
			
			if (socket != null) {
				handleIncomingRequest(socket);
			}
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
		
		/* Find the file on the device */
		File file = getFileByHash(hash);
		
		/* Create a byte array representation of the file */
		byte[] fileData = toByteArray(file);
		
		/* Send the data to the remote device */
		writeFile(fileData);
		
		try {
			socket.close();
		} catch (IOException e) {
			Log.e(TAG, "Closing the bluetooth socket failed.");
		}
	}


	/**
	 * Writes the specified buffer to the current stream.
	 * 
	 * @param buffer The data to be send.
	 */
	private void writeFile(byte[] buffer) {
		Log.d(TAG, "Sending file."); 

		try {
			mOutStream.writeInt(buffer.length);
			mOutStream.write(buffer, 0, buffer.length);
			mOutStream.flush();
			
			mOutStream.close();
		
		} catch (IOException e) {
			Log.e(TAG, "Exception occured during writing", e);

		}		
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
		
		String filepath = SHARED_FILES_DIR + hash;
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
	
			mInStream = new DataInputStream(socket.getInputStream());			
			length = mInStream.read(buffer);
			readHash = new String(buffer, 0, length);
			mInStream.close();
			
		} catch (IOException e) {
			Log.e(TAG, "Couldn't extract streams for Bluetooth transmission.");
		}
		
		return readHash;
		
	}

	/**
	 * Shuts down the current server client connection.
	 */
	public void cancel() {
		try {
			Log.d(TAG, "Close BluetoothServerSocket. Stop listening.");
			
			mServerListens = false;
			mBtServerSocket.close();
		} catch (IOException e) {
			Log.e(TAG, "Error while closing Bluetooth socket");
		}
	}
}
