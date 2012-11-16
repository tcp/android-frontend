package project.cs.lisa.util.database.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;

import org.json.JSONException;
import org.json.JSONObject;

import project.cs.lisa.exceptions.DatabaseException;
import project.cs.lisa.metadata.Metadata;
import project.cs.lisa.metadata.MetadataParser;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.util.UProperties;
import project.cs.lisa.util.database.IODatabase;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContext;

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
	
	/** The Datamodel facotry */
	private DatamodelFactory mDatamodelFactory;
	
	/** Database */
	private SQLiteDatabase mDatabase;
	
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
		
		mIoDatabase = new IODatabase(context,mDatamodelFactory);		
		mDatabase = mIoDatabase.getWritableDatabase();
		
		
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
		
		Identifier identifier = mDatamodelFactory.createIdentifier();

		// The expected values of the information object we want to insert
		String expectedHash = "61C";
		String expectedHashAlg = "sha-256";
		String expectedContentType = "text/plain";
        String expectedFilepath = "/home/lisa/something.txt";
        String expectedFilesize = "58432";
        String expectedUrl1 = "www.google.com";
        String expectedUrl2 = "www.amazon.com";
		
		
        // Add IO fields to identifier
		addIdentifierLabel(identifier, 
				SailDefinedLabelName.HASH_CONTENT.getLabelName(), expectedHash);

		addIdentifierLabel(identifier, 
				SailDefinedLabelName.HASH_ALG.getLabelName(), expectedHashAlg); 
		
		addIdentifierLabel(identifier, 
				SailDefinedLabelName.CONTENT_TYPE.getLabelName(), expectedContentType);
        
		
		// Create the metadata
        Metadata metaData = new Metadata();      
		metaData.insert(LABEL_FILEPATH, expectedFilepath);
		metaData.insert(LABEL_FILESIZE, expectedFilesize);
		metaData.insert(LABEL_URL, expectedUrl1);
		metaData.insert(LABEL_URL, expectedUrl2);
		
		String expectedMetadata = metaData.convertToString();	
		addIdentifierLabel(identifier, 
				SailDefinedLabelName.META_DATA.getLabelName(), expectedMetadata);
		
		// Create the Information Object
		InformationObject io = mDatamodelFactory.createInformationObject();
		io.setIdentifier(identifier);
		
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
	 * Adds an identifier label for the specified identifier for the passed label properties.
	 * 
	 * @param identifier	The identifier to modify
	 * @param labelName		The label name
	 * @param labelValue	The label value
	 */
	private void addIdentifierLabel(Identifier identifier, String labelName, String labelValue) {
		 IdentifierLabel hashLabel = mDatamodelFactory.createIdentifierLabel();
         hashLabel.setLabelName(labelName);
         hashLabel.setLabelValue(labelValue);
         identifier.addIdentifierLabel(hashLabel);
	}
	
}
