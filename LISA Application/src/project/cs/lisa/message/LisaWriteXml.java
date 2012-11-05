/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
package project.cs.lisa.message;

import java.io.IOException;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class LisaWriteXml {
	/**
	 * This function creates a new XML element based on the string that was set
	 * previously.
	 * @return false if creating the XML failed,
	 * 				 true  if creating the XML succeeded
	 */
	
	public StringWriter createXmlMessage(Message message) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter sw = new StringWriter();
		
		try {
	    serializer.setOutput(sw);
	    serializer.startDocument("UTF-8", true);
	    serializer.startTag("", "message");
	    serializer.attribute("", "string", message.tempMessage);
	    serializer.endTag("", "message");
	    serializer.endDocument();
    }
		catch (IllegalArgumentException e) {
	    // TODO Auto-generated catch block
			Log.d("XMLSerializer", "Illegal Argument");
	    e.printStackTrace();
    }
		catch (IllegalStateException e) {
	    // TODO Auto-generated catch block
			Log.d("XMLSerializer", "Illegal State");
	    e.printStackTrace();
    }
		catch (IOException e) {
	    // TODO Auto-generated catch block
			Log.d("XMLSerializer", "IO Exception");
	    e.printStackTrace();
    }
				
		return sw;
	}
}
