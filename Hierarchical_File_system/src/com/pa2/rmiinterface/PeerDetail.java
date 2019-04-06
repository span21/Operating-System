package com.pa2.rmiinterface;

import java.io.Serializable;

public class PeerDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int peerId;
	private int portNum;
	String directoryLocation;

	public int getPeerId() {
		return peerId;
	}

	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}

	public int getPortNum() {
		return portNum;
	}

	public void setPortNum(int portNum) {
		this.portNum = portNum;
	}

	public String getDirectoryLocation() {
		return directoryLocation;
	}

	public void setDirectoryLocation(String directoryLocation) {
		this.directoryLocation = directoryLocation;
	}

	public PeerDetail(int peerId, int portNum, String directoryLocation) {
		this.peerId = peerId;
		this.portNum = portNum;
		this.directoryLocation = directoryLocation;
	}

	public String compareObjects(Object obj) {
		PeerDetail other = (PeerDetail) obj;
		if (directoryLocation.equals(other.directoryLocation))
			return "Directory already registered";
		if (peerId == other.peerId)
			return "Peer Id already exists";
		if (portNum == other.portNum)
			return "Port num already used";
		return "Peer Unique";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((directoryLocation == null) ? 0 : directoryLocation.hashCode());
		result = prime * result + peerId;
		result = prime * result + portNum;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		PeerDetail other = (PeerDetail) obj;
		if (directoryLocation == null) {
			if (other.directoryLocation != null)
				return false;
		} else if (!directoryLocation.equals(other.directoryLocation))
			return false;
		if (peerId != other.peerId)
			return false;
		if (portNum != other.portNum)
			return false;
		return true;
	}

}
