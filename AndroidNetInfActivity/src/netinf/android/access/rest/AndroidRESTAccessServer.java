package netinf.android.access.rest;

import netinf.common.datamodel.DatamodelFactory;
import netinf.node.access.AccessServer;
import netinf.node.api.impl.LocalNodeConnection;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

import com.google.inject.Inject;
import com.google.inject.name.Named;


public class AndroidRESTAccessServer implements AccessServer {

	   private Component component;

	   @Inject
	   public AndroidRESTAccessServer(@Named("access.http.port") int port, LocalNodeConnection connection, DatamodelFactory factory) {
	      component = new Component();
	      component.getServers().add(Protocol.HTTP, port);

	      Application application = new AndroidRESTApplication(connection, factory);

	      component.getDefaultHost().attach(application);
	   }
	   
	   /**
	    * Starts the RESTAccessServer.
	    */
	  public void start() {
	      try {
	         component.start();
	      } catch (Exception e) {
	        
	      }
	   }

	   /**
	    * Stops the RESTAccessServer.
	    */
	   
	   public void stop() {
	      try {
	         component.stop();
	      } catch (Exception e) {
	        
	      }
	   }
}
