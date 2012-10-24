package project.cs.lisa.application;

import java.io.File;

import project.cs.lisa.R;
import project.cs.lisa.application.http.NetInfRequest;
import project.cs.lisa.netinf.node.LisaStarterNodeThread;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DemoSprint2Activity extends Activity {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        mApplication = (MainApplication) getApplication();
        setupBroadcastReceiver();
        setupNode();
        setContentView(R.layout.activity_demo_sprint2);
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

        /* Create a new get request with the current hash */
        Log.d(TAG, "Requesting the following hash: " + hash.substring(0,3));
        NetInfRequest getRequest = new NetInfRequest(
                this, HOST, PORT,
                NetInfRequest.RequestType.GET, HASH_ALG, hash.substring(0,3));
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
        
        /* Get the file path of the selected image. */
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(
                           selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        
        /* From File Manager */
        if (filePath == null) {
            filePath = selectedImage.getPath();            
        }

        File file = new File(filePath);
        if (file.exists()) {

            /* Extract here the content type */
            String contentType = "";
            
            /* Create hash */
            String hash = "";
            
            /* Create metadata */
            String metaData = "";
            
            /* Publish! */
            Log.d(TAG, "Trying to publish a new file.");
            NetInfRequest publishRequest = new NetInfRequest(
                    this, HOST, PORT,
                    NetInfRequest.RequestType.PUBLISH, HASH_ALG, hash.substring(0,3));
            publishRequest.execute(new String[] {contentType, metaData});

        }                       
    }

}