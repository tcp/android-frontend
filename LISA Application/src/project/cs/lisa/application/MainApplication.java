package project.cs.lisa.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import project.cs.lisa.netinf.node.module.LisaModule;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MainApplication extends Application {

	public static final String PROPERTIES = "config.properties";
	public static final String TAG = "MainApplication";
	
	private Properties mProperties;
	private Injector mInjector;
	
	private static Context context;
	
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        loadProperties();
        Log.d(TAG, "Creating injector...");
        mInjector = Guice.createInjector(new LisaModule(mProperties));
        
        /* Get the context of this application */
        MainApplication.context = getApplicationContext();
    }
	
    public Properties getProperties() {
    	Log.d(TAG, "getProperties()");
    	return mProperties;
    }
    
    public Injector getInjector() {
    	Log.d(TAG, "getInjector()");
    	return mInjector;
    }
    
    private void loadProperties() {
    	Log.d(TAG, "loadProperties()");
    	mProperties = new Properties();
    	try {
	    	Resources resources = getResources();
	    	AssetManager assets = resources.getAssets();
	    	InputStream is = assets.open(PROPERTIES);
	    	mProperties.load(is);
    	} catch (IOException e) {
    		Log.e(TAG, e.toString(), e);
    	}
    }
    
    /**
     * Returns the current application context.
     * This is useful i.e. for registering broadcast receivers.
     * 
     * @return  The application context.
     */
    public static Context getAppContext() {
        return MainApplication.context;
    }
}