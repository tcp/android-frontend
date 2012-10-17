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
package netinf.node.module;

import java.io.IOException;
import java.util.Properties;

import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.communication.MessageEncoderController;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.module.LogModule;
import netinf.node.api.NetInfNode;
import netinf.node.api.impl.NetInfNodeImpl;
import netinf.node.api.impl.NetInfNodeReceiveHandler;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionServiceSelector;
import netinf.node.resolution.eventprocessing.EventPublisher;
import netinf.node.resolution.eventprocessing.impl.EventPublisherImpl;
import netinf.node.resolution.eventprocessing.impl.EventService;
import netinf.node.resolution.impl.ResolutionControllerImpl;
import netinf.node.resolution.impl.SimpleResolutionServiceSelector;
import netinf.node.resolution.locator.LocatorCostProvider;
import netinf.node.resolution.locator.impl.RandomCostProvider;
import netinf.node.search.SearchController;
import netinf.node.search.impl.SearchControllerImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * This are general bindings which apply to all nodes. Everything that is variable, has to be defined in the subclasses.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class AbstractNodeModule extends AbstractModule {

   private final Properties properties;

   private static Integer eventServicePort;

   private static String eventServiceHost;

   @Inject(optional = true)
   public static void setEventServicePort(@Named("event_service.port") Integer esp) {
      eventServicePort = esp;
   }

   @Inject(optional = true)
   public static void setEventServiceHost(@Named("event_service.host") String esh) {
      eventServiceHost = esh;
   }

   public AbstractNodeModule(Properties properties) {
      this.properties = properties;
   }

   @Override
   protected void configure() {
      if (this.properties != null) {
         Names.bindProperties(binder(), getProperties());
         install(new LogModule(getProperties()));
      } else {
         throw new NetInfUncheckedException("Could not bind properties");
      }
      requestStaticInjection(AbstractNodeModule.class);
      bind(NetInfNode.class).to(NetInfNodeImpl.class).in(Singleton.class);
      // bind(NetInfNodeConnection.class).to(ConvenienceCommunicator.class);
      // bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(LocalNodeConnection.class);

      // SecurityRelated stuff
      install(new NodeSecurityModule());

      bind(ResolutionController.class).to(ResolutionControllerImpl.class).in(Singleton.class);
      bind(ResolutionServiceSelector.class).to(SimpleResolutionServiceSelector.class);
      if (properties.containsKey("event_service.host") && properties.containsKey("event_service.port")) {
         bind(EventPublisher.class).to(EventPublisherImpl.class);
      }

      bind(SearchController.class).to(SearchControllerImpl.class).in(Singleton.class);

      bind(AsyncReceiveHandler.class).to(NetInfNodeReceiveHandler.class);
      bind(MessageEncoderController.class).in(Singleton.class);

      bind(LocatorCostProvider.class).to(RandomCostProvider.class);

   }

   public Properties getProperties() {
      return this.properties;
   }

   @Provides
   @EventService
   Communicator provideEventServiceCommunicator(Provider<Communicator> communicatorProvider) {
      try {
         Communicator communicator = communicatorProvider.get();
         communicator.setup(eventServiceHost, eventServicePort);
         return communicator;
      } catch (IOException e) {
         throw new NetInfUncheckedException("Could not start connection to event service");
      }
   }

}
