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
package netinf.node.resolution;

import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.messages.NetInfMessage;

/**
 * The front-end to ResolutionService instances
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface ResolutionController {
   /**
    * Returns the list of supported requests
    * 
    * @return the list of supported requests
    */
   List<Class<? extends NetInfMessage>> getSupportedOperations();

   NetInfMessage processNetInfMessage(NetInfMessage netInfMessage);

   /**
    * Returns a copy of the list of resolution services managed by the resolution controller.
    * 
    * @return the managed ResolutionService instances
    */
   List<ResolutionServiceIdentityObject> getResolutionServices();

   /**
    * Add an resolution service to this resolution controller. Adding a resolution service already managed by the resolution
    * controller will have no effect.
    * 
    * @param resolutionService
    */
   void addResolutionService(ResolutionService resolutionService);

   void removeResolutionService(ResolutionService resolutionService);

   void removeResolutionService(ResolutionServiceIdentityObject resolutionServiceInformation);

   // Same methods as ResolutionService but with an additional parameter that
   // specifies the preferred ResolutionService. Allows overriding the default
   // preferences of the ResolutionController.
   InformationObject get(Identifier identifier);

   InformationObject get(Identifier identifier, String userName, String privateKey);

   InformationObject get(Identifier identifier, List<ResolutionServiceIdentityObject> resolutionServicesToUse);

   InformationObject get(Identifier identifier, List<ResolutionServiceIdentityObject> resolutionServicesToUse, String userName,
         String privateKey);

   List<InformationObject> getAllVersions(Identifier identifier);

   List<InformationObject> getAllVersions(Identifier identifier, List<ResolutionServiceIdentityObject> resolutionServicesToUse);

   /**
    * Determines suitable {@link ResolutionService} and calls {@link ResolutionController#put(InformationObject, List)}.
    * 
    * @param informationObject
    */
   void put(InformationObject informationObject);

   void put(InformationObject informationObject, String userName, String privateKey);

   void put(InformationObject informationObject, List<ResolutionServiceIdentityObject> resolutionServicesToUse);

   void put(InformationObject informationObject, List<ResolutionServiceIdentityObject> resolutionServicesToUse, String userName,
         String privateKey);

   void delete(Identifier identifier);

   void delete(Identifier identifier, List<ResolutionServiceIdentityObject> resolutionServicesToUse);

   void initReslolutionInterceptors(ResolutionInterceptor[] resolutionInterceptors);

   void addResolutionInterceptor(ResolutionInterceptor interceptor);

   void removeResolutionInterceptor(ResolutionInterceptor interceptor);
}
