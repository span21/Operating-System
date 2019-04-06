package com.pa2.server;

public class SearchedPeerInfo {
	
	private String messageId;
	private int UpstreamPeerID;
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public int getUpstreamPeerID() {
		return UpstreamPeerID;
	}
	public void setUpstreamPeerID(int upstreamPeerID) {
		UpstreamPeerID = upstreamPeerID;
	}
	

}
