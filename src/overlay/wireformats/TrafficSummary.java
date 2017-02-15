package overlay.wireformats;

public class TrafficSummary extends Message {

	public TrafficSummary(int sendTracker, int receiveTracker, int relayTracker) {
		type = 10;
		identifier = sendTracker + " " + receiveTracker + " " + relayTracker;
	}
}
