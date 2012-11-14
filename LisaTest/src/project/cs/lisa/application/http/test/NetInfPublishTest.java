package project.cs.lisa.application.http.test;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.mock.MockServer;
import android.test.InstrumentationTestCase;
import android.util.Log;

public class NetInfPublishTest extends InstrumentationTestCase {

    public static final String TAG = "NetInfPublishTest";

    private static final int TIMEOUT_SECONDS = 5;

    private static final String mHash = "hashThatExists";

    private static final String mHost = "localhost";
    private static final String mHashAlg = "sha-256";
    private static final String mBluetoothMac = "11:22:33:44:55:66";

    private MockServer mMockServer;

    public static class MockServerResource extends ServerResource {
        @Put
        public String mockGet() {
            Log.d("TEST", "@Get");
            assertNotNull("Hash not in URI query", getQuery().getFirstValue("hash"));
            assertNotNull("Hash algorithm not in URI query", getQuery().getFirstValue("hashAlg"));

            if (getQuery().getFirstValue("hash").equals(mHash)) {
                JSONObject json = new JSONObject();
                json.put("status", "ok");
                return json.toJSONString();
            } else {
                JSONObject json = new JSONObject();
                json.put("status", "failed");
                return json.toJSONString();
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Setup HTTP Server
        mMockServer = new MockServer();
        mMockServer.attach("/publish", MockServerResource.class);
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

    public void testPublishWithoutLocator() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        HashSet<Locator> locators = null;

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNull("Should not have received a response", jsonResponse);
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testPublishOneLocator() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        HashSet<Locator> locators = new HashSet<Locator>();
        Locator locator = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        locators.add(locator);

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNotNull("Should have received a response", jsonResponse);
                Object obj = JSONValue.parse(jsonResponse);
                assertTrue("Should have received JSON as response", obj instanceof JSONObject);
                JSONObject json = (JSONObject) obj;
                assertTrue("JSON response should have contained status",json.containsKey("status"));
                assertEquals("JSON response should have contained status ok", json.get("status"), "ok");
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testPublishMultipleLocators() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        HashSet<Locator> locators = new HashSet<Locator>();
        Locator locator1 = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        Locator locator2 = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        locators.add(locator1);
        locators.add(locator2);

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNotNull("Should have received a response", jsonResponse);
                Object obj = JSONValue.parse(jsonResponse);
                assertTrue("Should have received JSON as response", obj instanceof JSONObject);
                JSONObject json = (JSONObject) obj;
                assertTrue("JSON response should have contained status",json.containsKey("status"));
                assertEquals("JSON response should have contained status ok", json.get("status"), "ok");
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testPublishWrongPort() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Locator
        HashSet<Locator> locators = new HashSet<Locator>();
        Locator locator = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        locators.add(locator);

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.WRONG_PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNull("Should not have received a response", jsonResponse);
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

}
