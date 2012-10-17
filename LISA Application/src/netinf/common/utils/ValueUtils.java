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

import org.apache.log4j.Logger;

/**
 * The Class ValueUtils.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class ValueUtils {

   private static final Logger LOG = Logger.getLogger(ValueUtils.class);

   public static Object getObjectFromRaw(String type, String encoded) {
      Object returnObject = null;
      try {
         if (!type.contains(".")) {
            if (type.equals("Boolean")) {
               returnObject = Boolean.parseBoolean(encoded);
            }
            if (type.equals("Byte")) {
               returnObject = Byte.parseByte(encoded);
            }
            if (type.equals("Character")) {
               returnObject = new Character(encoded.charAt(0));
            }
            if (type.equals("Double")) {
               returnObject = Double.parseDouble(encoded);
            }
            if (type.equals("Float")) {
               returnObject = Float.parseFloat(encoded);
            }
            if (type.equals("Integer")) {
               returnObject = Integer.parseInt(encoded);
            }
            if (type.equals("Long")) {
               returnObject = Long.parseLong(encoded);
            }
            if (type.equals("Short")) {
               returnObject = Short.parseShort(encoded);
            }
            if (type.equals("String")) {
               returnObject = new String(encoded);
            }
         } else {

            returnObject = Class.forName(type).getConstructor(String.class).newInstance(encoded);
         }
      } catch (Exception e) {
         LOG.debug("Type " + type + " could not be instantiated. " + e.getMessage());
      }
      return returnObject;
   }

   public static Object getObjectFromRaw(String valueRaw) {
      if (valueRaw.contains(":")) {
         String[] splitted = splitRawValue(valueRaw);
         return getObjectFromRaw(splitted[0], splitted[1]);
      }
      return null;
   }

   public static String[] splitRawValue(String valueRaw) {
      if (valueRaw.contains(":")) {
         String[] returnValue = new String[2];
         returnValue[0] = valueRaw.substring(0, valueRaw.indexOf(':'));
         returnValue[1] = valueRaw.substring(valueRaw.indexOf(':') + 1);
         return returnValue;
      }
      return null;

   }
}
