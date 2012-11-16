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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import project.cs.lisa.application.MainNetInfActivity;
import project.cs.lisa.application.ListDialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Singleton for performing a Bluetooth discovery task.
 * 
 * @author Paolo Boschini
 * @author Kim-Anh Tran
 */
public class WifiHandler {

    /** The constant timeout for the Bluetooth discovery task. 
    private static final int TIMEOUT = 10000; */

    /** The Debug TAG for this Activity. */
    private static final String TAG = "WifiHandler";

    /** The filter for choosing what actions the broadcast receiver will catch. */
    private IntentFilter mIntentFilter;

    /** The list that contains the discovered wifi networks. */
    private List<ScanResult> wifiScannedNetworks;

    private ProgressDialog progressBar;

    WifiManager wifiManager;

    private String currentChosenNetwork;

    public WifiHandler() {
        wifiManager = (WifiManager) MainNetInfActivity.getActivity().getSystemService(Context.WIFI_SERVICE); 
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        progressBar = new ProgressDialog(MainNetInfActivity.getActivity());
        setUpBroadcastReceiver();
    }

    public void startDiscovery() {

        showProgressDialog("Scanning wifi networks...");

        // when the wifi scanning is done, call onReceive in mReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        MainNetInfActivity.getActivity().registerReceiver(mReceiver, filter);


        wifiManager.startScan();
    }

    private void showProgressDialog(String message) {
        // Start ProgressDialog for discovering wifi networks
        progressBar.setCancelable(true);
        progressBar.setMessage(message);
        progressBar.show();
    }

    public void connectToSelectedNetwork(String networkSSID) {
        Log.d(TAG, "Yeah, let's try to connect to " + networkSSID);

        showProgressDialog("Try to connect to " + networkSSID);

        currentChosenNetwork = networkSSID;

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int networkId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
    }

    public void onDiscoveryDone(Set<String> wifis) {
        Log.d(TAG, "onDiscoveryDone");
    }


    BroadcastReceiver mReceiver;
    /**
     * Broadcast Receiver mReceive that handles with WIFI 'signal' changes.
     * This is used to populate the device list as well as altering the text
     * from the variables.
     */
    private void setUpBroadcastReceiver() {
        Log.d(TAG, "Set up broadcast receiver.");

        mReceiver = new BroadcastReceiver() {

            private boolean doneScanning = false;

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                Log.d(TAG, action.toString());

                // this is called when the wifi discovery is done
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {

                    /*
                     *  Prevent the scanning to show the wifi dialog several time.
                     *  Android scans wifi networks continuously.
                     */
                    if (doneScanning) {
                        return;
                    }

                    doneScanning = true;

                    Log.d(TAG, "Finished scanning");

                    progressBar.dismiss();

                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    Set<String> wifis = new HashSet<String>();
                    for (ScanResult scanResult : scanResults) {
                        wifis.add(scanResult.SSID);
                    }

                    Log.d(TAG, wifis.toString());

                    onDiscoveryDone(wifis);
                }


                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {

                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String chosenNetWork = wifiInfo.getSSID();
                    SupplicantState state = wifiInfo.getSupplicantState();

                    Log.d(TAG, "state: " + state);
                    Log.d(TAG, "chosenNetwork: " + chosenNetWork);

                    if (chosenNetWork.equals(currentChosenNetwork) && state == SupplicantState.COMPLETED) {
                        new AlertDialog.Builder(MainNetInfActivity.getActivity())
                        .setMessage("You have been successfully connected to " + currentChosenNetwork)
                        .setTitle("Wifi message")
                        .setCancelable(true)
                        .setNeutralButton("OK", null)
                        .show();
                        
                        progressBar.dismiss();
                    }

                    if (doneScanning) {
                        MainNetInfActivity.getActivity().unregisterReceiver(mReceiver);
                    }
                }
            }
        };
    }
}