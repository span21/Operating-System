package com.pa3.server;

import java.io.Serializable;

import com.pa3.rmiinterface.PeerDetail;



public class FileDetail implements Serializable{

	String fileName;
	PeerDetail peer;
	int fileVersion;
	String status;
	int originalSuperPeerPort;
	PeerDetail originalPeerInfo;
	long downloadedTime;
	long timeToRefresh;
	boolean timeToRefreshstatus;

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

	public int getFileVersion() {
		return fileVersion;
	}

	public void setFileVersion(int fileVersion) {
		this.fileVersion = fileVersion;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getOriginalSuperPeerPort() {
		return originalSuperPeerPort;
	}

	public void setOriginalSuperPeerPort(int originalSuperPeerPort) {
		this.originalSuperPeerPort = originalSuperPeerPort;
	}

	public PeerDetail getOriginalPeerInfo() {
		return originalPeerInfo;
	}

	public void setOriginalPeerInfo(PeerDetail originalPeerInfo) {
		this.originalPeerInfo = originalPeerInfo;
	}

	public long getDownloadedTime() {
		return downloadedTime;
	}

	public void setDownloadedTime(long downloadedTime) {
		this.downloadedTime = downloadedTime;
	}

	public long getTimeToRefresh() {
		return timeToRefresh;
	}

	public void setTimeToRefresh(long timeToRefresh) {
		this.timeToRefresh = timeToRefresh;
	}

	public boolean isTimeToRefreshstatus() {
		return timeToRefreshstatus;
	}

	public void setTimeToRefreshstatus(boolean timeToRefreshstatus) {
		this.timeToRefreshstatus = timeToRefreshstatus;
	}

	
}
