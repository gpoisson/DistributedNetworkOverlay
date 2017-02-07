package overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import overlay.node.Node;
import overlay.wireformats.Message;
import overlay.wireformats.Register;
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
	private final int MESSAGING_NODES_LIST = 3;
	private final int LINK_WEIGHTS = 4;
	private final int TASK_INITIATE = 5;
	private final int TASK_COMPLETE = 6;
	private final int PULL_TRAFFIC_SUMMARY = 7;
	
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
				System.out.println(se.getMessage());
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
		
		else {
			if (debug) System.out.println("  TCPReceiver received unknown message.");
		}
		
	}

}
