package project.cs.lisa.netinf.node.resolution.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;


import project.cs.lisa.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.netinf.node.resolution.NameResolutionService;

import android.test.AndroidTestCase;

public class NameResolutionServiceTest extends AndroidTestCase {
	
	
	private DatamodelFactory mDatamodelFactory; 
	private NameResolutionService mNameResolutionService;
	
	
	
	
	public NameResolutionServiceTest() {
		mDatamodelFactory = new DatamodelFactoryImpl();
		mNameResolutionService = new NameResolutionService("127,0,0,1",80,mDatamodelFactory);
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
			
			HttpEntity entity = result.getEntity(); 
			
			assertEquals(result.getHeaders("Content-Type"),"application/x-www-form-urlencoded");
			InputStream stream = entity.getContent();
			assertEquals(uri,stream.toString());
			
			
			
			
			
			
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
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void testPut() {
		/*
		String hashAlg = "sha-256";
		String hash = "asdas";
		String contentType = "plain/text";
		String bluetoothMac = "12:CD";
		
		Identifier identifier = mDatamodelFactory.createIdentifier();
	      
        //Creating the HASH_ALG label
        IdentifierLabel identifierLabel = mDatamodelFactory.createIdentifierLabel();
        identifierLabel.setLabelName(SailDefinedLabelName.HASH_ALG.getLabelName());
        identifierLabel.setLabelValue(hashAlg);
        identifier.addIdentifierLabel(identifierLabel);
    
        //Creating the HASH_CONTENT label
        IdentifierLabel identifierLabel2 = mDatamodelFactory.createIdentifierLabel();
        identifierLabel2.setLabelName(SailDefinedLabelName.HASH_CONTENT.getLabelName());
        identifierLabel2.setLabelValue(hash);
        identifier.addIdentifierLabel(identifierLabel2);
        
        IdentifierLabel identifierLabel3 = mDatamodelFactory.createIdentifierLabel();
        identifierLabel3.setLabelName(SailDefinedLabelName.CONTENT_TYPE.getLabelName());
        identifierLabel3.setLabelValue(contentType);
        identifier.addIdentifierLabel(identifierLabel3);
        
        InformationObject io = mDatamodelFactory.createInformationObject();
        io.setIdentifier(identifier);
       
        Attribute address = mDatamodelFactory.createAttribute();
        //address.setAttributePurpose(SailDefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
        address.setIdentification(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
        address.setValue(bluetoothMac);
        io.addAttribute(address);   
        
        mNameResolutionService.put(io);
       */
        
	}
	
	public void testGet() {
		/*
		Identifier identifier = mDatamodelFactory.createIdentifier();
		
		String hashAlg = "sha-256";
		String hash = "asdas";
	      
        //Creating the HASH_ALG label
        IdentifierLabel identifierLabel = mDatamodelFactory.createIdentifierLabel();
        identifierLabel.setLabelName(SailDefinedLabelName.HASH_ALG.getLabelName());
        identifierLabel.setLabelValue(hashAlg);
        identifier.addIdentifierLabel(identifierLabel);
    
        //Creating the HASH_CONTENT label
        IdentifierLabel identifierLabel2 = mDatamodelFactory.createIdentifierLabel();
        identifierLabel2.setLabelName(SailDefinedLabelName.HASH_CONTENT.getLabelName());
        identifierLabel2.setLabelValue(hash);
        identifier.addIdentifierLabel(identifierLabel2);
        
        
		InformationObject io = mDatamodelFactory.createInformationObject();
		
		io = mNameResolutionService.get(identifier);
		
		//Assert content type is not null
		assertNotNull(io.getIdentifier().getIdentifierLabel(SailDefinedLabelName.CONTENT_TYPE.getLabelName()));
		//Assert meta-data is not null
		assertNotNull(io.getIdentifier().getIdentifierLabel("metadata"));
		*/
		
		
	}

}
