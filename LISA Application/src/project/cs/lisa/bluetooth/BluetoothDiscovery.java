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
package project.cs.lisa.bluetooth;

import java.util.ArrayList;
import java.util.List;

import project.cs.lisa.application.MainApplication;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Singleton for performing a Bluetooth discovery task.
 * 
 * @author Paolo Boschini
 * @author Kim-Anh Tran
 */
public enum BluetoothDiscovery {

    /** The unique reference for this singleton. */
    INSTANCE;

    /** The constant timeout for the bluetooth discovery task. */
    private static final int TIMEOUT = 10000;

    /** The TAG for this Activity. */
    private static final String TAG = "BluetoothDiscovery";

    /** The Bluetooth adapter. */
    private BluetoothAdapter mBluetoothAdapter = null;

    /** A broadcast receiver for intercepting broadcast messages i.e. Bluetooth activity. */
    private BroadcastReceiver mBroadcastReceiver;

    /** The filter for choosing what actions the broadcast receiver will catch. */
    private IntentFilter mIntentFilter;

    /** The list that contains the discovered bluetooth devices. */
    private ArrayList<String> mAvailableDevices;

    /**
     * Initiates the singleton.
     */
    private BluetoothDiscovery() {

        /* Setup a broadcast receiver for being notified when a new device was found. */
        setUpBroadcastReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        MainApplication.getAppContext().registerReceiver(mBroadcastReceiver, mIntentFilter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Returns a list of available Bluetooth devices.
     * It performs a discovery within a fixed timeout of 10 seconds.
     * Why 10 seconds? Well, because there is no sense
     * trying to discover Bluetooth devices for a longer time.
     * 
     * @return  The list of available devices represented by MAC:addresses.
     */
    public synchronized List<String> startBluetoothDiscovery() {

        /* Initiate a new list every time we start a discovery */
        mAvailableDevices = new ArrayList<String>();

        mBluetoothAdapter.startDiscovery();

        /* Wait for the discover to finish within n seconds */
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            Log.e(TAG, "Timeout sleep was interrupted.");
        }

        /* Discovery done */
        mBluetoothAdapter.cancelDiscovery();

        return mAvailableDevices;
    }


    /**
     * Determines which actions are to be intercepted from android services,
     * for now intercept only Bluetooth discovery.
     */
    private void setUpBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    /* This checks the Bluetooth signal strength of the surrounding devices. */
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    Log.d(TAG, device.getName() + ", " + device.getAddress() + ", " + rssi + " dB");
                    
                    /* Updates the list of available devices. */
                    mAvailableDevices.add(device.getAddress());            
                }
            }
        };
    }
}