package overlay.dijkstra;

public class ShortestPath {

	String[] msgFields;
	RoutingCache routingCache;
	int id;
	int externalPeerCount;
	
	public ShortestPath(String[] msgFields, RoutingCache routingCache, int id) {
		this.msgFields = msgFields;
		this.routingCache = routingCache;
		this.id = id;
	}
	
	private void findExternalPeerCount() {
		for (int wordIndex = 0; wordIndex < msgFields.length; wordIndex++) {
			if (msgFields[wordIndex].equals("nodes:")) {
				this.externalPeerCount = Integer.parseInt(msgFields[wordIndex + 1].split("\n")[0]) - 1;
				break;
			}
		}
	}
	
	public DijkstraNode[] findAllShortestPaths() {
		findExternalPeerCount();
		System.out.println(" Finding shortest path to " + externalPeerCount + " other nodes.");
		DijkstraNode[] dijkstraNodes = new DijkstraNode[externalPeerCount];
		
		return dijkstraNodes;
	}
}
