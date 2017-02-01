package overlay.node;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import overlay.transport.TCPSender;
import overlay.transport.TCPServerThread;

public class Registry extends Node {

	public Thread server;
	public Socket socket;
	public TCPSender sender;
	public Thread receiver;
	public ArrayList<Socket> messagingNodes;
	
	public Registry() {
		if (debug) System.out.println("Building registry node...");
		id = 0;
		messagingNodes = new ArrayList<Socket>();
	}
	
	public static void main(String[] args) {
		Registry reg = new Registry();
		reg.server = new Thread(new TCPServerThread(reg.messagingNodes, reg.portNumber, reg.debug));
		reg.server.start();
		if (reg.debug) System.out.println("Registry node built. Awaiting user input.");
		
		Scanner scanner = new Scanner(System.in); 
		while (scanner.hasNext()) {
			String input = scanner.nextLine();
			if (input.equals("quit")) {
				if (reg.debug) System.out.println("Shutting down Registry node.");
				scanner.close();
				System.exit(0);
			}
		}
	}
}
