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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.security.Hashing;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;
import netinf.node.api.impl.LocalNodeConnection;
import netinf.node.chunking.Chunk;
import netinf.node.chunking.ChunkedBO;
import netinf.node.transferdispatcher.LocatorSelector;
import netinf.node.transferdispatcher.TransferDispatcher;
import netinf.node.transferdispatcher.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * A single CacheJob. Used by {@link CachingInterceptor}
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class CacheJob extends Thread {

   private static final Logger LOG = Logger.getLogger(CacheJob.class);
   private List<BOCache> usedCaches;
   private LocalNodeConnection connection;
   private DataObject toBeCached;
   private TransferDispatcher transferDispatcher;
   private boolean useChunking;

   /**
    * Constructor
    * 
    * @param dO
    *           The DO that should be used for caching.
    * @param caches
    *           The to be used caches.
    * @param conn
    *           The connection to the local node.
    * @param useChunking
    *           The flag if chunking should be used.
    */
   public CacheJob(DataObject dO, List<BOCache> caches, LocalNodeConnection conn, boolean useChunking) {
      transferDispatcher = TransferDispatcher.getInstance();
      usedCaches = caches;
      connection = conn;
      toBeCached = dO;
      this.useChunking = useChunking;
   }

   @Override
   public void run() {
      // if it should be cached is already checked in CachingInterceptor

      // download file
      String cachedTmpFile = downloadAndCheckHashOfFile();
      if (cachedTmpFile == null) {
         return;
      }

      // add chunk list
      if (useChunking) {
         addChunkList(cachedTmpFile);
      }

      // store in available caches - top down to avoid false overwriting
      while (this.cachesExist()) {
         BOCache useCache = this.getNextCacheTopDown();
         if (useCache != null) {
            LOG.info("(CacheJob ) CachingJob STARTED for: " + useCache.getName());
            LOG.log(DemoLevel.DEMO, "(CacheJob ) Caching STARTED for: " + useCache.getName());
            boolean success = useCache.cache(toBeCached, cachedTmpFile);
            if (success) {
               LOG.info("(CacheJob ) CachingJob FINISHED for: " + useCache.getName());
               LOG.log(DemoLevel.DEMO, "(CacheJob ) Caching FINISHED for: " + useCache.getName());
               // put into RS
               try {
                  LOG.info("(CacheJob ) Putting back to RS (+ new locator from caching) for: " + useCache.getName());
                  connection.putIO(toBeCached);
               } catch (NetInfCheckedException e) {
                  LOG.warn("(CacheJob ) Error during putting back... " + e.getMessage());
               }
            } else {
               LOG.info("(CacheJob ) CachingJob FAILED for: " + useCache.getName());
            }
         }
      }

      // delete after caching
      deleteTmpFile(cachedTmpFile);
   }

   private boolean cachesExist() {
      if (usedCaches.isEmpty()) {
         return false;
      }
      return true;
   }

   private BOCache getNextCacheTopDown() {
      BOCache returnCache = null;
      int level = -1;

      // get highest available cache
      for (BOCache useCache : usedCaches) {
         if (useCache.getScope() >= level) {
            level = useCache.getScope();
            returnCache = useCache;
         }
      }

      // remove from current list
      if (returnCache != null) {
         usedCaches.remove(returnCache);
      }

      // return
      return returnCache;
   }

   /**
    * Downloads the BO of the underlying DO and checks the hash value.
    * 
    * @return The path to the downloaded file, otherwise null.
    */
   private String downloadAndCheckHashOfFile() {
      String hashOfBO = DatamodelUtils.getHash(toBeCached);
      LocatorSelector locSel = new LocatorSelector(toBeCached);
      while (locSel.hasNext()) {
         String url = locSel.next();
         try {
            String destination = Utils.getTmpFolder("netinfCache") + File.separator + hashOfBO + ".tmp";
            transferDispatcher.getStreamAndSave(url, destination, false);

            // get hash
            DataInputStream fis = new DataInputStream(new FileInputStream(destination));
            byte[] hashBytes = Hashing.hashSHA1(fis);
            IOUtils.closeQuietly(fis);

            if (hashOfBO.equalsIgnoreCase(Utils.hexStringFromBytes(hashBytes))) {
               LOG.log(DemoLevel.DEMO, "(CacheJob ) Hash of file is VALID");
               return destination;
            } else {
               LOG.info("Hash of file: " + hashOfBO + " -- Other: " + Utils.hexStringFromBytes(hashBytes));
               LOG.log(DemoLevel.DEMO, "(CacheJob ) Hash of downloaded file is invalid. Trying next locator");
            }

         } catch (FileNotFoundException ex) {
            LOG.warn("FileNotFound: " + url);
         } catch (IOException e) {
            LOG.warn("IOException:" + url);
         } catch (NetInfNoStreamProviderFoundException no) {
            LOG.warn("No StreamProvider found for: " + url);
         }
      }
      return null;
   }

   /**
    * Adds the chunk list to the underlying DO
    * 
    * @param cachedTmpFile
    *           Path to the temp file
    */
   private void addChunkList(String cachedTmpFile) {
      if (containsChunkList()) {
         return;
      }

      try {
         ChunkedBO chunkedBO = new ChunkedBO(cachedTmpFile);

         // Chunks
         Attribute chunksAttr = toBeCached.getDatamodelFactory().createAttribute();
         chunksAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
         chunksAttr.setIdentification(DefinedAttributeIdentification.CHUNKS.getURI());
         chunksAttr.setValue(chunkedBO.getTotalNoOfChunks());

         for (Chunk chunk : chunkedBO.getChunks()) {
            // Subattribute Chunk
            Attribute singleChunkAttr = toBeCached.getDatamodelFactory().createAttribute();
            singleChunkAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
            singleChunkAttr.setIdentification(DefinedAttributeIdentification.CHUNK.getURI());
            singleChunkAttr.setValue(chunk.getNumber());

            // SubSubattribute
            Attribute hashOfChunkAttr = toBeCached.getDatamodelFactory().createAttribute();
            hashOfChunkAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
            hashOfChunkAttr.setIdentification(DefinedAttributeIdentification.HASH_OF_CHUNK.getURI());
            hashOfChunkAttr.setValue(chunk.getHash());

            // add attributes
            singleChunkAttr.addSubattribute(hashOfChunkAttr);
            chunksAttr.addSubattribute(singleChunkAttr);
         }

         // add chunk list
         toBeCached.addAttribute(chunksAttr);

      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
   }

   /**
    * Checks if a chunk list already exists.
    * 
    * @return true if chunk list exists, otherwise false.
    */
   private boolean containsChunkList() {
      for (Attribute chunkLists : toBeCached.getAttribute(DefinedAttributeIdentification.CHUNKS.getURI())) {
         List<Attribute> chunks = chunkLists.getSubattribute(DefinedAttributeIdentification.CHUNK.getURI());
         if (!chunks.isEmpty()) {
            LOG.info("(CacheJob ) Chunklist already exists");
            return true;
         }
      }
      LOG.info("(CacheJob ) Chunklist does not exist");
      return false;
   }

   /**
    * Deletes the given file
    * 
    * @param path
    *           The path of the file
    */
   private void deleteTmpFile(String path) {
      File file = new File(path);
      file.delete();
   }

   /**
    * Provides the name of this CacheJob
    * 
    * @return Name as string.
    */
   public String getThreadName() {
      return toBeCached.getIdentifier().toString();
   }
}
