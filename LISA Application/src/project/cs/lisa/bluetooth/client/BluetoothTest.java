package project.cs.lisa.bluetooth.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
/**
 * This class is an attempt to communicate with two devices using bluetooth.
 * @author paolo
 *
 */

public class BluetoothTest extends Activity {

    /**
     * The TAG for this Activity.
     */
    private final String mTAG = "BluetoothTest";

    /**
     * The bluetooth adapter.
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    
    /**
     * A broadcast receiver for intercepting broadcast messages i.e. bluetooth activity.
     */
    private BroadcastReceiver mBroadcastReceiver;
    
    /**
     * Instance of BluetoothService for managing bluetooth connection and bluetooth transfers..
     */
    private BluetoothService mBluetoothService; 
    
    /**
     * Message types sent from the BluetoothChatService Handler.
     */
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_READ = 2;
    public static final int FILE_READ = 3;

    /**
     * The name of the connected device.
     */
    private String mConnectedDeviceName;

    private List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);

        System.out.println("Environment.getExternalStorageDirectory(): " + Environment.getExternalStorageDirectory());
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setUpBroadcastReceiver();
        registerReceiver(mBroadcastReceiver, new IntentFilter(
                BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mBroadcastReceiver, new IntentFilter(
                BluetoothDevice.ACTION_FOUND));
        registerReceiver(mBroadcastReceiver, new IntentFilter(
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    /**
     * Determines which actions are to be intercepted from android services,
     * for now intercept only bluetooth discovery.
     */
    private void setUpBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                Button button = (Button) findViewById(R.id.discover_devices);

                if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    button.setEnabled(false);
                    setStatus("Discovering devices");
                }
                if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                    button.setEnabled(true);
                    setStatus("Done discovering devices");
                    
                    for (BluetoothDevice device : mDevices) {
                        if (device.getName().equals("002")) {
                            setStatus("Found " + device.getName() + ", trying to connect.");
                            mBluetoothService = new BluetoothService(mHandler);
                            mBluetoothService.connect(mBluetoothAdapter.getRemoteDevice(device.getAddress()));
                        }
                    }
                }
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    /** This checks the Bluetooth signal strength of the surrounding devices. */
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                    Log.d(mTAG, "dB: " + rssi);
                    Toast.makeText(getApplicationContext(),"  RSSI: " + rssi + "dBm",
                            Toast.LENGTH_SHORT).show();
                    
                    Log.d(mTAG, device.getName() + "\n" + device.getAddress() + ", " + rssi + " dB");
                    mDevices.add(device);
                }
            }
        };
    }

    private final void setStatus(String status) {
//        final ActionBar actionBar = getActionBar();
//        actionBar.setSubtitle(status);
    }

    /**
     * Called when the button "Discover Devices" is clicked.
     * Fires up actions intercepted by the broadcast receiver. 
     * 
     * @param view The view
     */
    public void discoverBluetoothDevices(View view) {
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Called when the button "Send Message" is clicked.
     * This will connect to a device (hard coded for now)
     * and send a message via a socket.
     * 
     * @param view The view
     */
    public void openPic(View view) {
//        http://indyvision.net/2010/03/android-using-intents-open-files/
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/miao.jpg");
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        startActivity(intent); 
    }

    /**
     * Called when the button "Send Message" is clicked.
     * This will connect to a device (hard coded for now)
     * and send a message via a socket.
     * 
     * @param view The view
     */
    public void sendMessage(View view) {
//        String message = "ready";
//
//        byte[] send = message.getBytes();
//        mBluetoothService.write(send);
    }

    /**
     * The Handler that gets information back from the BluetoothService.
     */
    private final Handler mHandler = new Handler() {
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

                String filePath = Environment.getExternalStorageDirectory() + "/DCIM/001.jpg";
                
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
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_bluetooth_test, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(mTAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(mTAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(mTAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(mTAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(mTAG, "onDestroy");
        unregisterReceiver(mBroadcastReceiver);
    }

}