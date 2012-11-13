package project.cs.lisa.networksettings;

import project.cs.lisa.R;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ToggleButton;

/**
 * Class for handling general BT connection
 * TODO: Work in progress for Sprint 3, maybe? 
 * @author Thiago Costa Porto
 */

public class BTHandler {
    private final String TAG = "LisaBTHandler";
    private BluetoothAdapter mBluetoothAdapter;
    private IntentFilter mIntentFilter;
    
    public BTHandler() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mIntentFilter = new IntentFilter();
    }

    public boolean isEnabled() {
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)
            return true;
        return false;
    }

    public boolean discoverBTDevices(Context context) {
        if (!isEnabled()) {
            Log.d(TAG, "BT is disabled!");
            return false;
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        context.startActivity(discoverableIntent);
        return true;
    }

    public boolean forceEnable(Context context) {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
            //mBluetoothAdapter.enable();
            mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(mReceive, mIntentFilter);
            Intent startBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(startBluetoothIntent);
        }
        
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            context.startActivity(discoverableIntent);
        }
        
        return true;
    }
    
    private final BroadcastReceiver mReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.d(TAG, "BT Action State Changed!");
                if (mBluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "BT Enabled");
                    Log.d(TAG, "Checking BT Discoverable");
                    if (mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Log.d(TAG, "BT Discoverable");
                    }
                    else {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                        context.startActivity(discoverableIntent);
                        Log.d(TAG, "BT is NOT Discoverable yet");
                    }
                }
            }
        }
    };
}
