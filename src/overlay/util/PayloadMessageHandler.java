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
		
	}
	
	private int determineNextID(PayloadMessage pMsg) {
		int next = 0;
		
		return next;
	}
	
	
	
	public void handlePayloadMessage(PayloadMessage pMsg) {
		if (debug) System.out.println(" Handling payload message...");
		Socket relay;																// Reference to the socket to the next node in the path
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
		boolean isDestination = false;
		if (path[path.length-1] == messagingNode.id) { isDestination = true; }				// Determine if this is the message's final stop
		if (!isDestination){
			if (debug) System.out.println("  Not destination for this message, passing it on...");
			int next = 0;
			for (int step = 0; step < path.length; step++){							// Determine where to relay the message to
				if (path[step] == messagingNode.id) { next = path[step + 1]; }
				if (debug) {
					System.out.println("  Next step in message path: " + next);
					String p = "";
					for (int s = 0; s < path.length; s++){
						p += path[s] + " ";
					}
					System.out.println("  Full path: " + p);
				}
				for (int node = 0; node < messagingNode.mNodeIDs.size(); node++){
					if (messagingNode.mNodeIDs.get(node) == next){
						relay = messagingNode.mNodeSockets.get(node + 1);
						try {
							if (debug) System.out.println("\t  Sending <" + pMsg.toString() + "> to node " + path[step]);
							TCPSender sender = new TCPSender(relay, this.debug);
							sender.sendData(pMsg.getByteArray());
							messagingNode.relayTracker++;
						} catch (IOException ioe) {
							System.out.println(ioe);
						}
						break;
					}
				}
			}
		}
		else if (isDestination){
			if (debug) System.out.println("  Received payload message at the end of its path. Preparing to process.");
		}
	}







}
