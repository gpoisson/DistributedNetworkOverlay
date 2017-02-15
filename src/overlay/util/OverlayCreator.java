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
	public MessagingNodesList mnList;
	private boolean debug;
	
	public OverlayCreator(MessagingNodesList mnList, int linksPerNode, boolean debug) {
		// Overlay Creator is given the list of messaging nodes
		numNodes = mnList.nodeRefs.size();
		this.linksPerNode = linksPerNode;
		mnList.setPeerCount(numNodes);
		this.nodeRefs = mnList.nodeRefs;
		this.mnList = mnList;
		this.debug = debug;
		if (this.debug) System.out.println("OverlayCreator instantiated in the Registry.");
	}

	public void buildOverlay() {
		/*		PARTITION-FREE OVERLAY ALGORITHM
		 * 			First connect each node to each subsequent node to create a linear topology
		 * 			Iterate through the nodes which have the fewest links
		 * 			For each node, connect it to a node with the next fewest links
		 * 			Continue until all nodes have the specified number of links
		 * 			Assume all nodes begin with zero links. Assume there are at least two nodes.
		 */
		if (debug) System.out.println(" Registry OverlayCreator preparing to build overlay for " + numNodes + " nodes with " + linksPerNode + " links each.");
		if (numNodes == 1) {
			System.out.println("System contains only one node. No connections needed. Setting number of peer nodes to 0.");
			mnList.setPeerCount(0);
		}
		if (linksPerNode >= numNodes) {
			System.out.println("Specified number of links is greater than the number of links supported by " + numNodes + " nodes.");
			linksPerNode = numNodes - 1;
			System.out.println("Number of links adjusted to max number supported by this system. Each node will have " + linksPerNode + " links.");
		}
		if (numNodes > 1) {
			int number_of_links_in_completed_overlay = numNodes * linksPerNode;
			int current_number_of_links_in_overlay = 0;
			
			for (int node = 0; node < nodeRefs.size(); node++){
				if (node < nodeRefs.size()-1){
					connectNodes(node, node + 1);
					current_number_of_links_in_overlay += 2;
				}
				else{
					connectNodes(node, 0);
					current_number_of_links_in_overlay += 2;
				}
			}
			
			Random rand = new Random();
			while (current_number_of_links_in_overlay < number_of_links_in_completed_overlay){
				int randNode1 = rand.nextInt(nodeRefs.size());
				int randNode2 = rand.nextInt(nodeRefs.size());
				while (nodeRefs.get(randNode1).neighbors.size() >= linksPerNode){
					randNode1 = rand.nextInt(nodeRefs.size());
				}
				while ((randNode1 == randNode2) || (nodeRefs.get(randNode2).neighbors.size() >= linksPerNode) || (nodesAreLinked(randNode1, randNode2))){
					randNode2 = rand.nextInt(nodeRefs.size());
				}
				connectNodes(randNode1, randNode2);
				current_number_of_links_in_overlay += 2;				
			}
			
			/*
			fewest_number_of_links = nodeRefs.get(index_of_node_with_fewest_links).neighbors.size();
			second_fewest_number_of_links = current_number_of_links_in_overlay;
			
			// Generate linear topology
			for (int node = 0; node < nodeRefs.size(); node++){
				if (node < nodeRefs.size()-1){
					connectNodes(node, node + 1);
					current_number_of_links_in_overlay += 2;
				}
				else{
					connectNodes(node, 0);
					current_number_of_links_in_overlay += 2;
				}
				
				for (int current_node_index = 0; current_node_index < numNodes; current_node_index++) {			// Iterate through nodes to find the node with the fewest links
					if (nodeRefs.get(current_node_index).neighbors.size() < fewest_number_of_links) {
						index_of_node_with_fewest_links = current_node_index;
						fewest_number_of_links = nodeRefs.get(current_node_index).neighbors.size();
						if (debug) System.out.println(" New value for fewest no. of links:  Node " + nodeRefs.get(index_of_node_with_fewest_links).getId() + " - " + fewest_number_of_links + " links");
					}
				}
				for (int current_node_index = 0; current_node_index < numNodes; current_node_index++) {			// Iterate through nodes to find the node with the second fewest links
					if ((current_node_index != index_of_node_with_fewest_links) && (!nodesAreLinked(index_of_node_with_fewest_links, current_node_index)) && (nodeRefs.get(current_node_index).neighbors.size() < second_fewest_number_of_links)) {
						index_of_node_with_second_fewest_links = current_node_index;
						second_fewest_number_of_links = nodeRefs.get(current_node_index).neighbors.size();
						if (debug) System.out.println(" New value for second fewest no. of links:  Node " + nodeRefs.get(index_of_node_with_second_fewest_links).getId() + " - " + second_fewest_number_of_links + " links");
					}
				}
			}
			
			// Evenly distribute the rest of the links
			while (current_number_of_links_in_overlay < number_of_links_in_completed_overlay) {					// Repeat the search for incomplete nodes until all nodes are complete
				if (debug) System.out.println(" Overlay contains " + current_number_of_links_in_overlay + "/" + number_of_links_in_completed_overlay + " links.");
				index_of_node_with_fewest_links = 0;
				index_of_node_with_second_fewest_links = 1;
				fewest_number_of_links = nodeRefs.get(index_of_node_with_fewest_links).neighbors.size();		// At each iteration, assume the first node has the fewest links
				second_fewest_number_of_links = current_number_of_links_in_overlay;								// At each iteration, assume the second fewest links is a large number
				if (debug) System.out.println(" Assuming the node with the fewest links is node " + nodeRefs.get(index_of_node_with_fewest_links).getId() + " with " + second_fewest_number_of_links + " links.");
				if (debug) System.out.println(" Assuming the node with the second fewest links is node " + nodeRefs.get(index_of_node_with_second_fewest_links).getId() + " with " + fewest_number_of_links + " links.");
				for (int current_node_index = 0; current_node_index < numNodes; current_node_index++) {			// Iterate through nodes to find the node with the fewest links
					if (nodeRefs.get(current_node_index).neighbors.size() < fewest_number_of_links) {
						index_of_node_with_fewest_links = current_node_index;
						fewest_number_of_links = nodeRefs.get(current_node_index).neighbors.size();
						if (debug) System.out.println(" New value for fewest no. of links:  Node " + nodeRefs.get(index_of_node_with_fewest_links).getId() + " - " + fewest_number_of_links + " links");
					}
				}
				for (int current_node_index = 0; current_node_index < numNodes; current_node_index++) {			// Iterate through nodes to find the node with the second fewest links
					if ((current_node_index != index_of_node_with_fewest_links) && (!nodesAreLinked(index_of_node_with_fewest_links, current_node_index)) && (nodeRefs.get(current_node_index).neighbors.size() < second_fewest_number_of_links)) {
						index_of_node_with_second_fewest_links = current_node_index;
						second_fewest_number_of_links = nodeRefs.get(current_node_index).neighbors.size();
						if (debug) System.out.println(" New value for second fewest no. of links:  Node " + nodeRefs.get(index_of_node_with_second_fewest_links).getId() + " - " + second_fewest_number_of_links + " links");
					}
				}
				connectNodes(index_of_node_with_fewest_links, index_of_node_with_second_fewest_links);
				current_number_of_links_in_overlay += 2;
			}*/
			
		}
		if (debug) System.out.println(" Registry overlay complete.");
	}
	
	// Given the indeces of two nodes within nodeRefs, returns true if the nodes are directly connected
	public boolean nodesAreLinked(int index1, int index2) {
		if (debug) System.out.println("  Checking if nodes " + nodeRefs.get(index1).getId() + " and " + nodeRefs.get(index2).getId() + " are already connected...");
		int comparison_ID = nodeRefs.get(index2).getId();
		for (int i = 0; i < nodeRefs.get(index1).neighbors.size(); i++) {
			//if (debug) System.out.println("   Node " + nodeRefs.get(index1).getId() + " is linked to node " + nodeRefs.get(index1).neighbors.get(i).id + "...");
			if (nodeRefs.get(index1).neighbors.get(i).id == comparison_ID) { 
				if (debug) System.out.println("   Node " + nodeRefs.get(index1).getId() + " is already connected to " + nodeRefs.get(index2).getId());
				return true; 
			}
		}
		return false;
	}
	
	// Establish a weighted link between nodes with two given indeces
	public void connectNodes(int index1, int index2) {
		int newWeight = randomIntRange(1,10);		
		LinkWeight node1_to_node2 = new LinkWeight(nodeRefs.get(index2).getId(), newWeight);
		LinkWeight node2_to_node1 = new LinkWeight(nodeRefs.get(index1).getId(), newWeight);
		nodeRefs.get(index1).neighbors.add(node1_to_node2);
		nodeRefs.get(index2).neighbors.add(node2_to_node1);
		if (debug) System.out.println("\tConnecting messaging nodes " + nodeRefs.get(index1).getId() + ", " + nodeRefs.get(index2).getId() + " with link of weight " + newWeight);
	}
	
	// Returns a random integer in between a positive min value and a positive max value (inclusive)
	public int randomIntRange(int positiveMin, int positiveMax) {
		Random rand = new Random();
		int randomInt = rand.nextInt((positiveMax - positiveMin) + 1) + positiveMin;
		return randomInt;		
	}
}
