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
import project.cs.lisa.bluetooth.threads.ConnectedBluetoothThread;
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
    private ConnectedBluetoothThread mConnectedThread;

    /** Handler for managing Bluetooth connection status. */
    private Handler mBluetoothHandler;
    
    /**
     * Default constructor.
     */
    public BluetoothProvider() {
        /** Handler for managing Bluetooth connection status. */
        mBluetoothHandler = new BluetoothConnectionHandler(this);
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
     * Start the ConnectedThread to begin managing a Bluetooth connection.
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected to
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {

        /** Update the UI with the device name.
         * This should be fixed with some class that updates the ui.
         
        mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME, -1, -1, device.getName())
        .sendToTarget();
         */
        
        /**
         * Starts listening for an incoming object file. This is a new thread
         * and it will block reading from the stream until the other device
         * responds.
         */
        mConnectedThread = new ConnectedBluetoothThread(mBluetoothHandler, socket);
        mConnectedThread.start();

        String message = "ready";
        byte[] out = message.getBytes();
        mConnectedThread.write(out);
    }

    /**
     * Fill please.
     * @param   locator     A locator.
     * @param   hash        A hash
     * @return A byte array? 
     */
    @Override
    public byte[] getByteArray(String locator, String hash) {
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