package project.cs.lisa.message;
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

import java.io.IOException;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

/**
 * The Message class, which contains the representation of the message that we
 * intend to send and receive to the server. 
 * @author Thiago Costa Porto
 * @author Paolo Boschini
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
	
	public String getMessage() {
		return tempMessage;
	}
	
	public void setMessage(String message) {
		tempMessage = message;
	}
}
