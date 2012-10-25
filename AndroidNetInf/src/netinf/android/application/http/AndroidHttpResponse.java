package netinf.android.application.http;

import netinf.common.datamodel.InformationObject;


public class AndroidHttpResponse {
	
	private int method;
	private InformationObject informationObject;
	
	public int getMethod() {
		return method;
	}
	public void setMethod(int method) {
		this.method = method;
	}
	public InformationObject getInformationObject() {
		return informationObject;
	}
	public void setInformationObject(InformationObject informationObject) {
		this.informationObject = informationObject;
	}
}
