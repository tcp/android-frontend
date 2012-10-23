package project.cs.lisa.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LisaHash {
    /**
     * LISA uses this hash code from Anders. Read below for more info. We changed the name of the
     * class from HashingSHA256_EncodingBase64URL to LisaHash because its easier to our
     * eyes.

    // Creates a sha-256 hash of an input file, and makes a base64url encoding of the hash. 
    // Finally, a NetInf name prefix is added. 
    // The base64 code below is borrowed from Christian d'Heureuse, see the license terms below.
    // I have modified this code to base64url
    // Anders E

    // Copyright 2003-2010 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
    // www.source-code.biz, www.inventec.ch/chdh
    //
    // This module is multi-licensed and may be used under the terms
    // of any of the following licenses:
    //
    //  EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
    //  LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
    //  GPL, GNU General Public License, V2 or later, http://www.gnu.org/licenses/gpl.html
    //  AL, Apache License, V2.0 or later, http://www.apache.org/licenses
    //  BSD, BSD License, http://www.opensource.org/licenses/bsd-license.php
    //  MIT, MIT License, http://www.opensource.org/licenses/MIT
    //
    // Please contact the author if you need another license.
    // This module is provided "as is", without warranties of any kind.
     *
     **/

    /**
     * A Base64 encoder/decoder.
     * 
     * <p>
     * This class is used to encode and decode data in Base64 format as described in
     * RFC 1521.
     * 
     * <p>
     * Project home page: <a
     * href="http://www.source-code.biz/base64coder/java/">www.
     * source-code.biz/base64coder/java</a><br>
     * Author: Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland<br>
     * Multi-licensed: EPL / LGPL / GPL / AL / BSD / MIT.
     */


    // The line separator string of the operating system.
    private static final String systemLineSeparator = System
            .getProperty("line.separator"); // CR + LF (ASCII 13, 10)

    String base64String;
    byte[] mByteArray;
    
    // TODO: Remove this after the sprint 2
    // Our flag for returning max 'LisaFlag' chars of the hashing.
    // 0 and the flag is not set, returns normal result.
    int mLisaFlag = 0;

    // Mapping table from 6-bit nibbles to Base64 characters.
    private static final char[] map1 = new char[64];
    
    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++)
            map1[i++] = c;
        for (char c = 'a'; c <= 'z'; c++)
            map1[i++] = c;
        for (char c = '0'; c <= '9'; c++)
            map1[i++] = c;
        // map1[i++] = '+'; //base64
        map1[i++] = '-'; // base64url
        // map1[i++] = '/'; //base64
        map1[i++] = '_'; // base64url
    }

    public LisaHash (byte[] byteArray){          
        mByteArray = byteArray;
    }

    /**
     * Encodes a byte array into Base 64 format and breaks the output into lines
     * of 76 characters. This method is compatible with
     * <code>sun.misc.BASE64Encoder.encodeBuffer(byte[])</code>.
     * 
     * @param in
     *            An array containing the data bytes to be encoded.
     * @return A String containing the Base64 encoded data, broken into lines.
     */
    
    public static String encodeLines(byte[] in) {
        return encodeLines(in, 0, in.length, 76, systemLineSeparator);
    }


    /**
     * Encodes a byte array into Base 64 format and breaks the output into
     * lines.
     * 
     * @param in
     *            An array containing the data bytes to be encoded.
     * @param iOff
     *            Offset of the first byte in <code>in</code> to be processed.
     * @param iLen
     *            Number of bytes to be processed in <code>in</code>, starting
     *            at <code>iOff</code>.
     * @param lineLen
     *            Line length for the output data. Should be a multiple of 4.
     * @param lineSeparator
     *            The line separator to be used to separate the output lines.
     * @return A String containing the Base64 encoded data, broken into lines.
     */
   
    public static String encodeLines(byte[] in, int iOff, int iLen,
            int lineLen, String lineSeparator) {
        int blockLen = (lineLen * 3) / 4;
        if (blockLen <= 0)
            throw new IllegalArgumentException();
        int lines = (iLen + blockLen - 1) / blockLen;
        int bufLen = ((iLen + 2) / 3) * 4 + lines * lineSeparator.length();
        StringBuilder buf = new StringBuilder(bufLen);
        int ip = 0;
        while (ip < iLen) {
            int l = Math.min(iLen - ip, blockLen);
            buf.append(encode(in, iOff + ip, l));
            buf.append(lineSeparator);
            ip += l;
        }
        return buf.toString();
    }

    /**
     * Encodes a byte array into Base64 format. No blanks or line breaks are
     * inserted in the output.
     * 
     * @param in
     *            An array containing the data bytes to be encoded.
     * @param iOff
     *            Offset of the first byte in <code>in</code> to be processed.
     * @param iLen
     *            Number of bytes to process in <code>in</code>, starting at
     *            <code>iOff</code>.
     * @return A character array containing the Base64 encoded data.
     */
    
    public static char[] encode(byte[] in, int iOff, int iLen) {
        int oDataLen = (iLen * 4 + 2) / 3; // output length without padding
        int oLen = ((iLen + 2) / 3) * 4; // output length including padding
        char[] out = new char[oLen];
        int ip = iOff;
        int iEnd = iOff + iLen;
        int op = 0;
        while (ip < iEnd) {
            int i0 = in[ip++] & 0xff;
            int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
            int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
            int o0 = i0 >>> 2;
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            int o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '=';
            op++;
            out[op] = op < oDataLen ? map1[o3] : '=';
            op++;
        }
        return out;
    }

    public String encodeResult(){
        String output = null;
        byte[] hash = null;

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // "MD5");
        digest.update(mByteArray);
        hash = digest.digest();
        BigInteger bigInt = new BigInteger(1, hash);

        // convert bigInt to hex format
        output = bigInt.toString(16);

        System.out.println("SHA-256 Hex format = " + output);

        base64String = encodeLines(hash);
        // This string ends with "=" + CR+LF (lineseparator, see encodeLines()).
        // These are removed in the next step below.

        // endCharacters(base64String, 5);

        //              String test = base64String.substring(base64String.length() - 2);
        //              String test2= "=" + systemLineSeparator;

        if (base64String.substring(base64String.length() - 2).equals(
                "=" + systemLineSeparator))
            base64String = base64String.substring(0, base64String.length() - 2);

        else
            System.out.println("This code needs debugging!");

        byte[] base64Bytes = base64String.getBytes();

        int numberOfBytes = base64Bytes.length; // length seems to be an
        // attribute rather than a
        // method

        System.out.println("SHA-256 base64url byte length: " + numberOfBytes);

        // TODO: Remove this after sprint 2
        if (mLisaFlag > 0)
            return base64String.substring(0, mLisaFlag);
        
        return base64String;
    }
    
    // TODO: Remove this after sprint 2
    public void hashLisa(int maxChars) {
        mLisaFlag = maxChars;
    }
}