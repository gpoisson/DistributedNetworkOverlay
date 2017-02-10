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
	
	public void findAllShortestPaths() {
		findExternalPeerCount();
		compileRoutingCache();
		for (int i = 0; i < routingCache.dijkstraNodes.size(); i++) {
			System.out.println(" ID: " + routingCache.dijkstraNodes.get(i).id + "\tDistance: " + routingCache.dijkstraNodes.get(i).distance + "\tNeighbor Count: " + routingCache.dijkstraNodes.get(i).neighbors.size());
			if (routingCache.dijkstraNodes.get(i).id == id) {
				
			}
		}
	}
}
