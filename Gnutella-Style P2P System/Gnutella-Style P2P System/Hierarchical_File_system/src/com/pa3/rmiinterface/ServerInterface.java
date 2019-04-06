package com.pa3.rmiinterface;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import com.pa3.server.FileDetail;
import com.pa3.server.ResultDetail;

public interface ServerInterface extends Remote {

	//registering peer method
	public String registerPeer(PeerDetail peer, List<String> fileNames, boolean isRegisterFile,int superPeerPort) throws RemoteException;

	//method to search file
	public List<PeerDetail> searchFile(String fileName) throws RemoteException;

	//delete file
	public String deleteFile(PeerDetail peer, String fileName) throws RemoteException;

	// query to send to neighbouring peers
	public PeerDetail query(String messageID, int timetolive, String fileName, int fromPeerPort, int superPeerPort)
			throws MalformedURLException, RemoteException, NotBoundException, NumberFormatException, IOException;

	//query hit if the file is found.
	public PeerDetail queryHit(String messageID, int timetolive, String fileName, String leafNode, int portNumber,
			int fromPeerPort, List<PeerDetail> peerlist)
			throws RemoteException, MalformedURLException, NotBoundException, NumberFormatException, IOException;
	
	public void querylineartopology(String messageID, int timetolive, String fileName, int fromPeerPort, int superPeerPort)
			throws MalformedURLException, RemoteException, NotBoundException, NumberFormatException, IOException;
	/*--------- start change ----------*/
	public FileDetail modifyExistingFile(String filename ,PeerDetail peer ,boolean isModifiyfile,int superPeerPort) throws IOException, NotBoundException ;
	
	public boolean searchAndDelete(String fileName, int fileversion) throws RemoteException;
	
	public String addDownloadedFile(PeerDetail peer,PeerDetail currentPeerInfo ,List<String> filename , boolean isRegisterFile) throws RemoteException;
	
	public boolean sendapushrequest(String fileName ,FileDetail details , int superPeerPort,PeerDetail peer) throws RemoteException ,IOException, NotBoundException;
	
	public boolean checkFileVersion(String fileName ,int version) throws RemoteException;
	
	public List<FileDetail> sendpullrequest(boolean isFile, String fileName) throws NotBoundException, NumberFormatException, IOException;
	
	public void checkDownloadedList() throws NotBoundException, NumberFormatException, IOException;
	
	public boolean peerPullRequest() throws NumberFormatException, NotBoundException, IOException;
	
	public List<ResultDetail> searchResultFile(String filename) throws RemoteException;
	/*--------- end change ----------*/
	

}
