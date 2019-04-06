package com.pa3.server;

import java.io.Serializable;

import com.pa3.rmiinterface.PeerDetail;

public class ResultDetail implements Serializable {
	
	PeerDetail file;
	String status;
	public PeerDetail getFile() {
		return file;
	}
	public void setFile(PeerDetail file) {
		this.file = file;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

}
