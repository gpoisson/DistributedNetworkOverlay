package overlay.wireformats;

public class Register extends Message {
	
	public Register(String ipAddress, int localPort) {
		type = 0;
		identifier = ipAddress + " " + localPort;
	}

}
