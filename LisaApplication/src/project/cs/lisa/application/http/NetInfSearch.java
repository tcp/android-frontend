package project.cs.lisa.application.http;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;

import project.cs.lisa.application.MainNetInfActivity;
import project.cs.lisa.exceptions.NullEntityException;
import project.cs.lisa.search.SearchRequest;
import project.cs.lisa.util.UProperties;
import android.util.Log;

public class NetInfSearch extends NetInfRequest {

    /** Debug tag. **/
    public static final String TAG = "NetInfSearch";

    private String mHost;
    
    private String mPort;
    
    private String mTokens;
    
    private String mExt;
    
    private String mMsgId;
    
    /**
     * Creates a new asynchronous NetInf GET.
     * @param activity     Activity creating this object
     * @param host         Target host of the message
     * @param port         Target port
     */
    public NetInfSearch(MainNetInfActivity activity, String host, String port,
            String msgId, String tokens, String ext) {
        super(activity, host, port);

        mHost = host;
        mPort = port;
        mMsgId = msgId;
        mTokens = tokens;
        mExt = ext;
        
        // TODO make this beautiful
        setPathPrefix("search");
        addQuery("msgId", msgId);
        addQuery("ext", ext);
        addQuery("tokens", tokens);
    }

    /**
     * Asks the NetInf node to retrieve a file using HTTP.
     * @param   voids   Nothing.
     * @return          JSON response from the NetInf node
     *                  or null if the request failed
     */
    @Override
    protected String doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground()");
        
        HttpGet search = new HttpGet(getUri());

//        SearchRequest sr = new SearchRequest(
//                UProperties.INSTANCE.getPropertyWithName("nrs.http.host"),
//                Integer.parseInt(UProperties.INSTANCE.getPropertyWithName("nrs.http.port")));
//        
//        try {
//            String ames = sr.search(mMsgId, mTokens, mExt).toJSONString().toString();
//            Log.d(TAG, "ames " + ames);
//            return ames;
//        } catch (NullPointerException e) {
//            Log.d(TAG, "NullPointerException");
//        }
        
        try {
            try {
                execute(search);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (NullEntityException e) {
            Log.d(TAG, "something failed during search");
        }
        
        return null;
    }
}
