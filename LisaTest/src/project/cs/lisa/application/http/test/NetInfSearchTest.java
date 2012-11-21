package project.cs.lisa.application.http.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import project.cs.lisa.application.MainNetInfActivity;
import project.cs.lisa.application.http.NetInfSearch;

import project.cs.lisa.mock.MockServer;

import android.test.InstrumentationTestCase;

/**
 * Test cases for NetInfSearch
 * @author Thiago Costa Porto
 */
public class NetInfSearchTest extends InstrumentationTestCase {
    public static final String TAG = "NetInfSearchTest";

    private static final int TIMEOUT_SECONDS = 5;

    // Correct Host
    private static final String mHost = "localhost";
    
    private static final String mMsgId = "189371278363-123893712";
    private static final String mTokens = "lingus";
    private static final String mExt = "";

    private MockServer mMockServer;

    /* TEST SPECIFIC STRINGS */
    // Wrong Host
    private static final String mWrongHost = "LINUS-host";
    
    // Wrong Port
    private static final String mWrongPort = "8172";
    
    /**
     * Mock Server. SuppressedWarnings because... I can.
     * @author Thiago Costa Porto
     */
    public static class MockServerResource extends ServerResource {
        @SuppressWarnings("unchecked")
        @Get
        public String mockGet() {
            if (getQuery().getFirstValue("tokens").equals("BADDYBAO")) {
                JSONObject json = new JSONObject();
                return json.toJSONString();
            }
            JSONObject json = new JSONObject();
            json.put("NetInf", "v.01a");
            json.put("msgid", mMsgId);
            json.put("status", 200);
            json.put("ts", "2012-11-07T10:02:15+00:00");
            json.put("results", "bacon");
            return json.toJSONString();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Setup HTTP Server
        mMockServer = new MockServer();
        mMockServer.attach("/search", MockServerResource.class);

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

    /**
     * Tests a successful search request.
     * @throws Throwable
     */
    public void testSearchSuccess() throws Throwable {
        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create activity
//        MainNetInfActivity mActivity = MainNetInfActivity.getActivity();

        // Create search
        final NetInfSearch search = new NetInfSearch(mHost,
                Integer.toString(MockServer.PORT), mTokens, mExt) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNotNull("Should have received a response", jsonResponse);
                Object obj = JSONValue.parse(jsonResponse);
                assertTrue("Should have received JSON as response", obj instanceof JSONObject);
                JSONObject json = (JSONObject) obj;
                assertTrue("JSON response should have NetInf field",json.containsKey("NetInf"));
                assertTrue("JSON response should have msgId", json.containsKey("msgid"));
                assertTrue("JSON response should have status", json.containsKey("status"));
                assertTrue("JSON response should have timestamp (ts)", json.containsKey("ts"));
                assertTrue("JSON response should have results", json.containsKey("results"));
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                search.execute();
            }

        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    public void testSearchTokenNotFound() throws Throwable {
     // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create activity
//        MainNetInfActivity mActivity = MainNetInfActivity.getActivity();

        // Create search
        final NetInfSearch search = new NetInfSearch(mHost,
                Integer.toString(MockServer.PORT), "BADDYBAO", mExt) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNotNull("Should have received a response", jsonResponse);
                Object obj = JSONValue.parse(jsonResponse);
                assertTrue("Should have received JSON as response", obj instanceof JSONObject);
                JSONObject json = (JSONObject) obj;
                assertTrue("JSON response should be empty", json.isEmpty());
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                search.execute();
            }

        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }


    public void testSearchHostUnknown() throws Throwable {
        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create activity
//        MainNetInfActivity mActivity = MainNetInfActivity.getActivity();

        // Create search
        final NetInfSearch search = new NetInfSearch(mWrongHost,
                Integer.toString(MockServer.PORT), mTokens, mExt) {
            @Override
            protected void onPostExecute(String jsonResponse) {
                assertNull("Should NOT have received a response", jsonResponse);
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                search.execute();
            }

        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    public void testSearchPortNotSet() throws Throwable {

    }

    public void testSearchPortInvalid() throws Throwable {

    }

    public void testSearchConnectionFailed() throws Throwable {

    }

    public void testSearchConnectionSuccessful() throws Throwable {

    }
    
    public void testSearchTokensNotEncoded() throws Throwable {
        
    }
    
    public void testSearchExtNotEncoded() throws Throwable {
        
    }
    
    public void testSearchMsgIdNotEncoded() throws Throwable {
        
    }
    
    public void testSearchNoHashReturn() throws Throwable {
        
    }
    
    public void testSearchBadMessageReturned() throws Throwable {
        
    }
    //
    //        public void testRetrieveNonExistent() throws Throwable {
    //
    //            // Signal used to wait for ASyncTask
    //            final CountDownLatch signal = new CountDownLatch(1);
    //
    //            final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, Integer.toString(MockServer.PORT), mHashAlg, mWrongHash) {
    //                @Override
    //                protected void onPostExecute(String jsonResponse) {
    //                    assertNull("Should not have received a response", jsonResponse);
    //                    signal.countDown();
    //                }
    //            };
    //
    //            // Run on UI thread
    //            runTestOnUiThread(new Runnable() {
    //                @Override
    //                public void run() {
    //                    retrieve.execute();
    //                }
    //            });
    //
    //            // Wait a few seconds for the done signal, if timeout fail
    //            assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
    //                    signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    //
    //        }
    //
    //        public void testWrongPort() throws Throwable {
    //
    //            // Signal used to wait for ASyncTask
    //            final CountDownLatch signal = new CountDownLatch(1);
    //
    //            final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, Integer.toString(MockServer.WRONG_PORT), mHashAlg, mHash) {
    //                @Override
    //                protected void onPostExecute(String jsonResponse) {
    //                    assertNull("Should not have received a response", jsonResponse);
    //                    signal.countDown();
    //                }
    //            };
    //
    //            // Run on UI thread
    //            runTestOnUiThread(new Runnable() {
    //                @Override
    //                public void run() {
    //                    retrieve.execute();
    //                }
    //            });
    //
    //            // Wait a few seconds for the done signal, if timeout fail
    //            assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
    //                    signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    //
    //        }
    //
    //    }
}
