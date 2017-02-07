package overlay.wireformats;

import java.util.ArrayList;

import overlay.node.NodeReference;

public class MessagingNodesList extends Message {

	public MessagingNodesList(ArrayList<NodeReference> nodeRefs) {
		type = 3;
	}
	
	public String toString() {
		String mnList = "MESSAGING_NODES_LIST:\n";
		
		return mnList;
	}
}
