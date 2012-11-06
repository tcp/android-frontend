/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
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
package project.cs.lisa.bluetooth.provider;

/**
 * Interface for all ByteArrayProviders (Bluetooth and WiFi).
 * 
 * @author Miguel Sosa
 * @author Hugo Negrette
 */
public interface ByteArrayProvider {

   /**
    * Given a locator and a file hash, this method provides the byte array corresponding
    * to the hash.
    * 
    * @param locator
    *           The locator from where the file should be fetched
    * @param hash
    *           The hash of the file that will be fetched
    * @return The byte array corresponding to the hash of the file obtained from the address
    *         specified in the locator.
    */
   byte[] getByteArray(String locator, String hash);

   /**
    * Decides whether this locator can be handled or not.
    * 
    * @param locator
    *           The URL of the file.
    * @return True if the Provider can handle this URL, otherwise false.
    */
   boolean canHandle(String locator);

   /**
    * Describes the Stream Provider.
    * 
    * @return The name of the Stream Provider.
    */
   String describe();
}
