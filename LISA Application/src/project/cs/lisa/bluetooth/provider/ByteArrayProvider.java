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
