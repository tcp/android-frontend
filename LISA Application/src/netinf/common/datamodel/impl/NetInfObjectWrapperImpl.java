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
package netinf.common.datamodel.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.NetInfObjectWrapper;

import org.apache.log4j.Logger;

/**
 * The superclass of all impl-implementations of the datamodel classes. Instances of this class are not allowed to be created.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class NetInfObjectWrapperImpl implements Serializable, NetInfObjectWrapper {

   private static final Logger LOG = Logger.getLogger(NetInfObjectWrapperImpl.class);

   private static final long serialVersionUID = -5313272919865756604L;

   // Should not be serialized. Is accordingly missing after the serialization.
   protected transient DatamodelFactoryImpl datamodelFactory;

   public NetInfObjectWrapperImpl(DatamodelFactoryImpl datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
   }

   /**
    * In this case, we do not wrap any object. We simply return ourselves.
    * 
    * @see netinf.common.datamodel.NetInfObjectWrapper#getWrappedObject()
    */
   @Override
   public Object getWrappedObject() {
      return this;
   }

   @Override
   public byte[] serializeToBytes() {
      LOG.trace(null);

      byte[] result = null;

      ByteArrayOutputStream byteArrayOutputStream = null;
      ObjectOutputStream oos = null;
      try {

         byteArrayOutputStream = new ByteArrayOutputStream();
         oos = new ObjectOutputStream(byteArrayOutputStream);
         oos.writeObject(this);

         result = byteArrayOutputStream.toByteArray();
         LOG.trace("Succesfully serialized information object");

      } catch (IOException e) {
         LOG.error("Could not serialize information object", e);
      } finally {
         if (oos != null) {
            try {
               oos.close();
            } catch (IOException e) {
               LOG.error("Could not close output stream", e);
            }
         }

         if (byteArrayOutputStream != null) {
            try {
               byteArrayOutputStream.close();
            } catch (IOException e) {
               LOG.error("Could not close output stream", e);
            }
         }
      }

      return result;
   }

   @Override
   public Object clone() {
      return getDatamodelFactory().copyObject(this);
   }

   @Override
   public DatamodelFactory getDatamodelFactory() {
      if (datamodelFactory == null) {
         datamodelFactory = new DatamodelFactoryImpl();
      }
      return datamodelFactory;
   }

   /**
    * Internal Method of the impl implementation of the datamodel.
    */
   public void setDatamodelFactory(DatamodelFactoryImpl newDatamodelFactory) {
      datamodelFactory = newDatamodelFactory;
   }
}
