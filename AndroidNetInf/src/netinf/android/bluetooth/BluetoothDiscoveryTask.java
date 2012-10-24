package netinf.android.bluetooth;

import java.util.ArrayList;
import java.util.List;

import netinf.android.AndroidNetInfActivity;
import netinf.android.NetInfBroadcastReceiver;
import netinf.android.application.http.SendHttpPostRequestTask;
import netinf.android.common.datamodel.SailDefinedAttributeIdentification;
import netinf.android.dialog.MyProgressDialogFragment;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.util.Log;

public class BluetoothDiscoveryTask extends
		AsyncTask<Void, Void, InformationObject> {
    
    // Debugging
    private static final String TAG = "BluetoothDiscoveryTask";
    private static final boolean D = true;
    
    //Naming Variables
	private String TARGETHOST;
	private String TARGETPORT;
    
    private static final long discoveryTimeOut  = 400000000;
    
	//Handler messages types 
    public static final int MESSAGE_PEER_FOUND         = 1;
    
    private int counter =0;
	
    private InformationObject myIO = null;
       
    private AndroidNetInfActivity mActivity;

	ArrayList<String> discoveredPeers;
	private BluetoothAdapter mBluetoothAdapter = null;
   
    
    public BluetoothDiscoveryTask(AndroidNetInfActivity activity, String targetHost, String targetPort, InformationObject io){
    	
    	mActivity      = activity;    	
    	myIO           = io; 	
		TARGETHOST     = targetHost;
		TARGETPORT     = targetPort;
		
		discoveredPeers = new ArrayList<String>();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
        
    @Override
    protected void onPreExecute() {
    	// TODO Auto-generated method stub
    	super.onPreExecute();
	
    }
    
    
	@Override
	protected InformationObject doInBackground(Void... params) {
		
	
		ArrayList<String> matchingPeers = getNRSreceivedPeers();
       
        //Discovery process
        	
        if(mBluetoothAdapter!=null){
        	     	
            // If we're already discovering, stop it
            if (mBluetoothAdapter.isDiscovering()) {
            	mBluetoothAdapter.cancelDiscovery();
            }

            // Request discover from BluetoothAdapter
            mBluetoothAdapter.startDiscovery();
            if(D) Log.d(TAG, " Bluetooth Discovery process started!");
            
            counter = 0;
            
            while(counter<discoveryTimeOut){        	          		
            	counter++;
            }
        }
        
        mBluetoothAdapter.cancelDiscovery();
        
        discoveredPeers = ((NetInfBroadcastReceiver)mActivity.mReceiver).getBluetoothDiscoveredDevices();
       
        if(D) Log.d(TAG, " DiscoveredPeers = :" + discoveredPeers.toString());
        if(D) Log.d(TAG, " Discovery Done!");
        
        matchingPeers.retainAll(discoveredPeers);
               
        if(D) Log.d(TAG, " Matching Peers = :" + matchingPeers.toString());        
        if(D) Log.d(TAG, " IO before discovery processing  = " + myIO.toString());
               
        deleteUnreachableLocators(matchingPeers);
        
        if(D) Log.d(TAG, " Resulting IO after discovery processing  = " + myIO.toString());	

        return myIO;
	}
	
	@Override
	protected void onPostExecute(InformationObject response) {
		// TODO Auto-generated method stub
		super.onPostExecute(response);
		
		dismissDiscoveryProgressBar();
		((NetInfBroadcastReceiver)mActivity.mReceiver).clearBluetoothDiscoveredDevices();
		
		if(response!=null){
			DialogFragment newFragment = new MyProgressDialogFragment("Transferring the file over Bluetooth...");
			newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.TRANSFERRING);			
			new SendHttpPostRequestTask (mActivity, TARGETHOST, TARGETPORT,AndroidNetInfActivity.BLUETOOTH,myIO).execute();
		}
		
	}
   
	private ArrayList<String> getNRSreceivedPeers(){
		
		
		ArrayList<String> resultArray = new ArrayList<String>();
		String tmp;
		
		//Getting the locator from the IO
        List<Attribute> locators = myIO.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());	        	       
        if (!locators.isEmpty()) {             
			for(Attribute locator : locators){				
				if(!locator.getIdentification().equals(SailDefinedAttributeIdentification.WIFI_IP.getURI())){
					System.out.print(locator);
					tmp = locator.getValueRaw();
					tmp = tmp.substring(tmp.indexOf("/")+2);
					resultArray.add(tmp);
				}
			}
        }
        
        return resultArray;
	}
	
	private void deleteUnreachableLocators(ArrayList<String> matchingPeers){
		
		String tmp;
		List<Attribute> locators = myIO.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
		
        locators = myIO.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());	        	       
        if (!locators.isEmpty()) {             
			for(Attribute locator : locators){
				if(locator.getIdentification().equals(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI())){
					System.out.print(locator);
					tmp = locator.getValueRaw();
					tmp = tmp.substring(tmp.indexOf("/")+2);
					if(!matchingPeers.contains(tmp)){					
						myIO.removeAttribute(locator);
					    if(D) Log.d(TAG, " Bluetooth locator removed because is not in the device range = " + tmp);						   
					}
				}
			}
        }
	}
	
    private void dismissDiscoveryProgressBar(){
	    
    	FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
	    Fragment prev          = mActivity.getFragmentManager().findFragmentByTag(AndroidNetInfActivity.DISCOVERING);
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.commit();
    }
    
}
