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
			/* testinasdasdgaZ */
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
