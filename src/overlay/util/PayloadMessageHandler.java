package overlay.util;

import java.io.IOException;
import java.net.Socket;

import overlay.node.MessagingNode;
import overlay.transport.TCPSender;
import overlay.wireformats.PayloadMessage;

public class PayloadMessageHandler {

	private boolean debug = false;
	private MessagingNode messagingNode;
	
	public PayloadMessageHandler(MessagingNode messagingNode, boolean debug){
		this.debug = debug;
		this.messagingNode = messagingNode;
	}
	
	// Send this message from the current node to the next step in the path
	public void transmit(PayloadMessage pMsg) {
		int next = determineNextID(pMsg);
		Socket s = obtainSocket(next);
		try {
			TCPSender sender = new TCPSender(s, debug);
			sender.sendData(pMsg.getByteArray());
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
	public Socket obtainSocket(int next){
		for (int i = 0; i < messagingNode.mNodeIDs.size(); i++){
			if (messagingNode.mNodeIDs.get(i) == next) {
				if (debug) System.out.println("  Socket for node " + next + " obtained.");
				return messagingNode.mNodeSockets.get(i + 1);
			}
		}
		return null;
	}
	
	public int determineNextID(PayloadMessage pMsg) {
		if (debug) System.out.println(" Determining ID of next node in message's path...");
		int next = 0;
		String[] msgFields = pMsg.toString().split(" ");
		
		// First determine where in the message the path is specified
		int pathStart = 0;
		int pathEnd = 0;
		for (int word_index = 0; word_index < msgFields.length; word_index++) {
			if (msgFields[word_index].contains("path>")) {
				pathStart = word_index + 1;
			}
			else if (msgFields[word_index].contains("<path")) {
				pathEnd = word_index;
			}
		}
		
		// Path must have positive length
		assert pathEnd > pathStart;
		
		// Determine where in the message's path it currently is
		for (int word_index = pathStart; word_index < pathEnd; word_index++){
			if (Integer.parseInt(msgFields[word_index]) == messagingNode.id) {
				if (debug) System.out.println("  PayloadMessage has " + (pathEnd - word_index - 1) + " further transmissions remaining.");
				if ((pathEnd - word_index - 1) > 0) {
					next = Integer.parseInt(msgFields[word_index + 1]);
				}
			}
		}
		if (next == 0) { 		// This node's ID was not found in path --> this node must be the source.
			next = Integer.parseInt(msgFields[pathStart]);
		}
		if (debug) System.out.println("  PayloadMessage's next destination: " + next);
		return next;
	}
	
	public int determineNextID(String pMsg) {
		if (debug) System.out.println(" Determining ID of next node in message's path...");
		int next = 0;
		String[] msgFields = pMsg.split(" ");
		
		// First determine where in the message the path is specified
		int pathStart = 0;
		int pathEnd = 0;
		for (int word_index = 0; word_index < msgFields.length; word_index++) {
			if (msgFields[word_index].contains("path>")) {
				pathStart = word_index + 1;
			}
			else if (msgFields[word_index].contains("<path")) {
				pathEnd = word_index;
			}
		}
		
		// Path must have positive length
		assert pathEnd > pathStart;
		
		// Determine where in the message's path it currently is
		for (int word_index = pathStart; word_index < pathEnd; word_index++){
			if (Integer.parseInt(msgFields[word_index]) == messagingNode.id) {
				if (debug) System.out.println("  PayloadMessage has " + (pathEnd - word_index - 1) + " further transmissions remaining.");
				if ((pathEnd - word_index - 1) > 0) {
					next = Integer.parseInt(msgFields[word_index + 1]);
				}
				else {
					if (debug) System.out.println("  PayloadMessage has reached destination.");
					next = -1;
				}
			}
		}
		if (next == 0) { 		// This node's ID was not found in path --> this node must be the source.
			next = Integer.parseInt(msgFields[pathStart]);
		}
		if (debug) System.out.println("  PayloadMessage's next destination: " + next);
		return next;
	}
	
	
	






}
