package overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import overlay.transport.TCPReceiverThread;
import overlay.transport.TCPSender;
import overlay.transport.TCPServerThread;

public class Registry extends Node {

	public Thread server;
	public TCPSender sender;
	public Thread receiver;
	public ArrayList<Socket> messagingNodes;
	
	public Registry() {
		if (debug) System.out.println("Building registry node...");
		
		// Registry is 'node 0'
		id = 0;
		
		// Storage for sockets used to connect to Messaging Nodes
		messagingNodes = new ArrayList<Socket>();
	}
	
	public static void main(String[] args) {
		Registry reg = new Registry();
		
		// Spawn a server thread to listen for incoming connections and add them to messagingNodes
		reg.server = new Thread(new TCPServerThread(reg.messagingNodes, reg.portNumber, reg.debug));
		reg.server.start();
		
		if (reg.debug) System.out.println("Registry node built. Awaiting user input.");
		
		// Wait for user input on main thread
		Scanner scanner = new Scanner(System.in); 
		while (scanner.hasNext()) {
			String[] input = scanner.nextLine().split(" ");			// Prepare user input for parsing
			if (input[0].equals("quit")) {								// Not part of assignment -- just used for graceful exit during debugging
				if (reg.debug) System.out.println("Shutting down Registry node.");
				scanner.close();
				System.exit(0);
			}
			else if (input[0].equals("list-messaging") && input[1].equals("nodes")) {			// "list-messaging nodes"
				// Print information about all messaging nodes on separate lines  (hostname, port number)
				if (reg.debug) System.out.println("Messaging Node manifest:");
			}
			else if (input[0].equals("list-weights")) {				
				// List information about links composing the overlay
				if (reg.debug) System.out.println("Node link manifest:");
			}
			else if (input[0].equals("setup-overlay")) {			
				// Set up the overlay; each messaging node gets <numConnections> links
				int numConnections = Integer.parseInt(input[1]);
				if (reg.debug) System.out.println("Setting up overlay with " + numConnections + " links between nodes...");
			}
			else if (input[0].equals("send-overlay-link-weights")) {		
				// Send a Link_Weights message to all registered nodes
				if (reg.debug) System.out.println("Sending Link_Weights message to all registered nodes...");
			}
			else if (input[0].equals("start")) {
				// Nodes exchange <numRounds> messages
				int numRounds = Integer.parseInt(input[1]);
				if (reg.debug) System.out.println("Directing all nodes to send " + numRounds + " messages to each other...");
			}
		}
	}
}
