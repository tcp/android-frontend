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
package project.cs.lisa.metadata;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Implementation of Metadata support class.
 * @author Thiago Costa Porto
 */
public class Metadata {

    // Class TAG
    private final String TAG = "MetadataClass";

    // Metadata JSON Object
    private JSONObject mJSONObject;

    /**
     * Constructor
     */

    public Metadata() {
        mJSONObject = new JSONObject();
    }

    /**
     * Constructor that takes in a already formatted JSON String
     * @param _JSONString Formatted JSON String
     */

    public Metadata(String _JSONString) {
        mJSONObject = null;
        try {
            mJSONObject = new JSONObject(_JSONString);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Problems creating LisaMetadata with a previous valid JSON declaration");
            Log.d(TAG, "Got: " + _JSONString);
            Log.d(TAG, "Error was: " + e.toString());
            mJSONObject = new JSONObject(); // Creates an empty JSON Object
            e.printStackTrace();
        }
    }

    /**
     * Constructor for (key, value)
     * @param key String with the key
     * @param value String with the value
     */

    public Metadata(String key, String value) {
        mJSONObject = new JSONObject();
        insert(key, value);
    }

    /**
     * Constructor for string arrays. It is the developer responsibility to
     * pass arrays with the correct sizes. They are corresponding, meaning
     * key[0] goes with value[0].
     * Read it as: metadata[key(i)] = value(i)
     * @param key Array of keys
     * @param value Array of values
     */

    public Metadata(String[] key, String[] value) {
        mJSONObject = new JSONObject();

        if (key.length != value.length) {
            // Different size arrays
            Log.d(TAG, "The JSON Object was created, but you gave me two arrays of "
                    + "different sizes!");
            // Null or lost values
            if (key.length > value.length) {
                Log.d(TAG, "The JSON Object created has null values.");
            }
            else {
                Log.d(TAG, "The JSON Object created has lost values.");
            }
        }

        for (int i = 0; i < key.length; i++) {
            insert(key[0], value[0]);
        }
    }

    /**
     * Inserts a (key,value) to the JSON Object
     * @param key String with key
     * @param value String with value
     * @return true  if value was inserted
     *         false if value was not inserted
     */

    public boolean insert(String key, String value) {
        if (key == null) {
            Log.d(TAG, "Tried to use a null key on insert()");
            return false;
        }

        try {
//            mJSONObject.accumulate(URLEncoder.encode(key, "UTF-8"), URLEncoder.encode(value, "UTF-8"));
            mJSONObject.accumulate(key, value);
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Failed to insert JSON Object, (" + key + ", " + value +")");
            Log.d(TAG, "Error: " + e.toString());
            e.printStackTrace();
            return false;
        }
//        catch (UnsupportedEncodingException e) {
//            Log.d(TAG, "Failed to encode (" + value + ")");
//            Log.d(TAG, "Error: " + e.toString());
//            e.printStackTrace();
//            return false;
//        }

        return true;
    }

    /**
     * Get a value corresponding to the key
     * @param key String with the key
     * @return Value if it exists
     *         null  if things go wrong
     */

    public String get(String key) {
        if (key == null) {
            Log.d(TAG, "Tried to use a null key on get()");
            return null;
        }

        if (mJSONObject == null)
            return null;

        try {
            return mJSONObject.get(key).toString();
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Failed to retrieve JSON Object, (" + key + ")");
            Log.d(TAG, "Error:" + e.toString());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Converts JSON Object to a FORMATTED string
     * @return formatted string
     */

    public String convertToString() {
        try {
            return mJSONObject.toString(4);
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Failed to get convert JSON Object to string.");
            Log.d(TAG, e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a JSON string with the key "meta"
     * set to the JSON string representation of the metadata.
     * @return The JSON string
     */
    public String convertToMetadataString() {
        try {
            JSONObject meta = new JSONObject();
            meta.put("meta", mJSONObject);
            return meta.toString(4);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Failed to get convert JSON Object to string.");
            Log.d(TAG, e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cleans the JSONObject
     */

    public void clear() {
        Iterator<?> it = mJSONObject.keys();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    // TODO: Either keep this or fix the server code.
    public String remove_brackets(String str) {
        // TODO Auto-generated method stub
        Log.d(TAG, "str " + str);
        Log.d(TAG, "return: " + str.substring(str.indexOf("\"")+1, str.indexOf("\"", str.length()-6)));
        return str.substring(str.indexOf("\"")+1, str.indexOf("\"", str.length()-6));
    }
}
