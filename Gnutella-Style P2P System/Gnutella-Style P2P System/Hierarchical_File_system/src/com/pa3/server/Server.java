package com.pa3.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

import com.pa3.rmiinterface.ServerInterface;
import com.pa3.superClient.NeighborPeers;

public class Server {
	

	//main method server to start.
	public static void main(String args[]) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();
		System.out.println("Enter the port number to bind the Server:");
		try {
			/*System.setProperty("rmi.server.codebase",
					"file:/C:/Users/Sukhada Pande/Documents/workspace-sts-3.9.5.RELEASE/Decentralised_File_system/bin/");*/
			int serverPortNum = Integer.parseInt(reader.readLine());
			LocateRegistry.createRegistry(serverPortNum);
			ServerInterface server = new ServerImpl();
			String rmiRebind = "rmi/Server/" + serverPortNum;
			Naming.rebind(rmiRebind, server);
			System.out.println("Server is ready!");

		} catch (IOException | NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
