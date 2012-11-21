package project.cs.lisa.application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import project.cs.lisa.R;
import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.hash.Hash;
import project.cs.lisa.metadata.Metadata;
import project.cs.lisa.util.UProperties;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

/**
 * Loads the web page asynchronously.
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public class DownloadWebPageTask extends AsyncTask<URL, Void, File> {

    /** Debugging tag. */
    private static final String TAG = "DownloadWebPageTask";

    /** The directory containing the published files. */
    private String mSharedFolder;
    
    /** Creates a new task downloading a web page. */
    public DownloadWebPageTask() {
    	super();
    	
		String relativeFolderPath = UProperties.INSTANCE.getPropertyWithName("sharing.folder");
		mSharedFolder = Environment.getExternalStorageDirectory() + relativeFolderPath;
    }

    /**
     * Retrieves a web page.
     * @param urls
     *      Exactly one URL, the web page to retrieve
     * @return
     *      The retrieved web page as a file
     */
    @Override
    protected File doInBackground(URL... urls) {

        if (urls.length != 1) {
            return null;
        }

        URL url = urls[0];
        Log.d(TAG, url.toString());

        /*
         * Netinf calls:
         * 1. search for a URL (NetInfSearch), if fails go to A
         * 2. Get back a list of hashes, Choose a hash, do a retrieve (if fails go to A)
         * 3. publish
         * 4. Show page, done
         *
         * A. Download webpage with 3g
         * B. Hash the webpage, go to 3
         */

        // do NetInfSearch and override onPostExecute(String jsonResponse)

        try {
            if (true) { // should be false, the search fails
                return downloadWebPage(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
    private File downloadWebPage(URL url) throws IOException {

        Representation representation = new ClientResource(url.toString()).get();
        String contentType = representation.getMediaType().toString();

        // Create file and hash
        byte[] bytes = IOUtils.toByteArray(representation.getStream());
        String hash = hashContent(bytes);
        File file = new File(mSharedFolder + hash);
        FileUtils.writeByteArrayToFile(file, bytes);

        publishFile(file, url, hash, contentType);
        return file;
    }

    /**
     * Publishes a retrieved file with URL and content-type meta data.
     * @param file
     *      The file to publish
     * @param url
     *      The URL the file was retrieved from
     * @param hash
     *      The hash of the file
     * @param contentType
     *      The content-type of the file
     * @throws IOException
     *      In case the publish failed
     */
    private void publishFile(File file, URL url, String hash, String contentType)
            throws IOException {

        Metadata metadata = new Metadata();

        metadata.insert("filesize", String.valueOf(file.length()));
        metadata.insert("filepath", file.getAbsolutePath());
        metadata.insert("time", Long.toString(System.currentTimeMillis()));
        metadata.insert("url", url.toString());

        Log.d(TAG, "Trying to publish a new file.");

        // Try to get the Bluetooth MAC
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            throw new IOException("Error: Bluetooth not supported");
        } else if (!adapter.isEnabled()) {
            throw new IOException("Error: Bluetooth not enabled");
        } else {
            HashSet<Locator> locators = new HashSet<Locator>();
            locators.add(new Locator(Locator.Type.BLUETOOTH, adapter.getAddress()));

            NetInfPublish publishRequest = new NetInfPublish(
                    UProperties.INSTANCE.getPropertyWithName("access.http.host"),
                    UProperties.INSTANCE.getPropertyWithName("access.http.port"),
                    UProperties.INSTANCE.getPropertyWithName("hash.alg"),
                    hash,
                    locators) {

                @Override
                protected void onPostExecute(String jsonResponse) {
                    super.onPostExecute(jsonResponse);
                    // TODO If published failed, try to throw IOException!
                }

            };
            publishRequest.setContentType(contentType);
            publishRequest.setMetadata(metadata);
            publishRequest.execute();
        }
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
        Log.d(TAG, "The generated hash is: " + hash);
        result = hash.encodeResult(); // Use 0 for using the whole hash

        return result;
    }

    /**
     * Show a HTML in the web view.
     * @param webPage
     *      The web page
     */
    @Override
    protected void onPostExecute(File webPage) {
        if (webPage == null) {
            Log.d(TAG, "webPage == null");
            MainNetInfActivity.showToast("Could not download webpage.");
            return;
        }
        try {
            String result = FileUtils.readFileToString(webPage);
            WebView webView = (WebView) MainNetInfActivity.getActivity().findViewById(R.id.webView);
            webView.loadDataWithBaseURL(result, result, "text/html", null, null);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            MainNetInfActivity.showToast("Could not load the requested web page.");
        }
    }
}
