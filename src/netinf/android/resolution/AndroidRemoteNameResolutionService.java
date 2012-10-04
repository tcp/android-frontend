/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
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
package netinf.android.resolution;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;

import netinf.android.AndroidNetInfActivity;
import netinf.android.common.datamodel.SailDefinedAttributeIdentification;
import netinf.android.common.datamodel.SailDefinedLabelName;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.node.resolution.ResolutionService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import android.util.Log;

import com.google.inject.Inject;

/**
 * @author PG Augnet 2, University of Paderborns
 */
public class AndroidRemoteNameResolutionService extends AndroidAbstractResolutionService implements ResolutionService {

    // Debugging
    private static final String TAG = "AndroidRemoteNameResolutionService";
    private static final boolean D = true;
	
	private static String NRS_SERVER;
	
	private final static String NIMACBT = "nimacbt://";
	private final static String NIMACWD = "nimacwd://";
	private final static String NIHTTP  = "nihttp://";
    
    //Locator variables	
	private final DatamodelFactory datamodelFactory;
	
	static MultipartEntity reqEntity = new MultipartEntity();
	static HttpPost post;
	static Random randomGenerator = new Random();
	HttpClient client = new DefaultHttpClient();
	HttpResponse response;
	int resp_code=0;
	String bluetoothMac = null;
	String wifiMac=null;
	String ncsUrl =null;
	

   @Inject
   public AndroidRemoteNameResolutionService(DatamodelFactory datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
      NRS_SERVER = AndroidNetInfActivity.NRS_SERVER_URL;
   }
   
   public void initializeBluetoothMac(String mac){  
	   bluetoothMac = mac;
   }
   
