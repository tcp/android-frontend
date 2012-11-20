/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
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
 */
package project.cs.lisa.application;

import project.cs.lisa.netinf.node.module.Module;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Base class for maintaining a global application state.
 * 
 * @author Linus Sunde
 * @author Paolo Boschini
 * @author Thiago Costa Porto
 * 
 */
public class MainApplication extends Application {

    /** Debugging tag. */
    public static final String TAG = "MainApplication";

    /** Injector for injecting classes. */	
    private Injector mInjector;

    /** The context for this application. */
    private static Context sContext;
    
    /** A static version of the Injector for returning in the static function */
    private static Injector mStaticInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        Log.d(TAG, "Creating injector...");

        // Get the context of this application
        sContext = getApplicationContext();

        mInjector = Guice.createInjector(new Module());
        
        mStaticInjector = mInjector;
    }

    /**
     * Returns the injector for injecting classes.
     * @return  the injector
     */
    public Injector getInjector() {
        Log.d(TAG, "getInjector()");
        return mInjector;
    }
    
    public static Injector getStaticInjector() {
        Log.d(TAG, "getStaticInjector()");
        return mStaticInjector;
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