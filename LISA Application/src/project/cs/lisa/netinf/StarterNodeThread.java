package project.cs.lisa.netinf;

import netinf.node.api.NetInfNode;
import project.cs.lisa.MainActivity;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Provider;

public class StarterNodeThread extends Thread {
	
	private final Provider<NetInfNode> mNetInfNodeProvider;
	
	private NetInfNode mNode;
	private Context mContext;
	
	public StarterNodeThread(Context context, Provider<NetInfNode> netInfNodeProvider) {
		mNetInfNodeProvider = netInfNodeProvider;
		mContext = context;
	}
	
	@Override
	public void run() {
		mNode = mNetInfNodeProvider.get();
		
//		startResolution();
//		startAPIAccess();
//		startN2NAccess();
		
		Intent intent = new Intent();
		intent.setAction(MainActivity.NODE_STARTED);
		mContext.sendBroadcast(intent);
	}

	
}