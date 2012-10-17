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
/**
 * 
 */
package netinf.common.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

/**
 * The Interface SignatureAlgorithm. {@link SignatureAlgorithm} provides methods to hash (
 * {@link SignatureAlgorithm#hash(String, String)}) and sign ( {@link SignatureAlgorithm#sign(String, PrivateKey, String)})
 * strings, and to check the validity of a certain signature (
 * {@link SignatureAlgorithm#verifySignature(String, String, PublicKey, String)}).
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public interface SignatureAlgorithm {

   /**
    * Hashes a String and returns the hash
    * 
    * @param originalString
    *           String to hash
    * @param hashFunction
    *           Hash function to use
    * @return originalString hashed with hashFunction
    * @throws NoSuchAlgorithmException
    */
   String hash(String originalString, String hashFunction) throws NoSuchAlgorithmException;

   /**
    * Hashes a String, signs it, and returns the signed hash
    * 
    * @param originalString
    *           String to hash and sign
    * @param sk
    *           Private Key to use for signing
    * @param hashAndSignatureFunction
    *           Name of hash and signature function
    * @return Returns originalString hashed and signed by key given as PrivateKey sk
    * @throws NoSuchAlgorithmException
    * @throws InvalidKeyException
    * @throws SignatureException
    */
   String sign(String originalString, PrivateKey sk, String hashAndSignatureFunction) throws NoSuchAlgorithmException,
   InvalidKeyException, SignatureException;

   /**
    * Verify the signature for a certain string
    * 
    * @param originalString
    *           Original string, for which a signature is given
    * @param signature
    *           Signature of the original string
    * @param pk
    *           PublicKey according to PrivateKey used for Signature calculation
    * @param hashAndSignatureFunction
    *           Method that has been used to calculate the signature ({@link DefinedSignatureIdentification})
    * @return True, if signature was built from originalString, using the Private Key that belongs to the Public Key pk and the
    *         Signature Function hashAndSignatureFunction.
    * @throws SignatureException
    * @throws NoSuchAlgorithmException
    * @throws InvalidKeyException
    */
   boolean verifySignature(String originalString, String signature, PublicKey pk, String hashAndSignatureFunction)
   throws SignatureException, NoSuchAlgorithmException, InvalidKeyException;

}
