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

/**
 * Interface for a CachingServer.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public interface BOCacheServer {

   /**
    * Caches a given BO in the Server
    * 
    * @param bo
    *           the bitlevel-object
    * @param hashOfBO
    *           the hash-value of the given BO
    * @return true if the operation was successfully executed, otherwise false
    */
   boolean cacheBO(byte[] bo, String hashOfBO);

   /**
    * Get the URL of the cached BO
    * 
    * @param hashOfBO
    *           hash-value of the given BO
    * @return URL to the cached BO, in case of failure null
    */
   String getURL(String hashOfBO);

   /**
    * Checks if the cache contains a specific BO
    * 
    * @param hashOfBO
    *           the hash-value of the bitlevel-object
    * @return true if BO exists, otherwise false
    */
   boolean containsBO(String hashOfBO);

   /**
    * checks if the adapter is successfully connected to the cache server
    * 
    * @return true if cache is connected, otherwise false
    */
   boolean isConnected();

   /**
    * Provides the address of the cache server
    * 
    * @return the address (url)
    */
   String getAddress();

   /**
    * Provides the given scope of this CachinServer.
    * 
    * @return The scope as integer.
    */
   int getScope();
}
