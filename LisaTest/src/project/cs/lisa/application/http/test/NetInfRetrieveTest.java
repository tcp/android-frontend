package project.cs.lisa.application.http.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import project.cs.lisa.application.http.NetInfRetrieve;
import android.test.InstrumentationTestCase;
import android.util.Log;

public class NetInfRetrieveTest extends InstrumentationTestCase {

    public static final String TAG = "NetInfRetrieveTest";

    private static final int TIMEOUT_SECONDS = 5;

    private static final String mHash = "hashThatExists";
    private static final String mWrongHash = "hashThatDoesNotExists";
    private static final String mFilePath = "/path/to/file";
    private static final String mContentType = "image/jpeg";

    private static final String mHost = "localhost";
    private static final String mNoHost = "123.123.123.123";
    private static final String mMockPort = "9998";
    private static final String mWrongPort = "11111";
    private static final String mHashAlg = "sha-256";

    private MockServer mMockServer;

    public static class MockServer extends ServerResource {

        private Component mComponent;

        public void start() {
            mComponent = new Component();
            mComponent.getServers().add(Protocol.HTTP, Integer.parseInt(mMockPort));
            mComponent.getDefaultHost().attach("/retrieve", MockServer.class);
            try {
                mComponent.start();
            } catch (Exception e) {
                fail("Failed to start mock server");
            }
            assertNotNull(mComponent);
        }

        public void stop() {
            try {
                mComponent.stop();
            } catch (Exception e) {
                fail("Failed to stop mock server");
            }
        }

        @Get
        public String mockGet() {
            Log.d("TEST", "@Get");
            assertNotNull(getQuery().getFirstValue("hash"));
            assertNotNull(getQuery().getFirstValue("hashAlg"));
            if(getQuery().getFirstValue("hash").equals(mHash)) {
                JSONObject json = new JSONObject();
                json.put("filePath", mFilePath);
                json.put("contentType", mContentType);
                return json.toJSONString();
            } else {
                return null;
            }
        }

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        // Setup HTTP Server
        Log.d(TAG, "Starting Server...");
        mMockServer = new MockServer();
        mMockServer.start();
        Log.d(TAG, "Server Started");

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Shutdown server
        Log.d(TAG, "Stopping Server...");
        mMockServer.stop();
        Log.d(TAG, "Server Stopped");
    }

    public void testRetrieveExistent() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create retrieve
        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, mMockPort, mHashAlg, mHash) {

            @Override
            protected void onPostExecute(String jsonResponse) {
                // Asserts
                assertNotNull(jsonResponse);
                Object obj = JSONValue.parse(jsonResponse);
                assertTrue(obj instanceof JSONObject);
                JSONObject json = (JSONObject) obj;
                assertTrue(json.containsKey("filePath"));
                assertTrue(json.containsKey("contentType"));
                assertEquals(json.get("filePath"), mFilePath);
                assertEquals(json.get("contentType"), mContentType);
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieve.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue(signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testRetrieveNonExistent() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, mMockPort, mHashAlg, mWrongHash) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNull(jsonResponse);
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieve.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue(signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testWrongPort() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, mWrongPort, mHashAlg, mHash) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNull(jsonResponse);
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieve.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue(signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

}
