package overlay.dijkstra;

import java.util.ArrayList;

public class ShortestPath {

	String[] msgFields;
	RoutingCache routingCache;
	int id;
	int externalPeerCount;
	boolean debug;
	
	public ShortestPath(String[] msgFields, RoutingCache routingCache, int id, boolean debug) {
		this.msgFields = msgFields;
		this.routingCache = routingCache;
		this.id = id;
		this.debug = debug;
	}
	
	private void findExternalPeerCount() {
		for (int wordIndex = 0; wordIndex < msgFields.length; wordIndex++) {
			if (msgFields[wordIndex].equals("nodes:")) {
				this.externalPeerCount = Integer.parseInt(msgFields[wordIndex + 1].split("\n")[0]) - 1;
				break;
			}
		}
	}
	
	// For each node, populate an array of DijkstraNodes, which will be used to compute routes to external nodes
	private void compileRoutingCache() { 
		if (debug) System.out.println("  Compiling node " + id + "'s routing cache...");
		for (int wordIndex = 0; wordIndex < msgFields.length; wordIndex++) {
			if (msgFields[wordIndex].contains("Messaging")) {
				int dnId = Integer.parseInt(msgFields[wordIndex + 9].split("\n")[0]);
				if (debug) System.out.println("   Making a DijkstraNode for node " + dnId + " to put in the routing cache of node " + id);
				DijkstraNode dn = new DijkstraNode(dnId);
				routingCache.dijkstraNodes.add(dn);
			}
			else if (msgFields[wordIndex].contains("num--")) {
				int neighbor = Integer.parseInt(msgFields[wordIndex + 1].split("\t")[0]);
				int weight = Integer.parseInt(msgFields[wordIndex + 2].split("\n")[0]);
				routingCache.dijkstraNodes.get(routingCache.dijkstraNodes.size()-1).neighbors.add(neighbor);
				routingCache.dijkstraNodes.get(routingCache.dijkstraNodes.size()-1).weights.add(weight);
			}
		}
	}
	
	public void setAllInitialDistances() {
		if (debug) System.out.println(" Setting all DijkstraNode distances to initial values...");
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++) {
			if (routingCache.dijkstraNodes.get(i).id == id) {
				routingCache.dijkstraNodes.get(i).tentative_distance = 0;
				routingCache.dijkstraNodes.get(i).distance = 0;
				routingCache.dijkstraNodes.get(i).visited = false;
			}
			else {
				routingCache.dijkstraNodes.get(i).tentative_distance = 2147483647;
			}
		}
	}
	
	public void setAllNeighborDistances() {
		int working_index = 0;
		// Find the index of the DijkstraNode which represents this node, so as to work with its neighbor node references
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++) {
			if (routingCache.dijkstraNodes.get(i).id == id) {
				if (debug) System.out.println(" Setting DijkstraNode ID " + routingCache.dijkstraNodes.get(i).id + " as current working node." );
				working_index = i;
			}
		}
		for (int neighbor_index = 0; neighbor_index < routingCache.dijkstraNodes.get(working_index).neighbors.size(); neighbor_index++) {
			// Find the index of each of this node's neighbor nodes
			int neighbor_id = routingCache.dijkstraNodes.get(working_index).neighbors.get(neighbor_index);
			if (debug) System.out.println("  Considering neighboring node with ID " + neighbor_id);
			// Find the corresponding DijkstraNode and set its distance
			for (int graph_index = 0; graph_index < routingCache.dijkstraNodes.size(); graph_index++) {
				if (routingCache.dijkstraNodes.get(graph_index).id == neighbor_id) {
					routingCache.dijkstraNodes.get(graph_index).distance = routingCache.dijkstraNodes.get(working_index).weights.get(neighbor_index);
					routingCache.dijkstraNodes.get(graph_index).route.add(neighbor_id);
					if (debug) System.out.println("  Found neighboring node " + neighbor_id + ". Setting its distance to: " + routingCache.dijkstraNodes.get(graph_index).distance);
				}
			}
		}
	}
	
	public void setThisNodeVisited() {
		for (int dn_index = 0; dn_index < routingCache.dijkstraNodes.size(); dn_index++) {
			if (routingCache.dijkstraNodes.get(dn_index).id == id) {
				routingCache.dijkstraNodes.get(dn_index).visited = true;
				if (debug) System.out.println("  Marking node " + routingCache.dijkstraNodes.get(dn_index).id + " visited.");
			}
		}
	}
	
	/*
	 * 	DIJKSTRA'S ALGORITHM
	 * 	https://en.wikipedia.org/wiki/Dijkstra's_algorithm#Algorithm
	 * 	http://www.geeksforgeeks.org/dijkstras-shortest-path-algorithm-using-priority_queue-stl/
	 * 
	 * 	1. Set the distance for all external nodes to infinity
	 * 	2. Find distances of all immediate neighbor nodes
	 * 	3. Mark current node as visited
	 * 	4. For each neighbor node, measure the distances of all immediate neighbor nodes, excluding any visited nodes.
	 * 	. Stop when target node has been reached
	 */
	
	public void findAllShortestPaths() {
		findExternalPeerCount();
		compileRoutingCache();
		setAllInitialDistances();
		setAllNeighborDistances();
		setThisNodeVisited();
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++) {
			String route = "Route: ";
			for (int r = 0; r < routingCache.dijkstraNodes.get(i).route.size(); r++) {
				route += routingCache.dijkstraNodes.get(i).route.get(r) + " ";
			}
			if (debug) System.out.println(" ID: " + routingCache.dijkstraNodes.get(i).id + "\tDistance: " + routingCache.dijkstraNodes.get(i).distance + "\tNeighbor Count: " + routingCache.dijkstraNodes.get(i).neighbors.size() + "\t" + route);
			if (routingCache.dijkstraNodes.get(i).id == id) {
				
			}
		}
	}
}
