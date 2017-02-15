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
	
	private Socket obtainSocket(int next){
		for (int i = 0; i < messagingNode.mNodeIDs.size(); i++){
			if (messagingNode.mNodeIDs.get(i) == next) {
				if (debug) System.out.println("  Socket for node " + next + " obtained.");
				return messagingNode.mNodeSockets.get(i + 1);
			}
		}
		return null;
	}
	
	private int determineNextID(PayloadMessage pMsg) {
		if (debug) System.out.println(" Determining ID of next node in message's path...");
		int next = 0;
		String[] msgFields = pMsg.toString().split(" ");
		int startPath = 3;															// Index of first element of the path
		int endPath = 0;															// Need to find last element of path
		for (int word_index = 2; word_index < msgFields.length; word_index++){
			if (msgFields[word_index].contains("<path")){
				endPath = word_index;
			}
		}
		int pathLength = endPath - startPath;
		assert (pathLength > 0);													// Path length must be positive or there has been some error
		int[] path = new int[pathLength];
		for (int step = startPath; step < endPath; step++){
			path[step - startPath] = Integer.parseInt(msgFields[step]);
		}
		if (debug) {
			String p = "";
			for (int i = 0; i < path.length; i++){
				p += path[i] + " ";
			}
			System.out.println(" Full path: " + p);
		}
		for (int n = 0; n < path.length; n++){
			if (path[n] == messagingNode.id){
				if (n < path.length - 1){
					next = path[n+1];
					if (debug) System.out.println("  Determined next node in path to be: " + next);
				}
				else {
					next = -1;
					if (debug) System.out.println("  No further nodes in path.");
				}
			}
		}
		return next;
	}
	
	
	






}
