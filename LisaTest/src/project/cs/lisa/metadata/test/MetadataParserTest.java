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

		/**
		 * Extracts and compares the elements of a passed meta-data String.
		 */
		public void testExtractMetaData() {
			String jsonString = "{"
					+ "\"meta\": {"
					+ "         \"filename\": \"001.jpg\","
            		+ "         \"time\": ["
            		+ "             \"1352797492198\","
            		+ "             \"1352797502292\","
            		+ "             \"1352797530712\"],"
            		+ "         \"filetype\": \"image/jpeg\","
            		+ "          \"filesize\": \"5245329\"}}";

			String expectedFilename = "001.jpg";
			String expectedFilesize = "5245329";
			String expectedFiletype = "image/jpeg";
						
			String[] expectedTimestamps = {"1352797492198", "1352797502292", "1352797530712"};
			List<String> expectedTimeStampsList = Arrays.asList(expectedTimestamps);
			
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(jsonString);
			} catch (JSONException e) {
				Assert.fail("Creating a jason object from String failed.");
			}
			
			// Extract the meta data and check its contents
			Map<String, Object> map = null;
			try {
				map = MetadataParser.toMap(jsonObject);
			} catch (JSONException e) {
				Assert.fail("Should not have raised an exception.");
			}

			assertEquals(expectedFilename, map.get("filename"));
			assertEquals(expectedFiletype, map.get("filetype"));
			assertEquals(expectedFilesize, map.get("filesize"));
			
			// We know that the result will be a list of Strings
			@SuppressWarnings("unchecked")
			List<String> actualList = (List<String>) map.get("time");
			
			assertTrue(expectedTimeStampsList.containsAll(actualList));
			assertEquals(expectedTimeStampsList.size(), actualList.size());  

		}
		
		
}
