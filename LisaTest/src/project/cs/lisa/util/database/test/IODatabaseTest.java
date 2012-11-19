package project.cs.lisa.util.database.test;

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
import project.cs.lisa.metadata.MetadataParser;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.util.IOBuilder;
import project.cs.lisa.util.UProperties;
import project.cs.lisa.util.database.IODatabase;
import project.cs.lisa.util.database.IODatabaseFactory;
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
	
	/** The database under test. */
	private IODatabase mIoDatabase;
	
	/** The Datamodel factory */
	private DatamodelFactory mDatamodelFactory;
	
	/** The database factory. */
	private IODatabaseFactory mDatabaseFactory;
	
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
	}
	
	/**
	 * Creates an information object and tries to retrieve it again.
	 * Checks whether adding and getting the IO inserted into the database
	 * is working.
	 */
	public void testAddGetIO() {
		// The expected values of the information object we want to insert
		String expectedHash = "61C";
		String expectedHashAlg = "sha-256";
		String expectedContentType = "text/plain";
        String expectedFilepath = "/home/lisa/something.txt";
        String expectedFilesize = "58432";
        String expectedUrl1 = "www.google.com";
        String expectedUrl2 = "www.amazon.com";

        IOBuilder builder = new IOBuilder(mDatamodelFactory);
        builder.setHash(expectedHash)
        	.setHashAlgorithm(expectedHashAlg)
        	.setContentType(expectedContentType)
        	.addMetaData(LABEL_FILEPATH, expectedFilepath)
        	.addMetaData(LABEL_FILESIZE, expectedFilesize)
        	.addMetaData(LABEL_URL, expectedUrl1)
        	.addMetaData(LABEL_URL, expectedUrl2);
        InformationObject io = builder.build();

		
		// Perform the database requests
		InformationObject gotIo = null;
		try {
			mIoDatabase.addIO(io);
			gotIo = mIoDatabase.getIO(expectedHash);
			
		} catch (DatabaseException e) {
			Assert.fail("Should not have thrown an exception.");
		}
		
		Identifier newIdentifier = gotIo.getIdentifier();
		
		// Compare expected IO fields with the actual IO fields
		assertEquals(expectedHash, newIdentifier.getIdentifierLabel(
	            SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue());
		assertEquals(expectedHashAlg, newIdentifier.getIdentifierLabel(
	            SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue());
		assertEquals(expectedContentType, newIdentifier.getIdentifierLabel(
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
		assertEquals("/home/lisa/something.txt", (String)metadataMap.get(LABEL_FILEPATH));
		assertEquals("58432",(String) metadataMap.get(LABEL_FILESIZE));
		
		// Will always be a list of strings
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) metadataMap.get(LABEL_URL);
		List<String> expectedList = new LinkedList<String>();
		expectedList.add(expectedUrl1);
		expectedList.add(expectedUrl2);
		
		assertEquals(expectedList.size(), list.size());
		assertTrue(expectedList.containsAll(list));
		 
	}
	
	/**
	 * Tries to delete an information object from the database table.
	 */
	public void testDeleteIO() {
		// Create a IO for deleting
		String hash = "445";
		String hashAlg = "sha-256";
		String contentType = "text/plain";
        String filepath = "/home/lisa/test.txt";
        String filesize = "11";
        String url = "www.svt.se";

        IOBuilder builder = new IOBuilder(mDatamodelFactory);
        builder.setHash(hash)
        	.setHashAlgorithm(hashAlg)
        	.setContentType(contentType)
        	.addMetaData(LABEL_FILEPATH, filepath)
        	.addMetaData(LABEL_FILESIZE, filesize)
        	.addMetaData(LABEL_URL, url);
        InformationObject io = builder.build();
        
        try {
			mIoDatabase.addIO(io);
			mIoDatabase.deleteIO(io);
		} catch (DatabaseException e) {
			Assert.fail("Should not have thrown an exception.");
		}
        
        // Check whether the io is still available
        try {
			mIoDatabase.getIO(hash);
			Assert.fail("Should have thrown an exception since io was removed.");
		} catch (DatabaseException e) {
			// Success: io should not be stored in database anymore
		}
        
	}
	
}
