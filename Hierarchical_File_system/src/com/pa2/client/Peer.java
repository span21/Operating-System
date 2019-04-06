package com.pa2.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.pa2.rmiinterface.PeerDetail;
import com.pa2.rmiinterface.ServerInterface;
import com.pa2.superClient.NeighborPeers;

public class Peer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 440391339428115028L;
	private PeerDetail peerInfo;
	ArrayList<PeerDetail> peerDetailList = new ArrayList<>();

	public Peer(PeerDetail peerInfo) {
		this.peerInfo = peerInfo;
	}

	public PeerDetail getPeerInfo() {
		return peerInfo;
	}

	public void setPeerInfo(PeerDetail peerInfo) {
		this.peerInfo = peerInfo;
	}

	ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();
	List<Thread> threadInstancesList = new ArrayList<Thread>();
	HashMap<Integer, PeerDetail> peerInfomap = new HashMap<>();
	HashMap<Integer, PeerDetail> peertempmap = null;

	/**
	 * This method initiates the logic to search file , it first locally searches
	 * for the file and if not available it send query message to the neighbouring
	 * peer for searching the file in their register peers.
	 * 
	 * @param serverInterface - server interface to which peer is connected to.
	 * @param frompeerId      - peer id from which request is initiated.
	 * @param superPeerPort   - port number of the super peer , the requesting peer
	 *                        is connected.
	 * @param fromPeerPort    - peer port number from which request is initiated.
	 * @throws NotBoundException
	 */
	public void searchForFile(ServerInterface serverInterface, int frompeerId, int superPeerPort, int fromPeerPort)
			throws NotBoundException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			int searchCounter = 0;
			int userChoice = 0;
			ArrayList<Integer> peerList = null;
			ServerInterface serverTemp = null;
			List<PeerDetail> localPeerList = new ArrayList<>();
			int downloadFile = 0;
			int localPeerIdToDownload = 0;
			PeerDetail localpeerDownload = null;
			ArrayList<NeighborPeers> templist = new ArrayList<>();
			do {
				System.out.println("Peer " + this.peerInfo.getPeerId()
						+ " - Enter 1. Search for a File 2. Register a new file 3.Delete a file  4. Quit");
				userChoice = Integer.parseInt(reader.readLine());
				if (userChoice == 1) {
					System.out.println(
							"Peer " + this.peerInfo.getPeerId() + " - Enter the file name you want to search for:");
					String fileName = reader.readLine();

					// Searching file in its own Super-Peer.

					localPeerList = serverInterface.searchFile(fileName);

					if (!localPeerList.isEmpty() && localPeerList.size() != 0) {

						System.out.println("File is found in the respective local Super Peer on following peer ids");

						for (PeerDetail peerSearchResults : localPeerList) {

							System.out.println("Peer id " + peerSearchResults.getPeerId());
						}

						System.out.println("Do you want to download  the file 1.Yes 2.No");
						downloadFile = Integer.parseInt(reader.readLine());

						if (downloadFile == 1) {

							System.out.println("The file is locally available in the below peers:");

							System.out.println("Enter the Peer Id");
							localPeerIdToDownload = Integer.parseInt(reader.readLine());

							for (PeerDetail peerlocaldetails : localPeerList) {
								if (peerlocaldetails.getPeerId() == localPeerIdToDownload)
									localpeerDownload = peerlocaldetails;
							}

							// Download from the local super-peer.
							connectToPeerAndDownloadFile(localpeerDownload, fileName, serverInterface);
						}

					} else {

						long start = System.currentTimeMillis();
						

						// sequence number
						++searchCounter;
						String msgFormat = Integer.toString(frompeerId);
						// message id for propogating the message to neighbouring peers.
						String msgId = msgFormat + searchCounter;
						int timetoLive = 2;

						// Sending query message to server .
						sendQuery(serverInterface, msgId, timetoLive, fileName, fromPeerPort, superPeerPort);

					}
				} else if (userChoice == 2) {
					System.out.println("Peer " + this.peerInfo.getPeerId()
							+ " - Enter the file Name you want to register with server:");
					String fileName = reader.readLine();
					serverInterface.registerPeer(this.peerInfo, Arrays.asList(fileName), true);
				} else if (userChoice == 3) {
					System.out.println(
							"Peer " + this.peerInfo.getPeerId() + " - Enter the file Name you want to delete:");
					String fileName = reader.readLine();
					serverInterface.deleteFile(this.peerInfo, fileName);
				} else {
					System.out.println("Peer " + this.peerInfo.getPeerId() + " - Quitting");
					// serverInterface.deRegisterPeer(this.peerInfo);
					System.exit(0);
				}
			} while (userChoice != 4);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param serverInterface - server Interface to which the peer is registered to.
	 * @return
	 */
	public String registerPeer(ServerInterface serverInterface) {
		PeerDetail currentPeer = this.peerInfo;
		String result = null;
		try {
			List<String> fileNames = new ArrayList<>();
			// checking for the files in directory.
			if (Files.list(Paths.get(currentPeer.getDirectoryLocation())).findAny().isPresent()) {
				File[] files = new File(currentPeer.getDirectoryLocation()).listFiles();
				for (File file : files) {
					fileNames.add(file.getName());
				}
			}

			// registering the peer to server.
			result = serverInterface.registerPeer(currentPeer, fileNames, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * This method searches for files locally.
	 * 
	 * @param serverInterface - server interface the peer is registered to.
	 * @param fileName        - file to be searched.
	 * @param messageID       - message id for propogation
	 * @param timetolive      - time message lives.
	 * @param superPeerId     - id of super-peer peer is registered to.
	 * @return
	 * @throws MalformedURLException
	 * @throws NotBoundException
	 */
	public HashMap<Integer, PeerDetail> searchForFileinAllPeers(ServerInterface serverInterface, String fileName,
			String messageID, int timetolive, int superPeerId) throws MalformedURLException, NotBoundException {
		List<PeerDetail> peers = new ArrayList<>();
		HashMap<Integer, PeerDetail> peerInfo = new HashMap();

		try {

			peers = serverInterface.searchFile(fileName);

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (peers.contains(this.peerInfo)) {
			System.out.println("File already exists in your Directory!");
		} else if (peers.isEmpty()) {
			System.out.println("Sorry! File is not available in" + superPeerId);
		} else {
			System.out.println("Peer " + this.peerInfo.getPeerId()
					+ ".Here are the list of Peer Id's with the file you searched for!: ");
			for (int i = 0; i < peers.size(); i++) {
				System.out.print("Peer Id: " + peers.get(i).getPeerId() + " ");
			}
			System.out.println();
			peerInfo = peers.stream()
					.collect(Collectors.toMap(PeerDetail::getPeerId, Function.identity(), (o, n) -> o, HashMap::new));
		}
		return peerInfo;
	}

	/**
	 * This method is to download the file from the peer which acts as a server.
	 * 
	 * @param peer            - peer details from which the file is downloaded.
	 * @param fileName        - file to downloaded.
	 * @param serverInterface - server interface to which peer is registered.
	 */
	public void connectToPeerAndDownloadFile(PeerDetail peer, String fileName, ServerInterface serverInterface) {

		PeerInterface peerServer;
		try {
			peerServer = (PeerInterface) Naming.lookup("peerServer/" + peer.getPortNum());
			byte[] output = peerServer.retreive(fileName);
			File outputFile = new File(this.peerInfo.getDirectoryLocation() + "/" + fileName);
			FileOutputStream fos = new FileOutputStream(outputFile.getAbsolutePath());
			fos.write(output);
			fos.close();
			System.out.println("Peer " + this.peerInfo.getPeerId() + " - Successfully downloaded the file " + fileName
					+ " from peer" + peer.getPeerId());
			serverInterface.registerPeer(this.peerInfo, Arrays.asList(fileName), true);
		} catch (NotBoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * This method forward the query to the super-peer to which the peer is
	 * registered to.
	 * 
	 * @param serverInterface - server interface peer is registered to.
	 * @param msgId           - message id for query
	 * @param timeToLive      - time the request lives.
	 * @param fileName        - file name to be searched.
	 * @param frompeerId      - id of peer from which the request is coming.
	 * @param superPeerPort   - port of the super-peer the requesting peer is
	 *                        connected to.
	 * @throws NotBoundException
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void sendQuery(ServerInterface serverInterface, String msgId, int timeToLive, String fileName,
			int frompeerId, int superPeerPort) throws NotBoundException, NumberFormatException, IOException {

		serverInterface.query(msgId, timeToLive, fileName, frompeerId, superPeerPort);

	}

	// main method to start the peer.
	public static void main(String args[]) throws NotBoundException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String result = "";
		int peerId, peerPortNum, superPeerPort;
		String location;
		PeerDetail client;
		Peer peer;
		ServerInterface serverInterface = null;
		System.out.println("Hello Peer!");
		try {
			do {
				System.out.println("Enter your Peer ID:");
				peerId = Integer.parseInt(reader.readLine());
				System.out.println("Enter the port number to be used for communication:");
				peerPortNum = Integer.parseInt(reader.readLine());
				System.out.println("Enter your directory in file system:");
				location = reader.readLine();
				System.out.println("Enter the Super Peer you want to connect to");
				superPeerPort = Integer.parseInt(reader.readLine());

				client = new PeerDetail(peerId, peerPortNum, location);
				peer = new Peer(client);
				try {
					serverInterface = (ServerInterface) Naming.lookup("rmi/Server/" + superPeerPort);
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Registering..");
				result = peer.registerPeer(serverInterface);
				System.out.println(result);
			} while (result.contains("Registration Failure"));
			LocateRegistry.createRegistry(peerPortNum);
			PeerInterface peerInterface = new PeerImpl(client);
			Naming.rebind("peerServer/" + peerPortNum, peerInterface);
			peer.searchForFile(serverInterface, peerId, superPeerPort, peerPortNum);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
