package netinf.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import netinf.android.module.AndroidNetInfModule;
import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AndroidNetInfApplication extends Application {

	public static final String PROPERTIES = "android_netinfnode.properties";
	
    private Injector injector;
	private Properties mNodeProperties;

    @Override
    public void onCreate() {
        super.onCreate();
        mNodeProperties = getProperties(PROPERTIES);


        injector = Guice.createInjector(new AndroidNetInfModule(mNodeProperties));
//        injector = Guice.createInjector(new SenderAndroidNetInfModule(mNodeProperties));
    }

    public Injector getInjector() {
        return injector;
    }
    
    public Properties getProperties(String propertyFileName){
    	
    	Properties mPropertiesFile = new Properties();
    	
    	Resources resources = this.getResources();
    	AssetManager assetManager = resources.getAssets();
    	
    	try {
    	    InputStream inputStream = assetManager.open(propertyFileName); 
    	    mPropertiesFile.load(inputStream);
    	    System.out.println("The properties are now loaded");
    	    System.out.println("properties: " + mPropertiesFile);
    	} catch (IOException e) {
    	    System.err.println("Failed to open microlog property file");
    	    e.printStackTrace();
    	}
    	return mPropertiesFile;	
    }
}
