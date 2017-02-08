package overlay.util;

import java.util.ArrayList;

import overlay.node.NodeReference;
import overlay.wireformats.MessagingNodesList;

public class OverlayCreator {

	public int numNodes;
	public int linksPerNode;
	public ArrayList<NodeReference> nodeRefs;
	private boolean debug;
	
	public OverlayCreator(MessagingNodesList mnList, int linksPerNode, boolean debug) {
		// Overlay Creator is given the list of messaging nodes
		numNodes = mnList.nodeRefs.size();
		this.linksPerNode = linksPerNode;
		this.nodeRefs = mnList.nodeRefs;
		this.debug = debug;
		if (this.debug) System.out.println("OverlayCreator instantiated in the Registry.");
	}

	public void buildOverlay() {
		if (debug) System.out.println(" Registry OverlayCreator preparing to build overlay for " + numNodes + " nodes with " + linksPerNode + " links each.");
		
	}
}
