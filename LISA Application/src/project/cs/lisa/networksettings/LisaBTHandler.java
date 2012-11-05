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

public class LisaBTHandler {
    private final String TAG = "LisaBTHandler";
    private BluetoothAdapter mBluetoothAdapter;
    private IntentFilter mIntentFilter;

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
            mBluetoothAdapter.enable();
            mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(mReceive, mIntentFilter);
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
