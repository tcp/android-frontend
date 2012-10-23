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

public class LisaIOResource extends LisaServerResource {
	
	public static final String TAG = "LisaResource";
  
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
	
	@Post
	public void handlePost() {
		Log.d(TAG, "handlePost()");
	}
	
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
