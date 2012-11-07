package project.cs.lisa.netinf.node.access.rest;

import java.util.logging.Handler;
import java.util.logging.LogManager;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Extractor;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

import project.cs.lisa.netinf.node.access.rest.resources.BOResource;
import project.cs.lisa.netinf.node.access.rest.resources.IOResource;

public class LisaRESTApplication extends Application {
	
		/** Connection to a NetInfNode */
		private NetInfNodeConnection nodeConnection;
		/** Implementation of a DatamodelFacotry */
		private DatamodelFactory datamodelFactory;

		/**
		 * Contructs a new RESTful Application
		 * @param connection Connection with the NetInf node
		 * @param factory creates different objects necessary in the NetInf model
		 */
		public LisaRESTApplication(NetInfNodeConnection connection, DatamodelFactory factory) {
			// Disable Restlet Logging
			java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
			Handler[] handlers = rootLogger.getHandlers();
			rootLogger.removeHandler(handlers[0]);

			nodeConnection = connection;
			datamodelFactory = factory;
		}
	   
		public NetInfNodeConnection getNodeConnection() {
			return nodeConnection;
		}

		public DatamodelFactory getDatamodelFactory() {
			return datamodelFactory;
		}

		@Override
		public Restlet createInboundRoot() {
			Router router = new Router(getContext());
			
			// Redirect NetInf Publish requests
			String target = "/io?HASH_ALG={hash_alg}&HASH={hash}&CT={ct}" +
					"&METHOD={method}&BTMAC={btmac}&META={meta}";	     
			Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_CLIENT_TEMPORARY);
			Extractor extractor = new Extractor(getContext(), redirector);    	          
			extractor.extractFromQuery("btmac", "BTMAC", true);
			extractor.extractFromQuery("method", "METHOD", true);
			extractor.extractFromQuery("ct", "CT", true);
			extractor.extractFromQuery("meta", "META", true); 
			
			router.attach("/ni/{hash_alg};{hash}", extractor);
			router.attach("/io", IOResource.class);
			
			// Redirect NetInf Get requests
			String getTarget = "/bo?HASH_ALG={hash_alg}&HASH={hash}";  
			Redirector getRedirector = new Redirector(getContext(), getTarget, Redirector.MODE_CLIENT_TEMPORARY);
			Extractor getExtractor = new Extractor(getContext(), getRedirector);
			
			router.attach("/bo/{hash_alg};{hash}", getExtractor);
			router.attach("/bo", BOResource.class);
			
			return router;
		}
}