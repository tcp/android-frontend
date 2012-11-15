/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
 */
package project.cs.lisa.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import project.cs.lisa.R;
import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.application.http.NetInfRetrieve;
import project.cs.lisa.application.http.NetInfSearch;
import project.cs.lisa.bluetooth.BluetoothServer;
import project.cs.lisa.hash.Hash;
import project.cs.lisa.metadata.Metadata;
import project.cs.lisa.netinf.node.StarterNodeThread;
import project.cs.lisa.networksettings.BTHandler;
import project.cs.lisa.search.SearchRequest;
import project.cs.lisa.util.UProperties;
import project.cs.lisa.viewfile.ViewFile;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity that acts as a starting point for the application.
 * It provides functions for the user interaction and for setting up
 * the application.
 *
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public class MainNetInfActivity extends Activity {

    /** Debugging tag. */
    private static final String TAG = "MainNetInfActivity";

	/** Represents the number of attempts to initialize a BluetoothServer. */
	private static final int NUMBER_OF_ATTEMPTS = 2;

    /** Message communicating if the node were started successfully. */
    public static final String NODE_STARTED_MESSAGE = "project.cs.list.node.started";

    /** Number of characters of the hash to use. **/
    public static final int HASH_LENGTH = 3;

    /** Reference to the global application state. */
    private MainApplication mApplication;

    /** Please comment. */
    private StarterNodeThread mStarterNodeThread;

    /** The Server listening for incoming Bluetooth requests. */
    private BluetoothServer mBluetoothServer;

    /** Activity context. */
    private static MainNetInfActivity mMainNetInfActivity;

    /** Toast. **/
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        mApplication = (MainApplication) getApplication();
        mMainNetInfActivity = this;
        mToast = new Toast(this);

        setupBluetoothAvailability();
        setupBroadcastReceiver();
        setupNode();
        setupBluetoothServer();

        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * Receives messages from the StarterNodeThread when the node is starter.
     * Right now it does not do anything. Just log
     */
    private void setupBroadcastReceiver() {
        Log.d(TAG, "setupBroadcastReceiver()");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                default:
                    Log.d(TAG, intent.getAction());
                    break;
                }
            }
        }, new IntentFilter(NODE_STARTED_MESSAGE));
    }

    /**
     * Initialize and run the StarterNodeThread.
     */
    private void setupNode() {
        Log.d(TAG, "setupNode()");
        // Start NetInfNode
        mStarterNodeThread = new StarterNodeThread(mApplication);
        mStarterNodeThread.start();
    }

    /**
     * Function to forceably initialize Bluetooth and enable discoverability option.
     */
    private void setupBluetoothAvailability() {
        BTHandler bt = new BTHandler();
        bt.forceEnable(mMainNetInfActivity);
    }

    /**
     * Gets a file from another node according to the input hash.
     * @param v The view that fired this event.
     */
    public final void getButtonClicked(final View v) {
        Log.d(TAG, "getButtonClicked()");

        // Store the input string
        EditText editText = (EditText) findViewById(R.id.hash_field);
        String hash = editText.getText().toString();
        
/*        if (hash.length() != HASH_LENGTH) {
            Toast.makeText(getApplicationContext(),
                    "Only three characters are allowed!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a new get request with the current hash
        Log.d(TAG, "Requesting the following hash: " + hash.substring(0, HASH_LENGTH));

        NetInfRetrieve retrieve = new NetInfRetrieve(
                UProperties.INSTANCE.getPropertyWithName("access.http.host"),
                UProperties.INSTANCE.getPropertyWithName("access.http.port"),
                makeMsgId(),
                hash,
                "not defined yet");

        search.execute();
        
        // Create a new get request with the current hash
        Log.d(TAG, "Requesting the following hash: " + hash.substring(0, HASH_LENGTH));
//
//        NetInfRetrieve retrieve = new NetInfRetrieve(this,
//                UProperties.INSTANCE.getPropertyWithName("access.http.host"),
//                UProperties.INSTANCE.getPropertyWithName("access.http.port"),
//                UProperties.INSTANCE.getPropertyWithName("hash.alg"),
//                hash.substring(0, HASH_LENGTH)) {
//
//            @Override
//            protected void onPostExecute(String jsonResponse) {
//                /*
//                 * If the get request couldn't download the file
//                 * it will notify the user and stop processing.
//                 */
//                Log.d(TAG, "jsonResponse: " + jsonResponse);
//                if (jsonResponse == null) {
//                    getActivity().showToast(
//                            "Getting file failed. Check your Internet and Bluetooth connections");
//                    return;
//                }
//
//                // Parse the JSON
//                Metadata json = new Metadata(jsonResponse);
//                String filePath = json.get("filePath");
//                String contentType = json.get("contentType");
//                Log.d(TAG, "contentType = " + contentType);
//                Log.d(TAG, "filePath = " + filePath);
//
//                // Try to display the file
//                int code = ViewFile.displayContent(getActivity(), filePath, contentType);
//                Log.d(TAG, "code = " + code);
//                switch (code) {
//                case ViewFile.OK:
//                    break;
//                default:
//                    getActivity().showToast("Opening file failed.");
//                    break;
//                }
//            }
//
//
//        };
//
//        // Execute request
//        retrieve.execute();
//
        //        For now open the received file in the asynch task.
        //        Later, uncomment this code and use a Handler to get back
        //        the filePath and the contentType.

        //        String filePath = "";
        //        String contentType = "";
        //
        //        /* Display the file according to the file type. */
        //        Intent intent = new Intent(Intent.ACTION_VIEW);
        //        File file = new File(filePath);
        //
        //        /* Replace image/* with contentType */
        //        intent.setDataAndType(Uri.fromFile(file), "image/*");
        //        startActivity(intent);
    }

    private String makeMsgId() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        
        int randomNumber = new Random(System.currentTimeMillis()).nextInt();
        
        String msgId = deviceId + String.valueOf(randomNumber);
        
        return msgId;
    }

    /**
     * Creates an intent to select an image from the gallery.
     * @param v The view that fired this event.
     */
    // TODO: Deprecated? Although I think it is better opening image/* for now
    public final void publishButtonClicked(final View v) {
        Log.d(TAG, "publishButtonClicked()");

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }

    /**
     * Publish a file from the image gallery on the phone.
     * Creates the hash and extracts the content type.
     * @param requestCode The integer request code originally supplied
     * to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned
     * by the child activity through its setResult().
     * @param data An Intent, which can return result
     * data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");

        if (resultCode != RESULT_OK) {
            return;
        }

        String filePath = null;

        if (data.getScheme().equals("content")) {
            if (data.getData().getPath().contains("/external/images")) {

                /* Get the file path of the selected image. */
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(
                        selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        } else if (data.getScheme().equals("file")) {
            Uri selectedImage = data.getData();
            filePath = selectedImage.getPath();
        }

        Log.d(TAG, filePath);

        // Open file
        File file = new File(filePath);

        if (file.exists()) {
            /* Help class for files, extract content type */
            String contentType = ViewFile.getFileContentType(filePath);

            /* Help class for files, generate the hash */
            Hash lisaHash = null;
            String hash = null;

            // Try to hash the file
            try {
                lisaHash = new Hash(FileUtils.readFileToByteArray(file));
                hash = lisaHash.encodeResult(HASH_LENGTH); // Use 0 for using the whole hash
                Log.d(TAG, "The generated hash is: " + hash);
            } catch (IOException e1) {
                Log.e(TAG, "Error, could not open the file: " + file.getPath());
            }

            // f1 = file chosen for publishing
            // f2 = file that will hold content in shared folder
            File f1 = new File(filePath);
            File f2 = new File(Environment.getExternalStorageDirectory() + "/DCIM/Shared/" + hash);

            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(f1);
                out = new FileOutputStream(f2, true);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                Log.d(TAG, "File not found! Check if something went wrong when choosing file");
                e1.printStackTrace();
            }

            // Try copying file to shared folder
            try {
                try {
                    IOUtils.copy(in, out);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "Failed to copy file to shared folder");
                    e.printStackTrace();
                }
            } finally {
                Log.d(TAG, "Closing file streams for transfer");
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                Log.d(TAG, "File streams for transfer closed");
            }

            // Create meta data
            Metadata lisaMetaData = new Metadata();

            // Metadata has 3 fields: filesize, filename and filetype
            lisaMetaData.insert("filesize", String.valueOf(file.length()));
            lisaMetaData.insert("filename", file.getName());
            lisaMetaData.insert("filetype", ViewFile.getFileContentType(filePath));
            lisaMetaData.insert("time", Long.toString(System.currentTimeMillis()));

            // Metadata has 1 field: publish time

            // Convert metadata into readable format
