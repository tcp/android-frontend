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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import project.cs.lisa.R;
import project.cs.lisa.application.dialogs.ListDialog;
import project.cs.lisa.application.dialogs.OkButtonDialog;
import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.bluetooth.BluetoothServer;
import project.cs.lisa.hash.Hash;
import project.cs.lisa.metadata.Metadata;
import project.cs.lisa.netinf.node.StarterNodeThread;
import project.cs.lisa.networksettings.BTHandler;
import project.cs.lisa.util.UProperties;
import project.cs.lisa.viewfile.ViewFile;
import project.cs.lisa.wifi.WifiHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
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

    /** Activity context. */
    private static MainNetInfActivity mMainNetInfActivity;

    private MainApplication mApplication;

    private static Toast mToast;

    private StarterNodeThread mStarterNodeThread;

    private BluetoothServer mBluetoothServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main_sprint2);

        mApplication = (MainApplication) getApplication();
        mMainNetInfActivity = this;
        mToast = new Toast(this);

//        setupWifi();
        setupBluetoothAvailability();
        setupBroadcastReceiver();
        setupNode();
        setupBluetoothServer();

        // Get the input address
//        EditText editText = (EditText) findViewById(R.id.url);
//        editText.setText(UProperties.INSTANCE.getPropertyWithName("default.webpage"));

//        showDialog(new ShareDialog());

        /*
         * ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
         * Set the color of the progress bar programmatically.
         * Blue is the default one set in activity_main.xml
         * pb.setProgressDrawable(getResources().getDrawable(R.drawable.green_progress));
         * pb.setProgressDrawable(getResources().getDrawable(R.drawable.red_progress));
         * pb.setProgressDrawable(getResources().getDrawable(R.drawable.blue_progress));
         */
    }

    /**
     * Set up the WiFi connection.
     */
    private void setupWifi() {
        // Create OK dialog
        showDialog(new OkButtonDialog("Wifi Information", getString(R.string.dialog_wifi_msg), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "doPositiveClickWifiInfoMessage()");

                // This is run when OK is clicked
                // Create a WifiHandler
                WifiHandler wifiHandler = new WifiHandler() {
                    @Override
                    public void onDiscoveryDone(Set<String> wifis) {

                        // This is run when the WIFI discovery is done
                        // Create a ListDialog that shows the networks
                        ListDialog listDialog = new ListDialog(wifis) {
                            @Override
                            public void onConfirm(String wifi) {

                                // This is run when the ListDialog is confirmed
                                connectToSelectedNetwork(wifi);
                            }
                        };
                        showDialog(listDialog);
                    }
                };
                // Start WifiHandler discovery
                wifiHandler.startDiscovery();
            }
        }));
    }

    /**
     * Show a dialog.
     * @param dialog
     */
    private void showDialog(DialogFragment dialog) {
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "");
    }

    /**
     * Try to fetch the requested web page.
     * @param v
     */
    public final void goButtonClicked(final View v) {

        // get the web page address
        EditText editText = (EditText) findViewById(R.id.url);
        URL url = null;
        try {
            url = new URL(editText.getText().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            showToast("Malformed url!");
            return;
        }

        // Dismiss keyboard
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.requestFocus();

        if (!addressIsValid(url.toString())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Invalid url")
            .setTitle("Invalid url")
            .setNeutralButton("Ok, sorry :(", null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            // start downloading the web page
            DownloadWebPageTask task = new DownloadWebPageTask();
            task.execute(url);
        }
    }

    /**
     * Checks if a URL address is valid.
     * @return
     */
    public boolean addressIsValid(String url) {
        return url.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
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
    public static void showToast(String text) {
        Log.d(TAG, "showToast()");
        mToast.cancel();
        mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
        mToast.show();
    }

    /**
     * Cancel current toast.
     */
    public static void cancelToast() {
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

    // ========= IMPORTANT: LEGACY CODE FOR PUBLISHING A PICTURE =========
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
//                hash = lisaHash.encodeResult(HASH_LENGTH); // Use 0 for using the whole hash
                hash = lisaHash.encodeResult(); // Use 0 for using the whole hash
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
                out = new FileOutputStream(f2, false);
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
                showToast("Error: Bluetooth not supported");
            } else if (!adapter.isEnabled()) {
                showToast("Error: Bluetooth not enabled");
            } else {
                String bluetoothMac = adapter.getAddress();
                Log.d(TAG, "Creating locator with bluetoothMac = " + bluetoothMac);
                HashSet<Locator> locators = new HashSet<Locator>();
                locators.add(new Locator(Locator.Type.BLUETOOTH, bluetoothMac));

                NetInfPublish publishRequest = new NetInfPublish(
                        UProperties.INSTANCE.getPropertyWithName("access.http.host"),
                        UProperties.INSTANCE.getPropertyWithName("access.http.port"),
                        UProperties.INSTANCE.getPropertyWithName("hash.alg"),
//                        hash.substring(0, HASH_LENGTH),
//                        hash,
                        "TcoP1fQkoxsDq4B8uud+syvy0Inu0c7hVLOv7UWN4Nw",
                        locators);
                publishRequest.setContentType(contentType);
                publishRequest.setMetadata(lisaMetaData);
                publishRequest.setFile(f2);
                publishRequest.execute();
            }
        }
    }

//    public final void getButtonClicked(final View v) {
//
//    }


//    ============ END OLD CODE FOR PUBLISHING A PICTURE ============
}
