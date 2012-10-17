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

import java.security.PublicKey;
import java.util.Hashtable;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedSecurityException;

/**
 * The Interface Cryptography is used to encrypt and decrypt whole InformationObjects or a Tree of Properties and its
 * Subattributes. All the information about keys and readers can either be given or will be retrieved via
 * ConvenienceCommunicatior.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface Cryptography {

   /**
    * Encrypts the given InformationOject with the default algorithm. The InformationObject is encrypted at whole and the
    * resulting data is attached to a newly generated InformationObject with the same Identifier. The SecretKey to decrypt the
    * InformationObject will be added for each Key found in the readers list of the InformationObject.
    * 
    * @param informationObject
    *           the information object to be encrypted
    * @return the encrypted information object
    */
   InformationObject encrypt(InformationObject informationObject) throws NetInfCheckedSecurityException;

   /**
    * Encrypts the given InformationOject with the default algorithm. The InformationObject is encrypted at whole and the
    * resulting data is attached to a newly generated InformationObject with the same Identifier. The SecretKey to decrypt the
    * InformationObject will be added for each Key of the given list of readers.
    * 
    * @param informationObject
    *           the information object to be encrypted
    * @param readers
    *           the readers that are able to decrypt the information object
    * @return the encrypted information object
    */
   InformationObject encrypt(InformationObject informationObject, Hashtable<String, PublicKey> readerKeys)
   throws NetInfCheckedSecurityException;

   /**
    * Encrypts the given InformationObject with the given algorithms. The InformationObject is encrypted at whole and the
    * resulting data is attached to a newly generated InformationObject with the same Identifier. The SecretKey to decrypt the
    * InformationObject will be added for each Key found in the readers list of the InformationObject.
    * 
    * @param informationObject
    *           the information object to be encrypted
    * @param contentAlgorithm
    *           the algorithm used to encrypt the information object
    * @param keyAlgorithm
    *           the algorithm used to encrypt the reader key
    * @return the encrypted information object
    */
   InformationObject encrypt(InformationObject informationObject, String contentAlgorithm, String keyAlgorithm)
   throws NetInfCheckedSecurityException;

   /**
    * Encrypts the given InformationObject with the given algorithms. The InformationObject is encrypted at whole and the
    * resulting data is attached to a newly generated InformationObject with the same Identifier. The SecretKey to decrypt the
    * InformationObject will be added for each Key of the given list of readers.
    * 
    * @param informationObject
    *           the information object to be encrypted
    * @param readers
    *           the readers that are able to decrypt the information object
    * @param contentAlgorithm
    *           the algorithm used to encrypt the information object
    * @param keyAlgorithm
    *           the algorithm used to encrypt the reader key
    * @return the encrypted information object
    */
   InformationObject encrypt(InformationObject informationObject, Hashtable<String, PublicKey> readerKeys,
         String contentAlgorithm, String keyAlgorithm) throws NetInfCheckedSecurityException;

   /**
    * Encrypts the given attribute with the default algorithm. The Attribute is encrypted at whole and the resulting data is
    * attached to a newly generated Attribute with the ECRYPTED_CONTENT identifier. The SecretKey to decrypt the Attribute will be
    * added for each Key found in the readers list of the Attribute.
    * 
    * @param attribute
    *           the attribute to be encrypted
    * @return the encrypted attribute
    */
   Attribute encrypt(Attribute attribute) throws NetInfCheckedSecurityException;

   /**
    * Encrypts the given attribute with the default algorithm. The Attribute is encrypted at whole and the resulting data is
    * attached to a newly generated attribute with the ECRYPTED_CONTENT identifier. The SecretKey to decrypt the Attribute will be
    * added for each Key of the given list of readers.
    * 
    * @param attribute
    *           the attribute to be encrypted
    * @param readers
    *           the readers that are able to decrypt the attribute
    * @return the encrypted attribute
    */
   Attribute encrypt(Attribute attribute, Hashtable<String, PublicKey> readerKeys) throws NetInfCheckedSecurityException;

   /**
    * Encrypts the given Attribute with the given algorithms. The Attribute is encrypted at whole and the resulting data is
    * attached to a newly generated Attribute with the ECRYPTED_CONTENT identifier. The SecretKey to decrypt the Attribute will be
    * added for each Key found in the readers list of the Attribute.
    * 
    * @param attribute
    *           the attribute to be encrypted
    * @param algorithm
    *           the algorithm used to encrypt the attribute
    * @param keyAlgorithm
    *           the algorithm used to encrypt the reader key
    * @return the encrypted attribute
    */
   Attribute encrypt(Attribute attribute, String contentAlgorithm, String keyAlgorithm) throws NetInfCheckedSecurityException;

   /**
    * Encrypts the given Attribute with the given algorithms. The Attribute is encrypted at whole and the resulting data is
    * attached to a newly generated Attribute with the ECRYPTED_CONTENT identifier. The SecretKey to decrypt the Attribute will be
    * added for each Key of the given list of readers.
    * 
    * @param attribute
    *           the attribute to be encrypted
    * @param readers
    *           the readers that are able to decrypt the attribute
    * @param algorithm
    *           the algorithm used to encrypt the attribute
    * @param keyAlgorithm
    *           the algorithm used to encrypt the reader key
    * @return the encrypted attribute
    */
   Attribute encrypt(Attribute attribute, Hashtable<String, PublicKey> readerKeys, String contentAlgorithm, String keyAlgorithm)
   throws NetInfCheckedSecurityException;

   /**
    * Decrypts the given InformationObject. It has to contain an ENCRYPTED_CONTENT Attribute, which contains a list of encrypted
    * SecretKeys. If one of the keys is encrypted for one of the known PrivateKeys, the content may be decrypted. If the content
    * contains an InformationObject, it is return.
    * 
    * @param informationObject
    *           the information object to be decrypted
    * @return the decrypted information object
    */
   InformationObject decrypt(InformationObject informationObject) throws NetInfCheckedSecurityException;

   /**
    * Decrypts the given InformationObject. It has to contain an ENCRYPTED_CONTENT Attribute, which contains a list of encrypted
    * SecretKeys. If one of the keys is encrypted for one of the known PrivateKeys, the content may be decrypted. If the content
    * contains an InformationObject, it is return.
    * 
    * @param informationObject
    *           the information object to be decrypted
    * @param userName
    *           username that is used to load the private keys required for decryption
    * @param userName
    *           private key that is used to load the private keys required for decryption
    * @return the decrypted information object
    */
   InformationObject decrypt(InformationObject informationObject, String userName, String privateKey)
   throws NetInfCheckedSecurityException;

   /**
    * Decrypts the given Attribute. It has to contain or be an ENCRYPTED_CONTENT Attribute, which contains a list of encrypted
    * SecretKeys. If one of the keys is encrypted for one of the known PrivateKeys, the content may be decrypted. If the content
    * contains an Attribute, it is return.
    * 
    * @param attribute
    *           the attribute to be decrypted
    * @return the decrypted attribute
    */
   Attribute decrypt(Attribute attribute) throws NetInfCheckedSecurityException;

   /**
    * Decrypts the given Attribute. It has to contain or be an ENCRYPTED_CONTENT Attribute, which contains a list of encrypted
    * SecretKeys. If one of the keys is encrypted for one of the known PrivateKeys, the content may be decrypted. If the content
    * contains an Attribute, it is return.
    * 
    * @param attribute
    *           the attribute to be decrypted
    * @param userName
    *           username that is used to load the private keys required for decryption
    * @param userName
    *           private key that is used to load the private keys required for decryption
    * @return the decrypted attribute
    */
   Attribute decrypt(Attribute attribute, String userName, String privateKey) throws NetInfCheckedSecurityException;

}
