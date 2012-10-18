package project.cs.lisa.bluetooth.provider;

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

    /** Bluetooth provider for managing communication over threads. */
    private BluetoothProvider mBluetoothProvider;

    /** Message for getting the device name. */
    public static final int MESSAGE_DEVICE_NAME = 0;

    /** Message for getting the connection status. */
    public static final int MESSAGE_CONNECTIONS_STATUS = 1;

    /**
     * Default constructor.
     * @param bluetoothProvider
     */
    public BluetoothConnectionHandler(BluetoothProvider bluetoothProvider) {
        mBluetoothProvider = bluetoothProvider;
    }

    /**
     * The Handler that gets information back from the BluetoothService.
     */
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_CONNECTIONS_STATUS:
            switch (msg.arg1) {
            case ConnectBluetoothThread.STATE_CONNECTING:
                break;
            case ConnectBluetoothThread.STATE_CONNECTED:
                BluetoothSocket socket = (BluetoothSocket)msg.obj;
                mBluetoothProvider.connected(socket);
                break;
            default:
                break;
            }
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
//            System.out.println("############## FILE_READ ##############");
//
//            String filePath = Environment.getExternalStorageDirectory() + "/DCIM/" + Math.random() + ".jpg";
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
//                System.out.println("buffer.length: " + buffer.length);
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