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

import java.security.NoSuchAlgorithmException;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;

/**
 * The IdentityVerification Class is used for security checks especially regarding the verification of owner and writers of IOs
 * and their attributes. It furthermore provides a method to check if all attributes of an IO that have to be signed by the owner
 * are actually signed by the owner. Additionally, the IdentityVerification Class shall be used for proving, whether an identity
 * is trusted. The IdentityVerification Class is mainly used by the SecurityManager, integrated in security processing of IOs that
 * are received or shall be put (store operation) to a remote Resolution Service
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface IdentityVerification {

   /**
    * Check if Owner is verified
    * 
    * @param io
    *           The information object to check for owner verification.
    * @return Result, whether owner is verified
    * @throws NetInfCheckedException
    *            If related IdentityObjects can't be retrieved, NetInfCheckedException is thrown
    * @throws NoSuchAlgorithmException
    */
   IdentityVerificationResult isOwnerVerified(InformationObject io) throws NetInfCheckedException, NoSuchAlgorithmException;

   /**
    * Check if Writer is verified
    * 
    * @param io
    *           The information object to check for writer verification.
    * @return Result, whether writer is verified
    * @throws NetInfCheckedException
    *            If related IdentityObjects can't be retrieved, NetInfCheckedException is thrown
    */
   IdentityVerificationResult isWriterVerified(InformationObject io) throws NetInfCheckedException;

   /**
    * Check if Writer is verified
    * 
    * @param property
    *           The property to check for writer verification.
    * @return Result, whether writer is verified
    * @throws NetInfCheckedException
    *            If related IdentityObjects can't be retrieved, NetInfCheckedException is thrown
    */
   IdentityVerificationResult isWriterVerified(Attribute property) throws NetInfCheckedException;

   /**
    * Check if the information object is verified by the owner where necessary
    * 
    * @param io
    *           The information object to check for validation.
    * @return Result, whether the information object is verified by the owner or not
    */
   IdentityVerificationResult isIOVerifiedByOwner(InformationObject io);

   /**
    * Check if an identity is trusted. The implementation of this method is not yet completed.
    * 
    * @param ido
    *           The identity object to check for trust.
    * @return Result, whether identity is trusted
    * @throws NetInfCheckedException
    *            If related IdentityObjects can't be retrieved, NetInfCheckedException is thrown
    */
   IdentityVerificationResult isIdentityTrusted(IdentityObject ido) throws NetInfCheckedException;

}
