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
package project.cs.lisa.bluetooth.provider;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
 * The BluetoothProvider handles data transmission via bluetooth.
 * 
 * @author ktran
 * @author Paolo Boschini
 *
 */
public class BluetoothProvider implements ByteArrayProvider {

    /** Handler for managing Bluetooth connection status. */
    private Handler mBluetoothHandler;
    
    /** The bluetooth adapter. */
    private BluetoothAdapter mBluetoothAdapter = null;

    /** For now. Name of the BO we want to retrieve from a remote device. */
    private String mHash;

    /**
     * Default constructor.
     */
    public BluetoothProvider() {
        /** Handler for managing Bluetooth connection status. */
        mBluetoothHandler = new BluetoothConnectionHandler(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Returns a byte array after a successful connection and transmission
     * of a BO.
     * @param   locator     The source from where to fetch the BO
     * @param   hash        A hash identifying the BO
     * @return The byte array referring to the requested BO
     */
    @Override
    public byte[] getByteArray(String locator, String hash) {
        mHash = hash;
        BluetoothSocket socket = connectToRemoteDevice(locator);
        sendRequest(socket, hash);
        byte[] fileArray = downloadFile();
        return fileArray;
    }

    private BluetoothSocket connectToRemoteDevice(String locator) {
        mBluetoothAdapter.getRemoteDevice(locator);
        try {
            /** This is a blocking call and will only return on a
             * successful connection or an exception.
             */
            Log.d(TAG, "##################################");
            Log.d(TAG, "Trying to connect to a device through a socket...");
            Log.d(TAG, "##################################");
            mBluetoothHandler.obtainMessage(
                    BluetoothConnectionHandler.MESSAGE_CONNECTIONS_STATUS,
                    STATE_CONNECTING, -1, mSocket).sendToTarget();
            mSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Log.d(TAG, "##################################");
                Log.d(TAG, "Trying to close the socket due to a fail in the connection...");
                Log.d(TAG, "##################################");
                mSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return;
        }
    }

    private void sendRequest(BluetoothSocket socket, String hash) {
    }

    private byte[] downloadFile() {
        return null;
    }

    /**
     * Fill please.
     * @param   locator     A locator.
     * @return  Can handle what?
     */
    @Override
    public boolean canHandle(String locator) {
        return false;
    }

    /**
     * Fill please.
     * @return A description of this provider? 
     */
    @Override
    public String describe() {
        return null;
    }
}