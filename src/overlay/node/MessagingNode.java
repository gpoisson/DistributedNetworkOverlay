package overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import overlay.transport.TCPReceiverThread;
import overlay.transport.TCPSender;
import overlay.transport.TCPServerThread;
import overlay.wireformats.Deregister;
import overlay.wireformats.PayloadMessage;
import overlay.wireformats.Register;

public class MessagingNode extends Node {
	
	public Socket socket;			// Socket used for transmitting data
	public TCPSender sender;		// Marshalling of messages into byte[] prior to transmission
	public Thread receiver;			// Receiver thread listens for incoming connections 
	public Thread server;
	public ArrayList<Integer> mNodeIDs;
	public ArrayList<Socket> mNodeSockets;
	
	public MessagingNode() {
		if (debug) System.out.println("Building messaging node...");
		neighbors = new ArrayList<NodeReference>();
		mNodeSockets = new ArrayList<Socket>();
		mNodeIDs = new ArrayList<Integer>();
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
			System.exit(0);
		}
		
		try {
			MessagingNode mn = new MessagingNode();
			mn.hostname = args[0];
			mn.portNumber = Integer.parseInt(args[1]);
			
			mn.serverThread = new TCPServerThread(mn, mn.mNodeSockets, 0, mn.debug);
			mn.server = new Thread(mn.serverThread);
			mn.server.start();
			
			if (mn.debug) System.out.println(" Attempting to connect to registry via port number " + mn.portNumber);
			mn.socket = new Socket(mn.hostname, mn.portNumber);
			if (mn.debug) System.out.println(" Connection successfully established. Preparing to send registration request...");
			mn.sender = new TCPSender(mn.socket, mn.debug);
			mn.receiver = new Thread(new TCPReceiverThread(mn, mn.sender, mn.socket, mn.debug));
			mn.receiver.start();
			mn.register();
			if (mn.debug) System.out.println("Messaging node built.");
			
			Scanner scanner = new Scanner(System.in); 
			while (scanner.hasNext()) {
				String[] input = scanner.nextLine().split(" ");			// Prepare user input for parsing
				if (input.equals("quit")) {								// Not part of assignment -- just used for graceful exit during debugging
					if (mn.debug) System.out.println("Shutting down Messaging node.");
					scanner.close();
					System.exit(0);
				}
				else if (input[0].equals("print-shortest-path")) {
					// Print shortest path to all other messaging nodes
					if (mn.debug) System.out.println("Shortest path to all other nodes:");
					for (int neighborNode = 0; neighborNode < mn.routingCache.dijkstraNodes.size(); neighborNode++){
						if (mn.routingCache.dijkstraNodes.get(neighborNode).id == mn.id)
							System.out.println("--THIS NODE--");
						System.out.println(mn.routingCache.dijkstraNodes.get(neighborNode).toString());
					}
				}
				else if (input[0].equals("exit-overlay")) {
					// Deregister and shut down
					mn.deregister();
				}
			}
			
		} catch (IOException ioe) {
			System.out.println(ioe);
		} catch (NumberFormatException nfe) {
			System.out.println(nfe);
		}
	}
	
	// Register with the Registry Node
	private void register() {
		Register registerMessage = new Register(this.socket.getInetAddress().toString(), this.socket.getPort(), this.serverPortNumber);
		try {
			if (debug) System.out.println(" Transmitting registration request...");
			sender.sendData(registerMessage.getByteArray());
			if (debug) System.out.println(" Registration request sent. Awaiting registration response...");
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
	// Deregister from Registry Node
	private void deregister() {
		Deregister deregisterMessage = new Deregister(socket.getLocalAddress().toString(), socket.getPort());
		try {
			if (debug) System.out.println(" Transmitting deregistration request...");
			sender.sendData(deregisterMessage.getByteArray());
			if (debug) System.out.println(" Deregister request sent.");
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
	public void makeSockets(){
		if (debug) System.out.println("Making sockets to connect to other messaging nodes...");
		for (int neighbor = 0; neighbor < neighbors.size(); neighbor++){
			try {
				if (debug) System.out.println(" Establishing a socket to node " + neighbors.get(neighbor).getId() + ": " + neighbors.get(neighbor).getIP() + "  " + neighbors.get(neighbor).getPublicPort());
				Socket s = new Socket(neighbors.get(neighbor).getIP(), neighbors.get(neighbor).getPublicPort());
				mNodeSockets.add(s);
				mNodeIDs.add(neighbors.get(neighbor).getId());
			} catch (IOException ioe) {
				System.out.println(ioe);
			}
		}
		if (debug) System.out.println("Made " + mNodeSockets.size() + " sockets.");
		if (debug) System.out.println("Kept " + mNodeIDs.size() + " socket IDs.");
	}
		
	@Override
	public void transmitMessages(int numRounds) {
		makeSockets();
		// For each round, choose a random external node
		// Find routing plan in routing cache, encode into message
		// Generate random payload and transmit
		int messagesPerRound = 5;
		Random rand = new Random();
		
		for (int round = 0; round < numRounds; round++){
			// Randomly choose a node to send to
			int randomIndex = rand.nextInt(routingCache.dijkstraNodes.size());
			int randomNodeID = routingCache.dijkstraNodes.get(randomIndex).id;
			// Don't send messages to self
			while (randomNodeID == this.id){
				randomIndex = rand.nextInt(routingCache.dijkstraNodes.size());
				randomNodeID = routingCache.dijkstraNodes.get(randomIndex).id;			
			}
			String randomNodePath = routingCache.dijkstraNodes.get(randomIndex).path;
			
			if (debug) System.out.println(" Randomly picked node " + randomNodeID + " as destination...");
			for (int msg = 0; msg < messagesPerRound; msg++){
				PayloadMessage pMsg = new PayloadMessage();
				pMsg.encodeTransmissionPath(randomNodePath);
				pMsg.getByteArray();
				if (debug) System.out.println("   Payload message assembled and path encoded. Preparing to transmit...");
				if (debug) System.out.println("      Payload message contents: " + pMsg.toString());
				handlePayloadMessage(pMsg);
			}
		}
		
		// Send TASK_COMPLETE to registry
	}

	private void handlePayloadMessage(PayloadMessage pMsg) {
		if (debug) System.out.println(" Handling payload message...");
		Socket relay;																// Reference to the socket to the next node in the path
		String[] msgFields = pMsg.toString().split(" ");
		int startPath = 3;															// Index of first element of the path
		int endPath = 0;															// Need to find last element of path
		for (int word_index = 2; word_index < msgFields.length; word_index++){
			if (msgFields[word_index].contains("<path")){
				endPath = word_index;
			}
		}
		int pathLength = endPath - startPath;
		assert (pathLength > 0);													// Path length must be positive or there has been some error
		int[] path = new int[pathLength];
		for (int step = startPath; step < endPath; step++){
			path[step - startPath] = Integer.parseInt(msgFields[step]);
		}
		boolean isDestination = false;
		if (path[path.length-1] == this.id) { isDestination = true; }				// Determine if this is the message's final stop
		if (!isDestination){
			if (debug) System.out.println("  Not destination for this message, passing it on...");
			int next = 0;
			for (int step = 0; step < path.length; step++){							// Determine where to relay the message to
				if (path[step] == this.id) { next = path[step + 1]; }
				if (debug) {
					System.out.println("  Next step in message path: " + next);
					String p = "";
					for (int s = 0; s < path.length; s++){
						p += path[s] + " ";
					}
					System.out.println("  Full path: " + p);
				}
				for (int node = 0; node < mNodeIDs.size(); node++){
					if (mNodeIDs.get(node) == next){
						relay = mNodeSockets.get(node + 1);
						try {
							if (debug) System.out.println("\t  Sending <" + pMsg.toString() + "> to node " + path[step]);
							TCPSender sender = new TCPSender(relay, this.debug);
							sender.sendData(pMsg.getByteArray());
							relayTracker++;
						} catch (IOException ioe) {
							System.out.println(ioe);
						}
						break;
					}
				}
			}
		}
		else if (isDestination){
			if (debug) System.out.println("  Received payload message at the end of its path. Preparing to process.");
		}
	}

	public static void usage() {
		System.out.println("Usage: MessagingNode <hostname> <port number>");
	}

}
