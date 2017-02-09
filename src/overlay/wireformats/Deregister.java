package overlay.wireformats;

public class Deregister extends Message {
	
	public Deregister(String ip, int port) {
		type = 2;
		identifier = ip + " " + port;
	}
}
