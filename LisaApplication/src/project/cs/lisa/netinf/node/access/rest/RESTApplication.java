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
import org.restlet.routing.Router;

import project.cs.lisa.netinf.node.access.rest.resources.BOResource;
import project.cs.lisa.netinf.node.access.rest.resources.IOResource;
import project.cs.lisa.search.SearchRequest;

/**
 * Routes NetInf requests to the appropriate classes.
 * @author Linus Sunde
 *
 */
public class RESTApplication extends Application {

        /** Node Connection, used to access the local NetInf node. **/
		private NetInfNodeConnection mNodeConnection;
		/** Implementation of DatamodelFactory, used to create and edit InformationObjects etc. **/
		private DatamodelFactory mDatamodelFactory;

		/**
		 * Constructs a new RESTful Application for routing NetInf requests.
		 * @param connection Connection with the local NetInf node
		 * @param factory creates different objects necessary in the NetInf model
		 */
		public RESTApplication(NetInfNodeConnection connection, DatamodelFactory factory) {
			// Disable Restlet Logging
			java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
			Handler[] handlers = rootLogger.getHandlers();
			rootLogger.removeHandler(handlers[0]);

			mNodeConnection = connection;
			mDatamodelFactory = factory;
		}

		/**
		 * Gets a connection to the local NetInf node.
		 * @return the node connection
		 */
		public NetInfNodeConnection getNodeConnection() {
			return mNodeConnection;
		}

		/**
		 * Gets a data model factory implementation.
		 * @return the datamodel factory
		 */
		public DatamodelFactory getDatamodelFactory() {
			return mDatamodelFactory;
		}

		@Override
		public Restlet createInboundRoot() {
			Router router = new Router(getContext());

			router.attach("/publish", IOResource.class);

			router.attach("/retrieve", BOResource.class);
			
			router.attach("/search", SearchRequest.class);

			return router;
		}
}