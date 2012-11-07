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
/**
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
package project.cs.lisa.netinf.node.access.rest.resources;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedException;

import org.restlet.resource.Get;
import org.restlet.resource.Post;

import project.cs.lisa.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.lisa.netinf.common.datamodel.SailDefinedAttributePurpose;
import android.util.Log;

public class IOResource extends LisaServerResource {
	
	public static final String TAG = "IOResource";
  
    private final static String GET    = "GET";
    private final static String PUT    = "PUT";
    private final static String DELETE = "DELETE";
    private final static String CACHE  = "CACHE";
	
	private String mHashAlg;
	private String mHash;
	private String mContentType;
	private String mMethod;
	private String mBluetoothMac;
	private String mMeta;
	
    private DatamodelFactory mDatamodelFactory;
    private NetInfNodeConnection mNodeConnection;
	
	@Override
	protected void doInit() {
    	super.doInit();
    	Log.d(TAG, "doInit()");
    	
    	mHashAlg          = getQuery().getFirstValue("HASH_ALG", true);
    	mHash          	  = getQuery().getFirstValue("HASH", true);
    	mContentType      = getQuery().getFirstValue("CT", true);
    	mMethod           = getQuery().getFirstValue("METHOD", true);
    	mBluetoothMac     = getQuery().getFirstValue("BTMAC", true);
    	mMeta             = getQuery().getFirstValue("META", true);
        mDatamodelFactory = getDatamodelFactory();
        mNodeConnection   = getNodeConnection();
    	
        Log.d(TAG, "HASH_ALG=" + mHashAlg);
        Log.d(TAG, "HASH=" + mHash);
        Log.d(TAG, "CT=" + mContentType);
        Log.d(TAG, "METHOD=" + mMethod);
        Log.d(TAG, "BTMAC=" + mBluetoothMac);
        Log.d(TAG, "META=" + mMeta);
        
	}

	/**
	 * Handle the get request from the RESTful server and routes it according to the method
	 * @return an Information Object if it is a GET request
	 * 		   null if it is a PUT request
	 */
	@Get
	public InformationObject handleGet() {
		Log.d(TAG, "handleGet()");

	    // TODO handle other request types as well
		
		if(mMethod.equals(GET)){			 			
			return getIO();
		} 
		if(mMethod.equals(PUT)){           
            putIO();
            return null;
        }
		else {
		    return null;
		}
	}
	
	/**
	 * Not use yet
	 */
	@Post
	public void handlePost() {
		Log.d(TAG, "handlePost()");
	}
	
	/**
	 * Gets an IO given its name
	 * @return the IO that the NetInf node got.
	 */
    private InformationObject getIO(){
        
        InformationObject io = null;
        
        //Create Identifier     
        Identifier identifier = createIdentifier(mHashAlg, mHash);
        
        try {
            io = mNodeConnection.getIO(identifier);
        } catch (NetInfCheckedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
                                      
        return io;      
    }
    
    /**
     * Publish an IO
     */
    private void putIO(){
        
        //Create dummy IO
        InformationObject io = mDatamodelFactory.createInformationObject();
        
        //Creating and setting the identifier
        Identifier identifier = createIdentifier(mHashAlg, mHash, mContentType);
        io.setIdentifier(identifier); 
        
        if (mBluetoothMac.length()>0){
            Attribute address = mDatamodelFactory.createAttribute();
            address.setAttributePurpose(SailDefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            address.setIdentification(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
            address.setValue(mBluetoothMac);
            io.addAttribute(address);   
        }
        
        if (mMeta.length()>0){
            Attribute meta = mDatamodelFactory.createAttribute();
            meta.setAttributePurpose(SailDefinedAttributePurpose.META_ATTRIBUTE.toString());
            meta.setIdentification(SailDefinedAttributeIdentification.META_DATA.getURI());
            meta.setValue(mMeta);
            io.addAttribute(meta);
        }
                                           
        //Putting the IO
        try {
            Log.d(TAG, "putIO()");
            mNodeConnection.putIO(io);
            
        } catch (NetInfCheckedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }       
    }
	
}
