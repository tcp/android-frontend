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
package project.cs.lisa.bluetooth.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import project.cs.lisa.bluetooth.TransmissionStatus;
import project.cs.lisa.bluetooth.server.BluetoothActivity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * The ConnectedBluetoothThread handles the transmission between two devices. It can
 * be used for reading and writing to a stream.
 * 
 * @author ktran
 *
 */
public class ConnectedBluetoothThread extends Thread {
	
	/**
	 * Debug Tag.
	 */
    private static final String TAG = "ConnectedThread";
    
    /**
     * The read buffer size.
     */
    private static final int BUFFER_SIZE = 1024;

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
     * The Handler we communicate the results to.
     */
	private Handler mHandler;
	
	/**
	 * The remote device's address.
	 */
	private String mClientAddress;

    /**
	 * Creates a new thread dealing with reading and writing data.
	 * 
	 * @param myBtSocket The Bluetooth socket used for the data transfer.
	 */
	public ConnectedBluetoothThread(Handler myHandler, BluetoothSocket myBtSocket) {
		mBtSocket = myBtSocket;
		mClientAddress = mBtSocket.getRemoteDevice().getAddress();
		
		mHandler = myHandler;
		
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
		
		byte[] buffer = new byte[BUFFER_SIZE];
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
					BluetoothActivity.MESSAGE_WRITE, 
					TransmissionStatus.SUCCESS.ordinal(), -1, mClientAddress)
					.sendToTarget();
		} catch (IOException e) {
			Log.e(TAG, "Exception occured during writing", e);
			
			mHandler.obtainMessage(BluetoothActivity.MESSAGE_WRITE, 
					TransmissionStatus.FAILED.ordinal(), -1, mClientAddress)
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
		} catch (IOException e) {
			Log.d(TAG, "Error while closing Bluetooth socket");
		}
	}
}
