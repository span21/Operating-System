package com.pa3.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import com.pa3.client.PeerInterface;
import com.pa3.rmiinterface.PeerDetail;
import com.pa3.rmiinterface.ServerInterface;
import com.pa3.superClient.NeighborPeers;

/**
 * @author Sukhada Pande
 *
 */
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
	private List<FileDetail> downloadedList = new ArrayList<>(); // downloaded file list
	private List<FileDetail> mainFileList = new ArrayList<>(); // main file list
	private List<PeerDetail> peerList = new ArrayList<PeerDetail>();
	ArrayList<String> processdMsgIds = new ArrayList<String>();
	ArrayList<SearchedPeerInfo> peerSearchedList = new ArrayList<>();
	List<Integer> portfilefoundList = new ArrayList<>();
	List<Integer> portfilefoundglobal = new ArrayList<>();
	HashSet<Integer> portSet = new HashSet<>();

	ArrayList<String> pushprocessdMsgIds = new ArrayList<String>();
	// ArrayList<PeerDetail> finallist = new ArrayList<>();

	

	/**
	 * This method registers the peer to server. It checks if the peer ID registered
	 * is unique if yes registers the peer or fails the registration.
	 * 
	 * @param peer           - peer id registered
	 * @param fileNames      - names of file in peer directory.
	 * @param isRegisterFile - boolean to check if peer is already registered.
	 */
	@Override
	public synchronized String registerPeer(PeerDetail peer, List<String> fileNames, boolean isRegisterFile,
			int superPeerPort) {
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
				info.setOriginalPeerInfo(peer);
				info.setOriginalSuperPeerPort(superPeerPort);
				info.setFileName(f);
				info.setFileVersion(1);
				info.setStatus("VALID");
				fileList.add(info);
			}
			mainFileList.addAll(fileList);
			if (peerInfoUnique) {
				System.out.println("Successfully Registered peer " + peer.getPeerId());
			}
		}
		return status;
	}
	
	/**
	 * Add downloaded file to the super-peer
	 */

	public synchronized String addDownloadedFile(PeerDetail peer, PeerDetail currentPeerInfo, List<String> filename,
			boolean isRegisterFile) {
		String status = "Registration Success.";
		if (isRegisterFile) {
			for (String f : filename) {
				FileDetail info = new FileDetail();
				info.setPeer(currentPeerInfo);
				info.setOriginalPeerInfo(peer);
				info.setFileName(f);
				info.setFileVersion(1);
				info.setStatus("VALID");
				info.setOriginalSuperPeerPort(peer.getSuperPeerPort());
				info.setDownloadedTime(System.nanoTime());
				info.setTimeToRefresh(120000);
				downloadedList.add(info);
			}
			System.out.println(" Downloaded " + filename + " File added successfully");
			mainFileList.addAll(downloadedList);

		}

		return status;

	}

	/*
	 * (non-Javadoc)
	 * This method helps modify the existing files .
	 * @see
	 * com.pa2.rmiinterface.ServerInterface#modifyExistingFile(java.lang.String,
	 * com.pa2.rmiinterface.PeerDetail, boolean) This method invalides the existing
	 * file and make the new file valid.
	 */
	public FileDetail modifyExistingFile(String filename, PeerDetail peer, boolean isModifiyfile, int superPeerPort)
			throws IOException, NotBoundException {

		List<PeerDetail> peers = new ArrayList<>();
		List<FileDetail> deletemodifiedFilelist = new ArrayList<>();
		List<FileDetail> tempList = new ArrayList<>();
		int searchCounter = 0;
		boolean isfileupdated = false;
		boolean fileOwnedbyServer = false;
		boolean filefoundinsuperPeers = false;
		boolean iffilemodified = false;

		FileDetail tempinfo = null;

		int tempVersion = 0;
		if (!peerList.isEmpty()) {

			peers = searchItsOwnedFiles(filename);
			if (!peers.isEmpty() && peers.size() != 0) {

				for (FileDetail f : fileList) {

					if (f.getFileName().equals(filename)) {
						fileOwnedbyServer = true;

						System.out.println("Status of existing file : " + f.getStatus());
						System.out.println("Version of existinf file :" + f.getFileVersion());
						f.setStatus("INVALID");
						tempinfo = new FileDetail();
						tempinfo.setFileName(f.getFileName());
						tempinfo.setFileVersion(f.getFileVersion() + 1);
						tempVersion = tempinfo.getFileVersion();
						tempinfo.setPeer(peer);
						tempinfo.setOriginalPeerInfo(peer);
						tempinfo.setStatus("VALID");
						tempList.add(tempinfo);
						// fileList.add(tempinfo);
						isfileupdated = true;
					}
					if (f.getStatus().equalsIgnoreCase("INVALID") && fileOwnedbyServer) {
						deletemodifiedFilelist.add(f);

					}
				}

				/*
				 * if (fileOwnedbyServer) {
				 * 
				 * searchCounter++; String msgFormat = Integer.toString(peer.getPeerId()); //
				 * message id for propogating the message to neighbouring peers. String msgId =
				 * msgFormat + searchCounter; filefoundinsuperPeers = invalidate(msgId,
				 * superPeerPort, filename, tempVersion); }
				 */

			} else {

				fileOwnedbyServer = false;
				isfileupdated = false;
			}

		} else {

			System.out.println(" No files registered");
		}

		if (tempList.size() != 0 && !tempList.isEmpty()) {

			mainFileList.addAll(tempList);
			System.out.println("New file sucessfully added");

		}

		if (deletemodifiedile(filename, deletemodifiedFilelist)) {

			System.out.println("old file removed from the list");
		}

		if (filefoundinsuperPeers || isfileupdated && fileOwnedbyServer) {

			iffilemodified = true;
		}

		return tempinfo;
	}

	/**
	 * This method sends a Push request accross all the leaf-nodes to invalidate 
	 * updated file.
	 */
	public boolean sendapushrequest(String fileName, FileDetail details, int superPeerPort, PeerDetail peer)
			throws IOException, NotBoundException {

		int searchCounter = 0;
		boolean filefoundinsuperPeers = false;
		String msgFormat = Integer.toString(peer.getPeerId());
		String msgId = msgFormat + searchCounter;
		filefoundinsuperPeers = invalidate(msgId, superPeerPort, fileName, details.getFileVersion());

		return filefoundinsuperPeers;

	}
	/***
	 * This method sends pull request to check if any file downloaded in the peers have an updated version.
	 */

	public List<FileDetail> sendpullrequest(boolean isFile, String fileName)
			throws NotBoundException, NumberFormatException, IOException {
		ServerInterface serverInterface = null;
		List<FileDetail> tempList = new ArrayList<>();
		// List<String> fileNameList = new ArrayList<>();
		List<FileDetail> fileoutOfdateList = new ArrayList<>();
		boolean fileoutOfDate = false;

		for (FileDetail file : downloadedList) {

			if (file.isTimeToRefreshstatus()) {
				file.getOriginalSuperPeerPort();
				serverInterface = (ServerInterface) Naming.lookup("rmi/Server/" + file.getOriginalSuperPeerPort());
				fileoutOfDate = serverInterface.checkFileVersion(file.getFileName(), file.getFileVersion());
				if (fileoutOfDate) {

					tempList.add(file);

				}
			}
		}

		deleteFiles(tempList);
		System.out.println("File out of date removed from the list");
		return tempList;
		/*
		 * if(!fileoutOfdateList.isEmpty()) {
		 * 
		 * for(FileDetail f : fileoutOfdateList) {
		 * 
		 * 
		 * PeerInterface peerServer = (PeerInterface) Naming.lookup("peerServer/" +
		 * f.getPeer().getPortNum()); peerServer.fileoutOfOutInfo(f); }
		 * 
		 * }
		 */

	}

	/**
	 * Help peer send pull request throughout the network.
	 */
	public boolean peerPullRequest() throws NumberFormatException, NotBoundException, IOException {

		List<FileDetail> tempList = sendpullrequest(true, "");
		boolean successfullpullrequest = false;

		if (!tempList.isEmpty()) {

			for (FileDetail f : tempList) {

				PeerInterface peerServer = (PeerInterface) Naming.lookup("peerServer/" + f.getPeer().getPortNum());
				peerServer.fileoutOfOutInfo(f);
				successfullpullrequest = true;
			}

		}

		return successfullpullrequest;
	}

	public void deleteFiles(List<FileDetail> tempList) {

		for (FileDetail file : tempList) {

			downloadedList.remove(file);
			mainFileList.remove(file);

		}

	}

	/**
	 * Checks the version of the file downloaded by leaf-nodes.
	 */
	public boolean checkFileVersion(String fileName, int version) {

		boolean fileofDate = false;

		for (FileDetail f : mainFileList) {

			if (f.getFileName().equalsIgnoreCase(fileName) && f.getFileVersion() != version) {
				fileofDate = true;
				

			}

		}

		return fileofDate;
	}
	
	

	/**
	 * @param msgid
	 * @param superPeerport
	 * @param fileName
	 * @param versionNumber
	 * @return
	 * @throws IOException
	 * @throws NotBoundException
	 * This method sends invalidate message to all the peers , if a file gets modified.
	 */
	public boolean invalidate(String msgid, int superPeerport, String fileName, int versionNumber)
			throws IOException, NotBoundException {

		ArrayList<NeighborPeers> peerneighbour = new ArrayList<>();
		ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();
		ServerInterface serverInterface = null;
		boolean filedeleted = false;
		int counter = 0;

		if (this.processdMsgIds.contains(msgid)) {

			System.out.println(" This message is already processed ");
		} else {
			processdMsgIds.add(msgid);

			peerneighbour = (ArrayList<NeighborPeers>) getneighbouringSuperpeers(neighborPeers,
					getportInfo(superPeerport));

			for (int i = 0; i < peerneighbour.size(); i++) {
				System.out.println("Sending request to " + peerneighbour.get(i).getPeerId() + " "
						+ peerneighbour.get(i).getPortno());
				//sending invalidate message to neighbouring super-peers .
				serverInterface = (ServerInterface) Naming.lookup("rmi/Server/" + peerneighbour.get(i).getPortno());
				filedeleted = serverInterface.searchAndDelete(fileName, versionNumber);

				if (filedeleted) {
					counter++;
					System.out.println("File invalidated from server : " + peerneighbour.get(i).getPortno());

				}

			}

		}
		if (counter != 0) {

			filedeleted = true;

		}

		return filedeleted;

	}

	
	/* (non-Javadoc)
	 * @see com.pa3.rmiinterface.ServerInterface#searchAndDelete(java.lang.String, int)
	 * searches and deletes the file
	 */
	public boolean searchAndDelete(String fileName, int fileversion) throws RemoteException {

		boolean invalidateoldFile = false;
		List<FileDetail> deleteFilelist = new ArrayList<>();
		System.out.println(fileversion);
		// checks if file status is valid.
		for (FileDetail f : mainFileList) {
			if (f.getFileName().equals(fileName) && f.getStatus().equalsIgnoreCase("VALID")
					&& f.getFileVersion() != fileversion) {
				System.out.println(f.getFileName() + " is of old version :" + f.getFileVersion());

				f.setStatus("INVALID");
				invalidateoldFile = true;
				deleteFilelist.add(f);

			}

		}
		// invalidateoldFile = deletemodifiedile(fileName, deleteFilelist);

		return invalidateoldFile;
	}

	/**
	 * @param fileName
	 * @param deleteFilelist
	 * @return
	 * Deletes files if they are modified.
	 */
	public boolean deletemodifiedile(String fileName, List<FileDetail> deleteFilelist) {
		boolean deletedflag = false;

		for (FileDetail file : deleteFilelist) {

			mainFileList.remove(file); // mainfile list
			fileList.remove(file);
			deletedflag = true;
		}

		return deletedflag;
	}

	/**
	 * This method searches for file in its file list to check if it is available
	 * for downloading.
	 * 
	 * @param fileName - file name to be searched.
	 */

	/*
	 * public synchronized List<PeerDetail> searchFile(String fileName) {
	 * List<PeerDetail> peers = new ArrayList<>(); for (FileDetail f : fileList) {
	 * if (f.getFileName().equals(fileName)) { peers.add(f.getPeer()); } } return
	 * peers; }
	 */

	@Override
	public synchronized List<PeerDetail> searchFile(String fileName) {
		List<PeerDetail> peers = new ArrayList<>();
		for (FileDetail f : mainFileList) {
			if (f.getFileName().equals(fileName)) {
				peers.add(f.getPeer());

			}
		}
		return peers;
	}
	
	public List<ResultDetail> searchResultFile(String filename){
		
		List<ResultDetail> peers = new ArrayList<>();
		for (FileDetail f : mainFileList) {
			if (f.getFileName().equals(filename)) {
				ResultDetail r = new ResultDetail();
				r.setFile(f.getPeer());
				r.setStatus(f.getStatus());
				
				peers.add(r);
				

			}
		}
		return peers;
	}
	

	public List<PeerDetail> searchItsOwnedFiles(String fileName) {

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

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.pa3.rmiinterface.ServerInterface#query(java.lang.String, int,
	 *      java.lang.String, int, int) This method checks if the message is already
	 *      being processed , gets the neighbour of the super-peer to forward the
	 *      query message to search the file .
	 * @param messageID     - message to forward query.
	 * @param timetolive    - time the query can be forwarded.
	 * @param fileName      - file to be searched.
	 * @param fromPeerPort  - peer from which request comes.
	 * @param superPeerPort - port of super peer from which the request is being
	 *                      forwarded.
	 */
	@Override
	public PeerDetail query(String messageID, int timetolive, String fileName, int fromPeerPort, int superPeerPort)
			throws NotBoundException, NumberFormatException, IOException {
		List<PeerDetail> peerList =null;
		ServerInterface serverInterface = null;
		PeerDetail detail = null;
		int superPeerId = getportInfo(superPeerPort);
		ArrayList<NeighborPeers> templist = new ArrayList<>();
		ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();
		
		boolean fileSearchedandFound = false;
		ArrayList<PeerDetail> finallist = new ArrayList<>();
		// checks if the message is already processed.
		if (this.processdMsgIds.contains(messageID)) {

			System.out.println(" This message is already processed ");
		} else {
			processdMsgIds.add(messageID);
			// get the neighbours of the super-peer.
			templist = (ArrayList<NeighborPeers>) getneighbouringSuperpeers(neighborPeers, superPeerId);
			String leafNode = "192.168.0.3";
			int peerport = 0;
			for (int i = 0; i < templist.size(); i++) {
				System.out.println(
						"Sending request to " + templist.get(i).getPeerId() + " " + templist.get(i).getPortno());

				System.out.println("Time to live " + timetolive);
				serverInterface = (ServerInterface) Naming.lookup("rmi/Server/" + templist.get(i).getPortno());
				// searching the files in the neighbouring peers.
				peerList = new ArrayList<>();
				peerList = serverInterface.searchFile(fileName);
				
				System.out.println("File Searched");

				// if file is found peerlist is not empty.
				if (!peerList.isEmpty()) {

					for (PeerDetail peer : peerList) {

						System.out.println("File found in peer with Port No: " + peer.getPortNum());
						timetolive--;
						peerport = peer.getPortNum();
						// if file is found we send back the query hit message.
						//

						// System.out.println(peer.getPeerId());

					}
					
					finallist.addAll(peerList);

				}
			}

			// viewPeerlist(finallist);
			detail = queryHit(messageID, timetolive, fileName, leafNode, peerport, fromPeerPort, finallist);
		}

		return detail;
	}

	public void viewPeerlist(String messageID, int timetolive, String fileName, String leafNode, int portNumber,
			int fromPeerPort, List<PeerDetail> peerlist) throws NumberFormatException, NotBoundException, IOException {

		System.out.println("List of peer where file is found: ");

		for (PeerDetail p : peerlist) {
			System.out.println(p.getPortNum());
		}

	}

	/**
	 * This method get the neighbouring peers from the config.properties
	 * 
	 * @param neighborPeers - List of nieghbouring peers.
	 * @param superPeerID   - id of the super peer according to the
	 *                      portInfo.properties file.
	 * @return
	 */
	public List<NeighborPeers> getneighbouringSuperpeers(ArrayList<NeighborPeers> neighborPeers, int superPeerID) {

		String property = null;
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(
					"C:\\Users\\Sukhada Pande\\Documents\\workspace-sts-3.9.5.RELEASE\\Hierarchical_File_system\\src\\config.properties");

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

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.pa3.rmiinterface.ServerInterface#queryHit(java.lang.String, int,
	 *      java.lang.String, java.lang.String, int, int) This method sends backs
	 *      the request to the super-peer the request is coming from
	 * 
	 */
	@Override
	public PeerDetail queryHit(String messageID, int timetolive, String fileName, String leafNode, int portNumber,
			int fromPeerPort, List<PeerDetail> peerlist) throws NotBoundException, NumberFormatException, IOException {

		boolean queryHit = false;
		// data is saved with messageid and upstream peer
		SearchedPeerInfo peerinfo = new SearchedPeerInfo();
		peerinfo.setMessageId(messageID);
		peerinfo.setUpstreamPeerID(fromPeerPort);

		// calling remote method of the calling super-peer.
		PeerInterface peerServer = (PeerInterface) Naming.lookup("peerServer/" + fromPeerPort);

		// queryHit = peerServer.queryHit(messageID, timetolive, fileName, leafNode,
		// portNumber, peerlist);

		// peerServer.getValueMethod(portSet,fileName);
		return peerServer.queryHit(messageID, timetolive, fileName, leafNode, portNumber, peerlist);

	}

	public void transferMethod(HashSet<Integer> portSet) {

		// portfilefoundglobal.add(peerSearchedList);

	}

	public void checkDownloadedList() throws NotBoundException, NumberFormatException, IOException {

		List<FileDetail> fileoutOfdateList = new ArrayList<>();

		if (downloadedList != null) {

			for (FileDetail file : downloadedList) {

				long startdate = file.getDownloadedTime();

				long timetorefresh = file.getTimeToRefresh();
				long endtime = startdate + timetorefresh;

				if (endtime < System.nanoTime() || endtime == System.nanoTime()) {

					file.setTimeToRefreshstatus(true);
					System.out
							.println(" Its time to refresh " + file.getFileName() + "of " + file.getPeer().getPeerId());
					fileoutOfdateList.add(file);

				}

			}

			/*
			 * if(!fileoutOfdateList.isEmpty()) {
			 * 
			 * for(FileDetail f : fileoutOfdateList) {
			 * 
			 * 
			 * PeerInterface peerServer = (PeerInterface) Naming.lookup("peerServer/" +
			 * f.getPeer().getPortNum()); peerServer.fileoutOfOutInfo(f); }
			 * 
			 * }
			 */

		}
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

	/**
	 * This method returns the peer id associated with the port number to find its
	 * neighbouring super-peers
	 * 
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
		// System.out.println("Super Id is " + superPeerId);

		return superPeerId;

	}

	/**
	 * This method is used to send query in linear topology
	 */

	public void querylineartopology(String messageID, int timetolive, String fileName, int fromPeerPort,
			int superPeerPort) throws NotBoundException, NumberFormatException, IOException {
		List<PeerDetail> peerList = new ArrayList<>();
		ServerInterface serverInterface = null;
		int superPeerId = getportInfo(superPeerPort);
		ArrayList<NeighborPeers> templist = new ArrayList<>();

		int neighbourport = 0;
		// checks if the message is already processed.
		if (this.processdMsgIds.contains(messageID)) {

			System.out.println(" This message is already processed ");
		} else {
			processdMsgIds.add(messageID);
			// get the neighbours of the super-peer.

			do {

				neighbourport = getneighbourpeer(superPeerId);

				serverInterface = (ServerInterface) Naming.lookup("rmi/Server/" + neighbourport);
				// searching the files in the neighbouring peers.
				peerList = serverInterface.searchFile(fileName);
				System.out.println("File Searched");
				for (PeerDetail peer : peerList) {
					System.out.println("file found in" + peer.getPortNum());
				}

				long end = System.currentTimeMillis();
				System.out.println("end time" + end);
			} while (peerList.isEmpty());
		}
	}

	/**
	 * Get neighnours in linear topology.
	 * 
	 * @param superPeerID
	 * @return
	 */
	public int getneighbourpeer(int superPeerID) {
		System.out.println(superPeerID);

		String property = null;
		Properties prop = new Properties();
		InputStream input = null;
		int nextneighbour = 0;

		try {

			input = new FileInputStream(
					"C:\\Users\\Sukhada Pande\\Documents\\workspace-sts-3.9.5.RELEASE\\Hierarchical_File_system\\src\\config.properties");
			System.out.println("Loading the config file");
			// load a properties file

			prop.load(input);
			property = "peerid." + superPeerID + ".neighbor";
			nextneighbour = Integer.parseInt(prop.getProperty(property + ".port"));
			System.out.println("Neigbour port:" + nextneighbour);

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

		return nextneighbour;

	}
}
