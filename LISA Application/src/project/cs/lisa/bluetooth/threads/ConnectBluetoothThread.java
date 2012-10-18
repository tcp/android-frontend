package project.cs.lisa.bluetooth.threads;

import java.io.IOException;
import java.util.UUID;

import project.cs.lisa.bluetooth.provider.BluetoothConnectionHandler;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
public class ConnectBluetoothThread extends Thread {

    /** Debug Tag. */
    private static final String TAG = "ConnectBluetoothThread";

    /** Bluetooth Socket enabling the connection to the remote device. */
    private BluetoothSocket mSocket = null;

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

    /** Handler for managing Bluetooth connection status. */
    private Handler mBluetoothHandler;

    /** Message representing a connecting state before getting a socket. */
    public static final int STATE_CONNECTING = 0;

    /** Message representing a connected state after getting a socket. */
    public static final int STATE_CONNECTED = 1;

    /**
     * Instantiate a new ConnectBluetoothThread.
     * @param bluetoothHandler The handler.
     * @param device The remote device we are going to establish a connection with. 
     */
    public ConnectBluetoothThread(Handler bluetoothHandler, BluetoothDevice device) {
        BluetoothSocket tmp = null;
        mBluetoothHandler = bluetoothHandler;

        /** Get a BluetoothSocket for a connection with the given BluetoothDevice. */
        try {
            /** An insecure connection does never ask the user to pair
             * another device during a Bluetooth connection.
             */
            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSocket = tmp;
    }

    /**
     * Starts the thread for connecting to a remote device through Bluetooth.
     */
    public void run() {
        /** Try to establish a connection to the BluetoothSocket. */
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

        /**
         * A handler should sent back a confirmation message to the Bluetooth Provider
         * when the socket has been connected.
         */
        mBluetoothHandler.obtainMessage(
                BluetoothConnectionHandler.MESSAGE_CONNECTIONS_STATUS,
                STATE_CONNECTED, -1, mSocket).sendToTarget();
    }

    /**
     * Closes the socket.
     */
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}