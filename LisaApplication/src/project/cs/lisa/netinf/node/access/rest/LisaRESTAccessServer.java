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

import netinf.common.datamodel.DatamodelFactory;
import netinf.node.access.AccessServer;
import netinf.node.api.impl.LocalNodeConnection;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;


import com.google.inject.Inject;
import com.google.inject.name.Named;


public class LisaRESTAccessServer implements AccessServer {

	   private Component component;

	   /**
	    * Constructor that creates a new RESTful server
	    * @param port the connection port (is injected)
	    * @param connection the connection to the node 
	    * @param factory creates different objects necessary in the NetInf model
	    */
	   @Inject
	   public LisaRESTAccessServer(@Named("access.http.port") int port, LocalNodeConnection connection, DatamodelFactory factory) {
	      component = new Component();
	      component.getServers().add(Protocol.HTTP, port);
	      Application application = new LisaRESTApplication(connection, factory);
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
