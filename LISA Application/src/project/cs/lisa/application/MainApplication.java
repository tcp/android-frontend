/**

 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */
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

/**
 * Base class for maintaining a global application state.
 * @author Paolo Boschini
 */
public class MainApplication extends Application {

	/** Property files that contains the properties for this project. */
    public static final String PROPERTIES = "config.properties";
    
    /** Debugging tag. */
	public static final String TAG = "MainApplication";
	
	/** Reference to properties loaded in the project. */
	private Properties mProperties;
	
	/** Injector for injecting classes. */	
	private Injector mInjector;
	
	/** The context for this application. */
	private static Context sContext;
	
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        loadProperties(PROPERTIES);
        Log.d(TAG, "Creating injector...");
        mInjector = Guice.createInjector(new LisaModule(mProperties));
        
        /* Get the context of this application */
        sContext = getApplicationContext();
    }
	
    /**
     * Returns the property reference for this project.
     * @return  the property reference
     */
    public Properties getProperties() {
    	Log.d(TAG, "getProperties()");
    	return mProperties;
    }
    
    /**
     * Returns the injector for injecting classes.
     * @return  the injector
     */
    public Injector getInjector() {
    	Log.d(TAG, "getInjector()");
    	return mInjector;
    }
    
    /**
     * Loads properties from the passed property file.
     * @param property the property file to be loaded
     */
    private void loadProperties(String property) {
    	Log.d(TAG, "loadProperties()");
    	mProperties = new Properties();
    	try {
	    	Resources resources = getResources();
	    	AssetManager assets = resources.getAssets();
	    	InputStream is = assets.open(property);
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
        return sContext;
    }
}