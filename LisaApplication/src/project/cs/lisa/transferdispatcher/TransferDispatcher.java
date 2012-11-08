/**
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors
 *       may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
package project.cs.lisa.transferdispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import project.cs.lisa.bluetooth.BluetoothDiscovery;
import project.cs.lisa.bluetooth.provider.BluetoothProvider;
import project.cs.lisa.bluetooth.provider.ByteArrayProvider;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import android.util.Log;

/**
 * The TransferDispatcher. Responsible for Providing stream to IOs/Files.
 * 
 * @author PG NetInf 3, University of Paderborn.
 * @author Paolo Boshini
 * @author Kim-Anh Tran
 * 
 * @pat.name Singleton.
 * @pat.task Forces that only one instance of this class exists.
 */
public enum TransferDispatcher {

    /** The Transfer Dispatcher instance. */
    INSTANCE;

    /** The debug tag. */
    private static final String TAG = "TransferDispatcher";

    /** The prefix for identifying bluetooth locator nodes. */
    private static final String BLUETOOTH_PREFIX = "nimacbt://";
    
    /** The list of available byte array providers. */
    private List<ByteArrayProvider> mByteArrayProviders;

    /**
     * Initializes the Transfer Dispatcher with
     * existing providers.
     */
    private TransferDispatcher() {
        addByteArrayProviders();
    }

    /** Adds available ByteArrayProviders to the TransferDispatcher. */   
    private void addByteArrayProviders() {
        mByteArrayProviders = new ArrayList<ByteArrayProvider>();
        mByteArrayProviders.add(new BluetoothProvider());
    }

    /**
     * Provides the stream by a given DO.
     * 
     * @param io The information object
     * @return Stream to the underlying BO.
     * @throws IOException	Thrown if no locator could be found
     */
    public byte[] getByteArray(InformationObject io) throws IOException {

        List<Attribute> locators = extractLocators(io);

        /* Try to discover available devices and only keep those
         * locators that are right now available via bluetooth.
         */
        List<String> availableFilteredBluetoothLocators = filterLocators(locators);
        
        byte[] resultArray;
        String hash = io.getIdentifier().getIdentifierLabel(
        		SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();

        // Tries to retrieve the BO from the first possible locator. 
        for (String currentLocator : availableFilteredBluetoothLocators) {

        	resultArray = getByteArray(currentLocator, hash);         
            if (resultArray != null) {
            	Log.d(TAG, "Received data from the following locator " + currentLocator);
            	
            	return resultArray;
            }
        }

        throw new IOException("No suitable locator could be found.");
    }

    /**
     * Returns a sublist of locators that are currently 
     * available via Bluetooth.
     * 
     * @param locators	The list of all locators that own the BO.
     * @return			The sublist of locators that are available
     * 					via bluetooth.
     */
    private List<String> filterLocators(List<Attribute> locators) {
    	Log.d(TAG, "Filter locators.");
    	
    	BluetoothDiscovery btDiscovery = BluetoothDiscovery.INSTANCE;
        List<String> availableLocators = btDiscovery.startBluetoothDiscovery();
       
        // Configure locator identifiers to have the bluetooth locator node prefix. 
        for (String btLocator : availableLocators) {
        	btLocator = BLUETOOTH_PREFIX + btLocator;
        }
        
        /* 
         * Converts the Attribute locators to String locators that represent
         * the MAC address of each locator.
         */
        List<String> stringLocators = new ArrayList<String>(locators.size());
        for (Attribute locator : locators) {
        	stringLocators.add(extractLocatorAddress(locator.getValue(String.class)));
        }
        
        // Keep only those locators that are available right now 
        stringLocators.retainAll(availableLocators);
        
        Log.d(TAG, "Filtered locator list: " + stringLocators.toString());
        
        return stringLocators;
    }

    /**
     * Returns the list of locators from a specified information object.
     * 
     * @param io    The information object that contains the list of locators
     * @return      The list of locators
     */
    private List<Attribute> extractLocators(InformationObject io) {
        List<Attribute> locators = io.getAttributesForPurpose(
        		DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose());
        return locators;
    }


    /**
     * Given a locator and a file hash, this method provides the byte array corresponding
     * to the hash.
     * 
     * @param locator
     *           The locator from where the file should be fetched
     * @param hash
     *           The hash of the file that will be fetched
     * @return The byte array corresponding to the hash of the file obtained from the address
     *         specified in the locator.
     */
    public byte[] getByteArray(String locator, String hash) {

        /*
         * At this point any service (bluetooth, wifi) could be used to
         * retrieve the BO. We should have some way to choose the best
         * service depending on the available type of locators,
         * go through the list of locators contained in the io,
         * and start the transfer of the BO.
         */

        Log.d(TAG, "Connecting to the following locator: " + locator);
        
        ByteArrayProvider provider = getByteArrayProvider(locator);
        if (provider != null) {
        	String locatorMacAddress = extractLocatorAddress(locator);
        	return provider.getByteArray(locatorMacAddress, hash);
        	
        } else {
        	Log.e(TAG, "No provider could be found.");
        	return null;
        }
    }   

	/**
     * Provides the appropriate ByteArrayProvider.
     * 
     * @param locator	The locator from where the file will be fetched
     * @return The specific ByteArrayProvider.
     */
    private ByteArrayProvider getByteArrayProvider(String locator) {
        for (ByteArrayProvider provider : mByteArrayProviders) {
            if (provider.canHandle(locator)) {
                Log.d(TAG, "Choosing the following provider: " + provider.describe());
                return provider;
            }
        }
        return null;
    }    
    
    /**
     * Extracts and returns the mac address of the locator from
     * the ni address.
     *
     * @param locator	The locator's ni address.
     * @return			The mac address
     */
    private String extractLocatorAddress(String locator) {
		String macAddress = locator.split("://")[1];
		Log.d(TAG, "The locator's mac address: " + macAddress);
		
		return macAddress;
	}
}
