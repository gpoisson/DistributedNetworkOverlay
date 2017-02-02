package overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import overlay.transport.TCPSender;

public class MessagingNode extends Node {
	
	public Socket socket;			// Socket used for transmitting data
	public TCPSender sender;		// Marshalling of messages into byte[] prior to transmission
	public Thread receiver;			// Receiver thread listens for incoming connections 
	
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
			if (mn.debug) System.out.println(" Connection successfully established. Preparing to send registration request...");
			if (mn.debug) System.out.println("Messaging node built.");
			
			Scanner scanner = new Scanner(System.in); 
			while (scanner.hasNext()) {
				String input = scanner.nextLine();
				if (input.equals("quit")) {
					if (mn.debug) System.out.println("Shutting down Messaging node.");
					scanner.close();
					System.exit(0);
				}
			}
			
		} catch (IOException ioe) {
			System.out.println(ioe);
		} catch (NumberFormatException nfe) {
			System.out.println(nfe);
		}
	}
	
	public static void usage() {
		System.out.println("Usage: MessagingNode <hostname> <port number>");
	}

}
