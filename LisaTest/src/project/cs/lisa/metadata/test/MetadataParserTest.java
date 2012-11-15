package project.cs.lisa.metadata.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import project.cs.lisa.metadata.MetadataParser;
import android.test.AndroidTestCase;

/**
 * Tests the Metadata Parser.
 * 
 * @author Harold Martinez
 * @author Kim-Anh Tran
 *
 */
public class MetadataParserTest extends AndroidTestCase {

		/** The parser under test. */
		private MetadataParser parser;
		
		@Override
		protected void setUp() throws Exception {
			super.setUp();
			parser = new MetadataParser();
		}
		
		public void testExtractMetaData() {
			String jsonString = "{"
					+ "\"NetInf\": \"v0.1a\","
					+ "\"msgid\": \"69936003\","
					+ " \"status\": 201,"
					+ " \"ni\": \"ni:///sha-256;ypd\","
					+ " \"ts\": \"2012-11-13T10:6:1+00:00\","
					+ " \"ct\": \"image/jpeg\","
					+ "\"metadata\": {"
					+ "    \"meta\": {"
					+ "         \"filename\": \"001.jpg\","
            		+ "         \"time\": ["
            		+ "             \"1352797492198\","
            		+ "             \"1352797502292\","
            		+ "             \"1352797530712\"],"
            		+ "         \"filetype\": \"image/jpeg\","
            		+ "          \"filesize\": \"5245329\"},"
            		+ "      \"ct\": \"image/jpeg\"},"
            		+ "  \"loc\": ["
            		+ "      \"nimacbt://F0:E7:7E:3F:D2:43\"]}";
			
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(jsonString);
			} catch (JSONException e) {
				Assert.fail("Creating a jason object from String failed.");
			}
			
			// Extract the meta data and check its contents
			Map<String, Object> map = parser.extractMetaData(jsonObject);

			assertEquals(map.get("filename"),"001.jpg");
			assertEquals(map.get("filetype"),"image/jpeg");
			
			String[] expectedElements = {"1352797492198", "1352797502292", "1352797530712"};
			List<String> expectedList = Arrays.asList(expectedElements);
			
			// We know that the result will be a list of Strings
			@SuppressWarnings("unchecked")
			List<String> actualList = (List<String>) map.get("time");
			
			assertTrue(expectedList.containsAll(actualList));
			assertEquals(expectedList.size(), actualList.size());  

		}
		
		
}
