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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import project.cs.lisa.R;
import project.cs.lisa.application.http.NetInfRequest;
import project.cs.lisa.bluetooth.BluetoothServer;
import project.cs.lisa.file.LisaFileHandler;
import project.cs.lisa.hash.LisaHash;
import project.cs.lisa.metadata.LisaMetadata;
import project.cs.lisa.netinf.node.LisaStarterNodeThread;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.Toast;

public class MainNetInfActivity extends Activity {

    /** Debugging tag. */
    public static final String TAG = "DemoSpring2Activity";

    /** Hash algorithm constant. */
    public static final String HASH_ALG = "sha-256";

    /** Please comment. */
    public static final String NODE_STARTED = "project.cs.list.node.started";

    /** Local NetInf node that will redirect the request to an NRS. */
    private static final String HOST = "localhost";

    /** Port for connecting to the internal NetInf node. */
    private static final int PORT = 8080;

    /** Please comment. */
    private MainApplication mApplication;

    /** Please comment. */
    private LisaStarterNodeThread mStarterNodeThread;

    /**
     * The Server listening for incoming Bluetooth requests.
     */
    private BluetoothServer mBluetoothServer;
    
    /** Activity context **/
    private static Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        mApplication = (MainApplication) getApplication();
        mContext = this;

        setupBroadcastReceiver();
        setupNode();
        setupBluetoothServer();

        setContentView(R.layout.activity_demo_sprint2);
    }
    
    public static Context getContext() {
        return mContext;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_demo_sprint2, menu);
        return true;
    }

    /**
     * Please comment.
     */
    
    private void setupBroadcastReceiver() {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    default:
                        Log.d(TAG, intent.getAction());
                        break;
                }
            }
        }, new IntentFilter(NODE_STARTED));
    }

    /**
     * Please comment.
     */
    
    private void setupNode() {
        // Start NetInfNode
        mStarterNodeThread = new LisaStarterNodeThread(mApplication);
        mStarterNodeThread.start();
    }

    /**
     * Gets a file from another node according to the input hash.
     * @param v The view that fired this event.
     */
    
    public final void getButtonClicked(final View v) {

        /* Store the input string */
        EditText editText = (EditText) findViewById(R.id.hash_field);
        String hash = editText.getText().toString();

        if (hash.length() != 3) {
            Toast.makeText(getApplicationContext(),
                    "Only three characters are allowed!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new get request with the current hash
        Log.d(TAG, "Requesting the following hash: " + hash.substring(0,3));
        
        NetInfRequest getRequest = new NetInfRequest(
                this, HOST, PORT,
                NetInfRequest.RequestType.GET, HASH_ALG, hash.substring(0,3));

        // Execute request
        getRequest.execute();

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

    /**
     * Creates an intent to select an image from the gallery.
     * @param v The view that fired this event.
     */
    // TODO: Deprecated? Although I think it is better opening image/* for now
    public final void publishButtonClicked(final View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);                      
    }

    /**
     * Publish a file from the image gallery on the phone.
     * Creates the hash and extracts the content type. 
     */
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
        }
        else if (data.getScheme().equals("file")) {
            Uri selectedImage = data.getData();
            filePath = selectedImage.getPath();
        }
        
        Log.d(TAG, filePath);

        // Open file
        File file = new File(filePath);

        if (file.exists()) {            
            /* Help class for files, extract content type */
            String contentType = LisaFileHandler.getFileContentType(filePath);

            /* Help class for files, generate the hash */
            LisaHash lisaHash = null;
            String hash = null;
            
            // Try to hash the file
            try {
                lisaHash = new LisaHash(FileUtils.readFileToByteArray(file));
                hash = lisaHash.encodeResult(3); // Use 0 for using the whole hash 
                Log.d(TAG, "The generated hash is: " + hash);
            }
            catch (IOException e1) {
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
            }
            catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                Log.d(TAG, "File not found! Check if something went wrong when choosing file");
                e1.printStackTrace();
            }

            // Try copying file to shared folder
            try {
                try {
                    IOUtils.copy(in, out);
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "Failed to copy file to shared folder");
                    e.printStackTrace();
                }
            }
            finally {
                Log.d(TAG, "Closing file streams for transfer");
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                Log.d(TAG, "File streams for transfer closed");
            }

            // Create meta data
            LisaMetadata lisaMetaData = new LisaMetadata();

            // Metadata has 3 fields: filesize, filename and filetype
            lisaMetaData.insert("filesize", String.valueOf(file.length()));
            lisaMetaData.insert("filename", file.getName());
            lisaMetaData.insert("filetype", LisaFileHandler.getFileContentType(filePath));

            // Convert metadata into readable format
            String metaData = lisaMetaData.convertToString();

            // TODO: Remove this hack! Talk to other team about the metadata storage on their side
            metaData = lisaMetaData.remove_brackets(metaData);

            // Log the metadata
            Log.d(TAG, "metadata: " + metaData);

            // Publish!
            Log.d(TAG, "Trying to publish a new file.");
            NetInfRequest publishRequest = 
                    new NetInfRequest(this, HOST, PORT,
                            NetInfRequest.RequestType.PUBLISH, HASH_ALG, hash.substring(0,3));

            // Execute the publish
//            try {
                publishRequest.execute(new String[] {contentType, ""});
                        //URLEncoder.encode(metaData, "UTF-8")});
//            }
//            catch (UnsupportedEncodingException e) {
//                // TODO Auto-generated catch block
//                Log.d(TAG, "Error encoding");
//                e.printStackTrace();
//            }
        }                       
    }

    /**
     * Initiates and starts the Bluetooth Server.
     */

    private void setupBluetoothServer() {
        mBluetoothServer = new BluetoothServer();
        mBluetoothServer.start();
    }
}
