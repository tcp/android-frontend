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
package netinf.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.security.Hashing;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Utility methods that are used by more than a single package
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class Utils {
   private static final Logger LOG = Logger.getLogger(Utils.class);

   /**
    * Unserializes a Java Object. If possible, then use the appropriate DatamodelFactory method, like e.g.
    * DatamodelFactory#createInformationObjectFromBytes.
    */
   public static Object unserializeJavaObject(byte[] bytes) {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

      try {
         ObjectInputStream stream = new ObjectInputStream(byteArrayInputStream);
         return stream.readObject();
      } catch (IOException e) {
         LOG.error(e.getMessage());
         throw new NetInfUncheckedException(e);
      } catch (ClassNotFoundException e) {
         LOG.error(e.getMessage());
         throw new NetInfUncheckedException(e);
      }
   }

   public static String bytesToString(byte[] bytes) {
      StringBuilder string = new StringBuilder();
      for (byte b : bytes) {
         string.append((char) b);
      }
      return string.toString();
      // return Base64.encodeBase64String(bytes);

   }

   public static byte[] stringToBytes(String string) {
      char[] chars = string.toCharArray();
      byte[] bytes = new byte[chars.length];
      for (int i = 0; i < chars.length; i++) {
         bytes[i] = (byte) chars[i];
      }
      return bytes;
      // return Base64.decodeBase64(string);
   }

   public static Properties loadProperties(String pathToProperties) {
      Properties result = new Properties();

      FileInputStream stream = null;

      try {
         stream = new FileInputStream(pathToProperties);
         result.load(stream);

      } catch (FileNotFoundException e) {
         throw new NetInfUncheckedException(e);
      } catch (IOException e) {
         throw new NetInfUncheckedException(e);
      } finally {
         try {
            if (stream != null) {
               stream.close();
            }
         } catch (IOException e) {
            IOUtils.closeQuietly(stream);
         }
      }

      return result;
   }

   public static PublicKey stringToPublicKey(String publicKey) {
      return (PublicKey) stringToObject(publicKey);
   }

   public static PrivateKey stringToPrivateKey(String privateKey) {
      return (PrivateKey) stringToObject(privateKey);
   }

   public static Object stringToObject(String object) {
      try {
         ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decodeBase64(object));
         ObjectInputStream serializedObject = new ObjectInputStream(bais);
         return serializedObject.readObject();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      }
      return null;
   }

   public static String objectToString(Object object) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos;
      try {
         oos = new ObjectOutputStream(baos);
         oos.writeObject(object);
      } catch (IOException e) {
         return null;
      }
      return Base64.encodeBase64String(baos.toByteArray());
      // return Utils.bytesToString(baos.toByteArray());
   }

   public static String hexStringFromBytes(byte[] input) {
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < input.length; i++) {
         String hex = Integer.toHexString(0xFF & input[i]);

         if (hex.length() == 1) {
            hexString.append('0');
         }

         hexString.append(hex);
      }

      return hexString.toString();
   }

   /**
    * Get system tmp folder with the given subfolder
    * 
    * @return path to netinf tmp folder
    */
   public static String getTmpFolder(String subfolder) {
      String pathToTmp = System.getProperty("java.io.tmpdir") + File.separator + subfolder;
      File folder = new File(pathToTmp);
      if (folder.exists() && folder.isDirectory()) {
         return pathToTmp;
      } else {
         folder.mkdir();
         return pathToTmp;
      }
   }

   /**
    * Provides the ByteArry for a given file path.
    * 
    * @param filePath
    *           The Path to the file.
    * @return The ByteArray.
    * @throws IOException
    */
   public static byte[] getByteArray(String filePath) throws IOException {
      FileInputStream fis = new FileInputStream(filePath);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      IOUtils.copy(fis, bos);
      fis.close();
      bos.close();

      return bos.toByteArray();
   }

   public static boolean isValidHash(String hashOfBO, String filePath) {
      try {
         File file = new File(filePath);
         if (file.exists()) {
            DataInputStream fis = new DataInputStream(new FileInputStream(filePath));
            byte[] hashBytes = Hashing.hashSHA1(fis);
            IOUtils.closeQuietly(fis);
            if (hashOfBO.equalsIgnoreCase(Utils.hexStringFromBytes(hashBytes))) {
               LOG.info("(Utils ) Hash is valid: " + hashOfBO);
               return true;
            }
         }
      } catch (IOException e) {
         LOG.warn("(Utils ) Error while checking integrity: " + e.getMessage());
      }
      LOG.info("(Utils ) Hash is NOT valid: " + hashOfBO);
      return false;
   }

   public static boolean saveTemp(InputStream inStream, String destination) {
      File file = new File(destination);
      if (file.exists()) {
         return true;
      } else { // file does not exist
         OutputStream out = null;
         try {
            file.createNewFile();
            out = new FileOutputStream(file);
            IOUtils.copy(inStream, out);
            return true;
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(out);
         }
      }
      return false;
   }

}
