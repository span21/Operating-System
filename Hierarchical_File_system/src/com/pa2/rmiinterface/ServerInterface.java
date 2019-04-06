package com.pa2.rmiinterface;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface ServerInterface extends Remote {

	//registering peer method
	public String registerPeer(PeerDetail peer, List<String> fileNames, boolean isRegisterFile) throws RemoteException;

	//method to search file
	public List<PeerDetail> searchFile(String fileName) throws RemoteException;

	//delete file
	public String deleteFile(PeerDetail peer, String fileName) throws RemoteException;

	// query to send to neighbouring peers
	public void query(String messageID, int timetolive, String fileName, int fromPeerPort, int superPeerPort)
			throws MalformedURLException, RemoteException, NotBoundException, NumberFormatException, IOException;

	//query hit if the file is found.
	public void queryHit(String messageID, int timetolive, String fileName, String leafNode, int portNumber,
			int fromPeerPort)
			throws RemoteException, MalformedURLException, NotBoundException, NumberFormatException, IOException;
	
	public void querylineartopology(String messageID, int timetolive, String fileName, int fromPeerPort, int superPeerPort)
			throws MalformedURLException, RemoteException, NotBoundException, NumberFormatException, IOException;
	

}
