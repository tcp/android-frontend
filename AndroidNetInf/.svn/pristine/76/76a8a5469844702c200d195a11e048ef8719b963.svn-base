package netinf.android.wifi;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import netinf.android.AndroidNetInfActivity;
import netinf.android.application.http.SendHttpPostRequestTask;
import netinf.android.common.datamodel.SailDefinedAttributeIdentification;
import netinf.android.dialog.MyAlertDialogFragment;
import netinf.android.dialog.MyProgressDialogFragment;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WifiDiscoveryAndConnectTask extends
		AsyncTask<Void, Void, AndroidDiscoverAndConnectResponse> {
    
    // Debugging
    private static final String TAG = "WifiDiscoveryAndConnectTask";
    private static final boolean D = true;
    
    //Naming Variables
	private String TARGETHOST;
	private String TARGETPORT;
    
    private static final long discoveryTimeOut  = 140000000;
    private static final long connectionTimeOut = 140000000;
    
	//Handler messages types 
    public static final int MESSAGE_DISCOVERY_DONE     = 1;
    public static final int MESSAGE_CONNECTED_DONE     = 2;
    
    private int counter =0;
    
    private boolean discoverySuccess  = false;
    private boolean connectionSuccess = false;
    
	
    private InformationObject myIO = null;
       
    private AndroidNetInfActivity mActivity;
    NetInfWifiDirectManager mNetInfManager;
    AndroidDiscoverAndConnectResponse mResponse;
    WifiP2pDeviceList discoveredPeers;
    InetAddress mRemoteIpAddress;
        
    public WifiDiscoveryAndConnectTask(AndroidNetInfActivity activity, String targetHost, String targetPort, InformationObject io){
    	
    	mActivity      = activity;    	
    	myIO           = io; 	
		TARGETHOST     = targetHost;
		TARGETPORT     = targetPort;
    	mNetInfManager = mActivity.mNetInfManager;
    	mResponse      = new AndroidDiscoverAndConnectResponse();
    	mResponse.setInformationObject(myIO);
    }
    
    private final Handler mWifiDiscoveryAndConnectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_DISCOVERY_DONE :
            	setDiscoverySuccess(true);
            	discoveredPeers = (WifiP2pDeviceList) msg.obj;
            	break;
	        case MESSAGE_CONNECTED_DONE :
	        	setConnectionSuccess(true);
	        	mRemoteIpAddress = (InetAddress) msg.obj;
	        	break;
	        }                    
        }
    };
    
    @Override
    protected void onPreExecute() {
    	// TODO Auto-generated method stub
    	super.onPreExecute();
	
    }
    
    
	@Override
	protected AndroidDiscoverAndConnectResponse doInBackground(Void... params) {
		
		//MAC address of the remote peer that the device will attempt to connect

		ArrayList<String> matchingPeers = new ArrayList<String>();
		ArrayList<String> discoveredmatchingPeers = new ArrayList<String>();

		String tmp;

//		//Getting the locator from the IO
        List<Attribute> locators = myIO.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());	        	       
        if (!locators.isEmpty()) {             
			for(Attribute locator : locators){				
				if(!locator.getIdentification().equals(SailDefinedAttributeIdentification.WIFI_IP.getURI())){
					System.out.print(locator);
					tmp = locator.getValueRaw();
					tmp = tmp.substring(tmp.indexOf("/")+2);
					matchingPeers.add(tmp);
				}
			}
        }
        
        mNetInfManager.isClient=true;
        //Discovery Stage
        mNetInfManager.startDiscovery(mWifiDiscoveryAndConnectHandler);
        
        counter = 0;
        
        while(counter<discoveryTimeOut){        	
        	if(getDiscoverySuccess()){
        		if(D) Log.d(TAG, " Discovery has succeeded!");
        		setDiscoverySuccess(true);
        		break;
        	}
        		
        	counter++;
        }
		
        //Connect Stage
        
        if(getDiscoverySuccess()){
        	if(D) Log.d(TAG, " Connect Stage");
        	if(discoveredPeers!=null){
				for(WifiP2pDevice device : discoveredPeers.getDeviceList()){				
					System.out.print(device);
					discoveredmatchingPeers.add(device.deviceAddress);
				}

				matchingPeers.retainAll(discoveredmatchingPeers);

		        if(!matchingPeers.isEmpty()){
		        	String selectedPeer = matchingPeers.get(0);
					if(D) Log.d(TAG, "Wifi Direct Connection process start - Connecting to remote peer:" + selectedPeer);
					mNetInfManager.connectToDevice(selectedPeer);
	
					counter = 0;
			        
			        while(counter<connectionTimeOut){        	
			        	if(getConnectionSuccess()){
			        		if(D) Log.d(TAG, "Wifi Direct Connection process finished - Connection has succeeded!");
			        		setConnectionSuccess(true);
			        		break;
			        	}
			        		
			        	counter++;
			        }		
		        }
        	}
        }
        
        if(getConnectionSuccess()){
        	//We are successfully connected to the remote peer
        	if(mRemoteIpAddress!=null){        		
        		mResponse.setIpAddress(mRemoteIpAddress);
        		return mResponse;
        	}     	
        }
        return null;
	}
	
	@Override
	protected void onPostExecute(AndroidDiscoverAndConnectResponse response) {
		// TODO Auto-generated method stub
		super.onPostExecute(response);
		dismissConnectingProgressBar();
		if(response!=null){
			DialogFragment newFragment = new MyProgressDialogFragment("Transferring the file over Wi-Fi...");
			newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.TRANSFERRING);
			
			List<Attribute> ipAttributeList = myIO.getAttribute(SailDefinedAttributeIdentification.WIFI_IP.getURI());
	        if (!ipAttributeList.isEmpty()) {             
	        	Attribute ipAttribute = ipAttributeList.get(0);
	        	ipAttribute.setValue("niip:/" + response.getIpAddress().toString());
	        }
			
			new SendHttpPostRequestTask (mActivity, TARGETHOST, TARGETPORT,AndroidNetInfActivity.WIFI,myIO).execute();
		}
		else{
			
			String mTitle   = "Wi-Fi Direct Connection Error";
			String mMessage = "There was an error while establishing the Wi-Fi direct connection." + 
			                  "Try rebooting the Wi-Fi Direct antenna and try again.";
        	DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.WIFI_DIRECT_CONNECTION);
		}
		
	}
   
    private void dismissConnectingProgressBar(){
	    
    	FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
	    Fragment prev          = mActivity.getFragmentManager().findFragmentByTag(AndroidNetInfActivity.CONNECTING);
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.commit();
    }
    
    private synchronized boolean getDiscoverySuccess(){  	
    	return discoverySuccess;
    }
    
    private synchronized boolean getConnectionSuccess(){  	
    	return connectionSuccess;
    }
    
    private synchronized void setDiscoverySuccess(boolean value){    	
    	discoverySuccess = value;
    }
    
    private synchronized void setConnectionSuccess(boolean value){    	
    	connectionSuccess = value;
    }

}
