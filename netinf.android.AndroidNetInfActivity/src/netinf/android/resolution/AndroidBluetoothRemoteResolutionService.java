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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import netinf.android.common.communication.BluetoothConnection;
import netinf.android.common.communication.RemoteBluetoothConnection;
import netinf.android.common.datamodel.SailDefinedLabelName;
import netinf.common.communication.NetInfDeletedIOException;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.transfer.TransferJob;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.eventprocessing.EventPublisher;

import org.apache.log4j.Logger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * The Class RemoteResolutionService.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class AndroidBluetoothRemoteResolutionService extends AndroidAbstractResolutionService implements ResolutionService {

	private static final String TAG       = "AndroidBluetoothRemoteResolutionService";
	private static final String TTL_VALUE = "1";
	
	//Locator variables
    private final static String LOCAL     = "local:";
    private final static String BLUETOOTH = "bt:";
	
	private static final Logger LOG = Logger.getLogger(AndroidBluetoothRemoteResolutionService.class);

   private final RemoteBluetoothConnection communicator;

   private DatamodelFactory datamodelFactory;
   


   private final BluetoothAdapter mAdapter;
   private String myRemotePeerAddress;
   
   // Unique UUID for this application
   private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

   private boolean connected;

   @Inject
   public AndroidBluetoothRemoteResolutionService(RemoteBluetoothConnection communicator, @Named("access.bluetooth.remote.address")String remoteDevice){
      super();
      this.communicator = communicator;
      
  	  mAdapter = BluetoothAdapter.getDefaultAdapter();
	
  	  if (mAdapter == null) {
    	//An exception must be thrown here!!!!!
      }
  	  
  	  myRemotePeerAddress = remoteDevice;
   }
     
   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      this.datamodelFactory = factory;
   }

   public void setUp(String host, Integer port, SerializeFormat serializeFormat) {
      LOG.trace(null);
      this.connected = true;

   }

   public void tearDown() {
      this.communicator.tearDown();
   }



   public boolean isConnected() {
      return this.connected;
   }

   @Override
   public void delete(Identifier identifier) {
   }

   private BluetoothConnection getBluetoothConnection(String remotePeerAddress){
	   
	   BluetoothSocket mmSocket = null;
	   BluetoothConnection btConnection = null;
	   
	   BluetoothDevice device = mAdapter.getRemoteDevice(remotePeerAddress);
	   BluetoothSocket tmp = null;

       try {
             tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);          
       } catch (IOException e) {
           Log.e(TAG, "create() failed - (createInsecureRfcommSocketToServiceRecord) ", e);
       }
       mmSocket = tmp;
       
        // Always cancel discovery because it will slow down a connection
       mAdapter.cancelDiscovery();

       // Make a connection to the BluetoothSocket
       try {
           // This is a blocking call and will only return on a
           // successful connection or an exception
           mmSocket.connect();      
	   		try {
	   			btConnection = new BluetoothConnection(mmSocket);
	   			return btConnection;
	   			
	   		} catch (IOException e) {
	   			// TODO Auto-generated catch block
	   			e.printStackTrace();
	   		}
           
       } catch (IOException e) {
           // Close the socket
           try {
               mmSocket.close();
           } catch (IOException e2) {
               Log.e(TAG, "unable to close()  socket during connection failure", e2);
           }
           
           //What happends if I fail during the connection
           
//           connectionFailed();
//           return;
       }
       

		return null;	   
   }
   
   
   @Override
   public InformationObject get(Identifier identifier) {
	    
	    //First it is check if the identifier already contains the TTL label.
        //	   
	    //If the identifier contains that label, it means that is a request created
	    //by another peer and should be therefore not forward assuming TTL value =1.
	    //
	    //If none TTL label has been set yet, it means that the request is being created
	    //by this device and therefore I can be forwarded.
	   
	    if(identifier.getIdentifierLabel((SailDefinedLabelName.TTL).getLabelName())==null){
	    	 
	    	//Get the bluetooth connection to the remote peer. This should be a for in order 
	    	//to get all the surronding peers
	    	  
	    	BluetoothConnection myBTConnection = getBluetoothConnection(myRemotePeerAddress);
	    	if(myBTConnection!=null){
	    		//The request has been created locally
		    	try {	  	 
			    	 //Setting the TTL value to the identifier
			    	 IdentifierLabel mTtlLabel = datamodelFactory.createIdentifierLabel();
			    	 mTtlLabel.setLabelName(SailDefinedLabelName.TTL.getLabelName());
			    	 mTtlLabel.setLabelValue(TTL_VALUE);
			         identifier.addIdentifierLabel(mTtlLabel);
			    	 InformationObject result = this.communicator.getIO(identifier,myBTConnection);
			    	 if(result!=null){
				        List<Attribute> locators = result.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());	        	       
				        if (!locators.isEmpty()) {
				             
				        	String locator = locators.get(0).getValue(String.class);
				        	
				        	if(locator.contains(LOCAL)){
				        		//The file is stored remotely. We have to change the locator attribute
				        		String nameOfFile = locator.substring(locator.indexOf(":") + 1);
				        		
				        		//Deleting remote locator
				        		result.removeAttribute(DefinedAttributeIdentification.HTTP_URL.getURI());
				        		
				                //Creating and setting the new locator
				                Attribute newLocator = datamodelFactory.createAttribute();
				                newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
				                newLocator.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
				                newLocator.setValue(BLUETOOTH + nameOfFile);
				                result.addAttribute(newLocator);
				                
						        TransferJob mTransferJob = communicator.startTransfer(LOCAL + nameOfFile,"destination");
						        if(mTransferJob!=null)
						        	System.out.print("hola");
				                
				        	}
				        }
				        

				     }
			    	 
			    	 return result;
			      } catch (NetInfDeletedIOException ex) {
			         // FIXME The information that the IO has been deleted is lost here!
			         return null;
			      } catch (NetInfCheckedException ex) {
			         LOG.debug("Error getting the Information Object with Identifier: " + identifier, ex);
			         throw new NetInfUncheckedException(ex);		         
			      } 
	    		  finally{
	    			 try {
						myBTConnection.close();
					 } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					 }	    			
	    		  }	
		     }
		    else
		    	//The remote connection could not be established
		    	return null;
	    }
	    else
	    	//The request was created in another peer. Therefore it is not forwarded
	    	return null;
   }

   @Override
   public List<Identifier> getAllVersions(Identifier identifier) {
      try {
         List<Identifier> identifiers = new ArrayList<Identifier>();
         List<InformationObject> ios = this.communicator.getIOs(identifier);
         for (InformationObject io : ios) {
            identifiers.add(io.getIdentifier());
         }
         return identifiers;
      } catch (NetInfCheckedException ex) {
         LOG.debug("Error getting the Information Objects with Identifier: " + identifier, ex);
         throw new NetInfUncheckedException(ex);
      }
   }

   @Override
   public void put(InformationObject informationObject) {

   }

   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = this.datamodelFactory.createResolutionServiceIdentityObject();
      identity.setName("Bluetooth Remote Resolution Service ");
      identity.setDescription("This Resolution service uses a remote resolution service");
      identity.setDefaultPriority(10);
      return identity;
   }

   /**
    * This method is not supported for RemoteResolutionService
    */
   @Override
   public void addEventService(EventPublisher eventPublisher) {
      throw new UnsupportedOperationException("RemoteResolutionService will not publish events");
   }


   @Override
   public String describe() {
      return "a Remote Resolution service over Bluetooth";
   }

}
