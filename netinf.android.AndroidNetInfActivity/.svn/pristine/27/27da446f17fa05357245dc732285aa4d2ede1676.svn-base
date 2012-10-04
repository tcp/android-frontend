package netinf.android.common.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import netinf.common.communication.AtomicMessage;
import netinf.common.communication.Connection;
import android.bluetooth.BluetoothSocket;

public class BluetoothConnection implements Connection {
	   
	   private final BluetoothSocket bluetoothSocket;
	   private final DataInputStream in;
	   private final DataOutputStream out;

	   private static final int MAX_PAYLOAD_SIZE = 1048576;

	   public BluetoothConnection(BluetoothSocket socket) throws IOException {
	      this.bluetoothSocket = socket;
	      this.in = new DataInputStream(socket.getInputStream());
	      this.out = new DataOutputStream(socket.getOutputStream());
	   }
	  
	   public void send(AtomicMessage message) throws IOException {
	      synchronized (this.out) {
	         this.out.writeInt(message.getEncoding());
	         this.out.writeInt(message.getPayload().length);
	         this.out.write(message.getPayload());
	      }
	   }

	   public AtomicMessage receive() throws IOException {
	      synchronized (this.in) {
	           try{
			    	 int encoderId = this.in.readInt();
			         int payloadSize = this.in.readInt();
		
			         if (payloadSize > MAX_PAYLOAD_SIZE) {
			            throw new IOException("Payload size " + payloadSize + " exceeds " + MAX_PAYLOAD_SIZE + " bytes");
			         }
		
			         byte[] data = new byte[payloadSize];
			         this.in.readFully(data);
			         return new AtomicMessage(encoderId, data);
	          }
	           catch (IOException ex) {
	        	   	 this.close();
			         throw new IOException(ex);			         
			  }
	      }
	   }


	   public void close() throws IOException {
	      this.bluetoothSocket.close();
	   }
}
