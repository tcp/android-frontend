package project.cs.lisa.netinf.node.access.rest.resources;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;

import org.restlet.resource.Get;

import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.transferdispatcher.TransferDispatcher;
import android.os.Environment;
import android.util.Log;

public class BOResource extends LisaServerResource {

	/** Debugging Tag. */
	private static final String TAG = "BOResource";
	
	/** HashMap Key: Filepath */
	private static final String FILEPATH = "filePath";
	
	/** HashMap Key: Content type */
	private static final String CONTENT_TYPE = "contenType";

	private String mHashValue;

	private String mHashAlgorithm;


	/**
	 * Initializes the context of a BOResource.
	 */
	@Override
	protected void doInit() {
		super.doInit();
		mHashValue = getQuery().getFirstValue("HASH_VALUE", true);
		mHashAlgorithm = getQuery().getFirstValue("HASH_ALG", true);
	}


	@Get
	public HashMap<String, String> retrieveBO() {

		byte[] fileData = null;
		String filePath = null;
		String contentType = null;

		Identifier identifier = createIdentifier(mHashAlgorithm, mHashValue);
		InformationObject io = null;
		try {
			io = getNodeConnection().getIO(identifier);
		} catch (NetInfCheckedException e) {
			Log.d(TAG, "Failed retrieving the IO from the NRS. Hash value: " + mHashValue);
		}

		/* Retrieve the data corresponding to the hash from another device. */
		if (io != null) {

			contentType = io.getIdentifier().getIdentifierLabel(
					SailDefinedLabelName.CONTENT_TYPE.getLabelName())
					.getLabelValue();
			
			TransferDispatcher tsDispatcher = TransferDispatcher.INSTANCE;

			try {
				fileData = tsDispatcher.getByteArray(io);
			} catch (IOException e) {
				Log.d(TAG, "Couldn't retrieve the requested data.");
			}

			if (fileData != null) {

				String hash = io.getIdentifier().getIdentifierLabel(
						SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
				
				filePath = Environment.getExternalStorageDirectory() + "/LISA/" + hash;
				writeByteStreamToFile(filePath, fileData);

			} else {
				Log.d(TAG, "No file data to write.");
			}

		}

		HashMap<String, String> map = new HashMap<String, String>(2);
		map.put(FILEPATH, filePath);
		map.put(CONTENT_TYPE, contentType);
		
		return map;
	}


	/**
	 * Creates a new file containing the specified fileData at the specified
	 * targetPath.
	 * 
	 * @param targetPath	The location to create the file
	 * @param fileData		The data to write at the specified path
	 */
	private void writeByteStreamToFile(String targetPath, byte[] fileData) {

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(targetPath);
			fos.write(fileData);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Couldn't find file: " + targetPath);
		} catch (IOException e) {
			Log.d(TAG, "Failed while writing data to " + targetPath);
		} finally {	  

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					Log.d(TAG, "Failed closing the stream after writing to file.");
				}        	
			}
		}		
	}	

}
