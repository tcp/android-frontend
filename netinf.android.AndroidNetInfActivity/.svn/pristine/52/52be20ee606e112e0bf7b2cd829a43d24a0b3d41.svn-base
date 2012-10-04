/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package netinf.android;

import java.util.ArrayList;

import netinf.android.wifi.NetInfWifiDirectManager;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

/** 
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class NetInfBroadcastReceiver extends BroadcastReceiver {

    // Debugging
    private static final String TAG = "NetInfBroadcastReceiver";
	
    private WifiP2pManager manager;
    private Channel channel;

    NetInfWifiDirectManager mNetInfManager;
    
    ArrayList<String> bluetoothDiscoveredPeers;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public NetInfBroadcastReceiver(WifiP2pManager manager, Channel channel, NetInfWifiDirectManager netinfManager) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.mNetInfManager = netinfManager;
        bluetoothDiscoveredPeers = new ArrayList<String>();
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
            	mNetInfManager.setIsWifiP2pEnabled(true);
            } else {
            	mNetInfManager.setIsWifiP2pEnabled(false);
            }
            Log.d(TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, mNetInfManager);
            }
            Log.d(TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, mNetInfManager);
            } else {
                // It's a disconnect
            	Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION - disconnect");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        	Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        	Log.d(TAG, "BluetoothDevice.ACTION_FOUND");
        	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        	bluetoothDiscoveredPeers.add(device.getAddress());
        	Log.d(TAG, "Bluetooth Device Found = " + device.getName() + " " + device.getAddress());
        	Log.d(TAG, "bluetoothDiscoveredPeers = " + bluetoothDiscoveredPeers.toString()); 
        	       	
        }
    }
    
    public ArrayList<String> getBluetoothDiscoveredDevices(){
    	return bluetoothDiscoveredPeers;
    }
    
    public void clearBluetoothDiscoveredDevices(){
    	bluetoothDiscoveredPeers.clear();
    }
}
