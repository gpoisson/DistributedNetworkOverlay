package overlay.dijkstra;

import java.util.ArrayList;

public class DijkstraNode {

	public int id;						// ID of the external node
	public int distance;			// Calculated distance from parent node to external node
	public int tentative_distance;		// This value is changed during the computation
	public ArrayList<Integer> route;	// List of the IDs of the nodes which comprise the shortest path from the parent node to the external node 
	public ArrayList<Integer> neighbors;	// List of the immediate neighbors of a node
	public ArrayList<Integer> weights;		// List of the distances from each node to its immediate neighbors
	public int previous;
	
	public DijkstraNode(int id) {
		this.id = id;
		distance = 2147483647;
		tentative_distance = 2147483647;				// Initial distance assumed to be infinity (used max value allowable by type int)
		route = new ArrayList<Integer>();
		neighbors = new ArrayList<Integer>();
		weights = new ArrayList<Integer>();
		previous = 0;
	}
	
	public String toString() {
		String txt = "ID: " + id + "\tDistance: " + distance + "\n  Neighbors: ";
		for (int n = 0; n < neighbors.size(); n++) {
			txt += neighbors.get(n) + "\t";
		}
		txt += "\n  Weights:   ";
		for (int w = 0; w < weights.size(); w++) {
			txt += weights.get(w) + "\t";
		}
		txt += "\n  Route:     ";
		for (int r = 0; r < route.size(); r++) {
			txt += route.get(r) + "\t";
		}
		return txt;
	}
}
