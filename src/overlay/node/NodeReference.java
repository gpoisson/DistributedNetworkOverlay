package overlay.node;

public class NodeReference {
	
	private String ipAddress;
	private int localPort;
	private int publicPort;
	private int id;

	public void setIP(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public void setLocalPort(int port) {
		this.localPort = port;
	}
	public void setPublicPort(int port) {
		this.publicPort = port;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIP() { return this.ipAddress; }
	public int getLocalPort() { return this.localPort; }
	public int getPublicPort() { return this.publicPort; }
	public int getId() { return this.id; }
}
