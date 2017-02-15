package overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import overlay.dijkstra.RoutingCache;
import overlay.dijkstra.ShortestPath;
import overlay.node.Node;
import overlay.node.NodeReference;
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
	
	private void determineNodeId(String[] msgFields) {
		if (debug) System.out.println("  Scanning messaging nodes list for ID number.");
		for (int wordIndex = 0; wordIndex < msgFields.length; wordIndex++) {
			if (msgFields[wordIndex].contains("Messaging")) {
				String ip = msgFields[wordIndex + 3].split("\t")[0];
				int port = Integer.parseInt(msgFields[wordIndex + 5].split("\t")[0]);
				if (ip.contains(parent.hostname) && parent.portNumber == port) {
					parent.id = Integer.parseInt(msgFields[wordIndex + 9].split("\t")[0]);
				}
			}
		}
		if (debug) System.out.println("  This node's ID number is: " + parent.id);
	}
	
	public void compileNodeRefData(String[] msgFields, RoutingCache routingCache, ArrayList<NodeReference> neighbors){
		int parent_index = 0;
		for (int node = 0; node < routingCache.dijkstraNodes.size(); node++){
			if (routingCache.dijkstraNodes.get(node).id == parent.id){
				parent_index = node;
			}
		}
		for (int node = 0; node < routingCache.dijkstraNodes.get(parent_index).neighbors.size(); node++){
			NodeReference nodeRef = new NodeReference();
			int nodeID = routingCache.dijkstraNodes.get(parent_index).neighbors.get(node);
			nodeRef.setId(nodeID);
			for (int word_index = 0; word_index < msgFields.length; word_index++){
				if (msgFields[word_index].contains("num:") && (Integer.parseInt(msgFields[word_index + 1])) == nodeID){
					nodeRef.setPublicPort(Integer.parseInt(msgFields[word_index - 1].split("\t")[0]));
					String ip = msgFields[word_index - 5].split("\t")[0];
					if (ip.contains("localhost")) { ip = "localhost"; }
					nodeRef.setIP(ip);
					nodeRef.setLocalPort(Integer.parseInt(msgFields[word_index - 3].split("\t")[0]));
					neighbors.add(nodeRef);
				}
			}
		}
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
			if (parent.serverThread.shutDown == false)
				parent.serverThread.shutDown();
			if (debug) System.out.println("Node shutting down...");
			System.exit(0);
		}
		else if (msgType == MESSAGING_NODES_LIST) {
			if (debug) System.out.println("  TCPReceiver received MESSAGING_NODES_LIST message...");
		}
		else if (msgType == LINK_WEIGHTS) {
			if (debug) System.out.println("  TCPReceiver received LINK_WEIGHTS message...");
			determineNodeId(msgFields);
			parent.routingCache = new RoutingCache();
			ShortestPath pathCalculator = new ShortestPath(msgFields, parent.routingCache, parent.id, parent.debug);
			pathCalculator.findAllShortestPaths();
			compileNodeRefData(msgFields, parent.routingCache, parent.neighbors);
		}
		else if (msgType == TASK_INITIATE) {
			if (debug) System.out.println("  TCPReceiver received TASK_INITIATE message...");
			parent.transmitMessages(Integer.parseInt(msgFields[2]));
		}
		else if (msgType == TASK_COMPLETE) {
			if (debug) System.out.println("  TCPReceiver received TASK_COMPLETE message...");
		}
		else if (msgType == PULL_TRAFFIC_SUMMARY) {
			if (debug) System.out.println("  TCPReceiver received PULL_TRAFFIC_SUMMARY message...");
		}
		else if (msgType == PAYLOAD_MESSAGE) {
			if (debug) System.out.println("  TCPReceiver received PAYLOAD_MESSAGE message...");
			if (debug) System.out.println("    " + msgFields.toString());
		}
		else {
			if (debug) System.out.println("  TCPReceiver received unknown message.");
		}
		
	}

}
