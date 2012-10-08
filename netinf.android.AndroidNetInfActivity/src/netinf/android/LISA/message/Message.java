/**
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
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */

package netinf.android.LISA.message;

import java.io.IOException;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

/**
 * The Message class, which contains the representation of the message that we
 * intend to send and receive to the server. 
 * @author Thiago Costa Porto
 */

public class Message {
	public String tempMessage;			/* message */
	
	/**
	 * Empty message constructor
	 */
	
	public Message() {
		tempMessage = "";
	}
	
	/**
	 * Constructor with string
	 * @param string Message
	 */
	
	public Message(String string) {
		tempMessage = string;
	}
	
	/**
	 * This function creates a new XML element based on the string that was set
	 * previously.
	 * @return false if creating the XML failed,
	 * 				 true  if creating the XML succeeded
	 */
	
	public StringWriter createXmlMessage() {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter sw = new StringWriter();
		
		try {
	    serializer.setOutput(sw);
	    serializer.startDocument("UTF-8", true);
	    serializer.startTag("", "LISA Message");
	    serializer.attribute("", "string", this.tempMessage);
	    serializer.endTag("", "LISA Message");
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
	
	/**
	 * This function parses an XML element, storing its information in the
	 * classes' variables 
	 * @return false if parsing the XML failed,
	 * 				 true  if parsing the XML succeeded
	 */
	public boolean parseXmlMessage() {
				
		return true;
	}
}
