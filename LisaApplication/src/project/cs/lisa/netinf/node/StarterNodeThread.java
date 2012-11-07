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
package project.cs.lisa.netinf.node;

import netinf.node.access.AccessServer;
import netinf.node.api.NetInfNode;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionService;
import project.cs.lisa.application.MainApplication;
import project.cs.lisa.application.MainNetInfActivity;
import android.content.Intent;
import android.util.Log;

public class StarterNodeThread extends Thread {
	
	public static final String TAG = "StarterNodeThread";
	
	private NetInfNode mNode;
	private MainApplication mApplication;
	
	public StarterNodeThread(MainApplication application) {
		mApplication = application;
	}
	
	@Override
	public void run() {
		mNode = mApplication.getInjector().getInstance(NetInfNode.class);
		
		startResolution();	// Start resolution services
		startAPIAccess();	// Start REST API service
//		startN2NAccess();	// Start Node2Node services
		
		Intent intent = new Intent();
		intent.setAction(MainNetInfActivity.NODE_STARTED_MESSAGE);
		mApplication.sendBroadcast(intent);
	}

	/**
	 * Begin all the resolution services
	 */
	private void startResolution() {
		Log.d(TAG, "startResolution()");
		Log.d(TAG, "getting resolution controller...");
		ResolutionController resolutionController = mNode.getResolutionController();
		
		if (resolutionController != null) {
	        // Plug in Resolution Services
			Log.d(TAG, "getting resolution services...");
	        ResolutionService[] resolutionServices = mApplication.getInjector().getInstance(ResolutionService[].class);
	
	        if (resolutionServices.length == 0) {
	           Log.d(TAG, "(NODE ) I have no active resolution services");
	        }
	
	        Log.d(TAG, "adding resolution services...");
	        for (ResolutionService resolutionService : resolutionServices) {
	           resolutionController.addResolutionService(resolutionService);
	           Log.d(TAG, "Added resolution service '" + resolutionService.getClass().getCanonicalName() + "'");
	           Log.d(TAG, "(NODE ) I can resolve via " + resolutionService.describe());
	        }
		}
	}
	
	/**
	 * Enable access to the RESTful services
	 */
	private void startAPIAccess() {
		Log.d(TAG, "startAPIAccess()");
		AccessServer accessServer = mApplication.getInjector().getInstance(AccessServer.class);
		accessServer.start();
	}
	
	
	
}