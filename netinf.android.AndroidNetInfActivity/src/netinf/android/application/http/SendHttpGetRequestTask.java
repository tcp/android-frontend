package netinf.android.application.http;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import netinf.android.AndroidNetInfActivity;
import netinf.android.DisplayResult;
import netinf.android.bluetooth.BluetoothDiscoveryTask;
import netinf.android.common.datamodel.SailDefinedAttributeIdentification;
import netinf.android.common.datamodel.SailDefinedLabelName;
import netinf.android.dialog.MyAlertDialogFragment;
import netinf.android.dialog.MyCacheDialogFragment;
import netinf.android.dialog.MyProgressDialogFragment;
import netinf.android.wifi.NetInfWifiDirectManager;
import netinf.android.wifi.WifiDiscoveryAndConnectTask;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class SendHttpGetRequestTask extends AsyncTask<String, Void, AndroidHttpResponse> {
	
    // Debugging
    private static final String TAG = "SendHttpGetRequestTask";
    private static final boolean D = true;
	
	private static final String HTTP               = "http://";
	
	//Method types
	private static final int GET         = 0;
	private static final int PUT         = 1;
	private static final int DELETE      = 2;
	private static final int CACHE       = 3;
	  
    //HTTP requests parameters
	private static final int HTTP_METHOD   = 0;
	private static final int HASH          = 1;
	private static final int CACHE_CONTENT = 2;
	
    //Naming Variables
	private String HASH_ALGORITHM;
	private String TARGETHOST;
	private String TARGETPORT;
	
	private AndroidNetInfActivity mActivity;
	
    String methodType;		
    String hash;
    String cacheContent;
    String wifiMac      = null;
    String bluetoothMac = null;
    int    transportTechnology = 0;
    String mDialogMessage;
    
    InputStream content = null;
	String result=null;
	
	NetInfWifiDirectManager mNetInfManager;
	
	public SendHttpGetRequestTask (AndroidNetInfActivity activity,String hashAlg, String targetHost, String targetPort, int transport, String message){
		HASH_ALGORITHM      = hashAlg;
		TARGETHOST          = targetHost;
		TARGETPORT          = targetPort;
		mActivity           = activity;
		transportTechnology = transport;
		mDialogMessage      = message;
		
		
		//Getting MAC addresses for Bluetooth and Wifi from the Activity
		
		if(mActivity.bluetoothMacAddress!=null)
			bluetoothMac = mActivity.bluetoothMacAddress;
		if(mActivity.mNetInfManager.isWifiP2pEnabled){
			if(mActivity.wifiMacAddress!=null)
				wifiMac = mActivity.wifiMacAddress;
		}
	}
				
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
			
    	//Start progress dialog bar
		DialogFragment newFragment = new MyProgressDialogFragment(mDialogMessage);
		newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.SEARCHING);		
	}
	
 	@Override
	protected AndroidHttpResponse doInBackground(String... parameters) {
 				
 		AndroidHttpResponse result = new AndroidHttpResponse();
 		InformationObject resultIO = null; 		
 		
        HttpClient httpclient = new DefaultHttpClient();  
        HttpGet request;
        String requestUri = null;
 		
		//Getting the HTTP parameters
		for(int i=0;i<parameters.length;i++){
			switch(i){
				case HTTP_METHOD:
					methodType = parameters[i];
					break;
				case HASH:
					hash = parameters[i];
					break;
				case CACHE_CONTENT:
					cacheContent = parameters[i];
					break;
			}				
		}
			
		requestUri = HTTP + TARGETHOST + ":"+TARGETPORT+ 
                "/.well-known/ni/"+HASH_ALGORITHM+";"+
                hash +"?METHOD=" + methodType;
		
		//Setting the method type to the result object
		
		//GET
		if(methodType.equals("GET")){
	    	result.setMethod(GET);
		}
		
		//PUT
		if(methodType.equals("PUT")){
	    	String addressString = createMacAddressString();
			result.setMethod(PUT);
			if (addressString != null)
				requestUri = requestUri + addressString;	
		}
		
		//DELETE
		if(methodType.equals("DELETE")){
	    	result.setMethod(DELETE);
		}
		
		//CACHE
		if(methodType.equals("CACHE")){
	    	result.setMethod(CACHE);
	    	String addressString = createMacAddressString();
			if (addressString != null)
				requestUri = requestUri + addressString;
		}
		
    	//Executing the HTTP request  
        try {

             //Creating the request
            
        	 request = new HttpGet(requestUri);
             HttpResponse response = httpclient.execute(request);            		             
             content = response.getEntity().getContent(); 
             ObjectInputStream myObjInpStream = new ObjectInputStream(content);
             resultIO = (InformationObject)myObjInpStream.readObject();
         } 
         catch (Exception e) {
             System.out.print("error");
         }
        if(resultIO!=null){
        	//This will only happen when it is a GET request
        	result.setInformationObject(resultIO);
         }
        
        if(result.getMethod()== PUT||result.getMethod()== CACHE)
        	putIOinQRcodeServer(false);
        if(result.getMethod()== DELETE)
        	putIOinQRcodeServer(true);
           
    	return result;  			
	}

	@Override
	protected void onPostExecute(AndroidHttpResponse result) {
		
		String mTitle   = "";
		String mMessage = "";
		
		InformationObject resultIO = result.getInformationObject();
			
		switch (result.getMethod()){
		
			case GET:{
				String locator = null;
				if(resultIO!=null){
				
			        List<Attribute> locators = resultIO.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());	        	       
			        if (!locators.isEmpty()) {             
			        	locator = locators.get(0).getValue(String.class);
			        }
								
					//GET request
					if(locator!=null){
						
						dismissSearchProgressBar();					
						if(hasLocatorForSelectedTransportTechnology(resultIO,transportTechnology)){
						
							if(transportTechnology== AndroidNetInfActivity.WIFI){
								DialogFragment newFragment = new MyProgressDialogFragment("Connecting to Wi-Fi Direct remote peer...");
								newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.CONNECTING);	
								new WifiDiscoveryAndConnectTask(mActivity,TARGETHOST, TARGETPORT,resultIO).execute();
							}
							if(transportTechnology==AndroidNetInfActivity.BLUETOOTH){
								DialogFragment newFragment = new MyProgressDialogFragment("Discovering Bluetooth peers...");
								newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.DISCOVERING);	
								new BluetoothDiscoveryTask(mActivity,TARGETHOST, TARGETPORT,resultIO).execute();
							}
							if(transportTechnology==AndroidNetInfActivity.NCS){
								DialogFragment newFragment = new MyProgressDialogFragment("Transferring the file from the NCS...");
								newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.TRANSFERRING);	
								new SendHttpPostRequestTask (mActivity, TARGETHOST, TARGETPORT,AndroidNetInfActivity.NCS,resultIO).execute();
							}
						}
						else{
							mTitle   = "Transferring Error";
							mMessage = "There are not available locators for the selected technology. Please select another technology and try again";
				        	DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
				        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.TRANSFERRING_ERROR);						
						}
					}
					else{
						
						Identifier identifier = resultIO.getIdentifier();
						String hash = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
						String path  = Environment.getExternalStorageDirectory() + "/MySharedFiles/" + hash;
						Intent intent = new Intent(mActivity.getApplicationContext(), DisplayResult.class);
						intent.putExtra("file_path", path);
						dismissSearchProgressBar();
						mActivity.startActivityForResult(intent, 0);
					}
					
				}
				else{
					dismissSearchProgressBar();
					
	    			mTitle   = "GET IO result";
	    			mMessage = "The IO requested was not found in the DB";
		        	DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
		        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.GET_IO_NOT_FOUND);
				}
				
				break;			
			}
			
			case PUT:{
				dismissSearchProgressBar();
				if(cacheContent.equals("true")){
	    			mTitle   = "Cache content result";
	    			mMessage = "The file has been successfully cached in the NCS. A local IO has been also created for this file";
		        	DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
		        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.GET_IO_NOT_FOUND);
				}
				
				else{
					mTitle   = "PUT IO result";
					
					mMessage = "The IO was successfully put in the DB. IO details    \n" +
	            		       "Hash Algorithm = " + HASH_ALGORITHM               + "\n" +
	            		       "Hash Content   = " + hash                         + "\n" ;
					
		        	DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
		        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.PUT_IO);	
				}
			
				break;
			}
			
			case DELETE:{
				dismissSearchProgressBar();
				mTitle   = "DELETE IO result";
				mMessage = "The IO was successfully deleted";
	        	DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
	        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.DELETE_IO);
				break;
			}
			
			case CACHE:{
				dismissSearchProgressBar();
				
				mTitle   = "CACHE IO";
				
				mMessage = "The IO was successfully cached in the DB. IO details    \n" +
            		       "Hash Algorithm = " + HASH_ALGORITHM                  + "\n" +
            		       "Hash Content   = " + hash                            + "\n" ;
				
				String filePath = Environment.getExternalStorageDirectory() + "/MySharedFiles/" + hash;
				
	        	DialogFragment newFragment = new MyCacheDialogFragment(mTitle,mMessage,filePath);
	        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.CACHE_IO);				
				break;
			}
			
			default:{
				
				if(D) Log.d(TAG, "Method unsupported (PostExcecute method)");
			}
		}				
	}
	  
    private void dismissSearchProgressBar(){
	    
    	FragmentTransaction ft1 = mActivity.getFragmentManager().beginTransaction();
	    Fragment prev          = mActivity.getFragmentManager().findFragmentByTag(AndroidNetInfActivity.SEARCHING);
	    if (prev != null) {
	        ft1.remove(prev);
	    }
	    ft1.commit();
    }
    
    private String createMacAddressString(){
    	
    	String myAddressString = null;
        //Case Bluetooth and Wifi direct MAC available
        if(bluetoothMac!=null&&wifiMac!=null){
        	myAddressString = "&BTMAC="+ bluetoothMac + "&WIMAC="+ wifiMac;
        	if(D) Log.d(TAG, "Bluetooth mac address & Wi-Fi Direct mac address are available");
        }
        else{
       	//Case only Bluetooth MAC available
            if(bluetoothMac!=null&&wifiMac==null){
            	myAddressString = "&BTMAC="+ bluetoothMac;
            	if(D) Log.d(TAG, "Only Bluetooth mac address is available");
            }
            else{
           	 
           	 //Case only Wifi direct MAC available
                if(bluetoothMac==null&&wifiMac!=null){
                	myAddressString = "&WIMAC="+ wifiMac;
                	if(D) Log.d(TAG, "Only Wi-Fi Direct mac address is available");
                }
                else{
                	if(D) Log.d(TAG, "None of the adapter mac addresses are available");
                	return null;
               	 
                }
            }       	 
        }
        
        if(cacheContent.equals("true")){
        	myAddressString = myAddressString + "&NCS="+ AndroidNetInfActivity.NCS_SERVER_URL;
        }
        
    	return myAddressString;
    }
    
	private void putIOinQRcodeServer(boolean isDelete) {
	 	
		HttpResponse response = null;
		String ncsUrl ="NONE";
		
		if(D) Log.d(TAG, "Creating request for the QR code server");
		String qrcodeServerUrl = AndroidNetInfActivity.QRCODE_SERVER_URL + "/.well-known/ni/";
		
		if(isDelete){
			if(D) Log.d(TAG, "The request is for delete an IO from the QRcode server");
			qrcodeServerUrl = qrcodeServerUrl + "delete/";
		}
		
		if(cacheContent.equals("true")){
        	String tmp = AndroidNetInfActivity.NCS_SERVER_URL;
        	ncsUrl = tmp.substring(tmp.indexOf("/")+2);
		}
   	
		//Sending hash of the content to the web server to create QRcode of the it
		 
		 
		 try {
             HttpClient httpclient = new DefaultHttpClient();            		                		   
             
             if(bluetoothMac!=null&&wifiMac!=null){
            	 if(D) Log.d(TAG, "Locators registered - Bluetooth mac = " + bluetoothMac + "Wi-Fi Direct Mac = " + wifiMac + "NCS URL = " + ncsUrl);
            	 response = httpclient.execute(new HttpGet(qrcodeServerUrl + HASH_ALGORITHM + ";" + hash + ";" + bluetoothMac+ ";" + wifiMac + ";" + ncsUrl));
             }
             else {
            	 if(bluetoothMac!=null){
            		 if(D) Log.d(TAG, "Locators registered - Bluetooth mac = " + bluetoothMac + "Wi-Fi Direct Mac = NONE" + "NCS URL = " + ncsUrl);
            		 response = httpclient.execute(new HttpGet(qrcodeServerUrl + HASH_ALGORITHM + ";" + hash + ";" + bluetoothMac + ";NONE;" + ncsUrl ));
            	 }
            	 if(wifiMac!=null){
            		 if(D) Log.d(TAG, "Locators registered - Bluetooth mac = NONE Wi-Fi Direct Mac = wifiMac" + "NCS URL = " + ncsUrl);
            		 response = httpclient.execute(new HttpGet(qrcodeServerUrl + HASH_ALGORITHM + ";" + hash + ";NONE;" + wifiMac + ";"+ ncsUrl));
            	 }
             }
             
             if(D) Log.d(TAG, "Status code received from the QR code server:" + response.getStatusLine().getStatusCode());
         } 
         catch (Exception e) {
             System.out.print("error");
         }  	
	}
	

	
	private boolean hasLocatorForSelectedTransportTechnology(InformationObject io, int technology){
		
		boolean result = false;
		
		switch(technology){	
			case AndroidNetInfActivity.BLUETOOTH:{
				if((io.getAttribute(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI()).isEmpty())==false)
					result = true;
				
				break;
			}
			case AndroidNetInfActivity.WIFI:{
				if((io.getAttribute(SailDefinedAttributeIdentification.WIFI_MAC.getURI()).isEmpty())==false)
					result = true;
				
				break;
			}
			case AndroidNetInfActivity.NCS:{
				if((io.getAttribute(SailDefinedAttributeIdentification.NCS_URL.getURI()).isEmpty())==false)
					result = true;
					
				break;
			}		
		}
		
		
		return result;		
	}
}