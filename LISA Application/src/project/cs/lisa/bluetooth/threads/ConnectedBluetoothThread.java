package project.cs.lisa.bluetooth.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

/**
 * The ConnectedBluetoothThread han
 * 
 * @author ktran
 *
 */
public class ConnectedBluetoothThread extends Thread {
	
	/**
	 * Debug Tag.
	 */
    private static final String TAG = "ConnectedThread";
    
    /**
     * 
     */
    private static final int BUFFER_SIZE = 1024;
    
    /**
     * Bluetooth Socket enabling the connection to the remote device.
     */
	private final BluetoothSocket mBtSocket;
	
	/**
	 * Input Stream used for reading in requests.
	 */
	private final DataInputStream mInStream;
	
	/**
	 * Output Stream used for writing data.
	 */
	private final DataOutputStream mOutStream;
	
	/**
	 * The remote device's address.
	 */
	private String mClientAddress;
	
	/**
	 * Creates a new thread dealing with reading and writing data.
	 * 
	 * @param myBtSocket The Bluetooth socket used for the data transfer.
	 */
	public ConnectedBluetoothThread(BluetoothSocket myBtSocket) {
		mBtSocket = myBtSocket;
		mClientAddress = mBtSocket.getRemoteDevice().getAddress();
		
		DataInputStream tmpIn = null;
		DataOutputStream tmpOut = null;			
		
		try {
			tmpIn = new DataInputStream(mBtSocket.getInputStream());
			tmpOut = new DataOutputStream(mBtSocket.getOutputStream());
		} catch (IOException e) {
			Log.d(TAG, "Couldn't extract streams for Bluetooth transmission.");
		}
		
		mInStream = tmpIn;
		mOutStream = tmpOut;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Log.d(TAG, "Starting to receive the incoming message");
		
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytes;
		
			try {
				Log.d(TAG, "Reading..");
				bytes = mInStream.read(buffer);
				Log.d(TAG, "Finished.");
				
				Bundle bundle = new Bundle();
				bundle.putString("client", mClientAddress);
				bundle.putInt("size", bytes);
				bundle.putByteArray("data", buffer);
				
				/* TODO: Let who know? 
				Message msg = mHandler.obtainMessage(BluetoothActivity.MESSAGE_READ);
				msg.setData(bundle);		
				msg.sendToTarget();
				*/
				
			} catch (IOException e) {
				Log.d(TAG, "Error while receiving incoming file.");
			}			
		
	}  

	
	/**
	 * Writes the passed byte array to the Bluetooth socket.
	 * 
	 * @param buffer The byte array to send to the remote device.
	 */
	public void write(byte[] buffer) {
		Log.d(TAG, "Sending file."); 

		try {
			mOutStream.writeInt(buffer.length);
			mOutStream.write(buffer, 0, buffer.length);
			mOutStream.flush();
		
			/* TODO: Let who know?
			mHandler.obtainMessage(
					BluetoothActivity.MESSAGE_WRITE, Transmission.SUCCESS.ordinal(), -1, mClientAddress)
					.sendToTarget();
			*/
		} catch (IOException e) {
			Log.e(TAG, "Exception occured during writing", e);
			
			//mHandler.obtainMessage(BluetoothActivity.MESSAGE_WRITE, Transmission.FAILED.ordinal(), -1, mClientAddress)
			//.sendToTarget();
		}			
	}
	
	
	/**
	 * Shuts down the current server client connection.
	 */
	public void cancel() {
		try {
			Log.d(TAG, "Closing Bluetooth socket to " + mClientAddress);

			mOutStream.close();
			mInStream.close();
			
			mBtSocket.close();
			//mConnectedThreadsCollection.remove(mClientAddress);
		} catch (IOException e) {
			Log.d(TAG, "Error while closing Bluetooth socket");
		}
	}
}
