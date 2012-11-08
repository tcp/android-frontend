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

package project.cs.lisa.bluetooth.provider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import project.cs.lisa.R;
import project.cs.lisa.application.MainNetInfActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The BluetoothProvider handles data transmission via Bluetooth.
 * 
 * @author Kim-Anh Tran
 * @author Paolo Boschini
 *
 */
public class BluetoothProvider implements ByteArrayProvider {

    /** Debug Tag. */
    private static final String TAG = "BluetoothProvider";

    /** The Constant MY_UUID. */
    private static final UUID MY_UUID = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    
    /** All bluetooth locators have the following indicator in their address. */
    private static final String BLUETOOTH_LOCATOR_INDICATOR = "nimacbt";
   
    /** Represents the full loaded progress bar */
    private static final int FULL_PROGRESS = 100;
    
    /** Represents the number of attempts to connect to a remote device. */
    private static final int NUMBER_OF_ATTEMPTS = 2;

    /** The Bluetooth adapter. */
    private BluetoothAdapter mBluetoothAdapter = null;

    /** 
     * Creates a BluetoothProvider that will handle data transmission
     * via Bluetooth. 
     */
    public BluetoothProvider() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Returns a byte array after a successful connection and transmission
     * of a BO.
     * 
     * @param   locator     The source from where to fetch the BO
     * @param   hash        A hash identifying the BO
     * @return  The byte array referring to the requested BO
     */
    @Override
    public byte[] getByteArray(String locator, String hash) {
        byte[] fileArray = null;
        BluetoothSocket socket = null;

        try {
            // Connect
            socket = connectToRemoteDevice(locator);

            // Send request
            sendRequest(socket, hash);

            // Download file
            fileArray = downloadFile(socket);
            
        } catch (IOException e) {
            Log.e(TAG, "Connection to locator failed.");
            fileArray = null;
            
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Something went wrong when closing the socket!");
                }
            }
        }
        return fileArray;
    }

    /**
     * Attempt a connection to a remote device via Bluetooth
     * and returns a socket after a successful connection.
     * @param   locator     The device we want to connect to
     * @return  The Bluetooth socket for the communication.
     * @throws  IOException The exception for the socket.
     */
    private BluetoothSocket connectToRemoteDevice(String locator) throws IOException{
        Log.d(TAG, "Start requesting a socket to a remote device: " + locator);

        BluetoothSocket socket = null;
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(locator);
        
        // Tries to connect to remote device several times up to a limit
        int attempts = NUMBER_OF_ATTEMPTS;
        boolean connectionSucceeded = false;
        do {
	        try {
	        	// Get a BluetoothSocket for a connection with the given BluetoothDevice.
	        	socket   = device.createRfcommSocketToServiceRecord(MY_UUID);
	        	
	            /* This is a blocking call and will only return on a
	             * successful connection or an exception.
	             */
	            Log.d(TAG, "Trying to connect to a device through a socket...");
	            mBluetoothAdapter.cancelDiscovery();
	            socket.connect();
	            
	            connectionSucceeded = true;
	        } catch (IOException e) {
	        	--attempts;
	        }
        } while (!connectionSucceeded && attempts > 0);
        
        if (!connectionSucceeded) {
        	Log.e(TAG, "Device couldn't establish a connection to selected remote device.");
        	
        	throw new IOException("Couldn't establish connection to remote device.");
        }

        return socket;
    }

    /**
     * Send a request to a remote device sending the hash identifier
     * for retrieving the corresponding BO. 
     * @param   socket  The socket for connecting with the remote device    
     * @param   hash    The identifier for requesting the BO
     * @throws  IOException Exception for the stream.
     */
    private void sendRequest(BluetoothSocket socket, String hash) throws IOException {
        Log.d(TAG, "Write the hash request to the connected locator. ");

        DataOutputStream outStream = null;

        // Get the output stream for sending the hash
        outStream = new DataOutputStream(socket.getOutputStream());
        outStream.write(hash.getBytes());
    }

    /**
     * Attempt to retrieve a BO blocking the connection.
     * @param   socket  The socket for the connection
     * @return  The byte stream representing the retrieved BO
     * @throws  IOException Exception for the streams
     */
    private byte[] downloadFile(BluetoothSocket socket) throws IOException {
        Log.d(TAG, "Begining downloading the file");

        DataInputStream inStream = null;
        byte[] buffer = null;


        // Get the input stream for receiving the file
        inStream = new DataInputStream(socket.getInputStream());
        final int fileSize = inStream.readInt();
        buffer = new byte[fileSize];
        
        int offset = 0;  
        
        while (offset < fileSize) {  
            offset += inStream.read(buffer, offset, (fileSize - offset));
            onBufferRead(offset, fileSize);
        }
        
        inStream.close();  

        return buffer;
    }
    
    /**
     * Function that updates the view with the bytes that have been received.
     * @param offset how many bytes have been received
     * @param fileSize total file size
     */    
    public void onBufferRead(final int offset, final int fileSize) {

        // Get the activity from the main activity
        final Activity activity = (Activity) MainNetInfActivity.getContext();

        // Runnable that changes the view
        activity.runOnUiThread(new Runnable() {
            public void run() {
                TextView tv = (TextView) activity.findViewById(R.id.ProgressBarText);
                tv.setText("Downloading " + offset + " of " + fileSize + "");
                ProgressBar pb = (ProgressBar) activity.findViewById(R.id.progressbar_Horizontal);
                pb.setVisibility(ProgressBar.VISIBLE);
                pb.setProgress(FULL_PROGRESS * offset / fileSize);
            }
        });
    }

    /**
     * Checks if this provider can handle the locator from where to retrieve a BO.
     * 
     * @param   locator     The locator from where to retrieve the BO
     * @return  A boolean that specifies if this provider can handle the locator or not.
     */
    @Override
    public boolean canHandle(String locator) {
    	return locator.contains(BLUETOOTH_LOCATOR_INDICATOR);
    }

    /**
     * Description of this provider.
     *
     * @return A description of this provider? 
     */
    @Override
    public String describe() {
        return "A Bluetooth provider. This provider will attempt to retrieve a file"
                + "via a Bluetooth connection from a remote device.";
    }
}