/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.android.resolution;

import java.util.List;

import netinf.android.common.datamodel.SailDefinedLabelName;
import netinf.android.resolution.local.database.MySQLiteHelper;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.node.resolution.ResolutionService;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.google.inject.Inject;

/**
 * @author PG Augnet 2, University of Paderborns
 */
public class AndroidLocalResolutionService extends AndroidAbstractResolutionService implements ResolutionService {

    // Debugging
    private static final String TAG = "AndroidLocalResolutionService";
    private static final boolean D = true;
	   
	private final DatamodelFactory datamodelFactory;
  
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	private String[] allColumns = { MySQLiteHelper._ID,
            MySQLiteHelper.HASH_ALG,
            MySQLiteHelper.HASH_CONTENT};


   @Inject
   public AndroidLocalResolutionService(DatamodelFactory datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
   }

   
   public MySQLiteHelper openDatabase(Context context) throws SQLiteException {
	   dbHelper = new MySQLiteHelper(context);	   
	   database = dbHelper.getWritableDatabase();
	   return dbHelper;
   }
   
   
   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = this.datamodelFactory
            .createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName("AndroidLocalResolutionService");
      identity.setDefaultPriority(1000);
      identity.setDescription("Local resolution service used for retriving, putting and deleting IO from the SQLite database");
      return identity;
   }


   public void delete(Identifier identifier) {
	   
	   if(D) Log.d(TAG, "DELETE IO");	   
	   String hash = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
	   if(D) Log.d(TAG, "Deleting IO with hash: " + hash);
	   int numberOfRows = database.delete(MySQLiteHelper.TABLE_NAME, MySQLiteHelper.HASH_CONTENT
				+ " = '" + hash + "'", null);
		if(numberOfRows>0){
			if(D) Log.d(TAG, "IO deleted successfully");
		}
		else
			if(D) Log.d(TAG, "IO DO NOT deleted");
   }


   public String describe() {
      return "Local Resolution Service used for retriving, putting and deleting IO from the SQLite database";
   }


   public InformationObject get(Identifier identifier) {
	   
	   if(D) Log.d(TAG, "GET IO");
	   InformationObject myTmpIO = null;
	   String hash = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_NAME,
				allColumns, null, null,null, null, null);
		if(cursor.moveToFirst()){			
			while (!cursor.isAfterLast()) {				
				String tmpFileName = cursor.getString(MySQLiteHelper.HASH_CONTENT_COLUMN_INDEX);
				if(tmpFileName.equals(hash)){
					myTmpIO = cursorToIO(cursor);
					if(D) Log.d(TAG, "IO found in the database. IO =  " + myTmpIO.toString());
					return myTmpIO;									
				}
				cursor.moveToNext();
			}			
		}
		if(D) Log.d(TAG, "The requested IO was not found in the database.");
		return null;
   }

   //Unimplemented
   public List<Identifier> getAllVersions(Identifier identifier) {
      return null;
   }


   public void put(InformationObject informationObject) {
		
	   if(D) Log.d(TAG, "PUT IO");
		//Extracting values from IO's identifier
		String hashAlg     = informationObject.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
		String hashCont    = informationObject.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
					
		//Creating the Content value
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.HASH_ALG, hashAlg);
		values.put(MySQLiteHelper.HASH_CONTENT, hashCont);
		
		long insertId = database.insert(MySQLiteHelper.TABLE_NAME, null,values);
		// To show how to query
		Cursor cursor = database.query(MySQLiteHelper.TABLE_NAME,
				allColumns, MySQLiteHelper._ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		InformationObject myTmpIO = cursorToIO(cursor);
		if(D) Log.d(TAG, "The IO was successfully put. IO = " + myTmpIO.toString());

   }
   
	private InformationObject cursorToIO(Cursor cursor) {
		
		//Creating the IO
		InformationObject mIO = datamodelFactory.createInformationObject();
		
		//Creating the identifier for the IO		
		Identifier identifier = datamodelFactory.createIdentifier();
		
		//Creating a the HASH_ALG label
	    IdentifierLabel identifierLabel = datamodelFactory.createIdentifierLabel();
	    identifierLabel.setLabelName(SailDefinedLabelName.HASH_ALG.getLabelName());
	    identifierLabel.setLabelValue(cursor.getString(MySQLiteHelper.HASH_ALG_COLUMN_INDEX));
	    identifier.addIdentifierLabel(identifierLabel);
	    
	    //Creating a the HASH_COTENT label
	    IdentifierLabel identifierLabel2 = datamodelFactory.createIdentifierLabel();
	    identifierLabel2.setLabelName(SailDefinedLabelName.HASH_CONTENT.getLabelName());
	    identifierLabel2.setLabelValue(cursor.getString(MySQLiteHelper.HASH_CONTENT_COLUMN_INDEX));
	    identifier.addIdentifierLabel(identifierLabel2);
	    	    	    
	    mIO.setIdentifier(identifier);
	        	    
		return mIO;
	}

}
