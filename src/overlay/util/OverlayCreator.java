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
		/*		PARTITION-FREE OVERLAY ALGORITHM -- Greg Poisson
		 * 			Iterate through the nodes which have the fewest number of links
		 * 			For each node, connect it to a node with the next fewest number of links
		 * 			Continue until all nodes have the specified number of links
		 * 			Assume all nodes begin with zero links. Assume there are at least two nodes.
		 */
		if (debug) System.out.println(" Registry OverlayCreator preparing to build overlay for " + numNodes + " nodes with " + linksPerNode + " links each.");
		
		int index_of_node_with_fewest_links = 0;
		int fewest_number_of_links = 0;
		int index_of_node_with_second_fewest_links = index_of_node_with_fewest_links + 1;
		int second_fewest_number_of_links = 0;
		int number_of_links_in_completed_overlay = numNodes * linksPerNode;
		int current_number_of_links_in_overlay = 0;
		
		while (current_number_of_links_in_overlay < number_of_links_in_completed_overlay) {					// Repeat the search for incomplete nodes until all nodes are complete
			for (int current_node_index = 0; current_node_index < numNodes; current_node_index++) {			// Iterate through nodes to find the node with the fewest links
				if (nodeRefs.get(current_node_index).neighbors.size() < fewest_number_of_links) {
					index_of_node_with_fewest_links = current_node_index;
				}
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
