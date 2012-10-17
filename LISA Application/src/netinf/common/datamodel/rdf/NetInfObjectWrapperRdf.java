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
package netinf.common.datamodel.rdf;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.exceptions.NetInfCheckedException;

import com.google.inject.Inject;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This is a super class of all the individually serializeable rdf-datamodel objects. This class might only be used within the
 * rdf-implementation of the datamodel.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class NetInfObjectWrapperRdf implements NetInfObjectWrapper {

   private final DatamodelFactoryRdf datamodelFactoryRdf;
   private Resource resource;

   @Inject
   public NetInfObjectWrapperRdf(DatamodelFactoryRdf datamodelFactoryRdf) {
      this.datamodelFactoryRdf = datamodelFactoryRdf;
   }

   /**
    * If this method gets the value <code>null</code> the the resource is unset. Has to be overwritten.
    * 
    * @param resource
    * @throws NetInfCheckedException
    */
   public abstract void initFromResource(Resource resource) throws NetInfCheckedException;

   /**
    * Affects itself and all the attributes/subattributes. This givenResource must have a valid NetInfModel!
    * 
    * @param resource
    */
   public void bindToResource(Resource givenResource) {
      if (givenResource == null && this.getResource() != null) {
         // We are removed
         removeFromResource(getResource());
      } else if (givenResource != null && this.getResource() != null) {
         removeFromResource(getResource());
         addToResource(givenResource);
      } else if (givenResource != null && this.getResource() == null) {
         addToResource(givenResource);
      }
      // The last possibility does not change anything
   }

   /**
    * Has to be overwritten
    * 
    * @param givenResource
    */
   protected abstract void addToResource(Resource givenResource);

   /**
    * Has to be overwritten
    * 
    * @param resource
    */
   protected abstract void removeFromResource(Resource resource);

   @Override
   public Object clone() {
      return datamodelFactoryRdf.copyObject(this);
   }

   @Override
   public abstract byte[] serializeToBytes();

   @Override
   public Object getWrappedObject() {
      return getResource();
   }

   @Override
   public DatamodelFactory getDatamodelFactory() {
      return datamodelFactoryRdf;
   }

   public Resource getResource() {
      return resource;
   }

   protected void setResource(Resource resource) {
      this.resource = resource;
   }
}
