package overlay.node;

import java.util.ArrayList;

import overlay.dijkstra.DijkstraNode;
import overlay.dijkstra.RoutingCache;
import overlay.transport.TCPServerThread;
import overlay.wireformats.DeregisterResponse;

public abstract class Node {

	public String hostname = "";
	public int portNumber = 0;
	public int serverPortNumber = 0;
	public TCPServerThread serverThread;
	public ArrayList<NodeReference> neighbors;
	public int id = -1;
	public boolean debug = true;
	public int currentNeighborCount = 0;
	public int receiveTracker = 0;
	public int sendTracker = 0;
	public int relayTracker = 0;
	public RoutingCache routingCache;
	
	public String toString() {
		return "Host: " + hostname + "\tPort: " + portNumber;
	}
	
	public void register(String[] msgFields) {
		// Registry overrides this method
	}
	
	public void deregister(String[] msgFields) {
		// Registry overrides this method
	}

	public void transmitMessages(int numRounds) {
		// Messaging node overrides this method
	}
	
}
