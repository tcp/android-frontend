package project.cs.lisa.application;

import project.cs.lisa.R;
import project.cs.lisa.application.http.LisaGetTask;
import project.cs.lisa.bluetooth.BluetoothServer;
import project.cs.lisa.netinf.node.LisaStarterNodeThread;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static final String TAG = "MainActivity";
	public static final String NODE_STARTED = "project.cs.list.node.started";
	
	TextView mTextView;
	private MainApplication mApplication;
	private LisaStarterNodeThread mStarterNodeThread;
	
	/**
	 * The Server listening for incoming Bluetooth requests.
	 */
	private BluetoothServer mBluetoothServer;
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        mApplication = (MainApplication) getApplication();
        
        setupBroadcastReceiver();
        setupNode();
        setupBluetoothServer();
        
        setContentView(R.layout.activity_temp);
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
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
    
    public void onClick(View view) {
    	switch (view.getId()) {
    		case R.id.button1:
    			Log.d(TAG, "View: button1");
    			LisaGetTask getTask = new LisaGetTask(this, "localhost", 8080, LisaGetTask.MessageType.PUBLISH, "sha-256", "ABCDEFG");
    			getTask.execute(new String[] {"text/plain", "meta"});
    			break;
    		case R.id.button2:
    			Log.d(TAG, "View: button2");
    			LisaGetTask getTask2 = new LisaGetTask(this, "localhost", 8080, LisaGetTask.MessageType.GET, "sha-256", "ABCDEFG");
    			getTask2.execute();
    			break;
    		default:
    			Log.d(TAG, "Error: Unknown view clicked.");
    			break;
    	}
    }
    
    private void setupNode() {
    	// Start NetInfNode
    	mStarterNodeThread = new LisaStarterNodeThread(mApplication);
    	mStarterNodeThread.start();
    }
    
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	mBluetoothServer.cancel();
    }

    /**
     * Initiates and starts the Bluetooth Server.
     */
    private void setupBluetoothServer() {
    	mBluetoothServer = new BluetoothServer();
    	mBluetoothServer.start();
	}
    
}
