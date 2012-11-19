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

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiHandler {
    //constants
    public static final int WEP = 1;
    public static final int WAP = 2;
    public static final int OPEN_NETWORK = 3;

    public static final String TAG = "LISA_Network";

    /** dfsdfsdfsdf. */
    WifiConfiguration wifiConf;				/* WifiConfiguration object */

    /** dfsdfsdfsdf. */
    WifiManager wifiMgr;							/* WifiManager object */

    /** dfsdfsdfsdf. */
    WifiInfo wifiInfo;								/* WifiInfo object */
    
    /** dfsdfsdfsdf. */
    List<ScanResult> wifiScan;				/* List of ScanResult objects */

    /**
     * Constructor initializes WifiManager and WifiInfo.
     * @param context
     */
    public WifiHandler(Context context) {
        wifiMgr  = getWifiManager(context);		// gets wifiMgr in the current context 
        wifiInfo = getWifiInfo(context);			// gets wifiInfo in the current context
        wifiConf = getWifiConf(context);			// gets wifiConf in the current context
        wifiScan = getWifiInRange();					// gets wifiScan in the current context
    }

    /**
     * Function checkWifiEnabled checks if the WiFi connection
     * is enabled on the device. 
     * @param wifiMgr
     * @return true  if the WiFi connection is enabled,
     * 				 false if the WiFi connection is disabled
     */
    public boolean checkWifiEnabled() {
        // checks if WiFi is enabled
        return (wifiMgr != null && wifiMgr.isWifiEnabled());
    }

    /**
     * Function enableWifi enables WiFi connection on the device.
     * @param wifiMgr
     * @return true  if the attempt to enable WiFi succeeded,
     * 				 false if the attempt to enable WiFi failed. 
     */
    public boolean enableWifi() {
        // enables WiFi connection
        return wifiMgr.setWifiEnabled(true);
    }

    /**
     * Function disableWifi disables WiFi connection on the device.
     * @param wifiMgr
     * @return true  if WiFi connection was disabled,
     * 				 false if attempt to disable WiFi failed.
     */
    public boolean disableWifi() {
        // disables WiFi connection
        return wifiMgr.setWifiEnabled(false);
    }

    /**
     * Function getWifiManager gets the WiFiManager object from the device.
     * @param context
     * @return WifiManager object. Also sets the class variable
     * 				 wifiMgr as the WifiManager object returned.
     */
    public WifiManager getWifiManager(Context context) {
        WifiManager wifiMgr = null;

        // gets WifiManager obj from the system
        wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr == null) {
            Log.d("TAG", "WIFI_SERVICE is the wrong service name.");
        }

        return wifiMgr;
    }

    /**
     * Function getWifiInfo gets the current WiFi connection information in a
     * WifiInfo object from the device.
     * @param context
     * @return wifiInfo created object or
     * 				 null 		if wifi is not enabled.
     */
    public WifiInfo getWifiInfo(Context context) {
        WifiInfo wifiInfo = null;

        // gets WiFi network info of the current connection
        if (checkWifiEnabled()) {
            wifiInfo = (WifiInfo) wifiMgr.getConnectionInfo();	 
        }

        if (wifiInfo == null) {
            Log.d("TAG", "WifiInfo object is empty.");
        }

        return wifiInfo;
    }

    /**
     * Function that returns a WifiConfiguration object from the WifiInfo
     * object from the class. If wifiInfo exists, then we are able to retrieve
     * information from the current connection 
     * @param context
     * @return WifiConfiguration object created.
     */
    public WifiConfiguration getWifiConf(Context context) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        if (wifiInfo == null) {
            Log.d("TAG", "WifiInfo object is empty");
            return null;
        }

        wifiConfiguration.SSID = wifiInfo.getSSID();
        wifiConfiguration.networkId = wifiInfo.getNetworkId();

        return wifiConfiguration;
    }

    /**
     * Creates a new WifiConfiguration object for wifiConf.
     */
    public void clearWifiConfig() {
        wifiConf = new WifiConfiguration();
    }

    /**
     * Function getWifiInRange returns all the WiFi networks that are
     * accessible through the access point (device AP) found during the
     * last scan.
     * @param wifi
     * @return List of ScanResult containing information on all WiFi networks
     * 				 discovered in the range.
     */
    public List<ScanResult> getWifiInRange() {
        // gets ~last~ list of WiFi networks accessible through the access point.
        return (wifiScan = (List<ScanResult>) wifiMgr.getScanResults());
    }

    /**
     * Function that scans for wifi networks available in the devices range.
     * @return true  if scan started
     * 				 false if scan could not be started 
     */
    public boolean scanWifiInRange() {
        if (!checkWifiEnabled()) {
            return false;
        }

        if (!wifiMgr.startScan()) {
            Log.d("TAG", "Failed to scan wifi's in range.");
            return false;
        }

        return true;
    }

    /**
     * Function to disconnect from the currently connected WiFi AP.
     * @return true  if disconnection succeeded
     * 				 false if disconnection failed
     */
    public boolean disconnectFromWifi() {
        return (wifiMgr.disconnect());
    }

    /**
     * Function to connect to a selected network
     * @param networkSSID         network SSID name
     * @param	networkPassword     network password
     * @param networkId           network ID from WifiManager
     * @param SecurityProtocol    network security protocol
     * @return true  if connection to selected network succeeded
     * 				 false if connection to selected network failed
     */
    public boolean connectToSelectedNetwork(String networkSSID, String networkPassword) {
        int networkId;
        int SecurityProtocol = WEP;

        // Clear wifi configuration variable
        clearWifiConfig();

        // Sets network SSID name on wifiConf 
        wifiConf.SSID = "\"" + networkSSID + "\"";
        Log.d(TAG, "SSID Received: " + wifiConf.SSID);
        switch(SecurityProtocol) {
            // WEP "security".
            case WEP:
                wifiConf.wepKeys[0] = "\"" + networkPassword + "\""; 
                wifiConf.wepTxKeyIndex = 0;
                wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                break;

                // WAP security. We have to set preSharedKey.
            case WAP:
                wifiConf.preSharedKey = "\""+ networkPassword +"\"";
                break;

                // Network without security.
            case OPEN_NETWORK:
                wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
        }

        // Add WiFi configuration to list of recognizable networks
        if ((networkId = wifiMgr.addNetwork(wifiConf)) == -1) {
            Log.d("TAG", "Failed to add network configuration!");
            return false;
        }

        // Disconnect from current WiFi connection
        if (!disconnectFromWifi()) {
            Log.d("TAG", "Failed to disconnect from network!");
            return false;
        }

        // Enable network to be connected
        if (!wifiMgr.enableNetwork(networkId, true)) {
            Log.d("TAG", "Failed to enable network!");
            return false;
        }

        // Connect to network
        if (!wifiMgr.reconnect()) {
            Log.d("TAG", "Failed to connect!");
            return false;
        }

        return true;
    }
}
