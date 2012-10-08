package netinf.android.transferdispatcher.providers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import netinf.android.AndroidNetInfActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Environment;
import android.util.Log;


public class HttpProvider implements ByteArrayProvider {

    // Debugging
    private static final String TAG = "HttpProvider";
    private static final boolean D = true;
    
    	
	private final static String NIHTTP = "nihttp://";
	private static String NCS_SERVER;
	   
    HttpClient client = new DefaultHttpClient();
	HttpResponse response;
	int resp_code=0;
	
	static MultipartEntity reqEntity = new MultipartEntity();
	static HttpPost post;
	static Random randomGenerator = new Random();
		
	public HttpProvider(){
		
		NCS_SERVER= AndroidNetInfActivity.NCS_SERVER_URL;
		
	}
	
	
	@Override
	public byte[] getByteArray(String locator, String hash) {
		
		String niname = "ni:///sha-256;" + hash;
		String c_type;
		byte[] data = new byte[300000];
		
		try {
			
			if(D) Log.d(TAG, "Creating get request for niname = " + niname);
			if (createget(NCS_SERVER, niname)) {
							
				response = client.execute(post);
				resp_code = response.getStatusLine().getStatusCode();	
				if(D) Log.d(TAG, "response code received from the NCS = " + resp_code );
											
				if (200 == resp_code) {
					// Get content type
					c_type = response.getEntity().getContentType().getValue();
					
					if ("application/octet-stream".equalsIgnoreCase(c_type)) {
						// Response is content
						InputStream content = response.getEntity().getContent();

						
						ByteArrayOutputStream out = null;
						try {
							
					        out = new ByteArrayOutputStream();
					        
					        // Copy the bits from instream to outstream
					        byte[] buf = new byte[1024];
					        int len;
					        while ((len = content.read(buf)) > 0) {
					            out.write(buf, 0, len);
					        }
					        
					        if(D) Log.d(TAG, "Amount of bytes received from the NCS = " + len );
					        data = out.toByteArray();
					        content	.close();
					        out.close();
					        
					        return data;
					        
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
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
		
		return null;
	}
	

	
	
	@Override
	public boolean canHandle(String url) {
	      if (url.startsWith(NIHTTP)) {
	          return true;
	       }
	       return false;
	}

	@Override
	public String describe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean cacheContent(String hashAlg, String hashCont){
		
		
		 boolean success = false;          		                		   
         HttpResponse response;    		
 		 Random randomGenerator = new Random();    		    	

 				
 		 HttpClient client = new DefaultHttpClient();
 		 HttpPost post = new HttpPost(NCS_SERVER+ "/.well-known/netinfproto/publish");
 		    		
			try {
	
				ContentBody url = new StringBody("ni:///" + hashAlg + ";" + hashCont); 
				ContentBody msgid = new StringBody(Integer.toString(randomGenerator.nextInt(100000000)));
				ContentBody fullPut = new StringBody("yes");
				ContentBody ext = new StringBody("no extension");						
				ContentBody bin = new FileBody(new File(Environment.getExternalStorageDirectory() + "/MySharedFiles/" + hashCont));
				MultipartEntity reqEntity = new MultipartEntity();
				reqEntity.addPart("octets", bin);
				reqEntity.addPart("URI",url);
				reqEntity.addPart("msgid", msgid);
				reqEntity.addPart("fullPut", fullPut);
				reqEntity.addPart("ext", ext);	
				
				post.setEntity(reqEntity);				
				
	    		response = client.execute(post);
	    		
	    		int resp_code = response.getStatusLine().getStatusCode();
	    		if(resp_code==200)
	    			success =true;
	    		
				System.err.println("RESP_CODE: "+Integer.toString(resp_code));
				if(D) Log.d(TAG, " Responde code received from the NCS = "+ resp_code);
							
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // -n
			catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    
		 
		 
		 if(D) Log.d(TAG, " The content has been cached in the NCS");
		
		 return success;
		
	}
	
	boolean createget(String dst, String name)  {
		
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
	
	public static InputStream fromString(String str)
	{
		byte[] bytes = str.getBytes();
		return new ByteArrayInputStream(bytes);
	}

	
}
