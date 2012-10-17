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

import java.security.Key;
import java.security.PrivateKey;

import javax.crypto.SecretKey;

import netinf.common.exceptions.NetInfCheckedSecurityException;

/**
 * Interface for classes providing Cryptographic functionality such as encrypting and decrypting Strings as well as encrpyting and
 * decrypting PrivateKeys (asymmetric) using SecretKeys (symmetric).
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface CryptoAlgorithm {

   /**
    * Encrpyts content given as string with the given algorithm and key
    * 
    * @param algorithm
    *           the algorithm to be used to encrypt the content
    * @param key
    *           the key to be used to encrypt the content
    * @param unencrypted
    *           the content to be encrypted
    * @return
    * @throws NetInfCheckedSecurityException
    */
   String encrypt(String algorithm, Key key, String unencrypted) throws NetInfCheckedSecurityException;

   /**
    * Decrpyts content given as string with the given algorithm and key
    * 
    * @param algorithm
    *           the algorithm that was used to encrypt the content
    * @param key
    *           the key to be used to decrypt the content
    * @param encrypted
    *           the encrypted content
    * @return
    * @throws NetInfCheckedSecurityException
    */
   String decrypt(String algorithm, Key key, String encrypted) throws NetInfCheckedSecurityException;

   /**
    * Encrpyts the secret key given with the given algorithm and key
    * 
    * @param algorithm
    *           the algorithm to be used to encrypt the secret key
    * @param key
    *           the key to be used to encrypt the secret key
    * @param keyToEncrypt
    *           the secret key to be encrypted
    * @return
    * @throws NetInfCheckedSecurityException
    */
   String encryptSecretKey(String algorithmUsedToEncryptTheKey, Key key, SecretKey keyToEncrypt)
   throws NetInfCheckedSecurityException;

   /**
    * Decrpyts the secret key given with the given algorithm and key
    * 
    * @param algorithmUsedToEncryptTheKey
    *           the algorithm that was used to encrypt the secret key
    * @param algorithmKeyIsUsedFor
    *           the algorithm the secret key will be used for
    * @param key
    *           the key to be used to decrypt the secret key
    * @param keyToDecrypt
    *           the encrypted secret key
    * @return
    * @throws NetInfCheckedSecurityException
    */
   SecretKey decryptSecretKey(String algorithmUsedToEncryptTheKey, String algorithmKeyIsUsedFor, Key key, String keyToDecrypt)
   throws NetInfCheckedSecurityException;

   /**
    * Encrpyts the private key given with the given algorithm and key
    * 
    * @param algorithm
    *           the algorithm to be used to encrypt the private key
    * @param key
    *           the key to be used to encrypt the private key
    * @param keyToEncrypt
    *           the private key to be encrypted
    * @return
    * @throws NetInfCheckedSecurityException
    */
   String encryptPrivateKey(String algorithmUsedToEncryptTheKey, Key key, PrivateKey keyToEncrypt)
   throws NetInfCheckedSecurityException;

   /**
    * Decrpyts the private key given with the given algorithm and key
    * 
    * @param algorithmUsedToEncryptTheKey
    *           the algorithm that was used to encrypt the private key
    * @param algorithmKeyIsUsedFor
    *           the algorithm the private key will be used for
    * @param key
    *           the key to be used to decrypt the private key
    * @param keyToDecrypt
    *           the encrypted private key
    * @return
    * @throws NetInfCheckedSecurityException
    */
   PrivateKey decryptPrivateKey(String algorithmUsedToEncryptTheKey, String algorithmKeyIsUsedFor, Key key, String keyToDecrypt)
   throws NetInfCheckedSecurityException;

   /**
    * Generates a secret key to be used with the given algorithm for a password given as string
    * 
    * @param contentAlgorithmName
    *           the algorithm the secret key will be used for
    * @param password
    *           the password to be used as string
    * @return
    * @throws NetInfCheckedSecurityException
    */
   SecretKey getSecretKeyFromString(String contentAlgorithmName, String password) throws NetInfCheckedSecurityException;
}
