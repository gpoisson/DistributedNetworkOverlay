package overlay.node;

import java.io.IOException;
import java.net.Socket;

import overlay.transport.TCPSender;

public class MessagingNode extends Node {
	
	public Thread server;
	public Socket socket;
	public TCPSender sender;
	public Thread receiver;
	
	public MessagingNode() {
		if (debug) System.out.println("Building messaging node...");
		
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
			if (mn.debug) System.out.println("Messaging node built.");
		} catch (IOException e) {
			System.out.println(e);
		} catch (NumberFormatException nfe) {
			System.out.println(nfe);
		}

	}
	
	public static void usage() {
		System.out.println("Usage: MessagingNode <hostname> <port number>");
	}

}
