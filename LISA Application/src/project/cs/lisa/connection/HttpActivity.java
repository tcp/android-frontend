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

package project.cs.lisa.connection;

import project.cs.lisa.R;
import project.cs.lisa.networksettings.LisaNetworkSettings;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * HttpGetActivity allows you to send a HTTP GET and display the response.
 * @author Linus Sunde
 */
public class HttpActivity extends Activity {
	
	/** Prefix that is concatenated to the String that is to the server **/
	private final String PREFIX = "?io=";
	
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
    	String message = PREFIX + textMessage.getText().toString();
    	
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
    
    public void onClickWifi(View view) {
    	Button wifi_button = (Button) findViewById(R.id.button1);
    	wifi_button.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			//setContentView(R.layout.activity_wifi);
    		}
    	});
    	Intent intent = new Intent(this, LisaNetworkSettings.class);
    	startActivity(intent);
    }
    
}
