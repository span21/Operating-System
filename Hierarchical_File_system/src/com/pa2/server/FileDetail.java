package com.pa2.server;

import java.io.Serializable;

import com.pa2.rmiinterface.PeerDetail;



public class FileDetail implements Serializable{

	String fileName;
	PeerDetail peer;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public PeerDetail getPeer() {
		return peer;
	}

	public void setPeer(PeerDetail peer) {
		this.peer = peer;
	}

	
}
