package project.cs.lisa.application;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import project.cs.lisa.hash.Hash;
import project.cs.lisa.util.UProperties;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadWebObject extends AsyncTask<URL, Void, WebObject>{

    /** Debugging tag. */
    private static final String TAG = "DownloadWebObject";

    /** The directory containing the published files. */
    private String mSharedFolder;

    private URL mUrl;

    private String contentType;


    public DownloadWebObject() {
        String relativeFolderPath = UProperties.INSTANCE.getPropertyWithName("sharing.folder");
        mSharedFolder = Environment.getExternalStorageDirectory() + relativeFolderPath;
    }

    @Override
    protected WebObject doInBackground(URL... urls) {
        URL url = urls[0];
        WebObject webObject = null;
        try {
            webObject = downloadWebPage(url);
        } catch (IOException e) {
            Log.e(TAG, "Could not download web page from uplink.");
            e.printStackTrace();
        }
        return webObject;
    }

    /**
     * Checks for Internet connection.
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MainNetInfActivity.getActivity().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }

    /**
     * Downloads a web page and saves it to file.
     * @param url
     *      The URL of the web page to download
     * @return
     *      A file containing the downloaded web page
     * @throws IOException
     *      In case the web page could not be downloaded and saved
     */
    private WebObject downloadWebPage(URL url) throws IOException {

        if (!isNetworkConnected()) {
            return null;
        }

        Representation representation = null;
        try {
            representation = new ClientResource(url.toString()).get();
        } catch (ResourceException e) {
            Log.e(TAG, "Failed connecting to the Internet!");
            return null;   
        }

        String contentType = representation.getMediaType().toString();

        // Create file and hash
        byte[] bytes = IOUtils.toByteArray(representation.getStream());

        String hash = hashContent(bytes);
        File file = new File(mSharedFolder + hash);
        FileUtils.writeByteArrayToFile(file, bytes);

        WebObject webObject = new WebObject(contentType, file, hash);
        return webObject;
    }

    /**
     * Hashes data.
     * @param bytes
     *      The data
     * @return
     *      The hash
     */
    private String hashContent(byte[] bytes) {
        Hash hash = null;
        String result = null;

        hash = new Hash(bytes);
        Log.d(TAG, "The generated hash is: " + hash.encodeResult());
        result = hash.encodeResult(); // Use 0 for using the whole hash

        return result;
    }
}
