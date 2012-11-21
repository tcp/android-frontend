package project.cs.lisa.util.database.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;

import org.json.JSONException;
import org.json.JSONObject;

import project.cs.lisa.exceptions.DatabaseException;
import project.cs.lisa.metadata.Metadata;
import project.cs.lisa.metadata.MetadataParser;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.search.SearchResult;
import project.cs.lisa.util.IOBuilder;
import project.cs.lisa.util.UProperties;
import project.cs.lisa.util.database.IODatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

/**
 * Tests the IODatabase storing the information objects that
 * are accessible on the device.
 * 
 * @author Harold Martinez
 * @author Kim-Anh Tran
 *
 */
public class IODatabaseTest extends AndroidTestCase {
	
	/** Prefix for test: Database */
	private static final String TEST_FILE_PREFIX = "test_";
	
	// The fields of the information object that is created
	
	/** The hash. */
	private static final String HASH = "111";
	
	/** The hash algorithm. */
	private static final String HASH_ALG = "sha-256";
	
	/** The content type. */
	private static final String CONTENT_TYPE = "text/plain";
	
	/** The file path. */
	private static final String FILE_PATH = "/home/lisa/url.txt";
	
	/** the file size. */
	private static final String FILE_SIZE = "11";
	
	/** The first url corresponding to the object. */
	private static final String URL_1 = "www.dn.se";
	
	/** The second url corresponding to the object. */
	private static final String URL_2 = "www.svt.se";
	
	/** The actual information object. */
	private InformationObject mIo;
	
	/** The database under test. */
	private IODatabase mIoDatabase;
	
	/** The Datamodel factory */
	private DatamodelFactory mDatamodelFactory;
	
	/** Meta-data label for the filepath. */
	private String LABEL_FILEPATH;
	
	/** Meta-data label for the file size. */
	private String LABEL_FILESIZE;
	
	/** Meta-data label for the url. */
	private String LABEL_URL;
	
	
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	    RenamingDelegatingContext context 
        		= new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
		
		mDatamodelFactory = new DatamodelFactoryImpl();
		mIoDatabase =  new IODatabase(mDatamodelFactory, context);	
		
		// The meta data tag names
		UProperties instance = UProperties.INSTANCE;
		LABEL_FILEPATH = instance.getPropertyWithName("metadata.filepath");
		LABEL_FILESIZE = instance.getPropertyWithName("metadata.filesize");
		LABEL_URL = instance.getPropertyWithName("metadata.url");
		
		mIo = createIO();
	}
	
	/**
	 * Creates an information object and tries to retrieve it again.
	 * Checks whether adding and getting the IO inserted into the database
	 * is working.
	 */
	public void testAddGetIO() {
		// Perform the database requests
		InformationObject gotIo = null;
		try {
			mIoDatabase.addIO(mIo);
			gotIo = mIoDatabase.getIO(HASH);
			
		} catch (DatabaseException e) {
			Assert.fail("Should not have thrown an exception.");
		}
		
		Identifier newIdentifier = gotIo.getIdentifier();
		
		// Compare expected IO fields with the actual IO fields
		assertEquals(HASH, newIdentifier.getIdentifierLabel(
	            SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue());
		assertEquals(HASH_ALG, newIdentifier.getIdentifierLabel(
	            SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue());
		assertEquals(CONTENT_TYPE, newIdentifier.getIdentifierLabel(
	            SailDefinedLabelName.CONTENT_TYPE.getLabelName()).getLabelValue());
		
		
		// Check meta data key value pairs
		String gotIoMetadata = newIdentifier.getIdentifierLabel(
		            SailDefinedLabelName.META_DATA.getLabelName()).getLabelValue();
		
		Map<String, Object> metadataMap = new HashMap<String, Object>();
		try {
			metadataMap = MetadataParser.toMap(new JSONObject(gotIoMetadata));
		} catch (JSONException e) {
			Assert.fail("Should not have thrown an exception.");
		}
		assertEquals(FILE_PATH, (String)metadataMap.get(LABEL_FILEPATH));
		assertEquals(FILE_SIZE,(String) metadataMap.get(LABEL_FILESIZE));
		
		// Will always be a list of strings
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) metadataMap.get(LABEL_URL);
		List<String> expectedList = new LinkedList<String>();
		expectedList.add(URL_1);
		expectedList.add(URL_2);
		
		assertEquals(expectedList.size(), list.size());
		assertTrue(expectedList.containsAll(list));
		 
	}
	
	/**
	 * Tries to delete an information object from the database table.
	 */
	public void testDeleteIO() {
        
        try {
			mIoDatabase.addIO(mIo);
			mIoDatabase.deleteIO(mIo);
		} catch (DatabaseException e) {
			Assert.fail("Should not have thrown an exception.");
		}
        
        // Check whether the io is still available
        try {
			mIoDatabase.getIO(HASH);
			Assert.fail("Should have thrown an exception since io was removed.");
		} catch (DatabaseException e) {
			// Success: io should not be stored in database anymore
		}
        
	}
	
	/** 
	 * Tests searching for entries using a url.
	 */
	public void testSearchIO() {
       
		SearchResult result = null;
    	try {
    		mIoDatabase.addIO(mIo);
			result = mIoDatabase.searchIO(URL_1);
		} catch (DatabaseException e) {
			Assert.fail("Should not have thrown an exception.");
		}

    	// Check if the search result was created correctly.
    	assertEquals(HASH, result.getHash());
    	
    	Metadata metadata = result.getMetaData();
    	JSONObject jsonMetadata = null;
    	try {
			jsonMetadata = new JSONObject(metadata.convertToMetadataString());


		} catch (JSONException e) {
			Assert.fail("Should not have thrown an exception. Valid metadata String.");
		}
    	// Compare if the returned object is the right object we wanted to search for
    	Map<String, Object> map = null;
		try {
			map = MetadataParser.toMap(jsonMetadata);
		} catch (JSONException e) {
			Assert.fail("Should not have raised an exception");
		}
    	
		String[] expectedUrl = {URL_1, URL_2};
		List<String> expectedUrlList = Arrays.asList(expectedUrl);
    	
		// We know that the result will be a list of Strings: Url list
		@SuppressWarnings("unchecked")
		List<String> actualUrlList = (List<String>) map.get(LABEL_URL);
		assertTrue(expectedUrlList.containsAll(actualUrlList));
		
		assertEquals(FILE_PATH, map.get(LABEL_FILEPATH));
		assertEquals(FILE_SIZE, map.get(LABEL_FILESIZE));
	}
	
	private InformationObject createIO() {
        IOBuilder builder = new IOBuilder(mDatamodelFactory);
        return builder.setHash(HASH)
        		.setHashAlgorithm(HASH_ALG)
        		.setContentType(CONTENT_TYPE)
        		.addMetaData(LABEL_FILEPATH, FILE_PATH)
        		.addMetaData(LABEL_FILESIZE, FILE_SIZE)
        		.addMetaData(LABEL_URL, URL_1)
        		.addMetaData(LABEL_URL, URL_2).build();
	}
	
}
