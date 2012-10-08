package netinf.android.access.rest;

import java.util.logging.Handler;
import java.util.logging.LogManager;

import netinf.android.access.rest.resources.IOServer;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Extractor;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

public class AndroidRESTApplication extends Application {
	
	/** Connection to a NetInfNode */
	   private NetInfNodeConnection nodeConnection;
	   /** Implementation of a DatamodelFacotry */
	   private DatamodelFactory datamodelFactory;

	   public AndroidRESTApplication(NetInfNodeConnection connection, DatamodelFactory factory) {
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
	      
	      // IO routing
	      String targetIO1 = "{rh}/io?HASH_ALG={hash_alg}&HASH_CONT={hash_cont}&METHOD={Method}&BTMAC={bluetoothaddress}&WIMAC={wifiaddress}&NCS={ncsaddress}";	     
	      Redirector redirectorIO1 = new Redirector(getContext(), targetIO1, Redirector.MODE_CLIENT_TEMPORARY);
	      Extractor extractorIO1 = new Extractor(getContext(), redirectorIO1);	      	          
	      extractorIO1.extractFromQuery("bluetoothaddress", "BTMAC", true);
	      extractorIO1.extractFromQuery("wifiaddress", "WIMAC", true);
	      extractorIO1.extractFromQuery("ncsaddress", "NCS", true); 
	      extractorIO1.extractFromQuery("Method", "METHOD", true);
	      
	      router.attach(
		            "/.well-known/ni/{hash_alg};{hash_cont}",
		            extractorIO1);
	     	              
	     router.attach("/io", IOServer.class);
	     
//	      // IO routing
//	      String targetIO2 = "{rh}/transfer?IP={ip}&HASH_CONT={hash_cont}";	     
//	      Redirector redirectorIO2 = new Redirector(getContext(), targetIO2, Redirector.MODE_CLIENT_TEMPORARY);
//	      Extractor extractorIO2 = new Extractor(getContext(), redirectorIO2);	      	          
//	      extractorIO2.extractFromQuery("ip", "IP", true);
//	      extractorIO2.extractFromQuery("hash_cont", "HASH_CONT", true);
//	      
//	      router.attach(
//		            "/.well-known/ni/transfer",
//		            extractorIO2);
	     	              
	     router.attach("/transfer", IOServer.class);	

	      return router;
	   }
}
