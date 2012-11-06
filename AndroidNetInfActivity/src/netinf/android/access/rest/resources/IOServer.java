package netinf.android.access.rest.resources;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import netinf.android.common.datamodel.SailDefinedAttributeIdentification;
import netinf.android.common.datamodel.SailDefinedLabelName;
import netinf.android.transferdispatcher.TransferDispatcher;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DeleteMode;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedException;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import android.os.Environment;
import android.util.Log;

public class IOServer extends AndroidNetInfResource {	
	
    // Debugging
    private static final String TAG = "IOServer";
    private static final boolean D = true;
	
	//HTTP Methods' labels	
    private final static String GET    = "GET";
    private final static String PUT    = "PUT";
    private final static String DELETE = "DELETE";
    private final static String CACHE  = "CACHE";

    //IO's variable	   
    private String hashAlg;
    private String hashContent;
    private String methodType;
    private String bluetoothMac;
    private String wifiMac;
    private String ncsUrl;
   
    private DatamodelFactory datamodelFactory;
    private NetInfNodeConnection nodeConnection;
	     
    //Initializes the context of a IOServer
    @Override
    protected void doInit() {
      super.doInit();
      hashAlg     = getQuery().getFirstValue("HASH_ALG", true);
      hashContent = getQuery().getFirstValue("HASH_CONT", true);
      methodType  = getQuery().getFirstValue("METHOD", true);
      bluetoothMac= getQuery().getFirstValue("BTMAC", true);
      wifiMac     = getQuery().getFirstValue("WIMAC", true);
      ncsUrl      = getQuery().getFirstValue("NCS", true); 
      datamodelFactory = getDatamodelFactory();
      nodeConnection   = getNodeConnection();
      
    }
	   
	@Get  
 	public InformationObject processHTTPRequest() {  
		
		if(methodType.equals(GET)){			 			
			return getIO();
		}
		if(methodType.equals(PUT)){	      	
			putIO();
			return null;
		}
		if(methodType.equals(DELETE)){	      				   		 
			deleteIO();		
			return null;
		}
		if(methodType.equals(CACHE)){
			putIO();
			return null;	
		}
		else 
			return null;		
	}
	
    @Post  
    public String processHTTPPostRequest(Representation entity) {  
		byte[] myByteArray = null;
		String resultFilePath = null;
        
        Form form = new Form(entity); 
        InformationObject myIO = formToIO(form);
            
        if(myIO!=null){
        	
        	TransferDispatcher tsDispatcher = TransferDispatcher.getInstance();
        	
	        List<Attribute> locators = myIO.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());	        	       
	        if (!locators.isEmpty()) {     
	        	
        		if(D) Log.d(TAG, "The Transfer dispatcher will attempt to transfer the file from a suitable device/NCS");
	        	
		        try {
		        	if(D) Log.d(TAG, "Calling Transfer dispatcher getByteArray");
		        	myByteArray = tsDispatcher.getByteArray(myIO);
				} 
		
		        catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        
				if(myByteArray!=null){
					String hash = myIO.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
					resultFilePath = Environment.getExternalStorageDirectory() + "/MySharedFiles/" + hash;
			        writeByteStreamToFile (resultFilePath, myByteArray);		        				
				} 

	        }
	        else{
	        	Identifier identifier = myIO.getIdentifier();
				String hash = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
				String alg  = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
	        	tsDispatcher.cacheContentInNCS(alg,hash);
	        	if(D) Log.d(TAG, "The Transfer dispatcher will cache the content in the NCS");
	        	
	        }
        }

