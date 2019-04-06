package com.pa2.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import com.pa2.client.PeerInterface;
import com.pa2.rmiinterface.PeerDetail;
import com.pa2.rmiinterface.ServerInterface;
import com.pa2.superClient.NeighborPeers;

public class ServerImpl extends UnicastRemoteObject implements ServerInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6420522931723648289L;

	public ServerImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	private PeerDetail peerInfo;
	/**
	 * 
	 */
	private List<FileDetail> fileList = new ArrayList<FileDetail>();
	private List<PeerDetail> peerList = new ArrayList<PeerDetail>();
	ArrayList<String> processdMsgIds = new ArrayList<String>();
	ArrayList<SearchedPeerInfo> peerSearchedList = new ArrayList<>();
	List<Integer> portfilefoundList = new ArrayList<>();
	List<Integer> portfilefoundglobal = new ArrayList<>();
	HashSet<Integer> portSet = new HashSet<>();

	ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();

	/**
	 * This method registers the peer to server. It checks if the peer ID registered
	 * is unique if yes registers the peer or fails the registration.
	 * 
	 * @param peer           - peer id registered
	 * @param fileNames      - names of file in peer directory.
	 * @param isRegisterFile - boolean to check if peer is already registered.
	 */
	@Override
	public synchronized String registerPeer(PeerDetail peer, List<String> fileNames, boolean isRegisterFile) {
		boolean peerInfoUnique = false;
		String status = "Registration Success.";
		if (peerList.isEmpty()) {
			peerInfoUnique = true;
		} else if (!peerList.isEmpty() && !isRegisterFile) {
			for (PeerDetail peerInfo : peerList) {
				// compare if the peer is already present.
				String result = peerInfo.compareObjects(peer);
				if (!result.equals("Peer Unique")) {
					peerInfoUnique = false;
					status = "Registration Failure. Reason: " + result;
					break;
				}
				peerInfoUnique = true;
			}
		}
		if (peerInfoUnique) {
			System.out.println("Registering Peer " + peer.getPeerId());
			peerList.add(peer);
		}
		// checking for unique peer.
		if (peerInfoUnique || isRegisterFile) {
			for (String f : fileNames) {
				FileDetail info = new FileDetail();
				info.setPeer(peer);
				info.setFileName(f);
				fileList.add(info);
			}
			if (peerInfoUnique) {
				System.out.println("Successfully Registered peer " + peer.getPeerId());
			}
		}
		return status;
	}

	/**
	 * This method searches for file in its file list to check if it is available
	 * for downloading.
	 * 
	 * @param fileName - file name to be searched.
	 */
	@Override
	public synchronized List<PeerDetail> searchFile(String fileName) {
		List<PeerDetail> peers = new ArrayList<>();
		for (FileDetail f : fileList) {
			if (f.getFileName().equals(fileName)) {
				peers.add(f.getPeer());
			}
		}
		return peers;
	}

	/**
	 * This method lets you delete a file from peer.
	 * 
	 * @param peer     - PeerInfo object which is to be deleted
	 * @param fileName - file name to be deleted.
	 */
	@Override
	public synchronized String deleteFile(PeerDetail peer, String fileName) throws RemoteException {
		for (FileDetail f : fileList) {
			if (f.getPeer().equals(peer) && f.getFileName().equals(fileName)) {
				fileList.remove(f);
			}
		}
		return "Success";
	}

	/** (non-Javadoc)
	 * @see com.pa2.rmiinterface.ServerInterface#query(java.lang.String, int, java.lang.String, int, int)
	 * This method checks if the message is already being processed , gets the neighbour of the super-peer to forward 
	 * the query message to search the file .
	 * @param messageID - message to forward query.
	 * @param timetolive - time the query can be forwarded.
	 * @param fileName - file to be searched.
	 * @param fromPeerPort - peer from which request comes.
	 * @param superPeerPort - port of super peer from which the request is being forwarded.
	 */
	@Override
	public void query(String messageID, int timetolive, String fileName, int fromPeerPort, int superPeerPort)
			throws NotBoundException, NumberFormatException, IOException {
		List<PeerDetail> peerList = new ArrayList<>();
		ServerInterface serverInterface = null;
		int superPeerId = getportInfo(superPeerPort);
		ArrayList<NeighborPeers> templist = new ArrayList<>();
		// checks if the message is already processed.
		if (this.processdMsgIds.contains(messageID)) {

			System.out.println(" This message is already processed ");
		} else {
			processdMsgIds.add(messageID);
			//get the neighbours of the super-peer.
			templist = (ArrayList<NeighborPeers>) getneighbouringSuperpeers(neighborPeers, superPeerId);

			for (int i = 0; i < neighborPeers.size(); i++) {
				System.out.println("Sending request to " + neighborPeers.get(i).getPeerId() + " "
						+ neighborPeers.get(i).getPortno());

				System.out.println("Time to live " + timetolive);
				serverInterface = (ServerInterface) Naming.lookup("rmi/Server/" + neighborPeers.get(i).getPortno());
				// searching the files in the neighbouring peers.
				peerList = serverInterface.searchFile(fileName);
				System.out.println("File Searched");

				// if file is found peerlist is not empty.
				if (!peerList.isEmpty()) {

					for (PeerDetail peer : peerList) {
						String leafNode = "192.168.0.3";
						System.out.println("File found in peer with Port No: " + fromPeerPort);
						timetolive--;
						//if file is found we send back the query hit message.
						queryHit(messageID, timetolive, fileName, leafNode, peer.getPortNum(), fromPeerPort);

						//System.out.println(peer.getPeerId());

					}
				}
			}
		}
	}

	/**This method get the neighbouring peers from the config.properties 
	 * @param neighborPeers - List of nieghbouring peers.
	 * @param superPeerID - id of the super peer according to the portInfo.properties file.
	 * @return
	 */
	public List<NeighborPeers> getneighbouringSuperpeers(ArrayList<NeighborPeers> neighborPeers, int superPeerID) {

		String property = null;
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(
					"C:\\Users\\Sukhada Pande\\Documents\\workspace-sts-3.9.5.RELEASE\\Hierarchical_File_system\\src\\config.properties");
			System.out.println("Loading the config file");
			// load a properties file

			prop.load(input);
			property = "peerid." + superPeerID + ".neighbors";
			// get the property value and print it out
			String[] strNeighbors = prop.getProperty(property).split(",");
			for (int i = 0; i < strNeighbors.length; i++) {
				NeighborPeers tempPeer = new NeighborPeers();
				tempPeer.setPeerId(strNeighbors[i]);
				tempPeer.setIp(prop.getProperty(strNeighbors[i] + ".ip"));
				tempPeer.setPortno(Integer.parseInt(prop.getProperty(strNeighbors[i] + ".port")));
				neighborPeers.add(tempPeer);
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return neighborPeers;

	}

	/** (non-Javadoc)
	 * @see com.pa2.rmiinterface.ServerInterface#queryHit(java.lang.String, int, java.lang.String, java.lang.String, int, int)
	 * This method sends backs the request to the super-peer the request is coming from
	 * 
	 */
	@Override
	public void queryHit(String messageID, int timetolive, String fileName, String leafNode, int portNumber,
			int fromPeerPort) throws NotBoundException, NumberFormatException, IOException {

		// data is saved with messageid and upstream peer
		SearchedPeerInfo peerinfo = new SearchedPeerInfo();
		peerinfo.setMessageId(messageID);
		peerinfo.setUpstreamPeerID(fromPeerPort);

		portfilefoundList.add(portNumber);
		portSet.add(portNumber);

		peerSearchedList.add(peerinfo);

		// calling remote method of the calling super-peer.
		PeerInterface peerServer = (PeerInterface) Naming.lookup("peerServer/" + fromPeerPort);

		System.out.println(timetolive);
		peerServer.queryHit(messageID, timetolive, fileName, leafNode, portNumber);

		// peerServer.getValueMethod(portSet,fileName);

	}

	public void transferMethod(HashSet<Integer> portSet) {

		// portfilefoundglobal.add(peerSearchedList);

	}

	/**
	 * This method lets you de-register your peer from the indexing server.
	 * 
	 * @param peer- PeerInfo object to remove it from the indexing server list.
	 * @throws IOException
	 */
	// @Override
	/*
	 * public synchronized String deRegisterPeer(PeerDetail peer) throws
	 * RemoteException{ peerList.remove(peer); for(FileDetail f: fileList) {
	 * if(f.getPeer().equals(peer)) fileList.remove(f); } return "Success"; }
	 */

	/**This method returns the peer id associated with the port number to find its neighbouring super-peers
	 * @param superPeerPort - super-peer port
	 * @return
	 * @throws IOException
	 */
	public int getportInfo(int superPeerPort) throws IOException {

		InputStream input = null;
		String property = null;
		Properties prop = new Properties();
		int superPeerId;

		input = new FileInputStream(
				"C:\\Users\\Sukhada Pande\\Documents\\workspace-sts-3.9.5.RELEASE\\Hierarchical_File_system\\src\\portInfo.properties");
		System.out.println("Loading the config file");

		prop.load(input);
		property = "peerid." + superPeerPort + ".port";
		superPeerId = Integer.parseInt(prop.getProperty(property));
		//System.out.println("Super Id is " + superPeerId);

		return superPeerId;

	}
	/**
	 * This method is used to send query in linear topology
	 */
	
	public void querylineartopology(String messageID, int timetolive, String fileName, int fromPeerPort, int superPeerPort)
			throws NotBoundException, NumberFormatException, IOException {
		List<PeerDetail> peerList = new ArrayList<>();
		ServerInterface serverInterface = null;
		int superPeerId = getportInfo(superPeerPort);
		ArrayList<NeighborPeers> templist = new ArrayList<>();
		
		int neighbourport= 0;
		// checks if the message is already processed.
		if (this.processdMsgIds.contains(messageID)) {

			System.out.println(" This message is already processed ");
		} else {
			processdMsgIds.add(messageID);
			//get the neighbours of the super-peer.
			
			
			do {
				
				neighbourport = getneighbourpeer(superPeerId);
				
				serverInterface = (ServerInterface) Naming.lookup("rmi/Server/" + neighbourport);
				// searching the files in the neighbouring peers.
				peerList = serverInterface.searchFile(fileName);
				System.out.println("File Searched");
				for(PeerDetail peer:peerList ) {
					System.out.println("file found in" + peer.getPortNum());
				}
				
				long end = System.currentTimeMillis();
				System.out.println("end time" + end);
			}while (peerList.isEmpty());
		}
	}
	
	
	/**
	 * Get neighnours in linear topology.
	 * @param superPeerID
	 * @return
	 */
	public int getneighbourpeer( int superPeerID) {
		System.out.println(superPeerID);
		
		String property = null;
		Properties prop = new Properties();
		InputStream input = null;
		int nextneighbour=0;

		try {

			input = new FileInputStream(
					"C:\\Users\\Sukhada Pande\\Documents\\workspace-sts-3.9.5.RELEASE\\Hierarchical_File_system\\src\\config.properties");
			System.out.println("Loading the config file");
			// load a properties file

			prop.load(input);
			property = "peerid." + superPeerID + ".neighbor";
			nextneighbour=Integer.parseInt(prop.getProperty(property + ".port"));
			System.out.println("Neigbour port:" +nextneighbour);
		
	}catch (IOException ex) {
		ex.printStackTrace();
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
		return nextneighbour;

}
}
