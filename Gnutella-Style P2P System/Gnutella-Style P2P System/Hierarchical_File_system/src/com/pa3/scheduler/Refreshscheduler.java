package com.pa3.scheduler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.pa3.rmiinterface.ServerInterface;
import com.pa3.superClient.NeighborPeers;

public class Refreshscheduler {

	public static void main(String[] args) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {

				try {
					getneighbouringSuperpeers();
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};

		Timer timer = new Timer();
		long delay = 6000;
		long intevalPeriod = 1 * 9000000;

		// schedules the task to be run in an interval
		timer.scheduleAtFixedRate(task, delay, intevalPeriod);

	} // end of main

	/**
	 * This method marks all downloaded file of number of peers TTR expired if the time to refresh has being exhausted.
	 * @throws NotBoundException
	 */
	
	public static void getneighbouringSuperpeers() throws NotBoundException {

		String property = null;
		Properties prop = new Properties();
		List<NeighborPeers> peerList = new ArrayList<>();
		InputStream input = null;
		ServerInterface serverInterface = null;
		try {

			input = new FileInputStream(
					"C:\\Users\\Sukhada Pande\\Documents\\workspace-sts-3.9.5.RELEASE\\Hierarchical_File_system\\src\\portInfo.properties");
			System.out.println("Loading the config file");
			// load a properties file

			prop.load(input);
			property = "peers.available";
			// get the property value and print it out
			String[] availablePeers = prop.getProperty(property).split(",");
			for (int i = 0; i < availablePeers.length; i++) {
				NeighborPeers tempPeer = new NeighborPeers();
				tempPeer.setPeerId(availablePeers[i]);
				tempPeer.setIp(prop.getProperty(availablePeers[i] + ".ip"));
				tempPeer.setPortno(Integer.parseInt(prop.getProperty(availablePeers[i] + ".port")));
				peerList.add(tempPeer);
			}

			for(NeighborPeers peer : peerList) {
				
				System.out.println(peer.getPortno());
				serverInterface =(ServerInterface) Naming.lookup("rmi/Server/" + peer.getPortno());
				//checking downloaded files if they have TTR exhuasted and marking them TTR expired.
				serverInterface.checkDownloadedList();
				
				
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
		

	}
}
