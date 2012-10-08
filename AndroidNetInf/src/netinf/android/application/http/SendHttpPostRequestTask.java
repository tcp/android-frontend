package netinf.android.application.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netinf.android.AndroidNetInfActivity;
import netinf.android.common.datamodel.SailDefinedAttributeIdentification;
import netinf.android.common.datamodel.SailDefinedLabelName;
import netinf.android.dialog.MyAlertDialogFragment;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

public class SendHttpPostRequestTask extends AsyncTask<String, Void, String> {
	
    // Debugging
    private static final String TAG = "SendHttpPostRequestTask";
    private static final boolean D = true;
	
	private static final String HTTP = "http://";
		
    //Naming Variables
	private String TARGETHOST;
	private String TARGETPORT;
	
	private AndroidNetInfActivity mActivity;
	private InformationObject     mIO;
	String mHashAlg;
	String mHashContent;
	private boolean cacheContent = false;
	
    int    transportTechnology = 0;
    String mDialogMessage;  
    String transferringErrorMessage = "There was an error while transferring the file.";
    
	
	public SendHttpPostRequestTask (AndroidNetInfActivity activity, String targetHost, String targetPort, int transport,InformationObject IO){

		mActivity           = activity;
		TARGETHOST          = targetHost;
		TARGETPORT          = targetPort;
		transportTechnology = transport;
		mIO = IO;

	}
	
