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
import netinf.common.exceptions.NetInfCheckedException;

/**
 * The Security Manager is used to do Security Processing of incomming IOs (IOs that are retrieved from somewhere, e.g. a remote
 * Resolution Service) and outgoing IOs (e.g., IOs that are sent to a remote Resolution Service in a put operation) Incomming:
 * IO/Attributes are decrypted, Signatures are checked for validity. Outgoing: IO/Attributes are encrypted as required, Signatures
 * are built.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface SecurityManager {

   /**
    * Checks an InformationObject directly after receiving it and before giving it to the requester. Verifies identities and
    * checks integrity. If the reader is trusted encrypted content is decrypted with help of loaded keys.
    * 
    * @param informationObject
    *           The InformationObject that has to be checked
    * @param userName
    *           username that is used to load the private keys used for security checks
    * @param userName
    *           private key that is used to load the private keys used for security checks
    * @return The InformationObject processed by all security units
    * @throws NetInfCheckedException
    *            Is thrown if identity verification or integrity check fails or required communication is impossible.
    * @throws NoSuchAlgorithmException
    */
   InformationObject checkIncommingInformationObject(InformationObject informationObject, String userName, String privateKey)
   throws NetInfCheckedException, NoSuchAlgorithmException;

   /**
    * Checks an InformationObject directly after receiving it and before giving it to the requester. Verifies identities and
    * checks integrity. If the reader is trusted encrypted content is decrypted with help of loaded keys.
    * 
    * @param informationObject
    *           The InformationObject that has to be checked
    * @param receiverIsTrusted
    *           Whether the receiver of the checked InformationObject is trusted
    * @return The InformationObject processed by all security units
    * @throws NetInfCheckedException
    *            Is thrown if identity verification or integrity check fails or required communication is impossible.
    * @throws NoSuchAlgorithmException
    */
   InformationObject checkIncommingInformationObject(InformationObject informationObject, boolean receiverIsTrusted)
   throws NetInfCheckedException, NoSuchAlgorithmException;

   /**
    * Checks an InformationObject directly after receiving it and before pushing it to a resolution service. Verifies identities
    * and checks integrity. If the reader is trusted the content is signed with help of loaded keys and encrypted.
    * 
    * @param informationObject
    *           The InformationObject that has to be checked
    * @param userName
    *           username that is used to load the private keys used for security checks
    * @param userName
    *           private key that is used to load the private keys used for security checks
    * @return The InformationObject processed by all security units
    * @throws NetInfCheckedException
    *            Is thrown if identity verification or integrity check fails or required communication is impossible.
    * @throws NoSuchAlgorithmException
    */
   InformationObject checkOutgoingInformationObject(InformationObject informationObject, String userName, String privateKey)
   throws NetInfCheckedException, NoSuchAlgorithmException;

   /**
    * Checks an InformationObject directly after receiving it and before pushing it to a resolution service. Verifies identities
    * and checks integrity. If the reader is trusted the content is signed with help of loaded keys and encrypted.
    * 
    * @param informationObject
    *           The InformationObject that has to be checked
    * @param senderIsTrusted
    *           Whether the sender of the checked InformationObject is trusted
    * @return The InformationObject processed by all security units
    * @throws NetInfCheckedException
    *            Is thrown if identity verification or integrity check fails or required communication is impossible.
    * @throws NoSuchAlgorithmException
    */
   InformationObject checkOutgoingInformationObject(InformationObject informationObject, boolean senderIsTrusted)
   throws NetInfCheckedException, NoSuchAlgorithmException;
}
