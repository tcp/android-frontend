package project.cs.lisa.application.http.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import project.cs.lisa.application.http.NetInfRetrieve;
import project.cs.lisa.mock.MockServer;
import android.test.InstrumentationTestCase;

public class NetInfRetrieveTest extends InstrumentationTestCase {

    public static final String TAG = "NetInfRetrieveTest";

    private static final int TIMEOUT_SECONDS = 5;

    private static final String mHash = "hashThatExists";
    private static final String mWrongHash = "hashThatDoesNotExists";
    private static final String mFilePath = "/path/to/file";
    private static final String mContentType = "image/jpeg";

    private static final String mHost = "localhost";
    private static final String mHashAlg = "sha-256";

    private MockServer mMockServer;

    public static class MockServerResource extends ServerResource {
        @Get
        public String mockGet() {
            assertNotNull("Hash not in URI query", getQuery().getFirstValue("hash"));
            assertNotNull("Hash algorithm not in URI query", getQuery().getFirstValue("hashAlg"));
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
        mMockServer = new MockServer();
        mMockServer.attach("/retrieve", MockServerResource.class);
        assertNotNull("Failed to create mock server", mMockServer);
        mMockServer.start();
        assertTrue("Server should be started", mMockServer.isStarted());

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Shutdown server
        mMockServer.stop();
        assertTrue("Server should be stopped", mMockServer.isStopped());
    }

    public void testRetrieveExistent() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create retrieve
        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNotNull("Should have received a response", jsonResponse);
                Object obj = JSONValue.parse(jsonResponse);
                assertTrue("Should have received JSON as response", obj instanceof JSONObject);
                JSONObject json = (JSONObject) obj;
                assertTrue("JSON response should have contained file path",json.containsKey("filePath"));
                assertTrue("JSON response should have contained content type", json.containsKey("contentType"));
                assertEquals("JSON response contained wrong file path", json.get("filePath"), mFilePath);
                assertEquals("JSON response contained wrong content type", json.get("contentType"), mContentType);
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
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testRetrieveNonExistent() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, Integer.toString(MockServer.PORT), mHashAlg, mWrongHash) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNull("Should not have received a response", jsonResponse);
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
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testWrongPort() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, Integer.toString(MockServer.WRONG_PORT), mHashAlg, mHash) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNull("Should not have received a response", jsonResponse);
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
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

}
