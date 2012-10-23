package project.cs.lisa.json.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Metadata {

    private final String TAG = "MetadataClass";
    
    private final JSONObject mJSONObject;
    
    public Metadata() {
        mJSONObject = new JSONObject();
    }
    
    public boolean insert(String key, String value) {
        if (key == null) {
            Log.d(TAG, "Tried to use a null key on insert()");
            return false;
        }
        
        try {
            mJSONObject.accumulate(URLEncoder.encode(key, "UTF-8"), URLEncoder.encode(value, "UTF-8"));
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Failed to insert JSON Object, (" + key + ", " + value +")");
            Log.d(TAG, "Error: " + e.toString());
            e.printStackTrace();
            return false;
        }
        catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Failed to encode (" + value + ")");
            Log.d(TAG, "Error: " + e.toString());
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    public String get(String key) {
        if (key == null) {
            Log.d(TAG, "Tried to use a null key on get()");
            return null;
        }
        try {
            return mJSONObject.get(key).toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Failed to retrieve JSON Object, (" + key + ")");
            Log.d(TAG, "Error:" + e.toString());
            e.printStackTrace();
        }
        return null;
    }
    
    public String mToString() {
        try {
            return mJSONObject.toString(4);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public void clearMetadata() {
        
    }

 // Code to write a quick JSON Object:
 //  String jsonString = null;
 //  try {
//       JSONObject jobj = new JSONObject("{\"metadata\":{\"ct\":\"oi\"}}");
//       jsonString = new MetadataParser().extractMimeType(jobj);
 //  } catch (JSONException e) {
//       // TODO Auto-generated catch block
//       e.printStackTrace();
 //  }
 //  Log.d(TAG, "JSON Extracted String: " + jsonString);
}
