package project.cs.lisa.netinf;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import android.util.Log;

public class HelloWorldResource extends ServerResource {
	
	public static final String TAG = "HelloWorldResource";
	
	@Override
	protected void doInit() {
    	super.doInit();
    	Log.d(TAG, "doInit()");
	}

	@Get
	public String handleGet() {
		Log.d(TAG, "handleGet()");
		return new String("Duh, hello world!");
	}
	
	@Post
	public void handlePost() {
		Log.d(TAG, "handlePost()");
	}
	
}
