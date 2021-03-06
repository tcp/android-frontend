package netinf.android.access.bluetooth;

import java.io.IOException;
import java.util.UUID;

import javax.sql.ConnectionEvent;

import netinf.access.NetInfServer;
import netinf.android.common.communication.BluetoothConnection;
import netinf.common.communication.Communicator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class AndroidBluetoothServer extends NetInfServer{
		
	   // Debugging
    private static final String TAG = "AndroidBluetoothServer";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME= "AndroidNetInfActivity";

    // Unique UUID for this application
    private static final UUID MY_UUID=
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;

    private Provider<Communicator> communicatorProvider;
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
    public AndroidBluetoothServer(){
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
    	
        if (mAdapter == null) {
        	//An exception must be thrown here!!!!!
        }           
    }
    
    @Inject
    public void injectProviderCommunicator(Provider<Communicator> provider) {
       communicatorProvider = provider;
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

          try {
        	  if (D) Log.d(TAG, "Starting to listen for new connection within the Bluetooth Server on port");
        	  setName(" Bluetooth Connection Listener");

        	 BluetoothSocket mConnectedSocket;
        	  
             while (running) {

                if (D) Log.d(TAG, "In listen loop");
                mConnectedSocket = mmServerSocket.accept();
              
                if (D) Log.d(TAG, "Accepted new connection.");
                
                // If a connection was accepted
                if (mConnectedSocket != null) {

                    BluetoothConnection newConnection = new BluetoothConnection(mConnectedSocket);
                    Communicator newCommunicator = communicatorProvider.get();
                    newCommunicator.setConnection(newConnection);          
                    startCommunicator(newCommunicator, true);
                    
                    String deviceName = mConnectedSocket.getRemoteDevice().getName();
                    System.out.print(deviceName);                                      
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
