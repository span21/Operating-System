package com.pa3.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;

import com.pa3.rmiinterface.PeerDetail;
import com.pa3.server.FileDetail;
import com.pa3.server.ResultDetail;

/**
 * This interface defines the remotely accessible part of the peer(client)
 * 
 * 
 *
 */

public interface PeerInterface extends Remote {

	/**
	 * This method signature implies that the implementation of it should be able to
	 * retrieve a file when it's name is given
	 * 
	 * @param fileName This is the name of the file to be retrieved/downloaded
	 * @return byte array representing the file
	 * @throws RemoteException
	 */
	public byte[] retreive(String fileName) throws RemoteException;
	
	
	
	public PeerDetail queryHit(String messageID, int timetolive,String fileName,String leafNode ,int portNumber,List<PeerDetail> peerlist) throws RemoteException, NumberFormatException, IOException;

	public void getValueMethod(HashSet<Integer> portSet, String fileName)throws RemoteException, NumberFormatException, IOException;
	
	public void fileoutOfOutInfo(FileDetail file) throws NumberFormatException, IOException, NotBoundException;
}
