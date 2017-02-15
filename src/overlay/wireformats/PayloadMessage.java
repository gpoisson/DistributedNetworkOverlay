package overlay.wireformats;

import java.util.Random;

public class PayloadMessage extends Message {

	public PayloadMessage() {
		type = 9;
		identifier = generatePayload();
	}
	
	public String generatePayload() {
		Random random = new Random();
		int r = random.nextInt();
		return r + "";
	}
	
	public void encodeTransmissionPath(String path){
		identifier = "path> " + path + "<path " + identifier;
	}
}
