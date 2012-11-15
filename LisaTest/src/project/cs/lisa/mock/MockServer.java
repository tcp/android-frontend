package project.cs.lisa.mock;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.ServerResource;

import android.util.Log;

public class MockServer extends ServerResource {

    public static final String TAG = "MockServer";

    public static final int PORT = 9998;
    public static final int WRONG_PORT = 9997;

    private Component mComponent;

    public MockServer() {
        mComponent = new Component();
        mComponent.getServers().add(Protocol.HTTP, PORT);
    }

    public void attach(String pathTemplate, Class<? extends ServerResource> targetClass) {
        Log.w(TAG, targetClass.getName());
        mComponent.getDefaultHost().attach(pathTemplate, targetClass);
    }

    public void start() throws Exception {
        mComponent.start();
    }

    public void stop() throws Exception {
        mComponent.stop();
    }

    public boolean isStarted() {
        return mComponent.isStarted();
    }

    public boolean isStopped() {
        return mComponent.isStopped();
    }

}