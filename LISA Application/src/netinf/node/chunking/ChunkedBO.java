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
package netinf.node.chunking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.security.Hashing;
import netinf.common.utils.Utils;
import netinf.node.transferdispatcher.LocatorSelector;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * This class represents a chunked BO.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class ChunkedBO {

   private static final Logger LOG = Logger.getLogger(ChunkedBO.class);
   private int chunkSizeInBytes = 256 * 1024; // 256 KiB
   private List<Chunk> chunkList;
   private int totalNoOfChunks;
   private List<String> baseUrls;

   /**
    * Constructor.
    * 
    * @param dataObject
    *           The given DO.
    * @throws NetInfNotChunkableException
    */
   public ChunkedBO(DataObject dataObject) throws NetInfNotChunkableException {
      List<Attribute> chunks = this.getChunks(dataObject);
      if (chunks == null || chunks.isEmpty()) {
         throw new NetInfNotChunkableException("DataObject has NO list of chunks");
      }

      baseUrls = getRangeEnabledLocators(dataObject);
      if (baseUrls.isEmpty()) {
         throw new NetInfNotChunkableException("DataObject has no range enabled URLs");
      }

      chunkList = new ArrayList<Chunk>();
      for (Attribute chunkAttr : chunks) {
         chunkList.add(new Chunk(chunkAttr));
      }

      totalNoOfChunks = getTotalNumberOfChunks(dataObject);
      if (totalNoOfChunks != chunkList.size()) {
         throw new NetInfNotChunkableException("The number of chunks in the DO is not valid");
      }
   }

   /**
    * Provides a list with only range enabled locators.
    * 
    * @param obj
    *           The DataObject.
    * @return The locator list.
    */
   private List<String> getRangeEnabledLocators(DataObject obj) {
      List<String> urls = new ArrayList<String>();

      LocatorSelector selector = new LocatorSelector(obj);
      while (selector.hasNext()) {
         String loc = selector.next();
         if (providesRanges(loc)) {
            LOG.info("(ChunkedBO ) URL provides RANGEs: " + loc);
            urls.add(loc);
         } else {
            LOG.info("(ChunkedBO ) URL provides NOT RANGEs: " + loc);
         }
      }

      return urls;
   }

   private boolean providesRanges(String url) {
      HttpClient client = new DefaultHttpClient();
      try {
         HttpHead httpHead = new HttpHead(url);
         httpHead.setHeader("Range", "bytes=0-");
         try {
            HttpResponse response = client.execute(httpHead);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_PARTIAL_CONTENT || status == HttpStatus.SC_OK) {
               return true;
            }
         } catch (ClientProtocolException e) {
            LOG.debug(e.getMessage());
         } catch (IOException e) {
            LOG.debug(e.getMessage());
         }
      } catch (IllegalArgumentException e) {
         LOG.debug(e.getMessage());
      }
      return false;
   }

   @SuppressWarnings("unused")
   private List<String> getLocatorsWithChunkedLabel(DataObject obj) {
      List<String> urls = new ArrayList<String>();
      List<Attribute> attrs = obj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      for (Attribute attr : attrs) {
         List<Attribute> subAttrs = attr.getSubattribute(DefinedAttributeIdentification.CHUNKED.getURI());
         for (Attribute chunkedAttr : subAttrs) {
            if (chunkedAttr.getValue(String.class).equals("true")) {
               urls.add(attr.getValue(String.class));
            }
         }
      }
      return urls;
   }

   private List<Attribute> getChunks(DataObject obj) {
      List<Attribute> attrs = obj.getAttribute(DefinedAttributeIdentification.CHUNKS.getURI());
      for (Attribute chunkList : attrs) {
         List<Attribute> chunks = chunkList.getSubattribute(DefinedAttributeIdentification.CHUNK.getURI());
         if (!chunks.isEmpty()) {
            return chunks;
         }
      }
      return null;
   }

   private int getTotalNumberOfChunks(DataObject obj) {
      List<Attribute> attrs = obj.getAttribute(DefinedAttributeIdentification.CHUNKS.getURI());
      for (Attribute chunkList : attrs) {
         return chunkList.getValue(Integer.class);
      }
      return 0;
   }

   /**
    * Constructor
    * 
    * @param filePath
    * @param sizeInBytes
    * @throws FileNotFoundException
    */
   public ChunkedBO(String filePath) throws FileNotFoundException {
      if (filePath == null) {
         throw new FileNotFoundException("Given file does not exist");
      }

      chunkList = new ArrayList<Chunk>();

      // generate Chunks
      File file = new File(filePath);
      DataInputStream readStream = null;
      if (file.exists()) {
         try {
            readStream = new DataInputStream(new FileInputStream(file));
            String hash = null;
            int chunkCount = 0;
            byte[] tempBuf;
            long fileSize = file.length();
            ByteArrayOutputStream outStream = null;

            for (chunkCount = 0; chunkCount < fileSize / chunkSizeInBytes; chunkCount++) {
               outStream = new ByteArrayOutputStream(chunkSizeInBytes);

               for (int byteCount = 0; byteCount < chunkSizeInBytes; byteCount++) {
                  outStream.write(readStream.read());
               }

               tempBuf = outStream.toByteArray();
               hash = Utils.hexStringFromBytes(Hashing.hashSHA1(new ByteArrayInputStream(tempBuf)));
               chunkList.add(new Chunk(hash, chunkCount));
               // close the file
               outStream.close();
            }

            // loop for the last chunk (which may be smaller than the chunk size)
            if (fileSize != chunkSizeInBytes * (chunkCount - 1)) {
               // open the output file
               outStream = new ByteArrayOutputStream(chunkSizeInBytes);

               // write the rest of the file
               int b;
               while ((b = readStream.read()) != -1) {
                  outStream.write(b);
               }

               tempBuf = outStream.toByteArray();
               hash = Utils.hexStringFromBytes(Hashing.hashSHA1(new ByteArrayInputStream(tempBuf)));
               chunkList.add(new Chunk(hash, chunkCount));

               // close the file
               outStream.close();
            }

            // set total number
            totalNoOfChunks = chunkList.size();

         } catch (IOException e) {
            LOG.warn("(ChunkedBO ) error while creating chunks: " + e.getMessage());
         } finally {
            IOUtils.closeQuietly(readStream);
         }
      } else {
         throw new FileNotFoundException("(ChunkedBO ) File not found: " + filePath);
      }
   }

   public List<Chunk> getChunks() {
      return chunkList;
   }

   /**
    * Provides the size of a single chunk
    * 
    * @return Size as integer.
    */
   public int getChunkSize() {
      return chunkSizeInBytes;
   }

   /**
    * Gets the total number of chunks.
    * 
    * @return The total number as integer.
    */
   public int getTotalNoOfChunks() {
      return totalNoOfChunks;
   }

   /**
    * Provides a list of base urls.
    * 
    * @return The list.
    */
   public List<String> getBaseUrls() {
      return baseUrls;
   }

}