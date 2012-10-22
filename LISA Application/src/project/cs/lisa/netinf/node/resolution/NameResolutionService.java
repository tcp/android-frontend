package project.cs.lisa.netinf.node.resolution;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.node.resolution.ResolutionService;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONObject;

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
	private static final int TIMEOUT = 2000;
	//TODO is this ok?
	private static final Random randomGenerator = new Random();
	private HttpClient mClient; 
	
	@Inject
	public NameResolutionService() {
	    // Setup HTTP client
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        mClient = new DefaultHttpClient(params);
	}
	
	@Override
	public void delete(Identifier arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String describe() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InformationObject get(Identifier arg0) {
		// TODO Auto-generated method stub
		return null;
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
		
		HttpPost post = createPublish(hashAlg, hash, ct, bluetoothMac);
		
		try {
		    Log.d(TAG, "Executing HTTP Post to " + post.getURI());
			HttpResponse response = mClient.execute(post);
			Log.d(TAG, "RESP_CODE: " + Integer.toString(response.getStatusLine().getStatusCode()));
			Log.d(TAG, "PUBLISH_RESP: " + streamToString(response.getEntity().getContent()));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
    private HttpPost createPublish(String hashAlg, String hash, String contentType, String bluetoothMac) {
	    HttpPost post = null;
	    try {
	        // TODO this should not be *index.php when calling the actual NRS
	        post = new HttpPost(NRS_SERVER + ":" + NRS_SERVER_PORT + "/netinfproto/publish/index.php");
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
			
			StringBody rform = new StringBody("json");
			entity.addPart("rform", rform);
			
			JSONObject jsonext = new JSONObject();
			JSONObject meta = new JSONObject();
			meta.put("ct", contentType);
			meta.put("time", "today");
			jsonext.put("meta", meta);
			
			StringBody ext = new StringBody(jsonext.toString());
			entity.addPart("ext", ext);
			
			
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

}
