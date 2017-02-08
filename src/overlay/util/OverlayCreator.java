package overlay.util;

import java.util.ArrayList;
import java.util.Random;

import overlay.node.LinkWeight;
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
		if (numNodes < linksPerNode) { 
			System.out.println(" Specified number of connections (" + linksPerNode + ") is greater than the available number of nodes (" + numNodes + "). The number of links will be adjusted."); 
			linksPerNode = numNodes - 1;
		}
		for (int i = 0; i < numNodes; i++) {
			// Iterate over each node reference, which is maintained by the registry
			for (int link = 0; link < linksPerNode; link++) {
				int newNeighbor = randomIntRange(0,numNodes);												// Choose a random node from the list of registered nodes 
				while (newNeighbor == i) { newNeighbor = randomIntRange(0,numNodes); }						// If the current node was chosen, pick a different one -- don't link a node to itself
				int newWeight = randomIntRange(1,10);														// Compute a random weight
				LinkWeight linkWeight = new LinkWeight(nodeRefs.get(newNeighbor).getId(),newWeight);		// Define a LinkWeight object to represent the link
				if (debug) System.out.println("\tAttempting to establish link between nodes: " + nodeRefs.get(i).getId() + ", " + nodeRefs.get(newNeighbor).getId() + " -- Link weight: " + newWeight);
				nodeRefs.get(i).neighbors.add(linkWeight);
				if (debug) System.out.println("\tDefined new link between nodes: " + nodeRefs.get(i).getId() + ", " + nodeRefs.get(newNeighbor).getId() + " -- Link weight: " + newWeight);
			}
		}
	}
	
	// Returns a random integer in between a positive min value and a positive max value (inclusive)
	public int randomIntRange(int positiveMin, int positiveMax) {
		Random rand = new Random();
		int randomInt = rand.nextInt((positiveMax - positiveMin) + 1) + positiveMin;
		return randomInt;		
	}
}
