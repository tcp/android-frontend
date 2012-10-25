package project.cs.lisa.netinf.node.resolution;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;


import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.node.resolution.ResolutionService;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.entity.InputStreamEntity;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


import project.cs.lisa.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import android.util.Log;

import com.google.inject.Inject;

public class NameResolutionService extends LisaAbstractResolutionServiceWithoutId implements ResolutionService {

	
	private static final String TAG = "NameResolutionService";
	//TODO Extract NRS_SERVER IP address and port from a properties file or any other kind of config file 
	private static final String NRS_SERVER = "http://130.238.15.227";
	// TODO add to properties file
	private static final String NRS_SERVER_PORT = "1337"; 
	private static final int TIMEOUT = 5000;
	
	/* Response code in case of affiliated data and content*/
	private static final int RESPONSE_CODE_200 = 200;
	
	/* Response code in case of only affiliated data*/
	private static final int RESPONSE_CODE_203 = 203;
	
	/* Datamodel Factory*/
	private final DatamodelFactory mDatamodelFactory;
	

	//TODO is this ok?
	private static final Random randomGenerator = new Random();
	private HttpClient mClient; 
	
	@Inject
	public NameResolutionService(DatamodelFactory datamodelFactory) {
	    // Setup HTTP client
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        this.mClient = new DefaultHttpClient(params);
        this.mDatamodelFactory = datamodelFactory;
	}
	
