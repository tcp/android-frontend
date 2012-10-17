package project.cs.lisa;

import netinf.node.api.NetInfNode;
import project.cs.lisa.netinf.StarterNodeThread;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.google.inject.Injector;
import com.google.inject.Provider;

public class MainActivity extends Activity {
	
	public static final String NODE_STARTED = "project.cs.list.node.started";
	
	TextView mTextView;
	private MainApplication mApplication;
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void setup() {
    	mApplication = (MainApplication) getApplication();
    	Injector injector = mApplication.getInjector();
    	// Start NetInfNode
    	Provider<NetInfNode> netInfNodeProvider = injector.getProvider(NetInfNode.class);
    	new StarterNodeThread(mApplication, netInfNodeProvider).start();
    }
    
}
