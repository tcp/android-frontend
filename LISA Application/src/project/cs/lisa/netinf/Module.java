package project.cs.lisa.netinf;

import java.lang.reflect.Array;
import java.util.Properties;

import netinf.node.api.NetInfNode;
import netinf.node.api.impl.NetInfNodeImpl;
import netinf.node.resolution.ResolutionService;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.apache.commons.lang.ArrayUtils;

public class Module extends AbstractModule  {

	private Properties mProperties;
	
	public Module(Properties properties) {
		mProperties = properties;
	}
	
	@Override
	protected void configure() {
		
		Names.bindProperties(binder(), mProperties);
		
		bind(NetInfNode.class).to(NetInfNodeImpl.class).in(Singleton.class);
	      
	}
	
	@Singleton
	@Provides
	ResolutionService[] provideResolutionServices(HelloWorldResolutionService helloRs) {
		return (ResolutionService[]) ArrayUtils.add(ArrayUtils.EMPTY_OBJECT_ARRAY, helloRs);
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
