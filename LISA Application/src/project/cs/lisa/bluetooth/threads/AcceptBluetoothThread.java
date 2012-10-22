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

import java.io.IOException;
import java.util.UUID;

import project.cs.lisa.bluetooth.provider.BluetoothConnectionHandler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
 * The AcceptBluetoothThread listens for incoming Bluetooth pairing
 * requests.
 * 
 * @author ktran
 *
 */
public class AcceptBluetoothThread extends Thread {

	/** Debug Tag. */
	private static final String TAG = "AcceptBluetoothThread";
	
	/** Unique UUID. */
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
	
    /** Flag determining when to listen for incoming requests. */
    private boolean mServerListens;
    
    /** 
     * The Bluetooth Server Socket that is created as soon
     * as a connection is established.
     */
	private BluetoothServerSocket mBtServerSocket;
	
	/** Device's Bluetooth Adapter. */
	private BluetoothAdapter mBtAdapter;
	
	/** The Handler to send the bluetooth socket to. */
	private Handler mHandler;
	
	/**
	 * Creates a new AcceptBluetoothThread that waits for incoming
	 * bluetooth requests.
	 * 
	 * @param myHandler The handler that should be informed about the 
	 *                  bluetooth socket.
	 */
	public AcceptBluetoothThread(Handler myHandler) {
		mHandler = myHandler;
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
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
				mHandler.obtainMessage(
						BluetoothConnectionHandler.INCOMING_FILE_REQUEST, socket);
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
		} catch (IOException e) {
			Log.d(TAG, "Error while closing Bluetooth socket");
		}
	}
	
	
}
