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

import project.cs.lisa.netinf.node.access.rest.resources.LisaIOResource;

public class LisaRESTApplication extends Application {
	
		/** Connection to a NetInfNode */
		private NetInfNodeConnection nodeConnection;
		/** Implementation of a DatamodelFacotry */
		private DatamodelFactory datamodelFactory;

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
			
//			router.attach("/", HelloWorldResource.class).setMatchingMode(Template.MODE_STARTS_WITH);
			
			// Redirect short uri requests
			String target = "{rh}/io?HASH_ALG={hash_alg}&HASH_CONT={hash_cont}&CT={cont_type}" +
					"&METHOD={Method}&BTMAC={bluetoothaddress}";	     
			Redirector redirector = new Redirector(getContext(), target, Redirector.MODE_CLIENT_TEMPORARY);
			Extractor extractor = new Extractor(getContext(), redirector);	      	          
			extractor.extractFromQuery("bluetoothaddress", "BTMAC", true);
			extractor.extractFromQuery("Method", "METHOD", true);
			extractor.extractFromQuery("cont_type", "CT", true);
		      
			router.attach("/ni/{hash_alg};{hash_cont}", extractor);
			
			router.attach("/io", LisaIOResource.class);
			
			return router;
		}
}
