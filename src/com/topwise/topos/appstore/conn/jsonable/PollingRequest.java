package com.topwise.topos.appstore.conn.jsonable;

public class PollingRequest extends BaseRequest{
	public PollingRequest() {
		super();
	}
	
	public PollingRequest(boolean isWifi,long clientID) {
		super(isWifi,clientID);
	}
	
	public PollingRequest(boolean isWifi,long clientID,String controlVersion,String themeId) {
		super(isWifi,clientID,controlVersion,themeId);
	}
	public static JSONCreator<PollingRequest> CREATOR = new JSONCreator<PollingRequest>(PollingRequest.class);
}
