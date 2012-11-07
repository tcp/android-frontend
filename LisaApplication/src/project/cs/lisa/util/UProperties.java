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
package project.cs.lisa.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import project.cs.lisa.application.MainApplication;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * Singleton utility class for loading properties and
 * providing methods for getting the property values.
 * 
 * author Paolo Boschini
 * 
 */
public enum UProperties {

    /** The unique reference for this singleton. */
    INSTANCE;

    /** Debugging tag. */
    public static final String TAG = "UProperties";

    /** Property files that contains the properties for this project. */
    private static final String PROPERTIES_FILE = "config.properties";

    /** The property reference. */
    private Properties mProperties;

    /**
     * Initiates the singleton.
     */
    private UProperties() {
        loadProperties(PROPERTIES_FILE);
    }

    /**
     * Returns the value of a property given the correct key.
     * @param propertyName The property key
     * @return The property value that was requested
     */
    public String getPropertyWithName(String propertyName) {
        return mProperties.getProperty(propertyName);
    }

    /**
     * Returns the property reference.
     * @return the property
     */
    public Properties getProperty() {
        return mProperties;
    }

    /**
     * Loads properties from the passed property file.
     * @param property the property file to be loaded
     */
    private void loadProperties(String property) {
        Log.d(TAG, "loadProperties()");
        mProperties = new Properties();
        try {
            AssetManager assets = MainApplication.getAppContext().getResources().getAssets();
            InputStream is = assets.open(property);
            mProperties.load(is);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }
    }
}