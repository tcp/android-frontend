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
package netinf.node.transferdispatcher;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.log.demo.DemoLevel;
import netinf.node.chunking.Chunk;
import netinf.node.chunking.ChunkedBO;
import netinf.node.chunking.NetInfNotChunkableException;
import netinf.node.transferdispatcher.chunkstreams.ConcurrentChunkStream;
import netinf.node.transferdispatcher.chunkstreams.SequentialChunkStream;
import netinf.node.transferdispatcher.streamprovider.FTPStreamProvider;
import netinf.node.transferdispatcher.streamprovider.HTTPStreamProvider;
import netinf.node.transferdispatcher.streamprovider.NetInfNoStreamProviderFoundException;
import netinf.node.transferdispatcher.streamprovider.StreamProvider;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * The TransferDispatcher. Responsible for Providing stream to IOs/Files.
 * 
 * @author PG NetInf 3, University of Paderborn.
 * @pat.name Singleton.
 * @pat.task Forces that only one instance of this class exists.
 */
public final class TransferDispatcher {

   private static final Logger LOG = Logger.getLogger(TransferDispatcher.class);
   private List<StreamProvider> streamProviders;
   private static TransferDispatcher instance;
   private int chunkingTechnique = 1;

   /**
    * Constructor.
    */
   private TransferDispatcher() {
      addStreamProviders();
   }

   /**
    * Adds available StreamProviders to the TD.
    */
   private void addStreamProviders() {
      streamProviders = new ArrayList<StreamProvider>();
      streamProviders.add(new FTPStreamProvider());
      streamProviders.add(new HTTPStreamProvider());
   }

   /**
    * Provides the singleton instance of a TransferDispatcher.
    * 
    * @return The TransferDispatcher.
    */
   public static synchronized TransferDispatcher getInstance() {
      if (instance == null) {
         instance = new TransferDispatcher();
      }
      return instance;
   }

   /**
    * Provides a stream to the given url.
    * 
    * @param url
    *           The url of the file.
    * @return The Stream.
    * @throws IOException
    * @throws NetInfNoStreamProviderFoundException
    */
   public InputStream getStream(String url) throws IOException, NetInfNoStreamProviderFoundException {
      LOG.log(DemoLevel.DEMO, "(TD ) Getting Transfer-Stream from: " + url);
      StreamProvider dl = getStreamProvider(url);
      return dl.getStream(url);
   }

   /**
    * Provides the stream to a given chunk.
    * 
    * @param chunk
    *           The chunk-object.
    * @param chunkUrl
    *           The url to the chunk.
    * @return Stream to the chunk.
    * @throws IOException
    * @throws NetInfNoStreamProviderFoundException
    */
   public InputStream getStream(Chunk chunk, String chunkUrl) throws IOException, NetInfNoStreamProviderFoundException {
      LOG.log(DemoLevel.DEMO, "(TD ) Getting Transfer-Stream for Chunk from: " + chunkUrl);
      StreamProvider dl = getStreamProvider(chunkUrl);
      return dl.getStream(chunk, chunkUrl);
   }

   /**
    * Provides the stream by a given DO.
    * 
    * @param dataObj
    *           The DO.
    * @return Stream to the underlying BO.
    * @throws IOException
    */
   public InputStream getStream(DataObject dataObj) throws IOException {
      LOG.log(DemoLevel.DEMO, "(TD ) Getting Transfer-Stream from IO: " + dataObj.getIdentifier());

      // try to use at first chunks/ranges
      try {
         ChunkedBO chunkedBO = new ChunkedBO(dataObj);
         LOG.log(DemoLevel.DEMO, "(TD ) Chunks exist, use them...");

         switch (chunkingTechnique) {
         case 1:
            LOG.info("(TransferDispatcher ) Using ConcurrentChunkStream...");
            return new ConcurrentChunkStream(chunkedBO);
         case 2:
            LOG.info("(TransferDispatcher ) SequentialChunkStream...");
            return new SequentialChunkStream(chunkedBO);
         default:
            LOG.info("(TransferDispatcher ) Nothing selected... Using ConcurrentChunkStream");
            return new ConcurrentChunkStream(chunkedBO);
         }

      } catch (NetInfNotChunkableException e1) {
         LOG.info("(TransferDispatcher ) Chunking can not be used for this DO: " + e1.getMessage());
      }

      // then try normal locators
      LOG.info("(TransferDispatcher ) Try to get stream over normal locators");
      LOG.log(DemoLevel.DEMO, "(TD ) Try to get stream over normal locators");
      LocatorSelector locSel = new LocatorSelector(dataObj);
      while (locSel.hasNext()) {
         try {
            return this.getStream(locSel.next());
         } catch (NetInfNoStreamProviderFoundException e) {
            LOG.warn("(TransferDispatcher ) NoStreamProviderFoundException: " + e.getMessage());
         } catch (IOException e) {
            LOG.warn("(TransferDispatcher ) IOException: " + e.getMessage());
         }
      }

      throw new IOException("Stream could not be provided");
   }

   /**
    * Saves the a file (url) to the given destination.
    * 
    * @param url
    *           The source of the file (e.g. http).
    * @param destination
    *           Destination of the file (diskpath).
    * @param withContentType
    *           If the content-type should be encoded into the saved file.
    * @throws NetInfNoStreamProviderFoundException
    * @throws IOException
    */
   public void getStreamAndSave(String url, String destination, boolean withContentType)
         throws NetInfNoStreamProviderFoundException, IOException {

      File file = new File(destination);
      if (file.exists() && file.isFile()) {
         return;
      }

      LOG.log(DemoLevel.DEMO, "(TD ) Starting Download from: " + url);
      InputStream is = getStream(url);
      DataOutputStream dos = null;
      try {
         dos = new DataOutputStream(new FileOutputStream(destination));
         IOUtils.copy(is, dos);
      } catch (Exception e) {
         LOG.warn("(TransferDispatcher ) Could not download data from: " + url + " - " + e.getMessage());
      } finally {
         IOUtils.closeQuietly(is);
         IOUtils.closeQuietly(dos);
      }
   }

   /**
    * Provides the appropriate StreamProvider.
    * 
    * @param url
    *           The URL to which a stream shall be povided.
    * @return The specific StreamProvider.
    * @throws NetInfNoStreamProviderFoundException
    *            If no provider could be found.
    */
   StreamProvider getStreamProvider(String url) throws NetInfNoStreamProviderFoundException {
      for (StreamProvider sp : streamProviders) {
         if (sp.canHandle(url)) {
            LOG.log(DemoLevel.DEMO, "(TD ) Using " + sp.describe());
            return sp;
         }
      }
      throw new NetInfNoStreamProviderFoundException(url + " not supported");
   }

}