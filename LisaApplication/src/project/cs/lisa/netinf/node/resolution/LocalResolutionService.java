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

import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.node.resolution.ResolutionService;
import project.cs.lisa.application.MainApplication;
import project.cs.lisa.exceptions.DatabaseException;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.lisa.util.database.IODatabase;
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
		implements ResolutionService {
	
	/** The debug tag. */
	private static final String TAG = "LocalResolutionService";
	
	/** The datamodel factory used for creating information objects. */
	private DatamodelFactory mDatamodelFactory;
	
	/** The local database used for storing information objects. */
	private IODatabase mDatabase;
	
	/**
	 * Creates a new local resolution service.
	 * 
	 * @param application	The main application
	 * @param factory		The datamodel factory
	 */
	@Inject
	public LocalResolutionService(MainApplication application, DatamodelFactory factory) {
		mDatamodelFactory = factory;
		mDatabase = new IODatabase(factory, application.getApplicationContext());
	}

	@Override
	public void delete(Identifier arg0) {
		// TODO Auto-generated method stub
		
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
	public List<Identifier> getAllVersions(Identifier arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(InformationObject arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ResolutionServiceIdentityObject createIdentityObject() {
		// TODO Auto-generated method stub
		return null;
	}

}
