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

import java.io.IOException;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

/**
 * General Cache Class.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class BOCache {

   private static final Logger LOG = Logger.getLogger(BOCache.class);
   private BOCacheServer cacheServer;
   private String cacheName = "BOCache";
   private int mdhtLevel;

   /**
    * Constructor
    * 
    * @param server
    *           The to be used CachingServer
    * @param name
    *           The name of this cache
    * @param level
    *           The level/scope if this cache
    */
   public BOCache(BOCacheServer server, String name, int level) {
      cacheServer = server;
      cacheName = name;
      mdhtLevel = level;
   }

   /**
    * Stores a DO in the cache and adds the locator to the DO
    * 
    * @param dataObject
    *           the DataObject that should be stored
    * @param downloadedTmpFile
    *           The path to the temp file (used for caching).
    */
   public boolean cache(DataObject dataObject, String downloadedTmpFile) {
      String hashOfBO = DatamodelUtils.getHash(dataObject);
      String urlPath = cacheServer.getURL(hashOfBO);

      if (!contains(hashOfBO)) {
         try {
            boolean success = cacheServer.cacheBO(Utils.getByteArray(downloadedTmpFile), hashOfBO); // TODO: stream instead
                                                                                                    // byteArray
            if (success) {
               addLocator(dataObject, urlPath);
               return true;
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
      } else {
         LOG.info("DO already in cache, but locator not in DO - adding locator entry...");
         addLocator(dataObject, urlPath);
         return true;
      }

      return false;
   }

   /**
    * Checks if the cache contains a specific BO
    * 
    * @param hashfOfBO
    *           the hash-value of the BO
    * @return true if BO exists, otherwise false
    */
   public boolean contains(String hashOfBO) {
      if (isConnected()) {
         return cacheServer.containsBO(hashOfBO);
      }
      return false;
   }

   /**
    * checks if the cache is connected
    * 
    * @return true if the cache is successfully connected, otherwise false
    */
   public boolean isConnected() {
      if (cacheServer != null && cacheServer.isConnected()) {
         return true;
      }
      return false;
   }

   /**
    * Provides the base address of the cache
    * 
    * @return The address as a string.
    */
   public String getAddress() {
      return cacheServer.getAddress();
   }

   /**
    * Provides the name of the cache.
    * 
    * @return Name as string
    */
   public String getName() {
      return cacheName;
   }

   /**
    * Adds a new locator to the DataObject
    * 
    * @param dataObject
    *           The given DataObject
    * @param url
    *           The URL of the locator
    */
   private void addLocator(DataObject dataObject, String url) {
      // Locator - http_url
      Attribute attribute = dataObject.getDatamodelFactory().createAttribute();
      attribute.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      attribute.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      attribute.setValue(url);

      // Cache marker
      Attribute cacheMarker = dataObject.getDatamodelFactory().createAttribute();
      cacheMarker.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      cacheMarker.setIdentification(DefinedAttributeIdentification.CACHE.getURI());
      cacheMarker.setValue("true");
      attribute.addSubattribute(cacheMarker);

      // add chunk/range enabled flag
      Attribute chunkFlag = dataObject.getDatamodelFactory().createAttribute();
      chunkFlag.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      chunkFlag.setIdentification(DefinedAttributeIdentification.CHUNKED.getURI());
      chunkFlag.setValue("true");
      attribute.addSubattribute(chunkFlag);

      // add cache priority = cache level
      Attribute locPrio = dataObject.getDatamodelFactory().createAttribute();
      locPrio.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      locPrio.setIdentification(DefinedAttributeIdentification.LOCATOR_PRIORITY.getURI());
      locPrio.setValue(mdhtLevel);
      attribute.addSubattribute(locPrio);

      // add new locator
      if (!dataObject.getAttributes().contains(attribute)) {
         dataObject.addAttribute(attribute);
      }

      // level attribute
      Attribute level = dataObject.getDatamodelFactory().createAttribute();
      level.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
      level.setIdentification(DefinedAttributeIdentification.MDHT_LEVEL.getURI());
      level.setValue(mdhtLevel);

      // add/set level attribute
      Attribute levelAttr = dataObject.getSingleAttribute(DefinedAttributeIdentification.MDHT_LEVEL.getURI());
      if (levelAttr != null) {
         levelAttr.setValue(mdhtLevel);
      } else {
         dataObject.addAttribute(level);
      }

   }

   /**
    * Gets the scope of this cache.
    * 
    * @return The scope/mdht level.
    */
   public int getScope() {
      return mdhtLevel;
   }

}
