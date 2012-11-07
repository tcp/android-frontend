package project.cs.lisa.message;

import java.io.IOException;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class WriteXml {
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
