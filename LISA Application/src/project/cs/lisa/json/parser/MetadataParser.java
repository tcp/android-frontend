/**
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
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */
package project.cs.lisa.json.parser;

import org.json.JSONException;
import org.json.JSONObject;

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
}

// Code to write a quick JSON Object:
//  String jsonString = null;
//  try {
//      JSONObject jobj = new JSONObject("{\"metadata\":{\"ct\":\"oi\"}}");
//      jsonString = new MetadataParser().extractMimeType(jobj);
//  } catch (JSONException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//  }
//  Log.d(TAG, "JSON Extracted String: " + jsonString);