package overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import overlay.transport.TCPReceiverThread;
import overlay.transport.TCPSender;
import overlay.transport.TCPServerThread;
import overlay.util.PayloadMessageHandler;
import overlay.wireformats.Deregister;
import overlay.wireformats.PayloadMessage;
import overlay.wireformats.Register;
import overlay.wireformats.TaskComplete;

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
				TCPSender sender = new TCPSender(s, debug);
				mNodeSockets.add(s);
				mNodeIDs.add(neighbors.get(neighbor).getId());
				if (debug) System.out.println(" Establishing TCPReceiver to listen for node " + neighbors.get(neighbor).getId());
				Thread t = new Thread(new TCPReceiverThread(this, sender, s, debug));
				t.start();
				neighbors.get(neighbor).receivers.add(receiver);
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
		PayloadMessageHandler handler = new PayloadMessageHandler(this, debug);
		
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
				handler.transmit(pMsg);
				sendTracker++;
				if (debug) System.out.println("  Message sent. This node has sent " + sendTracker + " messages.");
			}
		}
		
		// Send TASK_COMPLETE to registry
		TaskComplete tcMsg = new TaskComplete();
		try {
			sender.sendData(tcMsg.getByteArray());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void usage() {
		System.out.println("Usage: MessagingNode <hostname> <port number>");
	}

}
