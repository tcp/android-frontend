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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

    /** Debug Tag. */
    private static final String TAG = "BluetoothProvider";

    /**
     * Universally unique identifier so that two or more devices
     * can establish a connection through the same service (in this case Bluetooth).
     * At design time rather than agreeing upon a port number the server and client
     * applications agree upon a unique identifier. Specifically, a developer chooses
     * this UUID at design time and when the program is run, it registers its Service ID
     * with the SDP server for that device. A client application trying to find a
     * specific service would query the SDP server on each device it finds to see
     * if the device offers any services with that same UUID. The following UUID
     * was generated using http://www.famkruithof.net/uuid/uuidgen.
     */
    private static final UUID MY_UUID = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    /** The bluetooth adapter. */
    private BluetoothAdapter mBluetoothAdapter = null;

    /** For now. Name of the BO we want to retrieve from a remote device. */
    private String mHash;

    /**
     * Default constructor.
     */
    public BluetoothProvider() {
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
        
        /** Connect */
        BluetoothSocket socket = connectToRemoteDevice(locator);

        /** Send request */
        sendRequest(socket, hash);

        /** Download file */
        byte[] fileArray = downloadFile(socket);

        return fileArray;
    }

    private BluetoothSocket connectToRemoteDevice(String locator) {

        BluetoothSocket socket = null;
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(locator);

        /** Get a BluetoothSocket for a connection with the given BluetoothDevice. */
        try {
            /** An insecure connection does never ask the user to pair
             * another device during a Bluetooth connection.
             */
            socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            /** This is a blocking call and will only return on a
             * successful connection or an exception.
             */
            Log.d(TAG, "Trying to connect to a device through a socket...");
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Log.d(TAG, "Trying to close the socket due to a fail in the connection...");
                socket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        
        return socket;
    }

    private void sendRequest(BluetoothSocket socket, String hash) {
        DataOutputStream outStream = null;

        /** Get the output stream for sending the hash */
        try {
            outStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
           outStream.write(hash.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] downloadFile(BluetoothSocket socket) {
        DataInputStream inStream = null;
        byte[] buffer = null;

        /** Get the input stream for receiving the file */
        try {
            inStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            int fileSize = inStream.readInt();
            inStream.readFully(buffer);
            buffer = new byte[fileSize];
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return buffer;
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