package project.cs.lisa.bluetooth.provider;

import project.cs.lisa.bluetooth.server.BluetoothService.ConnectedThread;
import project.cs.lisa.bluetooth.threads.ConnectBluetoothThread;
import project.cs.lisa.bluetooth.threads.ConnectedBluetoothThread;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
 * The BluetoothProvider handles data transmission via bluetooth.
 * 
 * @author ktran
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
    private ConnectedThread mConnectedThread;

    /**
     * TODO _Paolo Probably old comment?
     * Handler for scheduling messages between threads. Right now used
     * when Bluetooth actions take place (transfer a file or updating
     * the status of the connection with another device).
     */
    private Handler mHandler;

    /**
     * Default constructor.
     */
    public BluetoothProvider(Handler handler) {
        mHandler = handler;
    }

    /**
     * Start the ConnectBluetoothThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect to found by a discovery scanning
     */
    public synchronized void connect(BluetoothDevice device) {
        mConnectThread = new ConnectBluetoothThread(device);
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
        mConnectedThread = new ConnectedBluetoothThread(socket);
        mConnectedThread.start();

        String message = "ready";
        byte[] out = message.getBytes();
        mConnectedThread.write(out);
    }

    @Override
    public byte[] getByteArray(String locator, String hash) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canHandle(String locator) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String describe() {
        // TODO Auto-generated method stub
        return null;
    }
}