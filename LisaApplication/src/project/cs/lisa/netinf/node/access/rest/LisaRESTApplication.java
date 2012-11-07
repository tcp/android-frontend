/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
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