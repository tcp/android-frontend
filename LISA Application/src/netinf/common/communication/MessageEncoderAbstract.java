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
package netinf.common.communication;

import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.common.datamodel.impl.NetInfObjectWrapperImpl;
import netinf.common.datamodel.rdf.DatamodelFactoryRdf;
import netinf.common.datamodel.rdf.NetInfObjectWrapperRdf;
import netinf.common.datamodel.translation.DatamodelTranslator;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Common methods used by all MessageEncoder instances
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class MessageEncoderAbstract implements MessageEncoder {
   private static final Logger LOG = Logger.getLogger(MessageEncoderAbstract.class);

   private DatamodelFactoryRdf datamodelFactoryRdf;
   private DatamodelFactoryImpl datamodelFactoryImpl;
   private DatamodelTranslator datamodelTranslator;

   @Inject
   public void injectDatamodelTranslator(DatamodelTranslator datamodelTranslator) {
      this.datamodelTranslator = datamodelTranslator;
   }

   @Inject
   public void injectDatamodelFactories(DatamodelFactoryRdf datamodelFactoryRdf, DatamodelFactoryImpl datamodelFactoryImpl) {
      this.datamodelFactoryRdf = datamodelFactoryRdf;
      this.datamodelFactoryImpl = datamodelFactoryImpl;
   }

   protected byte[] serializeNetInfObjectToBytes(NetInfObjectWrapper object, SerializeFormat serializeFormat) {
      // Required Java, but is RDF
      if (serializeFormat.equals(SerializeFormat.JAVA) && object instanceof NetInfObjectWrapperRdf) {
         LOG.debug("Converting object from Java to Rdf, preparing to send");

         return this.datamodelTranslator.toImpl(object).serializeToBytes();
      } else

         // Required RDF, but is Java
         if (serializeFormat.equals(SerializeFormat.RDF) && object instanceof NetInfObjectWrapperImpl) {
            LOG.debug("Converting object from Rdf to Java, preparing to send");

            return this.datamodelTranslator.toRdf(object).serializeToBytes();
         } else {
            // Format is correct, no conversion
            return object.serializeToBytes();
         }
   }

   protected NetInfObjectWrapper unserializeNetInfObjectFromBytes(byte[] bytes, SerializeFormat serializeFormat) {
      if (serializeFormat.equals(SerializeFormat.JAVA)) {
         return this.datamodelFactoryImpl.createFromBytes(bytes);
      } else {
         return this.datamodelFactoryRdf.createFromBytes(bytes);
      }
   }
}
