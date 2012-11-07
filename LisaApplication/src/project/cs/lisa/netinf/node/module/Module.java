package project.cs.lisa.netinf.node.module;

import java.util.Properties;

import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.MessageEncoder;
import netinf.common.communication.MessageEncoderProtobuf;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.node.access.AccessServer;
import netinf.node.api.NetInfNode;
import netinf.node.api.impl.NetInfNodeImpl;
import netinf.node.api.impl.NetInfNodeReceiveHandler;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.ResolutionServiceSelector;
import netinf.node.resolution.impl.ResolutionControllerImplWithoutSecurity;
import netinf.node.resolution.impl.SimpleResolutionServiceSelector;
import netinf.node.transfer.TransferController;
import netinf.node.transfer.impl.TransferControllerImpl;

import org.apache.commons.lang.ArrayUtils;

import project.cs.lisa.netinf.node.access.rest.RESTAccessServer;
import project.cs.lisa.netinf.node.resolution.NameResolutionService;

import android.util.Log;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class Module extends AbstractModule  {

	public static final String TAG = "Module";
	
	private Properties mProperties;
	
	public Module(Properties properties) {
		Log.d(TAG, "Module()");
		mProperties = properties;
	}
	
	@Override
	protected void configure() {
		Log.d(TAG, "configure()");
		
		Log.d(TAG, "bindProperties()");
		Names.bindProperties(binder(), mProperties);
		
		Log.d(TAG, "Binding 1");
     	bind(MessageEncoder.class).to(MessageEncoderProtobuf.class).in(Singleton.class);
     	
     	Log.d(TAG, "Binding 2");
		bind(DatamodelFactory.class).to(DatamodelFactoryImpl.class);
		
		Log.d(TAG, "Binding 3");
	    bind(NetInfNode.class).to(NetInfNodeImpl.class).in(Singleton.class);
	    
	    Log.d(TAG, "Binding 4");
	    bind(AsyncReceiveHandler.class).to(NetInfNodeReceiveHandler.class);
	    
	    Log.d(TAG, "Binding 5");
        bind(ResolutionController.class).to(
                ResolutionControllerImplWithoutSecurity.class).in(Singleton.class);
        
        Log.d(TAG, "Binding 6");
        bind(ResolutionServiceSelector.class).to(SimpleResolutionServiceSelector.class);
        
        Log.d(TAG, "Binding 7");
        bind(TransferController.class).to(TransferControllerImpl.class).in(Singleton.class);
        
        Log.d(TAG, "Binding 8");
        bind(AccessServer.class).to(RESTAccessServer.class).in(Singleton.class);
	}
	
	@Singleton
	@Provides
	ResolutionService[] provideResolutionServices(NameResolutionService nrs) {
		ResolutionService[] empty = {}; 
		return (ResolutionService[]) ArrayUtils.add(empty, nrs);
	}
	
//	Examples:
	
//	/**
//	 * This method provides all the {@link ResolutionService}s which are automatically inserted into the node. In order to get an
//	 * instance of the according {@link ResolutionService}, add an additional parameter to this method, since this puts GUICE in
//	 * charge of creating the correct instance of the according service.
//	 * 
//	 * @param localResolutionService
//	 * @param rdfResolutionService
//	 * @return
//	 */
//	@Singleton
//	@Provides
//	ResolutionService[] provideResolutionServices(RemoteResolutionFactory remoteResolutionFactory,
//        RDFResolutionService rdfResolutionService) {
//     ResolutionService[] otherRS = { rdfResolutionService };
//     ResolutionService[] remoteRS = remoteResolutionFactory.getRemoteResolutionServices().toArray(new ResolutionService[] {});
//     return (ResolutionService[]) ArrayUtils.addAll(remoteRS, otherRS);
	
//     @Singleton
//     @Provides
//     ResolutionService[] provideResolutionServices(AndroidLocalResolutionService localResolutionService,
//     											  AndroidRemoteNameResolutionService remoteResolutionService) {
//        ResolutionService[] localRS  = { localResolutionService };
//        ResolutionService[] remoteRS = { remoteResolutionService };
//
//        return (ResolutionService[]) ArrayUtils.addAll(localRS,remoteRS);
//
//     }
	
}