   public void initializeWifiMac(String mac){   
	   wifiMac = mac;
   }
  
   
   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = this.datamodelFactory
            .createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName("AndroidRemoteNameResolutionService");
      identity.setDefaultPriority(500);
      identity.setDescription("Central name resolution server for publishing and retrieving IOs");
      return identity;
   }


   public void delete(Identifier identifier) {

	   //Extracting values from IO's identifier
		String hashAlg      = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
	    String hashValue    = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();	    
	    				
		if (createunpub(hashAlg,hashValue)) {		
			try {
				response = client.execute(post);
				resp_code = response.getStatusLine().getStatusCode();
				System.err.println("RESP_CODE: "+Integer.toString(resp_code));
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					System.out.println(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
		System.err.println("Publish creation failed!");
		}
//	   if(D) Log.d(TAG, "The IO was successfully put. IO = " + myTmpIO.toString());
	   
   }


   public String describe() {
      return "Remote Name Resolution Server for publishing and retrieving IOs";
   }
   

   public InformationObject get(Identifier identifier) {
	   
		String c_type;
		InformationObject myIO = null;
	    
		if(D) Log.d(TAG, "GET IO");
		
		//Extracting values from IO's identifier
		String hashAlg     = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
	    String hashValue   = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();

	    String niname = "ni:///" + hashAlg + ";" + hashValue;
		
		try {
		
			if(D) Log.d(TAG, "Creating get request for niname = " + niname);
			if (createget(NRS_SERVER, niname)) {
							
				response = client.execute(post);
				resp_code = response.getStatusLine().getStatusCode();	
				if(D) Log.d(TAG, "response code received from the NRS = " + resp_code );
											
				if (200 == resp_code) {
					// Get content type
					c_type = response.getEntity().getContentType().getValue();
					
					if ("application/json".equalsIgnoreCase(c_type)) {
						// Response is location list
						InputStream content = response.getEntity().getContent();														
						String resp = convertStreamToString(content);
					
						// String to JSONArray
						Object obj=JSONValue.parse(resp);
						JSONArray array=(JSONArray)obj;
						
						if(D) Log.d(TAG, "Locators array size = " + array.size() );
						
						if(array.size()>0){
							
							String locator = "";
							myIO = datamodelFactory.createInformationObject();
							myIO.setIdentifier(identifier);
						    Random randomGenerator = new Random();
						    String locatorType = null;
													
							// add new locations to loc_array	
							for (int i=0; i<array.size(); i++) {
								locator = array.get(i).toString();
								
					        	if(locator.contains(NIMACBT))
					        		locatorType = SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI();
					        	if(locator.contains(NIMACWD))
					        		locatorType = SailDefinedAttributeIdentification.WIFI_MAC.getURI();
					        	if(locator.contains(NIHTTP))
					        		locatorType = SailDefinedAttributeIdentification.NCS_URL.getURI();;
							
								Attribute newLocator   = datamodelFactory.createAttribute();
				                newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
				                newLocator.setIdentification(locatorType);
				                newLocator.setValue(locator);
				                
				                Attribute priorityAttr = datamodelFactory.createAttribute();
				                priorityAttr.setIdentification(SailDefinedAttributeIdentification.LOCATOR_PRIORITY.getURI());
				                priorityAttr.setValue(randomGenerator.nextInt(5000));
				                newLocator.addSubattribute(priorityAttr);
				                				                			                
				                myIO.addAttribute(newLocator);
				                
				                if(locatorType.equals(SailDefinedAttributeIdentification.WIFI_MAC.getURI())&&myIO.getAttribute(SailDefinedAttributeIdentification.WIFI_IP.getURI()).isEmpty()){
				                	
									Attribute ipLocator   = datamodelFactory.createAttribute();
									ipLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
									ipLocator.setIdentification(SailDefinedAttributeIdentification.WIFI_IP.getURI());
									
					                myIO.addAttribute(ipLocator);
				                	
				                }
							}
							
							if(D) Log.d(TAG, "IO found in the NRS. IO =  " + myIO.toString());
						}
						else
							if(D) Log.d(TAG, "The requested IO was not found in the database.");
					} 
					else {
						// Response codetype is not success (we expected that)
						System.err.println("Wrong ctype - The ctype value is not application/json");
					}
				} else {
					// Response codetype is not success (we expected that)
					System.err.println("RESP_CODE: "+Integer.toString(resp_code));
				}	
		} else {
			System.err.println("Command parse failed!");
		}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	    	
		return myIO;
   }

   //Unimplemented
   public List<Identifier> getAllVersions(Identifier identifier) {
      return null;
   }


   public void put(InformationObject informationObject) {
		
		//Extracting values from IO's identifier
		String hashAlg     = informationObject.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
		String hashCont    = informationObject.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();

		//Extracting WiFi and Bluetooth MACs
		Attribute bluetoothMacAttribute = informationObject.getSingleAttribute(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
		if(bluetoothMacAttribute!=null){
			bluetoothMac = bluetoothMacAttribute.getValueRaw();
			bluetoothMac = bluetoothMac.substring(bluetoothMac.indexOf(":")+1);
		}
		
		Attribute wifiMacAttribute = informationObject.getSingleAttribute(SailDefinedAttributeIdentification.WIFI_MAC.getURI());
		if(wifiMacAttribute!=null){
			wifiMac = wifiMacAttribute.getValueRaw();
			wifiMac = wifiMac.substring(wifiMac.indexOf(":")+1);
		}
		
		Attribute ncsUrlAttribute = informationObject.getSingleAttribute(SailDefinedAttributeIdentification.NCS_URL.getURI());
		if(ncsUrlAttribute!=null){
			ncsUrl = ncsUrlAttribute.getValueRaw();
			ncsUrl = ncsUrl.substring(ncsUrl.indexOf(":")+1);
		}
				
		if (createpub(hashAlg,hashCont)) {		
			try {
				response = client.execute(post);
				resp_code = response.getStatusLine().getStatusCode();
				System.err.println("RESP_CODE: "+Integer.toString(resp_code));
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					System.out.println(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
		System.err.println("Publish creation failed!");
		}
//	   if(D) Log.d(TAG, "The IO was successfully put. IO = " + myTmpIO.toString());

   }
	/**
	 * Creates NI publish HTTP POST signal.
	 * 
	 * @return 		boolean true/false in success/failure
	 * 
	 */
	
	private boolean createpub(String hashAlg,String hashValue)  {
		
		post = new HttpPost(NRS_SERVER +"/.well-known/netinfproto/publish"); 
			
		StringBody url;

		try {
			url = new StringBody("ni:///" + hashAlg + ";" + hashValue);
			reqEntity.addPart("URI",url);
			
			StringBody msgid = new StringBody(Integer.toString(randomGenerator.nextInt(100000000))); // generate
			reqEntity.addPart("msgid", msgid);
			
			if(bluetoothMac!=null){	
				StringBody l = new StringBody("nimacbt://" + bluetoothMac);
				reqEntity.addPart("loc1", l);
			}
			
			if(wifiMac!=null){
				StringBody l2 = new StringBody("nimacwd://" + wifiMac);
				reqEntity.addPart("loc2", l2);
			}
			
			if(ncsUrl!=null){
				StringBody l3 = new StringBody("ni" + ncsUrl);
				reqEntity.addPart("loc3", l3);
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		post.setEntity(reqEntity);
		
		return true;
	}
	
	/**
	 * Creates NI get HTTP POST signal.
	 * 
	 * @param dst destination (fqdn for now) of the signal destination
	 * @param name name of the content in NI format
	 * 
	 * @return boolean true (currently always).
	 * 
	 * 
	 */
	
	static boolean createget(String dst, String name)  {
		
		post = new HttpPost(dst+"/.well-known/netinfproto/get"); 
		
		String msgid = Integer.toString(randomGenerator.nextInt(100000000));
		String ext = "no extension";

		String uri= "URI="+name+"&msgid="+msgid+"&ext="+ext;
		
		String myEncodedUrl = null;
		try {
			myEncodedUrl = URLEncoder.encode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpEntity newEntity = new InputStreamEntity(fromString(myEncodedUrl), myEncodedUrl.getBytes().length);
		
		post.addHeader("Content-Type","application/x-www-form-urlencoded");
		post.setEntity(newEntity);
		return true;
	}
	
	/**
	 * Creates NI unpublish HTTP POST signal.
	 * 
	 * @return 		boolean true/false in success/failure
	 * 
	 */
	
	private boolean createunpub(String hashAlg,String hashValue)  {
		
		post = new HttpPost(NRS_SERVER +"/.well-known/netinfproto/unpublish"); 
			
		StringBody url;

		try {
			url = new StringBody("ni:///" + hashAlg + ";" + hashValue);
			reqEntity.addPart("URI",url);
			
			StringBody msgid = new StringBody(Integer.toString(randomGenerator.nextInt(100000000))); // generate
			reqEntity.addPart("msgid", msgid);
			
			if(bluetoothMac!=null){	
				StringBody l = new StringBody("nimacbt://" + bluetoothMac);
				reqEntity.addPart("loc1", l);
			}
			
			if(wifiMac!=null){
				StringBody l2 = new StringBody("nimacwd://" + wifiMac);
				reqEntity.addPart("loc2", l2);
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		post.setEntity(reqEntity);
		
		return true;
	}
	
	
	/**
	 * Converts a string to a type ByteArrayInputStream
	 * 
	 * @param str string to be converted
	 * 
	 * @return ByteArrayInputStream
	 */
	public static InputStream fromString(String str)
	{
	byte[] bytes = str.getBytes();
	return new ByteArrayInputStream(bytes);
	}
	
	
	/**
	* To convert the InputStream to String we use the
	* Reader.read(char[] buffer) method. We iterate until the
	* Reader return -1 which means there's no more data to
	* read. We use the StringWriter class to produce the string.
	* 
	* @param is inputStream from where content to be converted is read
	*
	* @exception throws IOException
	*
	* @return converted stream string (null if stream is empty)
	*/
	public static String convertStreamToString(InputStream is) throws IOException {
	
		if (is != null) {
			Writer writer = new StringWriter();
	
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(
					new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {       
			return "";
		}
	}
	
	
}
