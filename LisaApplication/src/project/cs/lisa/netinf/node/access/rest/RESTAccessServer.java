package project.cs.lisa.netinf.node.access.rest;

import netinf.common.datamodel.DatamodelFactory;
import netinf.node.access.AccessServer;
import netinf.node.api.impl.LocalNodeConnection;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;


import com.google.inject.Inject;
import com.google.inject.name.Named;


public class RESTAccessServer implements AccessServer {

	   private Component component;

	   /**
	    * Constructor that creates a new RESTful server
	    * @param port the connection port (is injected)
	    * @param connection the connection to the node 
	    * @param factory creates different objects necessary in the NetInf model
	    */
	   @Inject
	   public RESTAccessServer(@Named("access.http.port") int port, LocalNodeConnection connection, DatamodelFactory factory) {
	      component = new Component();
	      component.getServers().add(Protocol.HTTP, port);
	      Application application = new RESTApplication(connection, factory);
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
