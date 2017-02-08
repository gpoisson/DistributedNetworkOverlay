package overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import overlay.transport.TCPReceiverThread;
import overlay.transport.TCPSender;
import overlay.transport.TCPServerThread;
import overlay.util.OverlayCreator;
import overlay.wireformats.Message;
import overlay.wireformats.MessagingNodesList;

public class Registry extends Node {

	public Thread server;
	public TCPServerThread serverThread;
	public TCPSender sender;
	public Thread receiver;
	public ArrayList<Socket> messagingNodes;
	public ArrayList<NodeReference> nodeRefs;
	private int uniqueNodeId = 1;		// This values is only ever incremented, never decremented, to ensure ID numbers are unique.
	private OverlayCreator overlayCreator;
	public MessagingNodesList mnList;
	
	public Registry() {
		if (debug) System.out.println("Building registry node...");
		
		// Registry is 'node 0'
		id = 0;
		
		// Storage for sockets used to connect to Messaging Nodes
		messagingNodes = new ArrayList<Socket>();
		
		// Storage for node data
		nodeRefs = new ArrayList<NodeReference>();
		
		// Maintain the Messaging Nodes List
		mnList = new MessagingNodesList(nodeRefs);
	}
	
	@Override
	public void register(String[] msgFields) {
		if (debug) System.out.println(" Registering new node: ");
		NodeReference newNode = new NodeReference();
		newNode.setId(uniqueNodeId);
		newNode.setIP(msgFields[2]);
		newNode.setLocalPort(Integer.parseInt(msgFields[3]));
		newNode.setPublicPort(this.portNumber);
		uniqueNodeId++;
		if (debug) System.out.println("   Incoming node IP: " + newNode.getIP());
		if (debug) System.out.println("   Incoming node local port: " + newNode.getLocalPort());
		if (debug) System.out.println("   Incoming node public port: " + newNode.getPublicPort());
		if (debug) System.out.println("   Incoming node ID: " + newNode.getId());
		nodeRefs.add(newNode);
		if (debug) System.out.println(" There are now " + nodeRefs.size() + " nodes registered.");
	}
	
	@Override
	public void deregister(String[] msgFields) {
		int nodeId = Integer.parseInt(msgFields[0]);
		if (debug) System.out.println(" Deregistering node: " + nodeId);
		for (int i = 0; i < nodeRefs.size(); i++) {
			if (nodeRefs.get(i).getId() == nodeId) {
				nodeRefs.remove(i);
				if (debug) System.out.println(" Deregistration successful.");
			}
		}
	}
	
	public static void main(String[] args) {
		Registry reg = new Registry();
		
		// Spawn a server thread to listen for incoming connections and add them to messagingNodes
		reg.serverThread = new TCPServerThread(reg, reg.messagingNodes, reg.portNumber, reg.debug);
		reg.server = new Thread(reg.serverThread);
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
				System.out.println(reg.mnList.toString());
			}
			else if (input[0].equals("list-weights")) {				
				// List information about links composing the overlay
				if (reg.debug) System.out.println("Node link manifest:");
				System.out.println(reg.mnList.toString());
			}
			else if (input[0].equals("setup-overlay")) {			
				// Set up the overlay; each messaging node gets <numConnections> links
				// Executed before the "send-overlay-link-weights" command
				int numConnections = 4;
				if (input.length > 1) {
					numConnections = Integer.parseInt(input[1]); 
				}
				reg.serverThread.shutDown();
				if (reg.debug) System.out.println("Setting up overlay with " + numConnections + " links between nodes...");
				reg.overlayCreator = new OverlayCreator(reg.mnList, numConnections, reg.debug);
				reg.overlayCreator.buildOverlay();
			}
			else if (input[0].equals("send-overlay-link-weights")) {		
				// Send a Link_Weights message to all registered nodes
				// Executed after the "setup-overlay" command
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
