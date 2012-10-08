package com.example.connection;

import com.example.demosprint1.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

/**
 * HttpGetActivity allows you to send a HTTP GET and display the response.
 * @author Linus Sunde
 */
public class HttpActivity extends Activity {
	
	/** Debug Log Tag. **/
	private static final String TAG = "HttpActivity";
	
//	/** Receiver. **/
//	private BroadcastReceiver mReceiver;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        
//        mReceiver = new BroadcastReceiver() {
//        	@Override
//        	public void onReceive(Context context, Intent intent) {
//        		Log.d(TAG, "onReceive()");
//        		if (intent.getAction().equals(HttpGetTask.GET_RECEIVED)) {
////        			setLog("Received!");
//        		}
//        	}
//        };
//        registerReceiver(mReceiver, new IntentFilter(HttpGetTask.GET_RECEIVED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * Callback method for the buttons (or other views) used by this activity.
     * @param view		The pressed view.
     */
    public void onClick(View view) {
    	Log.d(TAG, "onClick()");
    	switch (view.getId()) {
    		case R.id.buttonGet:
    			sendHttpGet();
    			break;
    		case R.id.buttonClear:
    			clearLog();
    			break;
    		default:
    			Log.d(TAG, "Error: Unknown view clicked.");
    	}
    }
    
    /**
     * Sends an HTTP GET using the view values.
     */
    private void sendHttpGet() {
    	Log.d(TAG, "sendHttpGet()");
    	
    	EditText textHost = (EditText) findViewById(R.id.textHost);
    	EditText textPort = (EditText) findViewById(R.id.textPort);
    	EditText textMessage = (EditText) findViewById(R.id.textMessage);
    	
    	String host = textHost.getText().toString();
    	int port = Integer.parseInt((textPort.getText().toString()));
    	String message = textMessage.getText().toString();
    	
    	HttpGetTask get = new HttpGetTask(this, host, port, message);
    	
    	get.execute();
    }
    
    /**
     * Clear the content of the log EditText.
     */
    private void clearLog() {
    	Log.d(TAG, "clearLog()");
    	EditText log = (EditText) findViewById(R.id.textLog);
    	log.setText("");
    }
    
//    /**
//     * Set the content of the log EditText.
//     * @param text		The text to set
//     */
//    private void setLog(String text) {
//    	Log.d(TAG, "setLog()");
//    	EditText log = (EditText) findViewById(R.id.textLog);
//    	log.setText(text);
//    }
    
}