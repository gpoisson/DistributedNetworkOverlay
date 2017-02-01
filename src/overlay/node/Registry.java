package overlay.node;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import overlay.transport.TCPSender;
import overlay.transport.TCPServerThread;

public class Registry implements Node {

	public Thread server;
	public Socket socket;
	public TCPSender sender;
	public Thread receiver;
	public ArrayList<Socket> messagingNodes;
	
	public Registry() {
		if (debug) System.out.println("Building registry node...");
		messagingNodes = new ArrayList<Socket>();
		server = new Thread(new TCPServerThread(messagingNodes, portNumber, debug));
		server.start();
		if (debug) System.out.println("Registry node built. Awaiting user input.");
		
		Scanner scanner = new Scanner(System.in); 
		String input = scanner.nextLine();
		if (input.equals("quit")) {
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		Registry reg = new Registry();
	}

}
