package netinf.android.transferdispatcher.providers;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import netinf.android.common.communication.protobuf.TransferringMessages.TransferringMessage;
import android.util.Log;

public class TcpProvider implements ByteArrayProvider {

    // Debugging
    private static final String TAG = "TcpProvider";
    private static final boolean D = true;
    
    private static final int retryAttempts = 3;
    	
	private final static String NIIP = "niip://";
		

	@Override
	public byte[] getByteArray(String locator, String hash) {
	    
	    byte[] data = new byte[300000];
	    int connectAttemptsCounter = 0;

	    if(locator.contains(NIIP)){
	    	if(D) Log.d(TAG, "Socket Connection process start");
			String ip = getIpAddressFromLocator (locator);
			
			Socket socket = null;
			
			Socket socket1 = null;
			Socket socket2 = null;
			Socket socket3 = null;
			
			while(connectAttemptsCounter < retryAttempts){
				connectAttemptsCounter++;
				
				if(connectAttemptsCounter == 1){
					socket1 = new Socket();
					socket  = socket1;
					Log.d(TAG, "Socket 1");
				}
				if(connectAttemptsCounter == 2){
					socket2 = new Socket();
					socket  = socket2;
					Log.d(TAG, "Socket 2");
				}
				if(connectAttemptsCounter == 3){
					socket3 = new Socket();
					socket  = socket3;
					Log.d(TAG, "Socket 3");
				}
		    	try {
//		    		socket = new Socket();
					socket.bind(null);
					Log.d(TAG, "Connecting socket to remote peer. Target IP = " + ip);
					socket.connect(new InetSocketAddress(ip, 5000), 5000);
					Log.d(TAG, "Socket Connection process finished - Socket connected!!");
					if(D) Log.d(TAG, " Transferring Process - Starting file transfer");
				    data = getByteArrayFromRemotePeer(socket,hash);
			        if(data!=null){
			        	if(D) Log.d(TAG, " Transferring Process - File transferring done!");
			        	return data;
			        }
				    
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d(TAG, "Connection error! - Socket not connected");
				} finally {
	                if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
	                }
	            }
			}			

//
//			
//		    while(connectAttemptsCounter < retryAttempts){
//			 
//		    	socket = new Socket();
//		    	connectAttemptsCounter++;
//		    	// Make a connection to the BluetoothSocket
//			    try{
//			    	if(socket!=null){
//			    		Log.d(TAG, "Socket !=null");
//			    		Log.d(TAG, "Is bound? = " + socket.isBound());
//			    		Log.d(TAG, "Is closed? = " + socket.isClosed());
//			    		Log.d(TAG, "Is connected? = " + socket.isConnected());
//			    		socket = null;
//			    		
//			    	}
//			    	socket.bind(null);
//			    	Log.d(TAG, "Connecting socket to remote peer. Target IP = " + ip);
//					socket.connect(new InetSocketAddress(ip, 5000), 5000); 
//					Log.d(TAG, "Socket connected!!");
//			    		
//				    data = getByteArrayFromRemotePeer(socket,hash);
//				    if(data!=null)
//				        return data;
//			        
//			    } catch (IOException e) {
//					// TODO Auto-generated catch block
//			    	Log.d(TAG, "Error connecting to the socket. Retry attempt = " + connectAttemptsCounter);
//					e.printStackTrace();
//				}
//		        finally{	    	   
//	    	           try {
//	    	              if(socket!=null){
//	    	            	  socket.close();
//	    	              }
//	    	           } catch (IOException e2) {
//	    	               Log.e(TAG, "unable to close()  socket during connection failure", e2);
//	    	           }
//		    	           
//		    	 }		
//		    }
//		  return null;
		}
		return null;
	}
	

	@Override
	public boolean canHandle(String url) {
	      if (url.startsWith(NIIP)) {
	          return true;
	       }
	       return false;
	}

	@Override
	public String describe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private String getIpAddressFromLocator (String locator){
		
		String ip = "";		
		ip = locator.substring(locator.indexOf("/") + 2);				
		return ip;
		
	}
	
	private byte[] getByteArrayFromRemotePeer(Socket mmSocket, String hash){
		
		byte[] buffer = new byte[8192];
	    byte[] data = new byte[300000];
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
   		    else{
  	    	  if(D) Log.d(TAG, "TransferringMessage response code from the server was different than OK - Code=" + myReplyMessage.getCode() + "Data=" + myReplyMessage.getData());
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
