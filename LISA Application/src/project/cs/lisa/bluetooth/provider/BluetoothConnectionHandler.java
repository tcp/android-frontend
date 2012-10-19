package project.cs.lisa.bluetooth.provider;

import java.util.Hashtable;

import project.cs.lisa.bluetooth.server.BluetoothService.ConnectedThread;
import project.cs.lisa.bluetooth.threads.ConnectBluetoothThread;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

/**
 * 
 * @author Paolo Boschini
 *
 */
public class BluetoothConnectionHandler extends Handler {

    /** Message for getting the device name. */
    public static final int MESSAGE_DEVICE_NAME = 0;

    /** Message for getting the connection status. */
    public static final int MESSAGE_CONNECTIONS_STATUS = 1;

    /** Message for getting the data read during a bluetooth connection. */
    public static final int FILE_READ = 2;

    /** Message telling the hash was received*/
    public static final int HASH_READ = 3;

    /** Message initiating a file transfer. */
    public static final int INCOMING_FILE_REQUEST = 4;

    /** Message for initiating a file request */
    public static final int OUTGOING_FILE_REQUEST = 5;

    /** Debug tag. */
    private static final String TAG = "BluetoothConnectionHandler";

    /** Bluetooth provider for managing communication over threads. */
    private BluetoothProvider mBluetoothProvider;

    /**
     * The current collection containing all current ongoing 
     * connections via ConnectedThread objects.
     */
    private Hashtable<String, ConnectedThread> mConnectedThreadsCollection;

    /**
     * Default constructor.
     * @param   bluetoothProvider   Reference to the provider
     */
    public BluetoothConnectionHandler(BluetoothProvider bluetoothProvider) {
        mBluetoothProvider = bluetoothProvider;
    }

    /**
     * @param   msg     The message 
     * The Handler that gets information back from the BluetoothService.
     */
    @Override
    public void handleMessage(Message msg) {
        BluetoothSocket socket;
        
        switch (msg.what) {
        case OUTGOING_FILE_REQUEST:
            socket = (BluetoothSocket) msg.obj;
            mBluetoothProvider.startRequestingFile(socket);
            break;
        case FILE_READ:
            
            break;
        case HASH_READ:
            byte[] hashBuffer = (byte[]) msg.obj;
            int length = (int) msg.arg1;
            
            String hash = new String(hashBuffer, 0, length);
            mBluetoothProvider.startSendingFile(hash);
            break;
        case INCOMING_FILE_REQUEST:
            socket = (BluetoothSocket) msg.obj;
            mBluetoothProvider.startSendingFile(socket);
            break;
            //        case MESSAGE_WRITE:
            //            byte[] writeBuf = (byte[]) msg.obj;
            //            // construct a string from the buffer
            //            String writeMessage = new String(writeBuf);
            //            mConversationArrayAdapter.add("Me:  " + writeMessage);
            //            break;
            //        case MESSAGE_READ:
            //            byte[] readBuf = (byte[]) msg.obj;
            //            // construct a string from the valid bytes in the buffer
            //            String readMessage = new String(readBuf, 0, msg.arg1);
            //            break;
            //        case FILE_READ:
            //            Log.D(TAG, "############## FILE_READ ##############");
            //
            //            String filePath = Environment.getExternalStorageDirectory()
            //                        + "/DCIM/" + Math.random() + ".jpg";
            //
            //            /** Get the bytes from the transport service. */
            //            byte[] buffer = (byte[]) msg.obj;
            //
            //            /** Read the bytes into an output stream. */
            //            BufferedOutputStream bos = null;
            //            try {
            //                bos = new BufferedOutputStream(new FileOutputStream(filePath));
            //            } catch (FileNotFoundException e) {
            //                e.printStackTrace();
            //            }
            //
            //            /** Write the bytes to a file on the device. */
            //            try {
            //                Log.D(TAG,"buffer.length: " + buffer.length);
            //                bos.write(buffer);
            //                bos.flush();
            //                bos.close();
            //            } catch (IOException e) {
            //                e.printStackTrace();
            //            }
            //
            //            /** Display the file according to the file type. */
            //            Intent intent = new Intent(Intent.ACTION_VIEW);
            //            File file = new File(filePath);
            //            intent.setDataAndType(Uri.fromFile(file), "image/*");
            //            startActivity(intent);
            //
            //            break;
        case MESSAGE_DEVICE_NAME:
            String deviceName = (String) msg.obj;
            //            Toast.makeText(getApplicationContext(), deviceName, Toast.LENGTH_SHORT).show();
            break;
        default:
            break;
        }
    }
}