	public SendHttpPostRequestTask (AndroidNetInfActivity activity, String targetHost, String targetPort, int transport,String hashAlg, String hashContent){

		mActivity           = activity;
		TARGETHOST          = targetHost;
		TARGETPORT          = targetPort;
		transportTechnology = transport;
		mHashAlg = hashAlg;
		mHashContent = hashContent;
	}
				
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute(); 	
	}
	
 	@Override
	protected String doInBackground(String... parameters) {
 				
 		String resultFilePath = null;
 		
        HttpClient httpclient = new DefaultHttpClient();  
        HttpPost request = null;
        InputStream content = null;
        String requestUri = null;
        UrlEncodedFormEntity urlEntity = null;
 				
		requestUri = HTTP + TARGETHOST + ":"+TARGETPORT+ "/transfer";
		
		//Adding the parameters depending on the transference technology
		
		List<NameValuePair> nameValuePairs = null;

    	if(mIO!=null){
    		//We will add the IO to the post content part
    		nameValuePairs = createPostEntityFromIO(mIO);	
    	}
    	else {
    		if(mHashAlg!=null&&mHashContent!=null){
    			
    			cacheContent =true;
    			
    			nameValuePairs = new ArrayList<NameValuePair>(2);
    			
    			//Setting the hash value and hash algorithm parameters to the POST request
    	        nameValuePairs.add(new BasicNameValuePair("hashContent", mHashContent));
    	        nameValuePairs.add(new BasicNameValuePair("hashAlg", mHashAlg));
    	        
    	        if(D) Log.d(TAG, " HashContent parameter added. Value = " + mHashContent);
    	        if(D) Log.d(TAG, " HashAlg parameter added. Value = " + mHashContent);  			
    		}    		
    	}
    		
    	if(nameValuePairs!=null){	
    		try {				
				urlEntity = new UrlEncodedFormEntity(nameValuePairs);										
			} 
    		catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	//Executing the HTTP request  
	        try {

	             //Creating the request
	            
	        	 request = new HttpPost(requestUri);
	        	 if(urlEntity!=null)
	        		 request.setEntity(urlEntity);
	        	 if(D) Log.d(TAG, " Sending HTTP POST request to the control layer");
	             HttpResponse response = httpclient.execute(request); 
	             content = response.getEntity().getContent();
	             resultFilePath = convertStreamToString(content);
	             if(D) Log.d(TAG, "Result path received from the control layer = " + resultFilePath);
	             
	             if(resultFilePath!=null){
	            	 
	 		        //without this the Gallery will not see the picture
	 				String[] paths = { resultFilePath };
	 				String[] mediaType = {"image/jpeg"};
	 				MediaScannerConnection.scanFile(mActivity, paths, mediaType, null);
	 				return resultFilePath; 	            	 
	             }
	         } 
	         catch (Exception e) {
	             System.out.print("error");
	             return "";
	         } 
    	}
    	else{
    		if(D) Log.d(TAG, "There are not available locators in the range of the selected technology. Please try again or select another transport technology");
    		transferringErrorMessage ="There are not available locators in the range of the selected technology. Please try again or select another transport technology";
    	}
                   	
		return ""; 			
	}

	@Override
	protected void onPostExecute(String resultPath) {
		
		// TODO Auto-generated method stub
		super.onPostExecute(resultPath);
		dismissTransferProgressBar();
		
		if(!resultPath.isEmpty()){
	        //Cache the received IO
	        boolean isCached = cacheIOinDB(resultPath);
	        if(D) Log.d(TAG, "Was the BO received cached?: " + isCached);
		}
		else{	
			if(cacheContent){
								
//    			String mTitle   = "Cache content result";
//    			String mMessage = "The file has been successfully cached in the NCS. A local IO has been also created for this file";
//	        	DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
//	        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.GET_IO_NOT_FOUND);
				String cacheContentString;
				if(cacheContent)
					cacheContentString = "true";
				else
					cacheContentString = "false";
				
				SendHttpGetRequestTask task = new SendHttpGetRequestTask(mActivity,mHashAlg,TARGETHOST,TARGETPORT,transportTechnology,"Caching the IO...");
				task.execute(new String[] { "PUT", mHashContent, cacheContentString });
			}
			else{
				String mTitle   = "Transferring Error";
				String mMessage = transferringErrorMessage;
	        	DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
	        	newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.TRANSFERRING_ERROR);
			}
		}	
		
		if(transportTechnology == AndroidNetInfActivity.WIFI){
			if(mActivity.mNetInfManager.isClient)
				mActivity.mNetInfManager.isClient=false;
			
	        if(mActivity.mNetInfManager!=null){
	        	Log.d(TAG, "Disconnecting from the WiFi Direct group!");
	        	mActivity.mNetInfManager.disconnectFromGroup();
	        }
		}
	}
	  
    
    private boolean cacheIOinDB(String filePath){
    	
        String path        = filePath;
        File myFile = null;
    	        
		myFile = new File(path);
		if(myFile.exists()){
			try {
				byte[] myFileBytes = IOUtils.toByteArray(new FileInputStream(myFile));
				mActivity.createHTTPRequestFromByteArray("CACHE",myFileBytes,path);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} 
      
    	return true;   	
    }
    
    private void dismissTransferProgressBar(){
	    
    	FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
	    Fragment prev          = mActivity.getFragmentManager().findFragmentByTag(AndroidNetInfActivity.TRANSFERRING);
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.commit();
    }
    
    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}
    
    private List<NameValuePair> createPostEntityFromIO(InformationObject mIO){
    	
		if(D) Log.d(TAG, " Setting the POST request parameters");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(20);
		boolean hasAvailableLocators = false;
		
		//Getting the identifier
		Identifier identifier = mIO.getIdentifier();
		String hashContent = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
		String hashAlg     = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
		
		//Setting the hash value and hash algorithm parameters to the POST request
        nameValuePairs.add(new BasicNameValuePair("hashContent", hashContent));
        nameValuePairs.add(new BasicNameValuePair("hashAlg", hashAlg));
        
        if(D) Log.d(TAG, " HashContent parameter added. Value = " + hashContent );
        if(D) Log.d(TAG, " HashAlg parameter added. Value = " + hashAlg);
        
        //Setting the Locators
        List<Attribute> locators = mIO.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
        
        Iterator<Attribute> iterator = locators.iterator();
        String attributeIdentification;
        String locatorType = null;
        
        //This loops iterates thorugh all the locators contained in the IO
        while(iterator.hasNext()){
        	Attribute myCurrentAttribute = (Attribute)iterator.next();
        	attributeIdentification= myCurrentAttribute.getIdentification();
        	
        	//Bluetooth Locators
        	if(attributeIdentification.equals(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI())){
        		if(transportTechnology==AndroidNetInfActivity.BLUETOOTH){
	        		String locator = myCurrentAttribute.getValue(String.class);
        			locatorType = "bluetooth_locator";
		        	nameValuePairs.add(new BasicNameValuePair(locatorType,locator));
		        	hasAvailableLocators = true;
		        	if(D) Log.d(TAG, "Bluetooth locator added. Value = " + locator);
        		}
        	}
        	
        	//Wifi Locators
        	if(attributeIdentification.equals(SailDefinedAttributeIdentification.WIFI_IP.getURI())){
        		if(transportTechnology==AndroidNetInfActivity.WIFI){
        			String locator = myCurrentAttribute.getValue(String.class);
        			locatorType = "wifi_locator";
		        	nameValuePairs.add(new BasicNameValuePair(locatorType,locator));
		        	hasAvailableLocators = true;
		        	if(D) Log.d(TAG, "Wifi locator added. Value = " + locator);
        		}
        	}
        	
        	//NCS locators
        	if(attributeIdentification.equals(SailDefinedAttributeIdentification.NCS_URL.getURI())){
        		if(transportTechnology==AndroidNetInfActivity.NCS){
        			String locator = myCurrentAttribute.getValue(String.class);
        			locatorType = "ncs_locator";
		        	nameValuePairs.add(new BasicNameValuePair(locatorType,locator));
		        	hasAvailableLocators = true;
		        	if(D) Log.d(TAG, "NCS locator added. Value = " + locator);
        		}
        	}
        }
		if(hasAvailableLocators)
			return nameValuePairs;
		else
			return null;
    	
    	
    }

}