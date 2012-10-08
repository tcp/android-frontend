package netinf.android.wifi;

import java.net.InetAddress;

import netinf.android.access.tcp.TCPServer;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NetInfWifiDirectManager implements PeerListListener,ConnectionInfoListener {



	public static final String TAG = "NetInfWifiDirectManager";
	public boolean isWifiP2pEnabled = false;
	private WifiP2pManager mManager;
	private Channel mChannel;
	private Handler mHandler;
	public boolean isClient = false;

	private int discoveryStatus;
	private int connectionStatus;

	//Discovery process status
	public static final int NOT_DISCOVERING     = 1;
	public static final int DISCOVERING         = 2;
	public static final int DISCOVERING_DONE    = 3;

	//Connection process status
	public static final int NOT_CONNECTING      = 1;
	public static final int CONNECTING          = 2;
	public static final int CONNECTING_DONE     = 3;

	//TCP Server port
	public static final int PORT = 5000;

	public NetInfWifiDirectManager(WifiP2pManager manager, Channel channel){   	
		mManager = manager;
		mChannel = channel;
		discoveryStatus  = NOT_DISCOVERING;
		connectionStatus = NOT_CONNECTING;
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		Log.d(TAG, "onPeersAvailable");
		//mItereator = peers.getDeviceList().iterator();

		if(discoveryStatus==DISCOVERING){

			Log.d(TAG, " Sending discovery done message");
			discoveryStatus = DISCOVERING_DONE;
			Message msg = mHandler.obtainMessage(WifiDiscoveryAndConnectTask.MESSAGE_DISCOVERY_DONE);
			msg.obj=peers;
			mHandler.sendMessage(msg);
		}
		System.out.print("onPeersAvailable");
	}

	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo arg0) {
		Log.d(TAG, "onConnectionInfoAvailable");
		if(connectionStatus==CONNECTING){
			// TODO Auto-generated method stub
			InetAddress myIP = arg0.groupOwnerAddress;
			Log.d(TAG, "Group owner IP: " + myIP.toString());
			Log.d(TAG, "I am the Group owner: " + arg0.isGroupOwner);
			Log.d(TAG, " Sending connection done message");
			connectionStatus = CONNECTING_DONE ;
			Message msg = mHandler.obtainMessage(WifiDiscoveryAndConnectTask.MESSAGE_CONNECTED_DONE);
			msg.obj=myIP;
			mHandler.sendMessage(msg);
		}

		Log.d(TAG, "is Client? = " + isClient);
		if(!isClient){
			Log.d(TAG, "Starting a TCP server");
			new TCPServer(PORT).start();
		}
	}

	public void startDiscovery(Handler handler){
		mHandler = handler;
		discoveryStatus = DISCOVERING;
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {

			}
			@Override
			public void onFailure(int reasonCode) {

			}
		});
	}

	public void connectToDevice(String deviceAddress){
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		config.groupOwnerIntent=0;

		connectionStatus = CONNECTING;

		mManager.connect(mChannel, config, new ActionListener() {
			@Override
			public void onSuccess() {
				// WiFiDirectBroadcastReceiver will notify us. Ignore for now.
			}

			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "Connect failed. Retry. Reason = " + reason);
			}
		});
	}

	public void disconnectFromGroup(){
		mManager.removeGroup(mChannel, new ActionListener() {
			@Override
			public void onFailure(int reasonCode) {
				Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
			}

			@Override
			public void onSuccess() {
				Log.d(TAG, "The peer has successfully disconnected from the Wi-Fi Direct group");
			}
		});
	}
}
