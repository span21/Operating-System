package com.pa2.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.pa2.rmiinterface.PeerDetail;

public class PeerImpl extends UnicastRemoteObject implements PeerInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7495177934301029481L;

	private PeerDetail peer;

	public PeerImpl(PeerDetail peerInfo) throws RemoteException {
		this.peer = peerInfo;
	}

	List<Integer> PeerFoundList = new ArrayList<>();

	/**
	 * This method signature implies that it should be able to retrieve a file when
	 * it's name is given
	 * 
	 * @param fileName This is the name of the file to be retrieved/downloaded
	 * @return byte array representing the file
	 * @throws RemoteException
	 */
	public synchronized byte[] retreive(String fileName) throws RemoteException {
		byte[] fileContent = null;
		// Reading the file byte by byte.
		try {
			fileContent = Files.readAllBytes(new File(this.peer.getDirectoryLocation() + "/" + fileName).toPath());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return fileContent;

	}

	/** This method get the request as a result of queryhit.
	 *  (non-Javadoc)
	 * @see com.pa2.client.PeerInterface#queryHit(java.lang.String, int, java.lang.String, java.lang.String, int)
	 * @param messageID - message to forward query.
	 * @param timetolive - time the query can be forwarded.
	 * @param fileName - file to be searched.
	 * @param leafNode - peer where the file is available.
	 * @param portNumber - port of peer where file available.
	 */
	@Override
	public void queryHit(String messageID, int timetolive, String fileName, String leafNode, int portNumber)
			throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		if (portNumber != 0 && timetolive != 0) {

			System.out.println("The file is found in nearest neighbour with port no :");
			System.out.println(portNumber);
			long end = System.currentTimeMillis();
			
			System.out.println("Do you want to download the file 1. yes 2. No? ");
			int userInput = Integer.parseInt(reader.readLine());
			int peerPortForDownload = 0;
			if (userInput == 1) {

				System.out.println("Enter the peer port from which you want to download the file:");
				peerPortForDownload = Integer.parseInt(reader.readLine());
				PeerInterface peerServer;
				try {
					//remote call to peer to download file from
					peerServer = (PeerInterface) Naming.lookup("peerServer/" + peerPortForDownload);
					// method to obtain the file.
					byte[] output = peerServer.retreive(fileName);
					System.out.println(this.peer.getPeerId());
					File outputFile = new File(this.peer.getDirectoryLocation() + "/" + fileName);

					FileOutputStream fos = new FileOutputStream(outputFile.getAbsolutePath());
					System.out.println("File successfully downloaded !!");
					
					timetolive = 0;
					fos.write(output);
					fos.close();

				} catch (NotBoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {

			}
		}

	}

	@Override
	public void getValueMethod(HashSet<Integer> portSet, String fileName) throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("File found in the below peer ports");
		for (Integer i : portSet) {
			this.PeerFoundList.add(i);
			System.out.println("Peer Port : " + i);
		}
		System.out.println("Do you want to download the file 1. yes 2. No? ");
		int userInput = Integer.parseInt(reader.readLine());
		System.out.println("From below peer list");
		for (Integer peerport : PeerFoundList) {

			System.out.println(peerport);
		}
		int peerPortForDownload = 0;
		if (userInput == 1) {

			System.out.println("Enter the peer port from which you want to download the file:");
			peerPortForDownload = Integer.parseInt(reader.readLine());
			PeerInterface peerServer;
			try {
				peerServer = (PeerInterface) Naming.lookup("peerServer/" + peerPortForDownload);
				byte[] output = peerServer.retreive(fileName);
				System.out.println(this.peer.getPeerId());
				File outputFile = new File(this.peer.getDirectoryLocation() + "/" + fileName);
				System.out.println(outputFile);
				FileOutputStream fos = new FileOutputStream(outputFile.getAbsolutePath());
				fos.write(output);
				fos.close();

			} catch (NotBoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
