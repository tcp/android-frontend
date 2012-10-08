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
package netinf.android.access.tcp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sql.ConnectionEvent;

import netinf.access.NetInfServer;
import netinf.android.common.communication.protobuf.TransferringMessages.TransferringMessage;
import android.os.Environment;
import android.util.Log;

/**
 * Provides access to a NetInf Node via TCP
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TCPServer extends NetInfServer {
   
   // Debugging
   private static final String TAG = "TCPServer";

   private ServerSocket serverSocket;
   private ConnectionListener connectionListener;
   
   private final int port;

   
   public TCPServer(int port) {
      this.port = port;
   }

   @Override
   public void start()  {
     
	  Log.d(TAG, "Starting TCP server on port = " + port);
      try {
         serverSocket = new ServerSocket(port);
         Log.d(TAG, "Starting connection listener");
         
         connectionListener = new ConnectionListener();
         connectionListener.start();
      } catch (IOException e) {
    	  Log.d(TAG, "There was an error creating the TCP server socket!");
    	  e.printStackTrace();
      }
   }

   @Override
   public void stop() throws IOException {
	   Log.d(TAG, "Stoping TCP server");

      if (connectionListener != null) {
         connectionListener.interrupt();
      }

      try {
         if (serverSocket != null) {
            serverSocket.close();
         }
      } catch (IOException e) {
         throw e;
      }
   }

   public int getPort() {
      return port;
   }

   public String getAddress() {
      return serverSocket.getInetAddress().toString();
   }

   @Override
   public boolean isRunning() {
      return connectionListener.isRunning();
   }

   @Override
   public String describe() {
      return "TCP on port " + port;
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
      
      public ConnectionListener() {
         running = true;
      }

      @Override
      public void run() {
    	 Log.d(TAG, "Connection listener");

         try {
            while (running) {
               Log.d(TAG, "Accepting socket connections");
               Socket mConnectedSocket = serverSocket.accept();
               Log.d(TAG, "Socket accepted!");
	            // If a connection was accepted
	            if (mConnectedSocket != null) {
	            	Log.d(TAG, "Creating a new FileTransferTask ");
		   			new FileTransferTask(mConnectedSocket).start();
		   			running = false;
	            }
            }
         } 
         catch (IOException e) {
            Log.e(TAG, "The TCP Server encountered an error");
            e.printStackTrace();
         }
         finally{        	 
             if (serverSocket != null) {
                 try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              }
         }
      }

      @Override
      public void interrupt() {
    	 Log.d(TAG, "Interrupting connection listener");

         running = false;
         super.interrupt();
      }

      public boolean isRunning() {
         return running;
      }
      
   }
   
   class FileTransferTask extends Thread {
	      private boolean running;

	      DataInputStream           dataIn = null;
	      DataOutputStream         dataOut = null;
	      boolean 			   transmitFile = false;
	      Socket mConnectedSocket = null;
	      
	      public FileTransferTask(Socket socket) {
	    	  mConnectedSocket = socket;
	      }

	      @Override
	      public void run() {
	
          		Log.d(TAG, "New FileTransferTask ");

	            if (mConnectedSocket != null) {
	
		   			try {
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
			   			
			   			Log.d(TAG, "Transferring message received - hash = " + hash);
			
			   			File mTargetFile = new File (filePath);
			   			Log.d(TAG, "Checking if the file exists in the file system");
			   			if(mTargetFile.exists()){
			   			 	    
			   				myReplyMessage = TransferringMessage.newBuilder()
	  								.setCode(TransferringMessage.RequestCode.REPLY_OK)
	  								.setData("OK")
	  								.build();	
			   				transmitFile = true;
				   			Log.d(TAG, "The file was found and will be transmitted");
			   			}
			   			else{
			   				
				   		    myReplyMessage = TransferringMessage.newBuilder()
	  								.setCode(TransferringMessage.RequestCode.REPLY_ERROR)
	  								.setData("Error")
	  								.build();	
				   		    Log.d(TAG, "The file requested was NOT found. Sending an error message");
			   			}
			   			   

					   byte[] testSendBuffer = myReplyMessage.toByteArray();
					   
					   dataOut.writeInt(testSendBuffer.length);
					   dataOut.write(testSendBuffer);
					   
					   if(transmitFile){
						   Log.d(TAG, "Transmitting the file!");
						   transferRequestedFile(filePath);
					   }
					   

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
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
			                	   dataOut.close();
			                   } catch (IOException e) {
			                       // TODO Auto-generated catch block
			                       e.printStackTrace();
			                   }
			               }
		   				
			               if(mConnectedSocket != null){
			            	   if(mConnectedSocket.isConnected())
				                   try {
				                	   mConnectedSocket.close();
				                   } catch (IOException e) {
				                       // TODO Auto-generated catch block
				                       e.printStackTrace();
				                   }
			               }
		   			}
		   
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
	      
	      private void transferRequestedFile(String targetFilePath){
	        	
	        	InputStream inStream       = null;
	        	BufferedInputStream bis    = null;
	        	File mTargetFile           = null;

	        	try {
	            	//Open file¨
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

}
