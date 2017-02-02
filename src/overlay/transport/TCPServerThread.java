package overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServerThread implements Runnable {
	
	private ServerSocket serverSocket;
	private ArrayList<Socket> messagingNodes;
	private int portnumber;
	private boolean debug = false;
	public boolean shutDown = false;
	
	public TCPServerThread(ArrayList<Socket> messagingNodes, int portnumber, boolean debug) {
		this.messagingNodes = messagingNodes;
		this.portnumber = portnumber;
		this.debug = debug;
	}

	//@Override
	public void run() {
		if (debug) System.out.println(" TCPServerThread running in a new thread.");
		try {
			while (!shutDown) {
				serverSocket = new ServerSocket(portnumber);
				if (debug) System.out.println(" TCPServerThread awaiting new connection on port number " + serverSocket.getLocalPort());
				Socket newSocket = serverSocket.accept();
				messagingNodes.add(newSocket);
				if (debug) System.out.println(" TCPServerThread connected to new client.");
				if (debug) System.out.println(" TCPServerThread is spawning a TCPReceiverThread to listen for incoming data...");
				TCPSender sender = new TCPSender(newSocket, debug);
				Thread receiver = new Thread(new TCPReceiverThread(sender, newSocket, debug));
				receiver.start();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
		
		if (debug) System.out.println(" TCPServerThread exiting.");
	}

}
