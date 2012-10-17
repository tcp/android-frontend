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
package project.cs.lisa.wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import project.cs.lisa.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class WifiActivity extends Activity {

    private TextView mTextView;
    private WifiHandler wifi;
    private ListView lv;
    private int size = 0;
    private List<ScanResult> results;
    private String ITEM_KEY = "key";
    private ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    private ArrayAdapter<String> adapter;
    String[] networkNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WiFi Activity", "onCreate");
        setContentView(R.layout.activity_wifi);
        wifi = new WifiHandler(this);
        checkInitialToggle();
        //WifiReceiver receiverWifi = (WifiReceiver) new WifiReceiver(); 
        //registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        displayCheckWifi();
        Intent intent1 = new Intent(this, WifiDeviceList.class);
        startActivity(intent1);
    }

    public void onStart(Bundle savedInstanceState) {
        checkInitialToggle();
        displayCheckWifi();
        Log.d("WiFi Activity", "onStart");
    }

    /**
     * Checks whether the WiFi is connected and sets the toggle button
     * accordingly. 
     */

    public void checkInitialToggle() {
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.switch1);
        if (wifi.checkWifiEnabled()) {
            toggleButton.setChecked(true);
        }
        else {
            toggleButton.setChecked(false);
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

        boolean toggleOn = ((ToggleButton) view).isChecked();
        Log.d("WiFi Activity", "Toggle Response: " + toggleOn);

        if (toggleOn) {
            if (!wifi.enableWifi()) {
                Log.d("WiFi Activity", "Failed to enable WiFi");
                ((ToggleButton) view).setChecked(false);
            }
            else {
                Log.d("WiFi Activity", "WiFi enabled");
                ((ToggleButton) view).setChecked(true);
                Intent intent1 = new Intent(this, WifiDeviceList.class);
                startActivity(intent1);
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
            }
        }
    }

    public void displayCheckWifi() {
        mTextView = (TextView) findViewById(R.id.textView2);
        if (wifi.wifiInfo != null) {
            mTextView.setText(wifi.wifiInfo.getSSID());
        }
        else {
            mTextView.setText("Not connected");
        }
    }

    public void displayNetworks() {
        Dialog dialog = new Dialog(this);
        //dialog.setTitle("Available Networks");
        dialog.setContentView(R.layout.show_networks);
        List<ScanResult> listScan = wifi.wifiScan;
        //lv = (ListView) findViewById(R.id.listView1);
        if (listScan == null) {
            Log.d("WiFi Activity", "List of Scan is nully");
        }
        else {
            //mTextView = (TextView) findViewById(R.id.showtextView1);
            //mTextView.setText("Boobs");
            //mTextView.setText(listScan.toString());
            listScan.clear();
        }
    }

    /**
     * 
     * @author tcp
     *
     */
    @SuppressLint("UseValueOf")
    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            mTextView = (TextView) findViewById(R.id.textView3);
            StringBuilder sb = new StringBuilder();
            wifi.getWifiInRange();
            List<ScanResult> wifiList = wifi.wifiScan;
            mTextView.setText("Size is: " + wifiList.size() + "\n");
            for(int i = 0; i < wifiList.size(); i++){
                sb.append(new Integer(i+1).toString() + ".");
                sb.append((wifiList.get(i)).toString());
                sb.append("\n");
            }
            mTextView.append(sb);
            displayNetworks();
        }
    }
}
