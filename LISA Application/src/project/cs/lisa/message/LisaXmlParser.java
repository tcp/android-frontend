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
import java.io.InputStream;

import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class LisaXmlParser extends DefaultHandler {
	private static final String namespace = null; /* we dont use namespace */
	
	XmlPullParser parser;
	
	/**
	 * This function parses an XML element, storing its information in the
	 * classes' variables 
	 * @return false if parsing the XML failed,
	 * 				 true  if parsing the XML succeeded
	 * @throws IOException 
	 */
	
	public boolean ParseXmlMessage(InputStream in) throws XmlPullParserException, IOException {
		try {
			parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			readFeed(parser);
			/* gerrit test */
			/* testinasdasdasdadasdasdgaZ */
		}
		finally {
			in.close();
		}
		
		return true;
	}

	
	public boolean readFeed(XmlPullParser parser) {
		
		return true;
	}
}
