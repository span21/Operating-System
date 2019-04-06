package com.pa3.client;

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

import com.pa3.rmiinterface.PeerDetail;
import com.pa3.rmiinterface.PeerTestDetails;
import com.pa3.server.FileDetail;
import com.pa3.server.ResultDetail;

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

	/**
	 * This method get the request as a result of queryhit. (non-Javadoc)
	 * 
	 * @see com.pa3.client.PeerInterface#queryHit(java.lang.String, int,
	 *      java.lang.String, java.lang.String, int)
	 * @param messageID  - message to forward query.
	 * @param timetolive - time the query can be forwarded.
	 * @param fileName   - file to be searched.
	 * @param leafNode   - peer where the file is available.
	 * @param portNumber - port of peer where file available.
	 */
	@Override
	public PeerDetail queryHit(String messageID, int timetolive, String fileName, String leafNode, int portNumber,
			List<PeerDetail> peerlist) throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean fileDownloadedsuccessfully = false;
		PeerDetail detail = null;

		if (peerlist.size() != 0 && timetolive != 0) {

			for (PeerDetail peer : peerlist) {

				System.out.println("The file is found in neighbours with port no :");
				System.out.println("Peer Port No: " + peer.getPortNum());
				System.out.println("Peer id :" + peer.getPeerId());

			}
			
			

			System.out.println("Do you want to download the file 1. yes 2. No? ");
			int userInput = Integer.parseInt(reader.readLine());
			int peerPortForDownload = 0;
			if (userInput == 1) {

				System.out.println("Enter the peer port from which you want to download the file:");
				peerPortForDownload = Integer.parseInt(reader.readLine());

				for (PeerDetail peer : peerlist) {

					if (peerPortForDownload == peer.getPortNum()) {

						detail = new PeerDetail(peer.getPeerId(), peer.getPortNum(), peer.getDirectoryLocation(),
								peer.getSuperPeerPort());

					}

				}

				PeerInterface peerServer;
				try {
					// remote call to peer to download file from
					peerServer = (PeerInterface) Naming.lookup("peerServer/" + peerPortForDownload);
					// method to obtain the file.
					byte[] output = peerServer.retreive(fileName);
					
					File outputFile = new File(this.peer.getDirectoryLocation() + "/" + fileName);

					FileOutputStream fos = new FileOutputStream(outputFile.getAbsolutePath());
					System.out.println("File successfully downloaded !!");
					
					fileDownloadedsuccessfully = true;
					timetolive = 0;
					fos.write(output);
					fos.close();
					System.out.println("Returing peer");

				} catch (NotBoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {

			}
		}
		return detail;

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

	
	/* (non-Javadoc)
	 * @see com.pa3.client.PeerInterface#fileoutOfOutInfo(com.pa3.server.FileDetail)
	 * This method pings the peer that the file it has downloaded is invalid and gives an option to download
	 */
	/*--------- start change ----------*/
	public void fileoutOfOutInfo(FileDetail file) throws NumberFormatException, IOException, NotBoundException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("File : " + file.getFileName() + " is out of date");
		System.out.println(" Do you want to download a new verison? 1. yes 2. No");
		int input = Integer.parseInt(reader.readLine());
		PeerInterface peerServer=null;
		if(input ==1) {
			System.out.println(file.getOriginalPeerInfo().getPortNum());
			peerServer = (PeerInterface) Naming.lookup("peerServer/" + file.getOriginalPeerInfo().getPortNum());
			// method to obtain the file.
			byte[] output = peerServer.retreive(file.getFileName());
			//System.out.println(this.peer.getPeerId());
			File outputFile = new File(this.peer.getDirectoryLocation() + "/" + file.getFileName());

			FileOutputStream fos = new FileOutputStream(outputFile.getAbsolutePath());
			System.out.println("File successfully downloaded !!");
			
			
			fos.write(output);
			fos.close();
			System.out.println("Returing peer");
			
			
		}
		
		/*--------- end change ----------*/
		
	}
}
