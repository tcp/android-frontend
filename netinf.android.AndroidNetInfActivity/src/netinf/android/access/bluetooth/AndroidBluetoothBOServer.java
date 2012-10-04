package netinf.android.access.bluetooth;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.sql.ConnectionEvent;

import netinf.access.NetInfServer;
import netinf.android.common.communication.protobuf.TransferringMessages.TransferringMessage;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import com.google.inject.Inject;

public class AndroidBluetoothBOServer extends NetInfServer{
		
	   // Debugging
    private static final String TAG = "AndroidBluetoothBOTranferServer";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME= "AndroidBluetoothBOTranferServer";

    // Unique UUID for this application
    private static final UUID MY_UUID=
        UUID.fromString("ee4be6b0-7274-11e1-b0c4-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;


    private ConnectionListener mConnectionListener=null;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
   
    /**
     * Constructor. Prepares a new AndroidNetInfActivity session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    @Inject
    public AndroidBluetoothBOServer(){
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
    	
        if (mAdapter == null) {
        	//An exception must be thrown here!!!!!
        }  
    }
      
    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");
         if(mConnectionListener==null){
	         mConnectionListener = new ConnectionListener();
	         mConnectionListener.start();
         }
    }

    /**
     * Stop all threads
     * @throws IOException 
     */
    public synchronized void stop() throws IOException {
        if (D) Log.d(TAG, "stop");
        
        if (mConnectionListener != null) {
        	mConnectionListener.cancel();
        	mConnectionListener = null;
        }

    }

    /**
     * The listener interface for receiving connection events. The class that is interested in processing a connection event
     * implements this interface, and the object created with that class is registered with a component using the component's
     * <code>addConnectionListener<code> method. When
     * the connection event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see ConnectionEvent
     * @author PG Augnet 2, University of Paderborn
     */
    class ConnectionListener extends Thread {
       private boolean running;
       private final BluetoothServerSocket mmServerSocket;
       DataInputStream           dataIn = null;
       DataOutputStream         dataOut = null;
       
       public ConnectionListener() {
          running = true;        
          BluetoothServerSocket tmp = null;

          // Create a new listening server socket
          try {
               tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                          NAME, MY_UUID);  
          } catch (IOException e) {
              Log.e(TAG, "listen() failed (RfcommWithServiceRecord)", e);
          }
          mmServerSocket = tmp;
       }

       @Override
       public void run() {

	       boolean 			   transmitFile = false;
    	   
           try {
	        	if (D) Log.d(TAG, "Starting to listen for new connection within the BO Transfer Bluetooth Server");
	        	setName(" Bluetooth Connection Listener");
	
	        	BluetoothSocket mConnectedSocket;
	        	
	        	while(running){
	        	  	
		            if (D) Log.d(TAG, "In listen loop");
		            mConnectedSocket = mmServerSocket.accept();
		          
		            if (D) Log.d(TAG, "Accepted new connection.");
		            
		            // If a connection was accepted
		            if (mConnectedSocket != null) {
		
			   			dataIn      = new DataInputStream(mConnectedSocket.getInputStream());
			   			dataOut     = new DataOutputStream(mConnectedSocket.getOutputStream());
			   			TransferringMessage myReplyMessage;
			   			transmitFile = false;
		            	
			   			int payloadSize =  dataIn.readInt();
			   			byte[] data = new byte[payloadSize];
			   			dataIn.readFully(data);
			   					   			
			   			TransferringMessage myMessage = TransferringMessage.parseFrom(data);
			   			String hash = myMessage.getData();
			   			String filePath = Environment.getExternalStorageDirectory() + "/MySharedFiles/" + hash;
			   			
			   			File mTargetFile = new File (filePath);
			   			if(mTargetFile.exists()){
			   			 	    
			   				myReplyMessage = TransferringMessage.newBuilder()
	   								.setCode(TransferringMessage.RequestCode.REPLY_OK)
	   								.setData("OK")
	   								.build();	
			   				transmitFile = true;
			   			}
			   			else{
			   				
				   		    myReplyMessage = TransferringMessage.newBuilder()
	   								.setCode(TransferringMessage.RequestCode.REPLY_ERROR)
	   								.setData("Error")
	   								.build();			   				
			   			}
			   			   
   
					   byte[] testSendBuffer = myReplyMessage.toByteArray();
					   
					   dataOut.writeInt(testSendBuffer.length);
					   dataOut.write(testSendBuffer);
					   
					   if(transmitFile){
						   transferRequestedFile(filePath);
					   }
					   
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
		                	   dataOut.close();
		                   } catch (IOException e) {
		                       // TODO Auto-generated catch block
		                       e.printStackTrace();
		                   }
		               }
					   
		            }
	            }
             
          } catch (IOException e) {
        	 if (D) Log.e(TAG, "The Bluetooth Server encountered an error",e);
          }
       }

       @Override
       public void interrupt() {
          running = false;
          super.interrupt();
       }

       public boolean isRunning() {
          return running;
       }
       
       public void cancel() throws IOException {
           if (D) Log.d(TAG, "Cancel connection listener");
               mmServerSocket.close();

       }
       
       private void transferRequestedFile(String targetFilePath){
       	
       	InputStream inStream       = null;
       	BufferedInputStream bis    = null;
       	File mTargetFile           = null;

       	try {
           	//Open file
           	mTargetFile = new File(targetFilePath);
           	
       		inStream  = new FileInputStream(mTargetFile);
               bis    = new BufferedInputStream(inStream, 8 * 1024);
               
       		long fileSize = mTargetFile.length();            	                         

               byte[] buffer = new byte[8192];
               int len;
               
               dataOut.writeLong(fileSize);

               while ((len = bis.read(buffer)) != -1) {
            	   dataOut.write(buffer, 0, len);
                   len = dataOut.size();
               }
                
               dataOut.flush();
               
               Log.d(TAG, "sending data to connected thread");

           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }

           finally{
               if(inStream != null){
                   try {
                	   inStream.close();
                   } catch (IOException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                   }
               }
                             
               if(bis != null){
                   try {
                   	bis.close();
                   } catch (IOException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                   }
               }
               
           }     	
       }
       
    }
        
 
	@Override
	public String describe() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}
    

    
}