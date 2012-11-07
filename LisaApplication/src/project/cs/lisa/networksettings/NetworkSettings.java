/**
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
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */
package project.cs.lisa.networksettings;

import java.util.ListIterator;

import project.cs.lisa.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Wifi connection settings activity
 * @author Thiago Costa Porto
 */

public class NetworkSettings extends Activity {
    // Debug Tag
    private static final String TAG = "WiFiDeviceList";

    // Adapters and TextView
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mDiscoveredWifiAdapter;
    private TextView mConnectedWifiTextView;

    // Intent Filter
    private IntentFilter iFilter;

    // Wifi Handler
    private WifiHandler wifi;

    // vars
    private boolean scanning = false;
    private boolean listening = false;

    // note: mReceive is defined at the end of the class.
    // note: onItemClickListener is defined after this class.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up window
        setContentView(R.layout.show_networks);

        // init wifi
        wifi = new WifiHandler(this);

        // Activity result
        setResult(Activity.RESULT_CANCELED);

        // set up scan button
        Button scanButton = (Button) findViewById(R.id.wifi_scan_button);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Scan for Wifi networks
                doWifiScan();
                //v.setVisibility(View.GONE);
            }
        });

        // initializing adapters
        Log.d(TAG, "Setting up Adapters");
        mDiscoveredWifiAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // listview for connected wifi network
        mConnectedWifiTextView = (TextView) findViewById(R.id.textView1);
        if (wifi.wifiInfo != null && wifi.wifiInfo.getSSID() != null) {
            mConnectedWifiTextView.setText(wifi.wifiInfo.getSSID());
        }

        // listview for discovered wifi networks
        ListView discoveredWifiListView = (ListView) findViewById(R.id.discoveredWifi);
        discoveredWifiListView.setAdapter(mDiscoveredWifiAdapter);
        discoveredWifiListView.setOnItemClickListener(mDeviceClickListener);
        Log.d(TAG, "Adapters set");

        // set up toggle buttons
        checkInitialWifiToggle();
        checkInitialBTToggle();
        Log.d(TAG, "Buttons set");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If any broadcast listeners has been set, unregister
        if (listening) {
            unregisterReceiver(mReceive);
        }
        Log.d(TAG, "onDestroy");
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position_of_view, long row_id) {
            // Using row_id, we can use wifiScan and get the BSSID that
            // we are supposed to connect to
            Log.d(TAG, "Row clicked: " + row_id);

            // Connect to wifi network
            wifi.connectToSelectedNetwork(((TextView) v).getText().toString(), "");

            // Set up intent for connectivity            
            iFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mReceive, iFilter);

            // Set listening variable
            listening = true;
        }
    };

    /**
     * doWifiScan begins the search for WiFi networks in range. It also raises
     * the necessary intent for listening for the end of scan, so we can process
     * the list of devices found.
     */
    public void doWifiScan() {
        // Register for broadcasts when WiFi is scanned and results are available
        // read note on mReceive at the top.
        Log.d(TAG, "Setting up intentfilter");
        iFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(mReceive, iFilter);
        listening = true;
        Log.d(TAG, "Intentfilter registered");
        Log.d(TAG, "Listening for Scan Results");
        if (!scanning) {
            wifi.scanWifiInRange();
            scanning = true;
            Log.d(TAG, "Scanning for WiFi networks");
        }
    }

    /**
     * Function to check the initial state of the Toggle Button
     */
    public void checkInitialWifiToggle() {
        // Toggle Button
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);

        // Checks if WiFi is enabled and sets button accordingly.
        if (wifi.checkWifiEnabled()) {
            Log.d(TAG, "Wifi is enabled");
            toggleButton.setChecked(true);
        }
        else {
            Log.d(TAG, "Wifi is disabled");
            toggleButton.setChecked(false);
        }
    }

    public void checkInitialBTToggle() {
        // Toggle Button
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButtonBT);

        if (mBluetoothAdapter == null) {
            // Device does not support BlueTooth
            Log.d(TAG, "Device does not support Bluetooth");
            toggleButton.setChecked(false);
        } else {
            toggleButton.setChecked(true);
            Log.d(TAG, "Bluetooth is enabled");
            if (!mBluetoothAdapter.isEnabled()) {
                // BlueTooth is not enable :)
                Log.d(TAG, "Bluetooth is disabled");
                toggleButton.setChecked(false);
            }
        }
    }

    /**
     * Function to handle the Toggle Button that allows the user to
     * set the WIFI state on or off. Have in mind that this changes
     * the whole state, meaning that switching off stops the WiFi
     * service for the whole phone. 
     * @param view
     * TODO: Remove all these debug features or move them elsewhere!
     */

    public void onToggleClicked(View view) {
        Log.d("Wifi Activity", "view value: " + view);
        Log.d("WiFi Activity", "onClick()");

        // Toggle Button
        boolean toggleOn = ((ToggleButton) view).isChecked();
        Log.d("WiFi Activity", "Toggle Response: " + toggleOn);

        if (toggleOn) {
            // Changes button if WiFi is disconnected
            if (!wifi.enableWifi()) {
                Log.d("WiFi Activity", "Failed to enable WiFi");
                ((ToggleButton) view).setChecked(false);
                mConnectedWifiTextView.setText("Not connected");
                mDiscoveredWifiAdapter.clear();
            }
            else {
                // WiFi connected but we have not accessed wifiInfo yet.
                Log.d("WiFi Activity", "WiFi enabled");
                ((ToggleButton) view).setChecked(true);
                mConnectedWifiTextView.setText("Not connected");
            }
        }
        else {
            if (!wifi.disableWifi()) {
                Log.d("WiFi Activity", "Failed to disable WiFi");
                ((ToggleButton) view).setChecked(true);
            }
            else {
                Log.d("WiFi Activity", "WiFi disabled");
                ((ToggleButton) view).setChecked(false);
                mConnectedWifiTextView.setText("Not connected");
                mDiscoveredWifiAdapter.clear();
            }
        }
    }

    public void onToggleBTClicked(View view) {
        // Toggle Button
        boolean toggleOn = ((ToggleButton) view).isChecked();
        Log.d("WiFi Activity", "BT Toggle Response: " + toggleOn);

        // Loads BT
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (toggleOn) {
            if (mBluetoothAdapter.enable()) {
                Log.d(TAG, "Began BT startup");
                ((ToggleButton) view).setChecked(false);
                iFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mReceive, iFilter);
                Log.d(TAG, "Listening for BT state changes");
            }
            else {
                Log.d(TAG, "Unable to begin BT startup");
                ((ToggleButton) view).setChecked(false);
            }
        }
        else {
            if (mBluetoothAdapter.disable()) {
                Log.d(TAG, "Shutting down BT");
                ((ToggleButton) view).setChecked(false);
            }
            else {
                Log.d(TAG, "BT forced enabled");
                ((ToggleButton) view).setChecked(true);
            }
        }
    }

    /**
     * Broadcast Receiver mReceive that handles with WIFI 'signal' changes.
     * This is used to populate the device list as well as altering the text
     * from the variables.
     */
    private final BroadcastReceiver mReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Intent Action: " + action);

            // TODO: Switch case to handle more types
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                // State is now connected!
                Log.d(TAG, "Connectivity_Action received");

                // Refresh settings
                // TODO: Create refresh configs method in WifiHandler
                wifi = new WifiHandler(context);
                Log.d(TAG, "wifi.wifiInfo is " + wifi.wifiInfo);

                // Check if its connected
                if (wifi.wifiInfo != null &&
                        wifi.wifiInfo.getSupplicantState().toString().equals("COMPLETED")) {
                    Log.d(TAG, "mConnectedWifiTextView is " + mConnectedWifiTextView);
                    Log.d(TAG, "wifi.wifiInfo is " + wifi.wifiInfo);
                    // update the connected network text
                    mConnectedWifiTextView = (TextView) findViewById(R.id.textView1);
                    mConnectedWifiTextView.setText(wifi.wifiInfo.getSSID());
                    mDiscoveredWifiAdapter.clear();
                }
                else {
                    Log.d(TAG, "Still connecting");
                    mConnectedWifiTextView.setText("Connecting...");
                }
            }
            else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                if (scanning) {
                    Log.d(TAG, "Finished scanning");
                    // resets our scanning flag
                    scanning = false;

                    // sets wifiScan because search is done
                    wifi.wifiScan = wifi.getWifiInRange();
                    if (wifi.wifiScan == null)
                        Log.d(TAG, "Scan failed");

                    // debug: what is in wifiScan
                    Log.d(TAG, "wifiScan is " + wifi.wifiScan.toString());

                    // iterate over list of wifis
                    ListIterator<ScanResult> lit = wifi.wifiScan.listIterator();

                    // cleans adapter
                    mDiscoveredWifiAdapter.clear();

                    // populates adapter
                    while (lit.hasNext()) {
                        mDiscoveredWifiAdapter.add(lit.next().SSID);
                    }
                }
            }
            else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.d(TAG, "BT Action State Changed!");
                //Log.d(TAG, "" + action);
                if (mBluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Enabling BT");
                    ToggleButton toggleButtonBT = (ToggleButton) findViewById(R.id.toggleButtonBT);
                    if (mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        toggleButtonBT.setChecked(true);
                        Log.d(TAG, "BT Button should be enabled");
                    }
                    else {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                        startActivity(discoverableIntent);
                        toggleButtonBT.setText("Not Discoverable");
                        Log.d(TAG, "BT is not discoverable!");
                    }
                }
                else {
                    Log.d(TAG, "Disabling BT");
                    ToggleButton toggleButtonBT = (ToggleButton) findViewById(R.id.toggleButtonBT);
                    toggleButtonBT.setChecked(false);
                    Log.d(TAG, "BT Button should be disabled");
                }
            }
        }
    };
}
