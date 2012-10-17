package project.cs.lisa.bluetooth.provider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.example.bluetoothtest.BluetoothService;

public class BluetoothConnectionHandler {
    
    /**
     * Default constructor.
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
        case MESSAGE_STATE_CHANGE:
            switch (msg.arg1) {
            case BluetoothService.STATE_CONNECTED:
                setStatus("Connected to " + mConnectedDeviceName);
                break;
            case BluetoothService.STATE_CONNECTING:
                setStatus("Connecting...");
                break;
            case BluetoothService.STATE_LISTEN:
            case BluetoothService.STATE_NONE:
                setStatus("Not connected");
                break;
            }
            break;
            //            case MESSAGE_WRITE:
            //                byte[] writeBuf = (byte[]) msg.obj;
            //                // construct a string from the buffer
            //                String writeMessage = new String(writeBuf);
            //                mConversationArrayAdapter.add("Me:  " + writeMessage);
            //                break;
        case MESSAGE_READ:
            byte[] readBuf = (byte[]) msg.obj;
            // construct a string from the valid bytes in the buffer
            String readMessage = new String(readBuf, 0, msg.arg1);
            break;
        case FILE_READ:
            System.out.println("############## FILE_READ ##############");

            String filePath = Environment.getExternalStorageDirectory() + "/DCIM/" + Math.random() + ".jpg";

            /** Get the bytes from the transport service. */
            byte[] buffer = (byte[]) msg.obj;

            /** Read the bytes into an output stream. */
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(filePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            /** Write the bytes to a file on the device. */
            try {
                System.out.println("buffer.length: " + buffer.length);
                bos.write(buffer);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /** Display the file according to the file type. */
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(filePath);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            startActivity(intent);

            break;
        case MESSAGE_DEVICE_NAME:
            mConnectedDeviceName = (String) msg.obj;
            // save the connected device's name
            setStatus("Connected to :" + mConnectedDeviceName);
            break;
            //            case MESSAGE_TOAST:
            //                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
            //                        Toast.LENGTH_SHORT).show();
            //                break;
        default:
            break;
        }
    }
}