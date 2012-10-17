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
package netinf.common.log.demo;

import org.apache.log4j.Level;

/**
 * The Class DemoLevel. {@link org.apache.log4j.Level} for demo logging
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class DemoLevel extends Level {

   /**
    * 
    */
   private static final long serialVersionUID = -6525003362036082489L;

   /**
    * WARN < DEMO < INFO
    */
   public static final int DEMO_INT = WARN_INT - 5000;

   public static final Level DEMO = new DemoLevel(DEMO_INT, "DEMO", 6);

   protected DemoLevel(int level, String levelStr, int syslogEquivalent) {
      super(level, levelStr, syslogEquivalent);

   }

   /**
    * Checks whether <code>sArg</code> is "MY_TRACE" level. If yes then returns {@link DemoLevel#MY_TRACE}, else calls
    * {@link DemoLevel#toLevel(String, Level)} passing it {@link Level#DEBUG} as the defaultLevel
    * 
    * @see Level#toLevel(java.lang.String)
    * @see Level#toLevel(java.lang.String, org.apache.log4j.Level)
    */
   public static Level toLevel(String sArg) {
      if (sArg != null && sArg.toUpperCase().equals("DEMO")) {
         return DEMO;
      }
      return toLevel(sArg, Level.DEBUG);
   }

   /**
    * Checks whether <code>val</code> is {@link DemoLevel#MY_TRACE_INT}. If yes then returns {@link DemoLevel#MY_TRACE}, else
    * calls {@link DemoLevel#toLevel(int, Level)} passing it {@link Level#DEBUG} as the defaultLevel
    * 
    * @see Level#toLevel(int)
    * @see Level#toLevel(int, org.apache.log4j.Level)
    */
   public static Level toLevel(int val) {
      if (val == DEMO_INT) {
         return DEMO;
      }
      return toLevel(val, Level.DEBUG);
   }

   /**
    * Checks whether <code>val</code> is {@link DemoLevel#MY_TRACE_INT}. If yes then returns {@link DemoLevel#MY_TRACE}, else
    * calls {@link Level#toLevel(int, org.apache.log4j.Level)}
    * 
    * @see Level#toLevel(int, org.apache.log4j.Level)
    */
   public static Level toLevel(int val, Level defaultLevel) {
      if (val == DEMO_INT) {
         return DEMO;
      }
      return Level.toLevel(val, defaultLevel);
   }

   /**
    * Checks whether <code>sArg</code> is "MY_TRACE" level. If yes then returns {@link DemoLevel#MY_TRACE}, else calls
    * {@link Level#toLevel(java.lang.String, org.apache.log4j.Level)}
    * 
    * @see Level#toLevel(java.lang.String, org.apache.log4j.Level)
    */
   public static Level toLevel(String sArg, Level defaultLevel) {
      if (sArg != null && sArg.toUpperCase().equals("DEMO")) {
         return DEMO;
      }
      return Level.toLevel(sArg, defaultLevel);
   }

}
