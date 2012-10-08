package netinf.android.module;

import java.util.Properties;

import netinf.android.access.rest.AndroidRESTAccessServer;
import netinf.android.resolution.AndroidLocalResolutionService;
import netinf.android.resolution.AndroidRemoteNameResolutionService;
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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;


/**
 * Created by IntelliJ IDEA.
 * Date: Oct 23, 2010
 * Time: 7:59:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidNetInfModule extends AbstractModule {
   
	private Properties mNodeProperties;
	
	
	public AndroidNetInfModule(Properties properties) {
		mNodeProperties = properties;
	}
	
	
	@Override
    protected void configure() {  	

    	Names.bindProperties(binder(), mNodeProperties);
    	
     	bind(MessageEncoder.class).to(MessageEncoderProtobuf.class).in(Singleton.class);
		bind(DatamodelFactoryImpl.class);
		bind(DatamodelFactory.class).to(DatamodelFactoryImpl.class);
	    bind(NetInfNode.class).to(NetInfNodeImpl.class).in(Singleton.class);
	    bind(AsyncReceiveHandler.class).to(NetInfNodeReceiveHandler.class);
//
//
//		bind(Communicator.class).in(Singleton.class);
//		
        bind(ResolutionController.class).to(ResolutionControllerImplWithoutSecurity.class).in(Singleton.class);
        bind(ResolutionServiceSelector.class).to(SimpleResolutionServiceSelector.class);
        
        bind(TransferController.class).to(TransferControllerImpl.class).in(Singleton.class);
        
        bind(AccessServer.class).to(AndroidRESTAccessServer.class).in(Singleton.class);

    }
	
//    @Singleton
//    @Provides
//    ResolutionService[] provideResolutionServices(AndroidLocalResolutionService localResolutionService,
//    		                                      AndroidBluetoothRemoteResolutionService remoteResolutionService) {
//       ResolutionService[] localRS  = { localResolutionService };
//       ResolutionService[] remoteRS = { remoteResolutionService };
//
//       return (ResolutionService[]) ArrayUtils.addAll(localRS,remoteRS);
//
//    }
    @Singleton
    @Provides
    ResolutionService[] provideResolutionServices(AndroidLocalResolutionService localResolutionService,
    											  AndroidRemoteNameResolutionService remoteResolutionService) {
       ResolutionService[] localRS  = { localResolutionService };
       ResolutionService[] remoteRS = { remoteResolutionService };

       return (ResolutionService[]) ArrayUtils.addAll(localRS,remoteRS);

    }
    
}
