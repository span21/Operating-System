package com.pa2.client;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

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
	
	
	
	public void queryHit(String messageID, int timetolive,String fileName,String leafNode ,int portNumber) throws RemoteException, NumberFormatException, IOException;

	public void getValueMethod(HashSet<Integer> portSet, String fileName)throws RemoteException, NumberFormatException, IOException;
}
