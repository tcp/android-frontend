package project.cs.lisa.application;

import java.io.File;

import project.cs.lisa.R;
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

    /** Please comment. */
    public static final String NODE_STARTED = "project.cs.list.node.started";

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

        EditText editText = (EditText) findViewById(R.id.hash_field);
        String hash = editText.getText().toString();

        if (hash.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Fill in the hash filed please!", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Send the hash here, and get back the file path and the content type */
        String filePath = "";
        String contentType = "";

        /* Display the file according to the file type. */
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);

        /* Replace image/* with contentType */
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        startActivity(intent);
    }

    /**
     * Creates an intent to select an image from the gallery.
     * @param v The view that fired this event.
     */
    public final void pushButtonClicked(final View v) {
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
            /* Create hash */
            /* Publish! */
            Log.d(TAG, "Publishing...");
        }                       
    }

}