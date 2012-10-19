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
import android.util.Log;

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

    /**
     * Universally unique identifier so that two or more devices
     * can establish a connection through the same service (in this case Bluetooth).
     * At design time rather than agreeing upon a port number the server and client
     * applications agree upon a unique identifier. Specifically, a developer chooses
     * this UUID at design time and when the program is run, it registers its Service ID
     * with the SDP server for that device. A client application trying to find a
     * specific service would query the SDP server on each device it finds to see
     * if the device offers any services with that same UUID.
     * The following UUID was generated using http://www.famkruithof.net/uuid/uuidgen.
     */
    private static final UUID MY_UUID = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    /** The Bluetooth adapter. */
    private BluetoothAdapter mBluetoothAdapter = null;

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
     * @return  The byte array referring to the requested BO
     */
    @Override
    public byte[] getByteArray(String locator, String hash) {

        byte[] fileArray = null;
        BluetoothSocket socket = null;

        try {

            /* Connect */
            socket = connectToRemoteDevice(locator);

            /* Send request */
            sendRequest(socket, hash);

            /* Download file */
            fileArray = downloadFile(socket);

        } catch (IOException e) {
            Log.d(TAG, "Trying to close the socket due to a fail in the connection...");
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    Log.d(TAG, "Something went wrong when closing the socket!");
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
     */
    private BluetoothSocket connectToRemoteDevice(String locator) throws IOException {

        BluetoothSocket socket = null;
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(locator);

        /* Get a BluetoothSocket for a connection with the given BluetoothDevice.
         * An insecure connection does never ask the user to pair
         * another device during a Bluetooth connection.
         */
        socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);

        /* This is a blocking call and will only return on a
         * successful connection or an exception.
         */
        Log.d(TAG, "Trying to connect to a device through a socket...");
        socket.connect();

        return socket;
    }

    /**
     * Send a request to a remote device sending the hash identifier
     * for retrieving the corresponding BO. 
     * @param   socket  The socket for connecting with the remote device    
     * @param   hash    The identifier for requesting the BO
     */
    private void sendRequest(BluetoothSocket socket, String hash) throws IOException {
        DataOutputStream outStream = null;

        /* Get the output stream for sending the hash */
        outStream = new DataOutputStream(socket.getOutputStream());
        outStream.write(hash.getBytes());
    }

    /**
     * Attempt to retrieve a BO blocking the connection.
     * @param   socket  The socket for the connection
     * @return  The byte stream representing the retrieved BO
     */
    private byte[] downloadFile(BluetoothSocket socket) throws IOException {
        DataInputStream inStream = null;
        byte[] buffer = null;

        /* Get the input stream for receiving the file */
        inStream = new DataInputStream(socket.getInputStream());
        int fileSize = inStream.readInt();
        inStream.readFully(buffer);
        buffer = new byte[fileSize];

        return buffer;
    }

    /**
     * Checks if this provider can handle the locator from where to retrieve a BO.
     * @param   locator     The locator from where to retrieve the BO
     * @return  A boolean that specifies if this provider can handle the locator or not.
     */
    @Override
    public boolean canHandle(String locator) {
        return false;
    }

    /**
     * Description of this provider.
     * @return A description of this provider? 
     */
    @Override
    public String describe() {
        return "A Bluetooth provider. This provider will attempt to retrieve a file"
                + "via a Bluetooth connection from a remote device.";
    }
}
