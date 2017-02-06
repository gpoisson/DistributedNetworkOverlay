package overlay.wireformats;

public class Register extends Message {
	
	public Register(String ipAddress, int localPort) {
		identifier = ipAddress + " " + localPort;
		type = 0;
	}

}
