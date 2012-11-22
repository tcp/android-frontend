package project.cs.lisa.application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.lisa.R;
import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.application.http.NetInfRetrieve;
import project.cs.lisa.application.http.NetInfSearch;
import project.cs.lisa.metadata.Metadata;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.util.UProperties;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

/**
 * Loads the web page asynchronously.
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public class FetchWebPageTask extends AsyncTask<URL, Void, Void> {

    /** Debugging tag. */
    private static final String TAG = "FetchWebPageTask";

    /** The directory containing the published files. */
    private String mSharedFolder;

    /** The label for metadata filepath.*/
    private String mFilepath;

    /** The label name for content type.*/
    private String mContentType;

    /** Creates a new task downloading a web page. */
    public FetchWebPageTask() {
        super();

        mFilepath = UProperties.INSTANCE.getPropertyWithName("metadata.filepath");
        mContentType = SailDefinedLabelName.CONTENT_TYPE.getLabelName();

        String relativeFolderPath = UProperties.INSTANCE.getPropertyWithName("sharing.folder");
        mSharedFolder = Environment.getExternalStorageDirectory() + relativeFolderPath;
    }

    /** The current url for the current web page. */
    private URL mUrl;

    /**
     * Retrieves a web page.
     * @param urls
     *      Exactly one URL, the web page to retrieve
     * @return
     *      The retrieved web page as a file
     */
    @Override
    protected Void doInBackground(URL... urls) {

        if (urls.length != 1) {
            return null;
        }

        mUrl = urls[0];
        Log.d(TAG, "mUrl is: "+ mUrl.toString());

        try {
            NetInfSearch search = new NetInfSearch(
                    UProperties.INSTANCE.getPropertyWithName("access.http.host"),
                    UProperties.INSTANCE.getPropertyWithName("access.http.port"),
                    mUrl.toString(),
                    "empty"
                    ) {
                @Override
                public void onPostExecute(String jsonResponse) {

                    if (jsonResponse == null) {
                        Log.d(TAG, "Line 93: downloadAndDisplayWebPage()");
                        downloadAndDisplayWebPage();
                        return;
                    }

                    Object obj = JSONValue.parse(jsonResponse);
                    JSONObject searchResult = (JSONObject) obj;

                    // get from uplink
                    if (searchResult == null || ((Long) searchResult.get("status")) == 404) {
                        Log.d(TAG, "Line 103: downloadAndDisplayWebPage()");
                        downloadAndDisplayWebPage();
                        return;
                    }

                    Log.d(TAG, "jsonResponse: " + jsonResponse);
                    Log.d(TAG, "jsonObject: " + searchResult.toString());

                    // if the search returns something...
                    // You have a hash, proceed
                    // Extract hash from the json result
                    String hash = selectHash(searchResult);
                    retrieve(hash).execute();
                }

            };

            search.execute();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * asd.
     */
    private void downloadAndDisplayWebPage() {
        Log.d(TAG, "Downloading from uplink " + mUrl.toString());

        DownloadWebObject download = new DownloadWebObject() {
            @Override
            protected void onPostExecute(WebObject webObject) {

                if (webObject == null) {
                    MainNetInfActivity.getActivity().
                        showToast("You are probably not connected to the Internet!");
                    return;
                }
                
                File file = webObject.getFile();
                String hash = webObject.getHash();
                String contentType = webObject.getContentType();

                displayWebpage(file);
                try {
                    publishFile(file, mUrl, hash, contentType);
                } catch (IOException e) {
                    Log.e(TAG, "Could not publish file.");
                    e.printStackTrace();
                }
            }
        };
        download.execute(mUrl);
    }

    /**
     * asd.
     * @param hash as.
     * @return as.
     */
    private NetInfRetrieve retrieve(final String hash) {
        return new NetInfRetrieve(
                UProperties.INSTANCE.getPropertyWithName("access.http.host"),
                UProperties.INSTANCE.getPropertyWithName("access.http.port"),
                UProperties.INSTANCE.getPropertyWithName("hash.alg"),
                hash) {

            @Override
            protected void onPostExecute(String jsonResponse) {
                /*
                 * If the get request couldn't download the file
                 * it will notify the user and stop processing.
                 */
                Log.d(TAG, "jsonResponse: " + jsonResponse);
                if (jsonResponse == null) {
                    downloadAndDisplayWebPage();
                    return;
                }

                Object obj = JSONValue.parse(jsonResponse);
                JSONObject searchResult = (JSONObject) obj;
                Log.d(TAG, searchResult.toString());

                String filePath = (String)searchResult.get(mFilepath);
                String contentType = (String)searchResult.get(mContentType);

                Log.d(TAG, "Filepath = " + filePath + "Content Type = " + contentType);
                File file = new File(filePath);
                displayWebpage(file);
                try {
                    publishFile(file, mUrl, hash, contentType);
                } catch (IOException e) {
                    Log.e(TAG, "I could not publish the file");
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * asd.
     * @param searchResult asd.
     * @return something.
     */
    private String selectHash(JSONObject searchResult) {
        try {
            JSONArray hashResult = (JSONArray) searchResult.get("results");
            JSONObject firstOccurence = (JSONObject)hashResult.get(0);
            String ni = (String)firstOccurence.get("ni");
            return ni.split(";")[1];
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong with hash parsing!");
            e.printStackTrace();
        }
        return null;
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
                    locators);
            publishRequest.setContentType(contentType);
            publishRequest.setMetadata(metadata);

            Menu menu = (Menu) MainNetInfActivity.getActivity().getMenu();
            MenuItem fullPut = menu.findItem(R.id.menu_publish_file);
            if (fullPut.isChecked()) {
                publishRequest.setFile(file);
            }
            
            publishRequest.execute();
        }
    }

    /**
     * Show a HTML in the web view.
     * @param webPage
     *      The web page
     */
    private void displayWebpage(File webPage) {
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
