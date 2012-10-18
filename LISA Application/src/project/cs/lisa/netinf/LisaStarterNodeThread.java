package project.cs.lisa.netinf;

import netinf.node.access.AccessServer;
import netinf.node.api.NetInfNode;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionService;
import project.cs.lisa.MainActivity;
import project.cs.lisa.MainApplication;
import android.content.Intent;
import android.util.Log;

import com.google.inject.Provider;

public class LisaStarterNodeThread extends Thread {
	
	public static final String TAG = "StarterNodeThread";
	
	private NetInfNode mNode;
	private MainApplication mApplication;
	
	public LisaStarterNodeThread(MainApplication application) {
		mApplication = application;
	}
	
	@Override
	public void run() {
		mNode = mApplication.getInjector().getInstance(NetInfNode.class);
		
		startResolution();	// Start resolution services
		startAPIAccess();	// Start REST API service
//		startN2NAccess();	// Start Node2Node services
		
		Intent intent = new Intent();
		intent.setAction(MainActivity.NODE_STARTED);
		mApplication.sendBroadcast(intent);
	}

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
	
	private void startAPIAccess() {
		Log.d(TAG, "startAPIAccess()");
		AccessServer accessServer = mApplication.getInjector().getInstance(AccessServer.class);
		accessServer.start();
	}
	
	
	
}