package overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import overlay.transport.TCPReceiverThread;
import overlay.transport.TCPSender;
import overlay.wireformats.Deregister;
import overlay.wireformats.PayloadMessage;
import overlay.wireformats.Register;

public class MessagingNode extends Node {
	
	public Socket socket;			// Socket used for transmitting data
	public TCPSender sender;		// Marshalling of messages into byte[] prior to transmission
	public Thread receiver;			// Receiver thread listens for incoming connections 
	public ArrayList<NodeReference> neighbors;
	
	public MessagingNode() {
		if (debug) System.out.println("Building messaging node...");
		neighbors = new ArrayList<NodeReference>();
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
		Register registerMessage = new Register(this.socket.getInetAddress().toString(), this.socket.getPort());
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
	
	public void transmitMessages(int numRounds) {
		PayloadMessage pMsg = new PayloadMessage();
		// For each round, choose a random external node
		// Find routing plan in routing cache, encode into message
		// Generate random payload and transmit
	}

	public static void usage() {
		System.out.println("Usage: MessagingNode <hostname> <port number>");
	}

}