	@Override
	public void delete(Identifier arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String describe() {
		return "backend NRS";
	}

	@Override
	public InformationObject get(Identifier identifier) {
		Log.d(TAG,"get()");
		
		
		InformationObject io = mDatamodelFactory.createInformationObject();
		//Extracting values to identify the object we are going to get
		String hashAlg     = 
		        identifier.getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
	    String hashValue   = 
	    		identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
	    
	    
	    String uri = "ni:///" + hashAlg + ";" + hashValue;
	    
	    HttpPost post = createGet(uri);
	    
	    try {
	    	//Execute request
			HttpResponse response = mClient.execute(post);
			int responseCode = response.getStatusLine().getStatusCode();
			Log.d(TAG,"get(): Response Code is " + responseCode);
			
			if (responseCode == RESPONSE_CODE_203) {
			    Log.d(TAG, "" + response.getAllHeaders());
			    for (Header heather : response.getAllHeaders()) {
			        Log.d(TAG, "" + heather.getName() + ": " + heather.getValue());
			    }
			    HttpEntity entity = response.getEntity();
			    Log.d(TAG, "Entity: " + entity);
			    Header hdr = entity.getContentType();
			    Log.d(TAG, "Header: " + hdr);
				String contentType = hdr.getValue();
				Log.d(TAG, "String: " + contentType);
				Log.d(TAG,"Enter in Response Code 203");
				//Check if the response is a JSON
				if ("application/json".equalsIgnoreCase(contentType)) {
					Log.d(TAG,"Reading JSON");
					InputStream content = response.getEntity().getContent();
					Log.d(TAG,"Content" + content);
					String jsonString = streamToString(content);
					Log.d(TAG,"Content string" + jsonString);
					//Convert String to JSON Object
					JSONObject jsonObject = null;
					try {
						Object tempObject = JSONValue.parse(jsonString);
						Log.d(TAG, "JSONObject is " + JSONValue.parse(jsonString));
						jsonObject = (JSONObject) tempObject;
					} catch (Exception e) {
						Log.d(TAG,"Fan! " + e.toString());
					}
					Log.d(TAG,"JObject " + jsonObject.toString());
					//TODO Are we gonna do something with this?
					/*
					String netInfVersion = (String)jsonObject.get("NetInf");
					String msgid = (String)jsonObject.get("msgid");
					String ni = (String)jsonObject.get("ni");
					String ts = (String)jsonObject.get("ts");
					*/
					String ct = (String)(jsonObject.get("ct"));
					Log.d(TAG, "CT: " + ct);
					JSONObject metadata = (JSONObject)jsonObject.get("metadata");
					
					Log.d(TAG, "Metadata: " + metadata.toString());
					
					JSONArray locators = (JSONArray)(jsonObject.get("loc"));
					
					Log.d(TAG,"Locators: " + locators.toString());
					
					//Creating a Content Type label to the identifier
					IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
					label.setLabelName(SailDefinedLabelName.CONTENT_TYPE.getLabelName());
					label.setLabelValue(ct);
					identifier.addIdentifierLabel(label);
					
					//Updating io
					io.setIdentifier(identifier);
					
					for (Object locator: locators) {
						String loc = locator.toString();
						Log.d(TAG,"Locator: " + loc);
						String cutLocator = loc.split("://")[1];
						Log.d(TAG,"Cut locator: " + cutLocator);
						Attribute newLocator = mDatamodelFactory.createAttribute();
						newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
						newLocator.setIdentification(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
						newLocator.setValue(cutLocator);
						
						
						//TODO LocatorPriority??????????
						
						io.addAttribute(newLocator);
						
						
					}
	
				} 
				else {
					Log.e(TAG, "Wrong content type - The content type value is not application/json");
				}
				
				
			} else {
				if (responseCode == RESPONSE_CODE_200) {
					//TODO Do we need to implement this?
					Log.e(TAG, "Response Code 200 - Not handle yet");
				}
				else {
					Log.e(TAG, "Unexpected Response Code from server");
				}
			}
			
			
			
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.toString());
			io = null;
		} catch (SocketTimeoutException e) {
		    Log.d(TAG, "Returning null because we timed out");
		    Log.d(TAG, "You are probably not connected to the internet");
		    Log.d(TAG, "Also check if the server is up");
		    io = null;
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			io = null;
		}
	    
		return io;
	}

	@Override
	public List<Identifier> getAllVersions(Identifier arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(InformationObject io) {
	    Log.d(TAG, "put()");
	    
		//Extracting values from IO's identifier
		String hashAlg     = io.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
		String hash		   = io.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
		String ct		   = io.getIdentifier().getIdentifierLabel(SailDefinedLabelName.CONTENT_TYPE.getLabelName()).getLabelValue();
		
		Attribute bluetoothMacAttribute = io.getSingleAttribute(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
		String bluetoothMac = null;
        if(bluetoothMacAttribute != null){
            bluetoothMac = bluetoothMacAttribute.getValueRaw();
            bluetoothMac = bluetoothMac.substring(bluetoothMac.indexOf(":")+1);
        }
		
        // Get meta data, it is stored as an attribute in the io
        Attribute metaAttribute = io.getSingleAttribute(SailDefinedAttributeIdentification.META_DATA.getURI());
        String meta = null;
        if (metaAttribute != null) {
            meta = metaAttribute.getValueRaw();
            meta = meta.substring(meta.indexOf(":")+1);
        }
        
		HttpPost post = createPublish(hashAlg, hash, ct, bluetoothMac, meta);
		
		try {
		    Log.d(TAG, "Executing HTTP Post to " + post.getURI());
			HttpResponse response = mClient.execute(post);
			Log.d(TAG, "RESP_CODE: " + Integer.toString(response.getStatusLine().getStatusCode()));
			Log.d(TAG, "PUBLISH_RESP: " + streamToString(response.getEntity().getContent()));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
		    Log.d(TAG, "Did not put any objects because we timed out");
            Log.d(TAG, "You are probably not connected to the internet");
            Log.d(TAG, "Also check if the server is up");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
    private HttpPost createPublish(String hashAlg, String hash, String contentType, String bluetoothMac, String meta) {
	    HttpPost post = null;
	    try {
	        // TODO this should not be *index.php when calling the actual NRS
	        post = new HttpPost(NRS_SERVER + ":" + NRS_SERVER_PORT + "/netinfproto/publish");
	    }
	    catch (Exception e) {
	        Log.e(TAG, e.toString());
        }
	    Log.d(TAG, "Fuck!");
		MultipartEntity entity = new MultipartEntity();
		
		try {
			StringBody uri = new StringBody("ni:///" + hashAlg + ";" + hash + "?ct=" + contentType);
			entity.addPart("URI", uri);
		    
			StringBody msgid = new StringBody(Integer.toString(randomGenerator.nextInt(100000000))); // generate
			entity.addPart("msgid", msgid);
			
			//TODO Add the locators!
			if(bluetoothMac!=null){ 
                StringBody l = new StringBody("nimacbt://" + bluetoothMac);
                entity.addPart("loc1", l);
            }
			
			if(meta != null) {
			    StringBody ext = new StringBody(meta.toString());
			    entity.addPart("ext", ext);
			}
			
			StringBody rform = new StringBody("json");
			entity.addPart("rform", rform);
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
            entity.writeTo(System.out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		post.setEntity(entity);
		
		return post;
	}

	private static HttpPost createGet(String uri)  {
		
		HttpPost post = new HttpPost(NRS_SERVER + ":" + NRS_SERVER_PORT +"/netinfproto/get"); 
		
		String msgid = Integer.toString(randomGenerator.nextInt(100000000));
		String ext = "no extension";

		String completeUri= "URI="+uri+"&msgid="+msgid+"&ext="+ext;
		
		String encodeUrl = null;
		try {
			encodeUrl = URLEncoder.encode(completeUri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpEntity newEntity = new InputStreamEntity(fromString(encodeUrl), encodeUrl.getBytes().length);
		
		post.addHeader("Content-Type","application/x-www-form-urlencoded");
		post.setEntity(newEntity);
		return post;
	}


	@Override
	protected ResolutionServiceIdentityObject createIdentityObject() {
		// TODO Auto-generated method stub
		return null;
	}
	
    /**
     * Converts an InputStream into a String.
     * TODO Only here because of the ugly implementation?
     * @param input A input stream
     * @return String representation of the input stream
     */
	private String streamToString(InputStream input) {
        try {
            return new Scanner(input).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

	
	/**
	 * Converts a string to a type ByteArrayInputStream
	 * 
	 * @param str string to be converted
	 * 
	 * @return ByteArrayInputStream
	 */
	private static InputStream fromString(String str) {
		byte[] bytes = str.getBytes();
		return new ByteArrayInputStream(bytes);
	}
}
