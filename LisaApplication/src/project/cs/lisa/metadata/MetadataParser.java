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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import android.util.Log;

/**
 * Class for parsing json metadata.
 * @author Thiago Costa Porto
 */
public class MetadataParser {
    public static final String TAG = "MetadataParser";
    
    // TAGs from the metadata document (Alex, Jon, Linus)
    public static final String TAG_NetInf = "NetInf";
    public static final String TAG_msgId = "msgId";
    public static final String TAG_status = "status";
    public static final String TAG_ni = "ni";
    public static final String TAG_timestamp = "ts";
    public static final String TAG_metadata = "metadata";
    public static final String TAG_loc = "loc";
    public static final String TAG_meta = "meta";
    
    // JSON Object
    private JSONObject mJSONMetadata;

    /**
     * Extracts MIME Content-type from a JSON Object metadata.
     * Metadata has at least the format:
     * 
     * { "metadata" : { 
     *      "ct" : "content-type"
     *   }
     * }
     * 
     * Obviously, the metadata field should be there. The JSON Object
     * may contain other values.
     * @param json JSON Object
     * @return String with 
     */
    
    public String extractMimeContentType(JSONObject json) {
        Log.d(TAG, "" + json.toString());
        // Value ct (Content-type)
        String TAG_metadata_ct = "ct";

        // return string
        String mimetype = null;

        // Populate JSON Array with metadata
        try {
            // get metadata information
            mJSONMetadata = json.getJSONObject(TAG_metadata);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            // If someone knows a good way to inform the user.. maybe a
            // Toast?
            Log.d(TAG, "Unable to get JSON Array");
            Log.d(TAG, "" + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        try {
            // extract mimetype
           mimetype = mJSONMetadata.getString(TAG_metadata_ct);
           Log.d(TAG, "mimetype read: " + mimetype);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Malformed metadata!");
            Log.d(TAG, "" + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        return mimetype;
    }
    
    /** 
     * Returns a map that represents the meta-data contained in the JSON object.
     * If it fails to read the meta-data, it will return null.
     * 
     * @param json	The JSOn object.
     * @return		The map with all meta-data values.
     */
    public Map<String, Object> extractMetaData(JSONObject json) {
    	Map<String, Object> map = new LinkedHashMap<String, Object>();
    	
    	// Extract the metadata from the json object
    	JSONObject metadata; 
    	try {
			metadata = json.getJSONObject(TAG_metadata).getJSONObject(TAG_meta);
		} catch (JSONException e) {
			Log.e(TAG, "Extracting the meta-data failed.");
			return null;
		}
    	
    	//metada.keys does not have a defined type but it always will be a String
    	@SuppressWarnings("unchecked")
		Iterator<String> iterator = metadata.keys();
    	
    	while (iterator.hasNext()) {
    		String key = iterator.next();
    		Object value;
    		
			try {
				value = metadata.get(key);
				
	    		if (value instanceof JSONArray) {		
	    			List<String> list = extractList((JSONArray) value);
	    			map.put(key, list);
	    			
	    		} else {
	    			map.put(key, value);
	    		}
	    		
			} catch (JSONException e) {
				Log.e(TAG, "Extracting a value in a meta-data field failed");
				e.printStackTrace();
			}
    	}
    
    	
    	return map;
    }

    /** 
     * Converts a json array into a collection of corresponding string values.
     * 
     * @param jsonArray	The specified json array to convert.
     * @return			A collection containing the corresponding String values.
     */
	private List<String> extractList(JSONArray jsonArray) {
		List<String> list = new ArrayList<String>();     
		
		int len = jsonArray.size();
		for (int i = 0; i < len; i++) { 
			list.add(jsonArray.get(i).toString());
		} 
		
		return list;
	}
    
}