//            String metaData = lisaMetaData.convertToString();

            // TODO: Remove this hack! Talk to other team about the metadata storage on their side
//            metaData = lisaMetaData.remove_brackets(metaData);

            // Log the metadata
//            Log.d(TAG, "metadata: " + metaData);

            // Publish!
            Log.d(TAG, "Trying to publish a new file.");

            // Try to get the Bluetooth MAC
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            if (adapter == null) {
                MainNetInfActivity.getActivity().showToast("Error: Bluetooth not supported");
            } else if (!adapter.isEnabled()) {
                MainNetInfActivity.getActivity().showToast("Error: Bluetooth not enabled");
            } else {
                HashSet<Locator> locators = new HashSet<Locator>();
                locators.add(new Locator(Locator.Type.BLUETOOTH, adapter.getAddress()));

                NetInfPublish publishRequest = new NetInfPublish(
                        UProperties.INSTANCE.getPropertyWithName("access.http.host"),
                        UProperties.INSTANCE.getPropertyWithName("access.http.port"),
                        UProperties.INSTANCE.getPropertyWithName("hash.alg"),
                        hash.substring(0, HASH_LENGTH),
                        locators);
                publishRequest.setContentType(contentType);
                publishRequest.setMetadata(lisaMetaData);
                publishRequest.execute();
            }
        }
    }

    /**
     * Initiates and starts the Bluetooth Server.
     */
    private void setupBluetoothServer() {
        Log.d(TAG, "setupBluetoothServer()");

        // Tries to initialize the Bluetooth Server several times, if unsuccessful.
        int attempts = NUMBER_OF_ATTEMPTS;
        do {
        	try {
        		mBluetoothServer = new BluetoothServer();
        		mBluetoothServer.start();
        	} catch (IOException e) {
        		--attempts;
        		mBluetoothServer = null;
        	}
        } while (mBluetoothServer == null && attempts > 0);

        if (mBluetoothServer == null) {
        	Log.e(TAG, "BluetoothServer couldn't be initialized.");
        }
    }

    /**
     * Returns the context of this activity.
     * @return  the context
     */
    public static MainNetInfActivity getActivity() {
        return mMainNetInfActivity;
    }

    /**
     * Show a toast.
     * @param text      The text to show in the toast.
     */
    public void showToast(String text) {
        Log.d(TAG, "showToast()");
        mToast.cancel();
        mToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        mToast.show();
    }

    /**
     * Cancel current toast.
     */
    public void cancelToast() {
        Log.d(TAG, "cancelToast()");
        mToast.cancel();
    }

    /**
     * Hides the progress bar.
     */
    public void hideProgressBar() {
        Log.d(TAG, "hideProgressBar()");
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(ProgressBar.INVISIBLE);
        ProgressBar pb1 = (ProgressBar) findViewById(R.id.progressbar_Horizontal);
        pb1.setVisibility(ProgressBar.INVISIBLE);
        TextView tv = (TextView) findViewById(R.id.ProgressBarText);
        tv.setVisibility(TextView.INVISIBLE);
    }

    /**
     * Shows the progress bar.
     * @param text String with the text to show to the user. Normally informs
     *             if we are publishing, searching or requesting content.
     */
    public void showProgressBar(String text) {
        Log.d(TAG, "showProgressBar()");
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(ProgressBar.VISIBLE);
        TextView tv = (TextView) findViewById(R.id.ProgressBarText);
        tv.setVisibility(TextView.VISIBLE);
        tv.setText(text);
    }

}
