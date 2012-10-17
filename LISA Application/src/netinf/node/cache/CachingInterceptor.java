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
package netinf.node.cache;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.log.demo.DemoLevel;
import netinf.common.utils.DatamodelUtils;
import netinf.node.api.impl.LocalNodeConnection;
import netinf.node.cache.network.NetworkCache;
import netinf.node.cache.peerside.PeersideCache;
import netinf.node.resolution.ResolutionInterceptor;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An Interceptor that coordinates all available caches.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class CachingInterceptor implements ResolutionInterceptor {

   private static final Logger LOG = Logger.getLogger(CachingInterceptor.class);
   private LocalNodeConnection connection;
   private List<BOCache> usedCaches = new ArrayList<BOCache>();
   private Hashtable<String, Thread> runningCacheJobs = new Hashtable<String, Thread>();
   private boolean useChunking;

   /**
    * Constructor
    * 
    * @param useChunking
    *           The injected chunking flag.
    */
   @Inject
   public CachingInterceptor(@Named("chunking") final boolean useChunking) {
      this.useChunking = useChunking;
      if (this.useChunking) {
         LOG.info("(CachingInterceptor ) Chunking is enabled");
         LOG.log(DemoLevel.DEMO, "(Caching ) I use CHUNKING for caching BOs");
      } else {
         LOG.info("(CachingInterceptor ) Chunking is NOT enabled");
         LOG.log(DemoLevel.DEMO, "(Caching ) I do not use CHUNKING for caching BOs");
      }
   }

   @Inject(optional = true)
   public void setPeersideCaches(List<PeersideCache> caches) {
      for (PeersideCache cache : caches) {
         if (!cache.isConnected()) {
            LOG.info("(CachingInterceptor ) PeersideCache " + cache.getAddress() + " is not connected");
            LOG.log(DemoLevel.DEMO, "(Caching ) I cannot cache via the PEERSIDECACHE on: " + cache.getAddress());
         } else {
            BOCache peerCache = new BOCache((BOCacheServer) cache, "PeersideCache", cache.getScope());
            usedCaches.add(peerCache);
            LOG.info("(CachingInterceptor ) " + cache.getAddress() + " connected");
            LOG.log(DemoLevel.DEMO, "(Caching ) I can cache via the PEERSIDECACHE on: " + cache.getAddress());
         }
      }
   }

   @Inject(optional = true)
   public void setNeworkCaches(List<NetworkCache> caches) {
      for (NetworkCache cache : caches) {
         if (!cache.isConnected()) {
            LOG.info("(CachingInterceptor ) NetworkCache " + cache.getAddress() + " is not connected");
            LOG.log(DemoLevel.DEMO, "(Caching ) I cannot cache via the NetworkCache on: " + cache.getAddress());
         } else {
            BOCache netCache = new BOCache((BOCacheServer) cache, "NetworkCache", cache.getScope());
            usedCaches.add(netCache);
            LOG.info("(CachingInterceptor ) " + cache.getAddress() + " connected");
            LOG.log(DemoLevel.DEMO, "(Caching ) I can cache via the NetworkCache on: " + cache.getAddress());
         }
      }
   }

   @Inject
   public void setNodeConnection(LocalNodeConnection conn) {
      connection = conn;
   }

   @Override
   public InformationObject interceptGet(InformationObject io) {
      LOG.info("(CachingInterceptor ) GET intercepted...");

      if (!(io instanceof DataObject)) {
         LOG.info("(CachingInterceptor ) IO is no DataObject");
         return io;
      }

      List<BOCache> useThisCaches = whoShouldCache(io);
      if (useThisCaches.isEmpty()) {
         LOG.info("(CachingInterceptor ) nobody should cache this DO");
         return io;
      }

      if (!isCacheable(io)) {
         LOG.info("(CachingInterceptor ) DO is not cacheable");
         return io;
      }

      // is a thread already working on that DO?
      String key = io.getIdentifier().toString();
      if (runningCacheJobs.containsKey(key)) {
         if (runningCacheJobs.get(key).isAlive()) {
            LOG.info("(CachingInterceptor ) CacheJob already started for this DO");
            return io;
         } else {
            runningCacheJobs.remove(key);
         }
      }

      LOG.info("(CacheInterceptor ) starting caching...");
      DataObject dO = (DataObject) io.clone();

      // start the caching process
      CacheJob job = new CacheJob(dO, useThisCaches, connection, useChunking);
      runningCacheJobs.put(key, job);
      job.start();

      // return the old IO, the new cached one will be reputted
      return io;
   }

   /**
    * Provides a list of caches that should be used
    * 
    * @param obj
    *           The IO
    * @return A subset of all usable caches (which make sense)
    */
   private List<BOCache> whoShouldCache(InformationObject obj) {
      List<BOCache> useThisCaches = new ArrayList<BOCache>();
      // List<Attribute> locators = obj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      for (BOCache cache : usedCaches) {
         if (!cache.contains(DatamodelUtils.getHash(obj))) {
            useThisCaches.add(cache);
         }

         // boolean addCache = true;
         // for (Attribute loc : locators) {
         // if (loc.getValue(String.class).contains(cache.getAddress())) {
         // addCache = false;
         // }
         // }
         // if (addCache) {
         // useThisCaches.add(cache);
         // }
      }

      return useThisCaches;
   }

   /**
    * Determines whether this IO is cachable (hash, locators) or not.
    * 
    * @param obj
    *           The IO that has to be checked.
    * @return true if IO is cachable, otherwise false.
    */
   private boolean isCacheable(InformationObject obj) {
      String hash = DatamodelUtils.getHash(obj);
      if (hash == null) {
         return false;
      }

      List<Attribute> locators = obj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      if (locators.isEmpty()) {
         return false;
      }

      return true;
   }
}
