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

import project.cs.lisa.bluetooth.threads.ConnectBluetoothThread;
import project.cs.lisa.bluetooth.threads.ClientBluetoothThread;
import project.cs.lisa.bluetooth.threads.ServerBluetoothThread;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
 * The BluetoothProvider handles data transmission via bluetooth.
 * 
 * @author ktran
 * @author Paolo Boschini
 *
 */
public class BluetoothProvider implements ByteArrayProvider {

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a remote device.
     */
    private ConnectBluetoothThread mConnectThread;

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     *
     * TODO _Paolo check comment
     * Old comment?
     * In this class it will be responsible for requesting an object
     * using the hash value of the requested object and receiving
     * the byte stream of the corresponding object.
     */
    private ClientBluetoothThread mClientThread;

    private ServerBluetoothThread mServerThread;

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
     * Start the ConnectBluetoothThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect to found by a previous discovery scanning
     */
    public synchronized void connect(BluetoothDevice device) {
        mConnectThread = new ConnectBluetoothThread(mBluetoothHandler, device);
        mConnectThread.start();
    }

    /**
     * Start the client thread to request and download a file.
     * @param socket  The BluetoothSocket on which the connection was made
     */
    public synchronized void startRequestingFile(BluetoothSocket socket) {
        mClientThread = new ClientBluetoothThread(mBluetoothHandler, socket, mHash);
        mClientThread.start();
    }

    /**
     * Start the server for reading the hash and send the corresponding BO
     * @param socket  The BluetoothSocket on which the connection was made
     */
    public synchronized void startSendingFile(BluetoothSocket socket) {
        mServerThread = new ServerBluetoothThread(mBluetoothHandler, socket);
        mServerThread.start();
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
        connect(mBluetoothAdapter.getRemoteDevice(locator));
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