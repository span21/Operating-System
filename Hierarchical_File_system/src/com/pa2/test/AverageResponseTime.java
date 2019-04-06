package com.pa2.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import com.pa2.client.Peer;
import com.pa2.client.PeerImpl;
import com.pa2.client.PeerInterface;
import com.pa2.rmiinterface.PeerDetail;
import com.pa2.rmiinterface.ServerInterface;

/**
 * This class is a test to check the average response time of a peer when
 * multiple peers are placing requests.This also tests the time taken for 1000
 * sequential request from a peer to another peer
 * 
 * 
 *
 */
public class AverageResponseTime implements Runnable {
	private static ServerInterface serverInterface;

	private static PeerDetail client1;
	private static PeerDetail client2;
	private static PeerDetail client3;
	private static PeerDetail client4;
	private Peer peerTest;

	public AverageResponseTime(Peer peerTest) {
		this.peerTest = peerTest;
	}

	public static void main(String args[]) {
		
		client1 = new PeerDetail(1, 1234, "C://IIT/TEST/peerTest1");
		
		try {
			serverInterface = (ServerInterface) Naming.lookup("rmi/Server/"+ 8891);
			Peer peerTest1 = new Peer(client1);

			averageResponse(peerTest1);
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void averageResponse(Peer peerTest) {
		int responseTime = 0;
		for (int i = 0; i < 150; i++) {
			long start = System.currentTimeMillis();
			System.out.println("Start time " + start);

			try {
				
				String msgid = "1" + i;
				peerTest.sendQuery(serverInterface, msgid, 2, "TestFilePeer2_1", 1234, 8891);
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// peer id 2 and 3 are trying to connect to peer1

			long endTime = System.currentTimeMillis();
			System.out.println("Start time " + endTime);
			responseTime += (endTime - start);
			int result = responseTime / 150;
			System.out.println("Response time is " + result);
			System.out.println("Response time " + responseTime);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
