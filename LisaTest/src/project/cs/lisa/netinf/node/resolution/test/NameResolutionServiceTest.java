package project.cs.lisa.netinf.node.resolution.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.Header;
import org.apache.http.entity.mime.MultipartEntity;


import project.cs.lisa.netinf.node.resolution.NameResolutionService;

import android.test.AndroidTestCase;

public class NameResolutionServiceTest extends AndroidTestCase {
	
	
	private DatamodelFactory mDatamodelFactory; 
	private NameResolutionService mNameResolutionService;
	
	public NameResolutionServiceTest() {
		mDatamodelFactory = new DatamodelFactoryImpl();
		mNameResolutionService = new NameResolutionService(mDatamodelFactory);
	}
	
//	private void testPublish() {
//		// TODO Auto-generated method stub
//
//	}
	
	public void testCreatePublish() {
		String hashAlg = "SHA-256"; 
		String hash = "jdaskd";
		String contentType = "hello/world";
		String bluetoothMac = "12:25";
		String meta = "{ meta : hello }";
		HttpPost result;
		
		Method method = null;
		try {
			method = NameResolutionService.class.getDeclaredMethod("createPublish", String.class, String.class, String.class, String.class, String.class);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		method.setAccessible(true);
		
		
		
		try {
			result = (HttpPost)method.invoke(mNameResolutionService, hashAlg, hash, contentType, bluetoothMac, meta);
			MultipartEntity entity = new MultipartEntity();
			assertNotNull(entity);
			assertNotSame(entity.getContentLength(), -1);
			assertEquals(entity.getContentType().getValue().split("[")[0],"multipart/form-data");
			InputStream stream = entity.getContent();
			System.out.println(stream.toString());
			//assertEquals(entity.getContent())
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void testCreateGet() {
		
		String uri = "ni:///sha-256;asdGJS67821";
		HttpPost result;
		
		Method method = null;
		
		try {
			method = NameResolutionService.class.getDeclaredMethod("createGet", String.class);
			result = (HttpPost)method.invoke(mNameResolutionService, uri);
			
			//HttpEntity
			
			
			
			
			
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
