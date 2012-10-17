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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.UUID;

import project.cs.lisa.bluetooth.TransmissionStatus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The BluetoothService listens for incoming requests.
 * 
 * @author ktran
 *
 */
public class BluetoothService extends Thread {
	
	/**
	 * Debug Tag.
	 */
	private static final String TAG = "BluetoothService";
	
	/**
	 * Unique UUID.
	 */
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    	
    /**
     * Flag determining when to listen for incoming requests.
     */
    private boolean mServerListens;
    
    /** 
     * The Bluetooth Server Socket that is created as soon
     * as a connection is established.
     */
	private BluetoothServerSocket mBtServerSocket;
	
	/**
	 * Device's Bluetooth Adapter.
	 */
	private BluetoothAdapter mBtAdapter;
	
	/**
	 * The main activity's handler for sending feedback about the
	 * Bluetooth connections.
	 */
	private Handler mHandler;	
	
	/**
	 * The current collection containing all current ongoing 
	 * connections via ConnectedThread objects.
	 */
	private Hashtable<String, ConnectedThread> mConnectedThreadsCollection;
	
	/**
	 * Creates a new BluetoothService.
	 * 
	 * @param myHandler The main activity's handler
	 */
	public BluetoothService(Handler myHandler) {
		mHandler = myHandler;
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		mConnectedThreadsCollection = new Hashtable<String, ConnectedThread>();
		
		BluetoothServerSocket tmp = null;
		try {
			tmp = 
				mBtAdapter.listenUsingInsecureRfcommWithServiceRecord("Request Listener", MY_UUID);
		} catch (IOException e) {
			Log.d(TAG, "Bluetooth Server Socket couldn't be initialized.");
		}
		
		
		mBtServerSocket = tmp;
		mServerListens = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Log.d(TAG, "Start Listening..");
		BluetoothSocket socket = null;
		
		while (mServerListens) {
			try {
				socket = mBtServerSocket.accept();
				Log.d(TAG, "Accepted Request");
			} catch (IOException e) {
				Log.d(TAG, "Error occured during wating for an incoming connection request.");
				break;
			}
			
			if (socket != null) {
				handleIncomingRequest(socket);
			}
		}		
		
	}
	
	/**
	 * Closes the current Bluetooth server socket. Therefore, no
	 * more connections will be established.
	 */
	public void cancel() {
		try {
			mBtServerSocket.close();
			mServerListens = false;
			cancelThreads();
			mConnectedThreadsCollection = null;
		} catch (IOException e) {
			Log.d(TAG, "Couldn't close the Bluetooth Server Socket.");
		}
	}
	
	/**
	 * Closes the connection to the client by stopping the responsible thread.
	 * 
	 * @param key The key to identify the client.
	 */
	public void cancelClientConnection(String key) {
		mConnectedThreadsCollection.get(key).cancel();
	}
	
	/**
	 * Writes a file through the current Bluetooth Socket.
	 * 
	 * @param file The file to send
	 * @param key  The key used for identifying the responsible ConnectedThread
	 */
	public void write(File file, String key) {
		byte[] fileByteArray = new byte[(int) file.length()];
		FileInputStream fis;
		
		try {
			fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(fileByteArray, 0, fileByteArray.length);
			
			mConnectedThreadsCollection.get(key).write(fileByteArray);
			
			fis.close();
			bis.close();
			
		} catch (FileNotFoundException e) {
			Log.d(TAG, "The file " + file.getName() + " could not be found.");
			
		} catch (IOException e) {
			Log.d(TAG, "Exception occured during file to byte array conversion.", e);
		}
	}
	
	/**
	 * Starts a thread for handling the incoming request.
	 * 
	 * @param socket The Bluetooth socket that was established for communication.
	 */
	private void handleIncomingRequest(BluetoothSocket socket) {
		String clientAddress = socket.getRemoteDevice().getAddress();
		Log.d(TAG, "Client address: " + clientAddress);
		
		ConnectedThread connectionThread = new ConnectedThread(socket);
		mConnectedThreadsCollection.put(clientAddress, connectionThread);

		connectionThread.start();
	}
	
