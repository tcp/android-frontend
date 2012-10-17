package project.cs.lisa;

import java.io.IOException;
import java.util.Properties;

import project.cs.lisa.netinf.NetInfModule;
import android.app.Application;
import android.util.Log;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MainApplication extends Application {

	public static final String PROPERTIES = "config.properties";
	public static final String TAG = "MainApplication";
	
	private Properties mProperties;
	private Injector mInjector;
	
    @Override
    public void onCreate() {
        super.onCreate();
        try {
        	mProperties.load(getResources().getAssets().open(PROPERTIES));
        } catch (IOException e) {
        	Log.e(TAG, e.toString(), e);
        }
        mInjector = Guice.createInjector(new NetInfModule(mProperties));
    }
	
    public Properties getProperties() {
    	return mProperties;
    }
    
    public Injector getInjector() {
    	return mInjector;
    }
    
}
