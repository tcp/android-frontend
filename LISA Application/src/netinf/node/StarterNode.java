/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node;

import java.io.IOException;

import netinf.access.NetInfServer;
import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.node.access.AccessServer;
import netinf.node.api.NetInfNode;
import netinf.node.module.StandardNodeModule;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionService;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import netinf.node.transfer.TransferController;
import netinf.node.transfer.TransferService;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.hp.hpl.jena.sdb.shared.Access;

/**
 * This is the class that starts the whole NetInfNode. The properties file, and the module {@link StandardNodeModule} determine
 * the functionality of this netinfnode.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class StarterNode {

   private static final Logger LOG = Logger.getLogger(StarterNode.class);

   /**
    * The first method that is called for the NetInfNode. The NetInfNode can always be stopped by typing in the letter "x".
    * 
    * @param args
    *           The first parameter {@code args[0]} defines the module to use for the configuration of this node. If no parameter
    *           is given the module {@link StandardNodeModule} is used.
    * @throws IOException
    */
   public static void main(String[] args) throws IOException {
      Module mainModule = findCorrectModule(args);

      // Starts the whole NetInfNode
      StarterNode netInfNodeStarter = new StarterNode(mainModule);
      LOG.log(DemoLevel.DEMO, "(NODE ) Please wait while I'm starting up");
      boolean accessLaunched = netInfNodeStarter.start();

      if (accessLaunched) {
         LOG.log(DemoLevel.DEMO, "(NODE ) I'm ready");
         LOG.info("NetInfNode successfully started");
      } else {
         LOG.error("Initialization of essential NetInf Node components failed. Shutting down!");
      }
   }

   public static Module findCorrectModule(String[] args) {
      Module result = null;

      String moduleName = null;

      try {
         moduleName = args[0];

         Class<?> moduleClass = Class.forName(moduleName);
         Module module = (Module) moduleClass.newInstance();

         System.out.println("Using module '" + moduleName + "' for configuration");
         result = module;
      } catch (ClassNotFoundException e) {
         System.out.println("Not recognized module '" + moduleName + "'");
      } catch (InstantiationException e) {
         System.out.println("Not recognized module '" + moduleName + "'");
      } catch (IllegalAccessException e) {
         System.out.println("Not recognized module '" + moduleName + "'");
      } catch (ArrayIndexOutOfBoundsException aiooe) {
         System.out.println("No parameter given for module.");
      } finally {
         if (result == null) {
            System.out.println("Using defaul StandardNodeModule");
            result = new StandardNodeModule();
         }
      }

      return result;
   }

   private final Injector injector;
   private NetInfNode netInfNode;

   public StarterNode(Module module) throws IOException {
      injector = Guice.createInjector(module);
   }

   public boolean start() throws IOException {
      LOG.trace(null);

      netInfNode = injector.getInstance(NetInfNode.class);

      startResolution();
      startSearch();
      startTransfer();
      startAPIAccess();

      return startN2NAccess();
   }

   private void startResolution() {
      LOG.trace(null);
      ResolutionController resolutionController = netInfNode.getResolutionController();

      if (resolutionController != null) {
         // Plug in Resolution Services
         ResolutionService[] resolutionServices = injector.getInstance(ResolutionService[].class);

         if (resolutionServices.length == 0) {
            LOG.log(DemoLevel.DEMO, "(NODE ) I have no active resolution services");
         }

         for (ResolutionService resolutionService : resolutionServices) {
            resolutionController.addResolutionService(resolutionService);
            LOG.debug("Added resolution service '" + resolutionService.getClass().getCanonicalName() + "'");
            LOG.log(DemoLevel.DEMO, "(NODE ) I can resolve via " + resolutionService.describe());
         }
      }
   }

   private void startSearch() {
      LOG.trace(null);
      SearchController searchController = netInfNode.getSearchController();

      if (searchController != null) {
         // Plug in Search Services
         SearchService[] searchServices = injector.getInstance(SearchService[].class);

         if (searchServices.length == 0) {
            LOG.log(DemoLevel.DEMO, "(NODE ) I have no active search services");
         }

         for (SearchService searchService : searchServices) {
            searchController.addSearchService(searchService);
            LOG.debug("Added search service '" + searchService.getClass().getCanonicalName() + "'");
            LOG.log(DemoLevel.DEMO, "(NODE ) I can search via " + searchService.describe());
         }
      }
   }

   private void startTransfer() {
      LOG.trace(null);
      TransferController transferController = netInfNode.getTransferController();

      if (transferController != null) {
         // Plug in Transfer Services
         TransferService[] transferServices = injector.getInstance(TransferService[].class);

         if (transferServices.length == 0) {
            LOG.log(DemoLevel.DEMO, "(NODE ) I have no active transfer services");
         }

         for (TransferService transferService : transferServices) {
            transferController.addTransferService(transferService);
            LOG.debug("Added transerService service '" + transferService.getClass().getCanonicalName() + "'");
            LOG.log(DemoLevel.DEMO, "(NODE ) I can transfer with " + transferService.describe());
         }
      }
   }

   /**
    * @return <code>true</code> if at least one server is running. <code>false</code> otherwise.
    */
   private boolean startN2NAccess() {
      LOG.trace(null);

      boolean success = false;
      AsyncReceiveHandler asyncReceiveHandler = injector.getInstance(AsyncReceiveHandler.class);

      NetInfServer[] netInfServers = injector.getInstance(NetInfServer[].class);

      for (NetInfServer netInfServer : netInfServers) {
         netInfServer.setAsyncReceiveHandler(asyncReceiveHandler);
         try {
            netInfServer.start();
            success = true;
            LOG.debug("Added access service '" + netInfServer.getClass().getCanonicalName() + "'");
            LOG.log(DemoLevel.DEMO, "(NODE ) I can be accessed via " + netInfServer.describe());
         } catch (NetInfCheckedException e) {
            LOG.error("Failed to start '" + netInfServer.getClass().getCanonicalName() + "'", e);
         }
      }

      if (!success) {
         LOG.log(DemoLevel.DEMO, "(NODE ) I can't be accessed");
      }
      return success;
   }

   private void startAPIAccess() {
      LOG.trace(null);

      AccessServer[] accessServers = injector.getInstance(AccessServer[].class);
      for (AccessServer server : accessServers) {
         server.start();
         LOG.debug("Started AccessServer '" + server.getClass().getCanonicalName() + "'");
      }
   }

}
