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

package project.cs.lisa.wifi;

import java.util.ArrayList;
import java.util.List;

import project.cs.lisa.application.MainNetInfActivity;
import project.cs.lisa.application.WifiDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Singleton for performing a Bluetooth discovery task.
 * 
 * @author Paolo Boschini
 * @author Kim-Anh Tran
 */
public class WifiDiscovery {

    /** The constant timeout for the Bluetooth discovery task. 
    private static final int TIMEOUT = 10000; */

    /** The Debug TAG for this Activity. */
    private static final String TAG = "WifiDiscovery";

    /** The filter for choosing what actions the broadcast receiver will catch. */
    private IntentFilter mIntentFilter;

    /** The list that contains the discovered wifi networks. */
    private List<ScanResult> wifiScannedNetworks;
    
    private ProgressDialog progressBar;

    WifiManager wifiManager;

    public WifiDiscovery() {
        wifiManager = (WifiManager) MainNetInfActivity.getActivity().getSystemService(Context.WIFI_SERVICE); 
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    public void startDiscovery() {

        setUpProgressDialog();

        // when the wifi scanning is done, call onReceive in mReceiver
        IntentFilter iFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        MainNetInfActivity.getActivity().registerReceiver(mReceiver, iFilter);

        wifiManager.startScan();
    }

    private void setUpProgressDialog() {
        // Start ProgressDialog for discovering wifi networks
        progressBar = new ProgressDialog(MainNetInfActivity.getActivity());
        progressBar.setCancelable(true);
        progressBar.setMessage("Scanning wifi networks...");
        progressBar.show();
    }

    //    private BluetoothDiscovery() {
    //
    //        // Setup a broadcast receiver for being notified when a new device was found
    //        setUpBroadcastReceiver();
    //        mIntentFilter = new IntentFilter();
    //        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
    //        MainApplication.getAppContext().registerReceiver(mBroadcastReceiver, mIntentFilter);
    //
    //        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //    }
    //
    //    /**
    //     * Returns a list of available Bluetooth devices.
    //     * It performs a discovery within a fixed timeout of TIMEOUT seconds.
    //     * Why? Well, because there is no sense
    //     * trying to discover Bluetooth devices for a longer time.
    //     * 
    //     * @return  The list of available devices represented by MAC:addresses.
    //     */
    //    public synchronized List<String> startBluetoothDiscovery() {
    //        Log.d(TAG, "Start bluetooth discovery.");
    //
    //        // Initiate a new list every time we start a discovery 
    //        mAvailableDevices = new ArrayList<String>();
    //
    //        mBluetoothAdapter.startDiscovery();
    //
    //        // Wait for the discover to finish within n seconds 
    //        try {
    //            Thread.sleep(TIMEOUT);
    //        } catch (InterruptedException e) {
    //            Log.e(TAG, "Timeout sleep was interrupted.");
    //        }
    //
    //        // Discovery done, cancel it
    //        mBluetoothAdapter.cancelDiscovery();
    //
    //        Log.d(TAG, "Bluetooth discovery is finished.");
    //
    //        return mAvailableDevices;
    //    }

    /**
     * Broadcast Receiver mReceive that handles with WIFI 'signal' changes.
     * This is used to populate the device list as well as altering the text
     * from the variables.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // this is called when the wifi discovery is done
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                Log.d(TAG, "Finished scanning");
                progressBar.dismiss();
                
                List<String> wifis = new ArrayList<String>();
                for (ScanResult scanResult : wifiManager.getScanResults()) {
                    Log.d(TAG, scanResult.SSID);
                    wifis.add(scanResult.SSID);
                }
                
                (new WifiDialog(wifis)).show(MainNetInfActivity.getActivity().getFragmentManager(), "");
            }
        }
    };
}