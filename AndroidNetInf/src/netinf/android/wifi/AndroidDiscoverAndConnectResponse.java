package netinf.android.wifi;

import java.net.InetAddress;

import netinf.common.datamodel.InformationObject;


public class AndroidDiscoverAndConnectResponse {
	
	private InetAddress ipAddress;
	private InformationObject informationObject;
	
	public InetAddress getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public InformationObject getInformationObject() {
		return informationObject;
	}
	public void setInformationObject(InformationObject informationObject) {
		this.informationObject = informationObject;
	}

}
