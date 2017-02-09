package overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import overlay.node.Node;
import overlay.wireformats.DeregisterResponse;
import overlay.wireformats.RegisterResponse;

public class TCPReceiverThread implements Runnable {

	private Node parent;
	private Socket socket;
	private TCPSender sender;
	private DataInputStream din;
	private boolean debug;
	
	private final int REGISTER = 0;
	private final int REGISTER_RESPONSE = 1;
	private final int DEREGISTER = 2;
	private final int DEREGISTER_RESPONSE = 3;
	private final int MESSAGING_NODES_LIST = 4;
	private final int LINK_WEIGHTS = 5;
	private final int TASK_INITIATE = 6;
	private final int TASK_COMPLETE = 7;
	private final int PULL_TRAFFIC_SUMMARY = 8;
	private final int PAYLOAD_MESSAGE = 9;
	
	// TCPReceiverThread maintains a reference to the node's TCPSender thread,
	//    in order to send response messages when appropriate.
	public TCPReceiverThread(Node parent, TCPSender sender, Socket socket, boolean debug) throws IOException {
		this.parent = parent;
		this.socket = socket;
		this.sender = sender;
		this.debug = debug;
		din = new DataInputStream(socket.getInputStream());
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  TCPReceiverThread spawned.");
		int dataLength;
		while (socket != null) {
			try {
				if (debug) System.out.println("  TCPReceiverThread waiting for new message, beginning with length...");
				dataLength = din.readInt();
				if (debug) System.out.println("  TCPReceiverThread received message of length: " + dataLength);
				if (debug) System.out.println("  TCPReceiverThread awaiting byte array message delivery...");
				byte[] data = new byte[dataLength];
				din.readFully(data,  0,  dataLength);
				String str = new String(data, "UTF-8");
				if (debug) System.out.println("  TCPReceiverThread received byte[]: " + str);
				interpret(str);
			} catch (SocketException se) {
				if (debug) System.out.println("  TCPReceiverThread socket closed.");
				break;
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
				break;
			}
		}
		if (debug) System.out.println("  TCPReceiverThread exiting.");
		
	}

	// Interpret the received message
	private void interpret(String str) {
		// Identify the message type
		String[] msgFields = str.split(" ");
		int msgType = Integer.parseInt(msgFields[0]);
		
		// Register message (Registry only)
		if (msgType == REGISTER) {
			if (debug) System.out.println("  TCPReceiver received REGISTER message...");
			RegisterResponse rrMsg = new RegisterResponse();
			parent.register(msgFields);
			try {
				sender.sendData(rrMsg.getByteArray());
			} catch (IOException ioe) {
				System.out.println(ioe);
			}
		}
		// Register Response message (Messaging Node only)
		else if (msgType == REGISTER_RESPONSE) {
			if (debug) System.out.println("  TCPReceiver received REGISTER_RESPONSE message...");
		}
		else if (msgType == DEREGISTER) {
			if (debug) System.out.println("  TCPReceiver received DEREGISTER message...");
			parent.deregister(msgFields);
		}
		else if (msgType == DEREGISTER_RESPONSE) {
			if (debug) System.out.println("  TCPReceiver received DEREGISTER_RESPONSE message...");
			if (debug) System.out.println("Node shutting down...");
			System.exit(0);
		}
		else if (msgType == MESSAGING_NODES_LIST) {
			if (debug) System.out.println("  TCPReceiver received MESSAGING_NODES_LIST message...");
		}
		else if (msgType == LINK_WEIGHTS) {
			if (debug) System.out.println("  TCPReceiver received LINK_WEIGHTS message...");
		}
		else if (msgType == TASK_INITIATE) {
			if (debug) System.out.println("  TCPReceiver received TASK_INITIATE message...");
		}
		else if (msgType == TASK_COMPLETE) {
			if (debug) System.out.println("  TCPReceiver received TASK_COMPLETE message...");
		}
		else if (msgType == PULL_TRAFFIC_SUMMARY) {
			if (debug) System.out.println("  TCPReceiver received PULL_TRAFFIC_SUMMARY message...");
		}
		else {
			if (debug) System.out.println("  TCPReceiver received unknown message.");
		}
		
	}

}
