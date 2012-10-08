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
	
	public boolean createXmlMessage() {
		
		return true;
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
