package netinf.android.transferdispatcher.providers;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import netinf.android.common.communication.protobuf.TransferringMessages.TransferringMessage;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothProvider implements ByteArrayProvider {

    // Debugging
    private static final String TAG = "BluetoothStreamProvider";
    private static final boolean D = true;
    
    private static final int retryAttempts = 3;
    
    private static final UUID BO_TRANSFER_UUID=
            UUID.fromString("ee4be6b0-7274-11e1-b0c4-0800200c9a66");    
	
	private final static String NIMACBT = "nimacbt://";
	
	private BluetoothAdapter mBluetoothAdapter = null;
	BluetoothSocket mmSocket = null;
	
	public BluetoothProvider(){
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
      	if (mBluetoothAdapter == null) {
      		//Error must be thrown here!!!
      	} 
		
	}
	
	@Override
	public byte[] getByteArray(String locator, String hash) {
	    
	    byte[] data = new byte[3000000];
	    int connectAttemptsCounter = 0;
	    
	    
	    if(locator.contains(NIMACBT)){
	    	if(D) Log.d(TAG, " Connection process   - Starting connection process!");
	    	String mac = getMacAddressFromLocator (locator);
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
			
			BluetoothSocket mmSocket = null;
			BluetoothSocket tmp      = null;


		    try {
		          tmp = device.createInsecureRfcommSocketToServiceRecord(BO_TRANSFER_UUID); 
		    } catch (IOException e) {
		        if(D) Log.e(TAG, "create() failed - (createInsecureRfcommSocketToServiceRecord) ", e);
		    }
		    mmSocket = tmp;
		    
		    // Always cancel discovery because it will slow down a connection
		    mBluetoothAdapter.cancelDiscovery();
			
		    while(connectAttemptsCounter < retryAttempts){
			 
		    	connectAttemptsCounter++;
		    	// Make a connection to the BluetoothSocket
			    try {
			        // This is a blocking call and will only return on a
			        // successful connection or an exception
			    		
				        mmSocket.connect();
				        if(D) Log.d(TAG, " Connection process   - Connection established - Socket connected");
				        if(D) Log.d(TAG, " Transferring Process - Starting file transfer");
				        data = getByteArrayFromRemotePeer(mmSocket,hash);
				        if(data!=null){
				        	if(D) Log.d(TAG, " Transferring Process - File transferring done!");
				        	return data;
				        }
			        
			    }
			      catch (IOException e) {
			    	  if(D) Log.e(TAG, "failure while connecting to the socket - (createInsecureRfcommSocketToServiceRecord) ", e);
			    }
		       finally{	    	   
	    	           try {
	    	              if(mmSocket.isConnected()){
	    	            	  mmSocket.close();
	    	              }
	    	           } catch (IOException e2) {
	    	               Log.e(TAG, "unable to close()  socket during connection failure", e2);
	    	           }
		    	           
		    	 }		
		    }
		  return null;
		}
		return null;
	}
	

	@Override
	public boolean canHandle(String url) {
	      if (url.startsWith(NIMACBT)) {
	          return true;
	       }
	       return false;
	}

	@Override
	public String describe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private String getMacAddressFromLocator (String locator){
		
		String macAddress = "";		
		macAddress = locator.substring(locator.indexOf("/") + 2);				
		return macAddress;
		
	}
	
	private byte[] getByteArrayFromRemotePeer(BluetoothSocket mmSocket, String hash){
		
		byte[] buffer = new byte[8192];
	    byte[] data = new byte[3000000];
	    ByteArrayOutputStream byteBuffer  = new ByteArrayOutputStream();
		
	    DataInputStream           dataIn = null;
	    DataOutputStream         dataOut = null;
	    
	 
	    try {
	   	
		    dataIn = new DataInputStream(mmSocket.getInputStream());
		    dataOut = new DataOutputStream(mmSocket.getOutputStream());
	    	
   		    //Send
   		    
   		    TransferringMessage myMessage = TransferringMessage.newBuilder()
   		    								.setCode(TransferringMessage.RequestCode.REQUEST)
   		    								.setData(hash)
   		    								.build();
		   		    
		   		    
   		    byte[] testSendBuffer = myMessage.toByteArray();
   		    
   		    dataOut.writeInt(testSendBuffer.length);
   		    dataOut.write(testSendBuffer);
   		    
   			int controlMessageSize =  dataIn.readInt();
   			byte[] controlMessage = new byte[controlMessageSize];
   			dataIn.readFully(controlMessage);
   					   			
   			TransferringMessage myReplyMessage = TransferringMessage.parseFrom(controlMessage);
   		    if(myReplyMessage.getCode()==TransferringMessage.RequestCode.REPLY_OK){
   		    	
   		    	
   		    	long payloadSize = dataIn.readLong();		   					   			
							 	   												     	            
				int len=0;
				while(len<payloadSize){
					len = dataIn.read(buffer);
				    byteBuffer.write(buffer, 0, len);
				    len = byteBuffer.size();
				    System.out.print(len);
				}
				
				
				data = byteBuffer.toByteArray();
				len = data.length;
   		    	
   		    	return data;
   		    }
	        
	    }
	      catch (IOException e) {
	    	  if(D) Log.e(TAG, "failure while connecting to the socket - (createInsecureRfcommSocketToServiceRecord) ", e);
	    }
       finally{	    	   
    
               if(dataIn != null){
                   try {
                	   dataIn.close();
                   } catch (IOException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                   }
               }
               if(dataOut != null){
                   try {
                	   dataIn.close();
                   } catch (IOException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                   }
               }
                   
                   if(byteBuffer != null){
                       try {
                    	  byteBuffer.close();
                       } catch (IOException e) {
                           // TODO Auto-generated catch block
                           e.printStackTrace();
                       }
                   }
	           try {
	               mmSocket.close();
	           } catch (IOException e2) {
	               Log.e(TAG, "unable to close()  socket during connection failure", e2);
	           }
    	           
    	 }	    		
		 return null;
	}


	
}