        return resultFilePath;  
    }
	
	private InformationObject getIO(){
		
		InformationObject io = null;
		
		//Create Identifier		
		Identifier myIdentifier = createIdentifier(hashAlg,hashContent);
		
		try {
			io = nodeConnection.getIO(myIdentifier);
		} catch (NetInfCheckedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    if (io != null) {	    	     	  	    	  
	    	return io;
	    }

		return null;		
	}
	
	private void putIO(){
		
		//Create dummy IO
        InformationObject dummyIO = datamodelFactory.createInformationObject();
        
        //Creating and setting the identifier
        Identifier identifier     = createIdentifier(hashAlg,hashContent);
        dummyIO.setIdentifier(identifier); 
        
        if(bluetoothMac.length()>0){
			Attribute newAddress   = datamodelFactory.createAttribute();
			newAddress.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
			newAddress.setIdentification(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
			newAddress.setValue(bluetoothMac);
			dummyIO.addAttribute(newAddress); 	
        }
        
        if(wifiMac.length()>0){
			Attribute newAddress   = datamodelFactory.createAttribute();
			newAddress.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
			newAddress.setIdentification(SailDefinedAttributeIdentification.WIFI_MAC.getURI());
			newAddress.setValue(wifiMac);
			dummyIO.addAttribute(newAddress);  	
        }
        
        if(ncsUrl.length()>0){
			Attribute newAddress   = datamodelFactory.createAttribute();
			newAddress.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
			newAddress.setIdentification(SailDefinedAttributeIdentification.NCS_URL.getURI());
			newAddress.setValue(ncsUrl);
			dummyIO.addAttribute(newAddress);  	
        }
                                	       
        //Putting the IO
		try {
			nodeConnection.putIO(dummyIO);
			
		} catch (NetInfCheckedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void deleteIO(){
		
  		//Create dummy Identifier       
		Identifier myIdentifier    = createIdentifier(hashAlg,hashContent);
        
        //Create dummy IO 
        InformationObject ioToDelete = datamodelFactory.createInformationObject();
        ioToDelete.setIdentifier(myIdentifier);
		       
        try {
			nodeConnection.deleteIO(ioToDelete, DeleteMode.DELETE_DATA);
		 } catch (NetInfCheckedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 } 			
	}
	
	private InformationObject formToIO(Form form){
		
		int counter,i = 0;
		Random randomGenerator = new Random();
		InformationObject myReceivedIO = datamodelFactory.createInformationObject();
		
        String hashAlg = form.getFirstValue("hashAlg");
        String hashContent = form.getFirstValue("hashContent"); 
        
        //Creating and setting the identifier
        Identifier identifier     = createIdentifier(hashAlg,hashContent);
        myReceivedIO.setIdentifier(identifier); 
		
        //Add Bluetooth locators
        
        String [] bluetoothLocators = form.getValuesArray("bluetooth_locator");        
        counter = bluetoothLocators.length;
        
        for(i=0;i<counter;i++){
        				
			Attribute newLocator   = datamodelFactory.createAttribute();
            newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            newLocator.setIdentification(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
            newLocator.setValue(bluetoothLocators[i]);
            
            Attribute priorityAttr = datamodelFactory.createAttribute();
            priorityAttr.setIdentification(SailDefinedAttributeIdentification.LOCATOR_PRIORITY.getURI());
            priorityAttr.setValue(randomGenerator.nextInt(5000));
            newLocator.addSubattribute(priorityAttr);
            				                			                
            myReceivedIO.addAttribute(newLocator);
        }
       
        //Clearing counters
        
        i=0;
        counter = 0;
        
        //Add Wi-Fi locators
        
        String [] wifiLocators = form.getValuesArray("wifi_locator");    
        counter = wifiLocators.length;
        
        for(i=0;i<counter;i++){
        				
			Attribute newLocator   = datamodelFactory.createAttribute();
            newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            newLocator.setIdentification(SailDefinedAttributeIdentification.WIFI_IP.getURI());
            newLocator.setValue(wifiLocators[i]);
            
            Attribute priorityAttr = datamodelFactory.createAttribute();
            priorityAttr.setIdentification(SailDefinedAttributeIdentification.LOCATOR_PRIORITY.getURI());
            priorityAttr.setValue(randomGenerator.nextInt(5000));
            newLocator.addSubattribute(priorityAttr);
            				                			                
            myReceivedIO.addAttribute(newLocator);
        }
        
        //Clearing counters
        
        i=0;
        counter = 0;
        
        //Add NCS locators
        
        String [] NCSLocators = form.getValuesArray("ncs_locator");   
        counter = NCSLocators.length;
        
        for(i=0;i<counter;i++){
        				
			Attribute newLocator   = datamodelFactory.createAttribute();
            newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            newLocator.setIdentification(SailDefinedAttributeIdentification.NCS_URL.getURI());
            newLocator.setValue(NCSLocators[i]);
            
            Attribute priorityAttr = datamodelFactory.createAttribute();
            priorityAttr.setIdentification(SailDefinedAttributeIdentification.LOCATOR_PRIORITY.getURI());
            priorityAttr.setValue(randomGenerator.nextInt(5000));
            newLocator.addSubattribute(priorityAttr);
            				                			                
            myReceivedIO.addAttribute(newLocator);
        }
		
		return myReceivedIO;		
	}
	
	private void writeByteStreamToFile (String targetPath, byte[] myByteArray){
			
		FileOutputStream fos = null;
       
        try {
			fos = new FileOutputStream(targetPath);
			fos.write(myByteArray);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally{	        	
   			if(fos!=null)
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}        	
        }		
	}	
}
