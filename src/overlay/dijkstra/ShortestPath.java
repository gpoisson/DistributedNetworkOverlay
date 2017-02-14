package overlay.dijkstra;

import java.util.ArrayList;
import java.util.Scanner;

public class ShortestPath {

	String[] msgFields;
	RoutingCache routingCache;
	ArrayList<DijkstraNode> priorityQueue;
	int id;
	int externalPeerCount;
	boolean debug;
	
	public ShortestPath(String[] msgFields, RoutingCache routingCache, int id, boolean debug) {
		this.msgFields = msgFields;
		this.routingCache = routingCache;
		this.priorityQueue = new ArrayList<DijkstraNode>();
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
				routingCache.dijkstraNodes.get(i).previous = 0;
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
			/*for (int graph_index = 0; graph_index < routingCache.dijkstraNodes.size(); graph_index++) {
				if (routingCache.dijkstraNodes.get(graph_index).id == neighbor_id) {
					routingCache.dijkstraNodes.get(graph_index).distance = routingCache.dijkstraNodes.get(working_index).weights.get(neighbor_index);
					routingCache.dijkstraNodes.get(graph_index).route.add(neighbor_id);
					if (debug) System.out.println("  Found neighboring node " + neighbor_id + ". Setting its distance to: " + routingCache.dijkstraNodes.get(graph_index).distance);
				}
			}*/
		}
	}
	
	private void populatePriorityQueue() {
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++){
			priorityQueue.add(routingCache.dijkstraNodes.get(i));
		}
	}
	
	private DijkstraNode removeMinDist() {
		DijkstraNode min = routingCache.dijkstraNodes.get(0);
		/*int min_id = 0;
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++) {
			if (routingCache.dijkstraNodes.get(i).distance < min.distance){
				min = routingCache.dijkstraNodes.get(i);
				min_id = routingCache.dijkstraNodes.get(i).id;
			}
		}
		for (int p = 0; p < priorityQueue.size(); p++) {
			if (priorityQueue.get(p).id == min_id) {
				priorityQueue.remove(p);
				break;
			}
		}*/
		int min_dist = 2147483647;
		int min_index = 0;
		for (int i = 0; i < priorityQueue.size(); i++) {
			if (priorityQueue.get(i).distance < min_dist) {
				min = priorityQueue.get(i);
				min_dist = min.distance;
				min_index = i;
			}
		}
		priorityQueue.remove(min_index);
		return min;
	}
	
	private void relax(DijkstraNode u, int v_index, int weight) {
		DijkstraNode v = routingCache.dijkstraNodes.get(0);
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++) {
			if (routingCache.dijkstraNodes.get(i).id == v_index) {
				v = routingCache.dijkstraNodes.get(i);
			}
		}
		if (u.distance + weight < v.distance) {
			v.distance = u.distance + weight;
			v.previous = u.id;
		}
		if (debug) System.out.println("  Node " + v.id + " previous: " + v.previous);
	}
	
	private void compileRoute(DijkstraNode u) {
		if (debug) System.out.println("Compiling route to node " + u.id + "...");
		int goal_id = u.id;
		String route = "" + u.id;
		while (u.id != this.id) {
			route = u.previous + " " + route;
			//if (debug) System.out.println("  Current route: " + route);
			for (int p = 0; p < routingCache.dijkstraNodes.size(); p++) {
				if (routingCache.dijkstraNodes.get(p).id == u.previous)
					u = routingCache.dijkstraNodes.get(p);
			}
		}
		if (debug) System.out.println("Route compiled: " + route);
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++){
			if (routingCache.dijkstraNodes.get(i).id == goal_id){
				routingCache.dijkstraNodes.get(i).path = route;
			}
		}
		/*String[] routeList = route.split(" ");
		u.route = new ArrayList<Integer>();
		for (int step = 0; step < routeList.length; step++) {
			int nodeStep = Integer.parseInt(routeList[step]);
			if (nodeStep != this.id)
				u.route.add(nodeStep);
		}*/
	}
	
	public void findAllShortestPaths() {
		findExternalPeerCount();
		compileRoutingCache();
		
		for (int node = 0; node < routingCache.dijkstraNodes.size(); node++){
			DijkstraNode goal = routingCache.dijkstraNodes.get(node);
			if (goal.id == id) continue;
			else {
				if (debug) System.out.println("Finding shortest path from node " + this.id + " to node " + goal.id);
				setAllInitialDistances();
				setAllNeighborDistances();
				populatePriorityQueue();
				
				if (debug) System.out.println("Set all initial values and populated priority queue...");
				
				while (priorityQueue.size() > 0) {
					DijkstraNode u = removeMinDist();
					if (debug) System.out.println(" Removed node " + u.id + " from priority queue. Queue size: " + priorityQueue.size());
					for (int e = 0; e < u.neighbors.size(); e++) {
						relax(u, u.neighbors.get(e), u.weights.get(e));
					}
				}
				compileRoute(goal);
			}
		}		
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++) {
			if (debug) System.out.println(routingCache.dijkstraNodes.get(i).toString());
		}
		
	}
}
