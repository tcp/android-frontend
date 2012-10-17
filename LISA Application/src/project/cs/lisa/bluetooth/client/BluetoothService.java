package project.cs.lisa.bluetooth.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
 * This class acts as a Bluetooth client.
 * It provides functionalities for sending a request for an NDO to a
 * NetInf node through Bluetooth and it expects the requested data
 * object back as a stream of bytes. 
 *
 * @author Paolo Boschini
 *
 */
public class BluetoothService {

    /**
     * Universally unique identifier so that two or more devices
     * can establish a connection through the same service (in this case Bluetooth).
     * At design time rather than agreeing upon a port number the server and client
     * applications agree upon a unique identifier. Specifically, a developer chooses
     * this UUID at design time and when the program is run, it registers its Service ID
     * with the SDP server for that device. A client application trying to find a
     * specific service would query the SDP server on each device it finds to see
     * if the device offers any services with that same UUID. The following UUID
     * was generated using http://www.famkruithof.net/uuid/uuidgen..
     */
    private static final UUID MY_UUID = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device.
     */
    private ConnectThread mConnectThread;

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     * In this class it will be responsible for requesting an object
     * using the hash value of the requested object and receiving
     * the byte stream of the corresponding object.
     */
    private ConnectedThread mConnectedThread;

    /** The local Bluetooth adapter. */
    private BluetoothAdapter mBluetoothAdapter = null;

    /** The state of the communication with other devices. */
    private int mState;

    /**
     * Handler for scheduling messages between threads. Right now used
     * when Bluetooth actions take place (transfer a file or updating
     * the status of the connection with another device).
     */
    private Handler mHandler;

    /** Constants that indicate the current connection state. */
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    /**
     * 
     * @param handler The handler used for updating the main activity
     * about the status of things.
     */
    public BluetoothService(Handler handler) {
        mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    /**
     * Set the current state of the connection.
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        mState = state;

        /** Notify the new state to the Handler so the UI Activity can update */
        mHandler.obtainMessage(BluetoothTest.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect to
     */
    public synchronized void connect(BluetoothDevice device) {
        setState(STATE_CONNECTING);
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection.
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {

        /** Update the UI with the device name */
        mHandler.obtainMessage(BluetoothTest.MESSAGE_DEVICE_NAME, -1, -1, device.getName())
                .sendToTarget();

        setState(STATE_CONNECTED);

        /**
         * Starts listening for an incoming object file. This is a new thread
         * and it will block reading from the stream until the other device
         * responds.
         */
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        
        String message = "ready";
        byte[] out = message.getBytes();
        mConnectedThread.write(out);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(
                        MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                System.out.println("##################################");
                System.out.println("...Connecting...");
                System.out.println("##################################");
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final DataInputStream mmInStream;
        private final DataOutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            DataInputStream tmpIn = null;
            DataOutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                // Kim: changed cast into new
                tmpIn = new DataInputStream(mmSocket.getInputStream());
                tmpOut = new DataOutputStream(mmSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /**
         * Blocks the thread for reading an input stream.
         */
        @Override
        public void run() {
            try {
                int fileSize = mmInStream.readInt();
                byte[] buffer = new byte[fileSize];
                mmInStream.readFully(buffer);
                mHandler.obtainMessage(BluetoothTest.FILE_READ, -1, -1, buffer)
                .sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
