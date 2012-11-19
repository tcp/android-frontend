package project.cs.lisa.application.http;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.apache.http.client.methods.HttpGet;

import project.cs.lisa.application.MainNetInfActivity;
import project.cs.lisa.exceptions.NullEntityException;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Search functionality implementation on NetInfRequest level. From here,
 * the request should reach the REST service.
 * @author Thiago Costa Porto
 */
public class NetInfSearch extends NetInfRequest {

    /** Debug tag. **/
    public static final String TAG = "NetInfSearch";

    /** REST Host **/
    private String mHost;

    /** REST Port **/
    private String mPort;

    /** Keywords **/
    private String mTokens;

    /** Extensions **/
    private String mExt;

    /** Message ID **/
    private String mMsgId;

    /** Activity **/
    private MainNetInfActivity mActivity;

    /**
     * Creates a new asynchronous NetInf SEARCH.
     * @param activity     Activity creating this object
     * @param host         Target host of the message
     * @param port         Target port
     * @param tokens       Keywords to be searched
     * @param ext          Extensions
     */
    public NetInfSearch(MainNetInfActivity activity, String host, String port,
            String tokens, String ext) {
        super(activity, host, port);

        // Initialize variables
        mActivity = activity;
        mHost = host;
        mPort = port;
        mTokens = tokens;
        mExt = ext;

        // Set which action we are performing
        setPathPrefix("search");

        // Message ID
        mMsgId = newMsgId();

        // Add fields to URI
        addQuery("msgId", mMsgId);
        addQuery("ext", ext);
        addQuery("tokens", tokens);
    }

    /**
     * Asks the NetInf node to search for URL using HTTP.
     * @param   voids   Nothing.
     * @return          JSON response from the NetInf node
     *                  or null if the request failed
     */
    @Override
    protected String doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground()");


        // First, search in our own database
        // TODO: Include functions from DB handler here to search for an URL

        try {
            try {
                // Second, search in the NRS
                Log.d(TAG, "URI: " + getUri());
                HttpGet search = new HttpGet(getUri());
                execute(search);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (NullEntityException e) {
            Log.d(TAG, "Failed to receive a proper HTTP Response.");
        }

        return null;
    }

    /**
     * Creates a new message id that is probably unique in the server.
     * @return String with the created message id
     */
    private String newMsgId() {
        // Initiates a new Telephony Manager to extract deviceId and serial number.
        final TelephonyManager tm = (TelephonyManager) mActivity.getBaseContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        // Telephony Manager Device ID
        final String tmDevice;

        // Telephony Manager Serial Number
        final String tmSerial;

        // Android ID
        final String androidId;

        // Fetches IDs
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(
                mActivity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        // Gets device UUID
        UUID deviceUuid = new UUID(androidId.hashCode(),
                ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());

        // UUID to String
        String deviceId = deviceUuid.toString();

        // Random number
        int randomNumber = new Random(System.currentTimeMillis()).nextInt();

        // Bulks all of it together
        String msgId = deviceId + String.valueOf(randomNumber);

        // Returns created message id
        return msgId;
    }
}
