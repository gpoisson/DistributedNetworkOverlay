package overlay.dijkstra;

import java.util.ArrayList;

public class DijkstraNode {

	public int id;						// ID of the external node
	public int distance;				// Calculated distance from parent node to external node
	public ArrayList<Integer> route;	// List of the IDs of the nodes which comprise the shortest path from the parent node to the external node 
	public ArrayList<Integer> neighbors;	// List of the immediate neighbors of a node
	public ArrayList<Integer> weights;		// List of the distances from each node to its immediate neighbors
	public boolean visited;					// Set to true when all neighbor node distances have been computed
	
	public DijkstraNode(int id) {
		this.id = id;
		distance = 2147483647;				// Initial distance assumed to be infinity (used max value allowable by type int)
		route = new ArrayList<Integer>();
		neighbors = new ArrayList<Integer>();
		weights = new ArrayList<Integer>();
	}
}
