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
package project.cs.lisa.netinf.node.resolution;

import java.util.LinkedList;
import java.util.List;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import project.cs.lisa.application.MainApplication;
import project.cs.lisa.exceptions.DatabaseException;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.search.SearchResult;
import project.cs.lisa.util.database.IODatabase;
import project.cs.lisa.util.database.IODatabaseFactory;
import android.util.Log;

import com.google.inject.Inject;

/**
 * A local resolution service that provides access to the local
 * database.
 * 
 * @author Kim-Anh Tran
 *
 */
public class LocalResolutionService 
		extends AbstractResolutionServiceWithoutId
		implements ResolutionSearchService {
	
	/** The debug tag. */
	private static final String TAG = "LocalResolutionService";
	
	/** The factory creating the database. */
	@Inject
	private IODatabaseFactory mDatabaseFactory;
	
	/** The local database used for storing information objects. */
	private IODatabase mDatabase;
	
	/**
	 * Creates a new local resolution service.
	 * 
	 * @param databaseFactory	The factory used for creating the database.
	 */
	@Inject
	public LocalResolutionService(IODatabaseFactory databaseFactory) {
		mDatabaseFactory = databaseFactory;
		mDatabase = mDatabaseFactory.create(MainApplication.getAppContext());
	}

	@Override
	public void delete(Identifier identifier) {
		String hash = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
		mDatabase.deleteIO(hash);
	}

	@Override
	public String describe() {
		return "Database Service";
	}

	@Override
	public InformationObject get(Identifier identifier) {
		String hash = identifier.getIdentifierLabel(
				SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
		
		InformationObject io = null;
		try {
			io = mDatabase.getIO(hash);
		} catch (DatabaseException e) {
			Log.e(TAG, "Couldn't retrieve the information object associated with the hash = " 
					+ hash);
			return null;
		}
				
		return io;
	}

	@Override
	public List<SearchResult> search(List<String> keywords) {
		// Searching within the database will expect only one keyword: the url
		List<SearchResult> results = new LinkedList<SearchResult>();
		String url = keywords.get(0);
		SearchResult result = null;
		
		try {
			result = mDatabase.searchIO(url);
		} catch (DatabaseException e) {
			Log.e(TAG, "No entry found that corresponds to the url: " + url);
			return results;
		}
		
		results.add(result);

		return results;
	}
	
	@Override
	public void put(InformationObject io) {
		try {
			mDatabase.addIO(io);
		} catch (DatabaseException e) {
			Log.e(TAG, "Failed adding the information object into the database.");
		}
	}

	@Override
	protected ResolutionServiceIdentityObject createIdentityObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Identifier> getAllVersions(Identifier arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