	/** 
	 * Cancels all current threads running.
	 */
	private void cancelThreads() {
		Collection<ConnectedThread> threadCollection = mConnectedThreadsCollection.values();
		Iterator<ConnectedThread> iterator = threadCollection.iterator();
		
		while (iterator.hasNext()) {
			iterator.next().cancel();
		}
	}	
	
	/** 
	 * The ConnectedThread is responsible for receiving incoming data.
	 * 
	 * @author ktran
	 *
	 */
	public class ConnectedThread extends Thread {
		
		/**
		 * Debug Tag.
		 */
	    private static final String TAG = "ConnectedThread";
	    
	    /**
	     * Bluetooth Socket enabling the connection to the remote device.
	     */
		private final BluetoothSocket mBtSocket;
		
		/**
		 * Input Stream used for reading in requests.
		 */
		private final DataInputStream mInStream;
		
		/**
		 * Output Stream used for writing data.
		 */
		private final DataOutputStream mOutStream;
		
		/**
		 * Flag determining whether to keep listening or not.
		 */
		private boolean mConnectedListening;
		
		/**
		 * The remote device's address.
		 */
		private String mClientAddress;
		
		/**
		 * Creates a new thread dealing with reading and writing data.
		 * 
		 * @param myBtSocket The Bluetooth socket used for the data transfer.
		 */
		public ConnectedThread(BluetoothSocket myBtSocket) {
			mBtSocket = myBtSocket;
			mClientAddress = mBtSocket.getRemoteDevice().getAddress();
			
			DataInputStream tmpIn = null;
			DataOutputStream tmpOut = null;			
			
			try {
				tmpIn = new DataInputStream(mBtSocket.getInputStream());
				tmpOut = new DataOutputStream(mBtSocket.getOutputStream());
			} catch (IOException e) {
				Log.d(TAG, "Couldn't extract streams for Bluetooth transmission.");
			}
			
			mInStream = tmpIn;
			mOutStream = tmpOut;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			Log.d(TAG, "Starting to receive the incoming message");
			
			byte[] buffer = new byte[1024];
			int bytes;
			
				try {
					Log.d(TAG, "Reading..");
					bytes = mInStream.read(buffer);
					Log.d(TAG, "Finished.");
					
					Bundle bundle = new Bundle();
					bundle.putString("client", mClientAddress);
					bundle.putInt("size", bytes);
					bundle.putByteArray("data", buffer);
					
					Message msg = mHandler.obtainMessage(BluetoothActivity.MESSAGE_READ);
					msg.setData(bundle);		
					msg.sendToTarget();
					
				} catch (IOException e) {
					Log.d(TAG, "Error while receiving incoming file.");
				}			
			
		}  

		
		/**
		 * Writes the passed byte array to the Bluetooth socket.
		 * 
		 * @param buffer The byte array to send to the remote device.
		 */
		public void write(byte[] buffer) {
			Log.d(TAG, "Sending file."); 

			try {
				mOutStream.writeInt(buffer.length);
				mOutStream.write(buffer, 0, buffer.length);
				mOutStream.flush();
			
				mHandler.obtainMessage(
						BluetoothActivity.MESSAGE_WRITE, TransmissionStatus.SUCCESS.ordinal(), -1, mClientAddress)
						.sendToTarget();
				
			} catch (IOException e) {
				Log.e(TAG, "Exception occured during writing", e);
				
				mHandler.obtainMessage(BluetoothActivity.MESSAGE_WRITE, TransmissionStatus.FAILED.ordinal(), -1, mClientAddress)
				.sendToTarget();
			}			
		}
		
		
		/**
		 * Shuts down the current server client connection.
		 */
		public void cancel() {
			try {
				Log.d(TAG, "Closing Bluetooth socket to " + mClientAddress);

				mOutStream.close();
				mInStream.close();
				
				mBtSocket.close();
				mConnectedThreadsCollection.remove(mClientAddress);
			} catch (IOException e) {
				Log.d(TAG, "Error while closing Bluetooth socket");
			}
		}
		
	}

}
