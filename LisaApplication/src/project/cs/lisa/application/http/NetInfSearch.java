package project.cs.lisa.application.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.http.client.methods.HttpGet;

import project.cs.lisa.application.MainNetInfActivity;
import project.cs.lisa.exceptions.NullEntityException;
import project.cs.lisa.exceptions.NullHostException;
import project.cs.lisa.exceptions.NullPortException;
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
     * @throws NullHostException
     * @throws NullPortException
     */
    public NetInfSearch(String host, String port, String tokens, String ext)
            throws NullHostException, NullPortException {
        super(MainNetInfActivity.getActivity(), host, port);

        if (host == "" || host == null) {
            throw new NullHostException();
        }

        if (port == "" || port == null) {
            throw new NullPortException();
        }

        // Initialize variables
        mActivity = MainNetInfActivity.getActivity();
        mHost = host;
        mPort = port;
        mTokens = tokens;
        mExt = ext;

        // Set which action we are performing
        setPathPrefix("search");

        // Add fields to URI
        // Create new Message ID
        mMsgId = newMsgId();
        // Message ID
        addQuery("msgId", mMsgId);

        // Extension
        // TODO: When ext is filled, check if encoding is necessary
        addQuery("ext", ext);

        // Tokens. Encoding the URL might be necessary.
        try {
            addQuery("tokens", URLEncoder.encode(tokens, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

        // Second, search in the NRS
        HttpGet search;
        try {
            search = new HttpGet(getUri());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

        // Execute search
        try {
            return execute(search);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IO Exception executing search.");
        } catch (NullEntityException e) {
            Log.d(TAG, "Failed to receive a proper HTTP Response.");
        }

        // TODO: Should return `null`?
        return null;
    }

    /**
     * Creates a new message id that is probably unique in the server.
     * @return String with the created message id
     */
    private String newMsgId() {
        // TODO: Validate TelephoneManager as a viable option for serial number.
        // TODO: Right now, this code FAILS tests because there is no TM on emulator.
        // Initiates a new Telephony Manager to extract deviceId and serial number.
//        final TelephonyManager tm = (TelephonyManager) mActivity.getBaseContext()
//                .getSystemService(Context.TELEPHONY_SERVICE);

        // Telephony Manager Device ID
//        final String tmDevice;

        // Telephony Manager Serial Number
//        final String tmSerial;

        // Android ID
//        final String androidId;

        // Fetches IDs
//        tmDevice = "" + tm.getDeviceId();
//        tmSerial = "" + tm.getSimSerialNumber();
//        androidId = "" + android.provider.Settings.Secure.getString(
//                mActivity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        // Gets device UUID
//        UUID deviceUuid = new UUID(androidId.hashCode(),
//                ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());

        // UUID to String
//        String deviceId = deviceUuid.toString();

        // Random number
        int randomNumber = new Random(System.currentTimeMillis()).nextInt();

        // Bulks all of it together
        String msgId = /*deviceId + */String.valueOf(randomNumber);

        // Returns created message id
        return msgId;
    }
}
