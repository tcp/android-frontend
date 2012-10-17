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
package netinf.common.security;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedException;

/**
 * The Integrity Class is used for security checks especially regarding the integrity of IOs and their Attributes. It provides
 * methods to create Attributes containing signatures of Attribute values, and check the correctness/validity of signatures when
 * IOs are received. The Integrity Class is mainly used by the SecurityManager, integrated in security processing of IOs that are
 * received or shall be put (store operation) to a remote Resolution Service
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface Integrity {

   /**
    * Sign an Information Object.
    * 
    * @param io
    *           The information object to be signed.
    * @return Result, whether signing succeeded or failed
    */
   IntegrityResult sign(InformationObject io);

   /**
    * Sign an Information Object.
    * 
    * @param io
    *           The information object to be signed.
    * @param userName
    *           username that is used to load the private keys required to sign the information object
    * @param userName
    *           private key that is used to load the private keys required to sign the information object
    * @return Result, whether signing succeeded or failed
    */
   IntegrityResult sign(InformationObject io, String userName, String privateKey);

   /**
    * Sign an Attribute.
    * 
    * @param attribute
    *           The attribute to be signed.
    * @return Result, whether signing succeeded or failed
    */
   IntegrityResult sign(Attribute attribute);

   /**
    * Sign an Attribute.
    * 
    * @param attribute
    *           The attribute to be signed.
    * @param userName
    *           username that is used to load the private keys required to sign the attribute
    * @param userName
    *           private key that is used to load the private keys required to sign the attribute
    * @return Result, whether signing succeeded or failed
    */
   IntegrityResult sign(Attribute attribute, String userName, String privateKey);

   /**
    * Check Signature validity of Information Object
    * 
    * @param io
    *           The information object to check for valid signature.
    * @return Result, whether signature is valid, invalid, or signature test was not able
    * @throws NetInfCheckedException
    *            If related IdentityObjects (to retrieve PublicKeys) can't be retrieved, NetInfCheckedException is thrown
    */
   IntegrityResult isSignatureValid(InformationObject io) throws NetInfCheckedException;

   /**
    * Check Signature validity of Attribute
    * 
    * @param property
    *           The property to check for valid signature.
    * @return Result, whether signature is valid, invalid, or signature test was not able
    * @throws NetInfCheckedException
    *            If related IdentityObjects (to retrieve PublicKeys) can't be retrieved, NetInfCheckedException is thrown
    */
   IntegrityResult isSignatureValid(Attribute property) throws NetInfCheckedException;

}
