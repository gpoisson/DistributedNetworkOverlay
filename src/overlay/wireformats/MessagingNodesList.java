package overlay.wireformats;

import java.util.ArrayList;

import overlay.node.NodeReference;

public class MessagingNodesList extends Message {

	public ArrayList<NodeReference> nodeRefs;
	public int peerCount;									// Number of immediate neighbors to each node
	
	public MessagingNodesList(ArrayList<NodeReference> nodeRefs) {
		type = 3;
		this.nodeRefs = nodeRefs;
	}
	
	public String toString() {
		String mnList = "MESSAGING_NODES_LIST:\n";
		
		return mnList;
	}
	
	public void setPeerCount(int peerCount) {
		this.peerCount = peerCount;
	}
}
