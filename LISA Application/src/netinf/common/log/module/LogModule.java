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
package netinf.common.log.module;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import netinf.common.exceptions.NetInfUncheckedException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import com.google.inject.AbstractModule;

/**
 * The <code>Logger</code> is intended to initialize the logger of log4j.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class LogModule extends AbstractModule {

   private static final String CONFIGS_FOLDER = "../configs/";
   private static final Logger LOG = Logger.getLogger(LogModule.class);
   private final Properties properties;
   private String xmlFile;

   public LogModule(Properties properties) {
      this.properties = properties;
      this.xmlFile = null;
   }

   public LogModule(String xmlFile) throws FileNotFoundException {
      if (xmlFile == null || !(new File(CONFIGS_FOLDER + xmlFile).exists())) {
         throw new FileNotFoundException("couldn't load XML config: " + xmlFile);
      }
      this.properties = null;
      this.xmlFile = CONFIGS_FOLDER + xmlFile;
   }

   @Override
   protected void configure() {
      // Determine if xml-file is given
      if (properties != null && xmlFile == null) {
         if (properties.containsKey("logging.xmlfile")) {
            System.out.println("Trying to initialize logging from XML");
            this.xmlFile = CONFIGS_FOLDER + properties.getProperty("logging.xmlfile");

            if (!(new File(CONFIGS_FOLDER + xmlFile).exists())) {
               throw new NetInfUncheckedException("couldn't load XML config: " + xmlFile);
            }
         }
      }

      // Initialize the file, the higher prioirty is the xml-file
      if (this.xmlFile != null) {
         DOMConfigurator.configure(this.xmlFile);
         LOG.info("Successfully initialized the log4j logger from XML");
      } else if (this.properties != null) {
         PropertyConfigurator.configure(this.properties);
         LOG.info("Successfully initialized the log4j logger from properties");
      }
   }
}
