package overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender {
	
	private Socket socket;
	private DataOutputStream dout;
	private boolean debug;
	
	public TCPSender(Socket socket, boolean debug) throws IOException {
		this.debug = debug;
		if (debug) System.out.println("  TCPSender created.");
		this.socket = socket;
		dout = new DataOutputStream(this.socket.getOutputStream());
	}
	
	public synchronized void sendData(byte[] dataToSend) throws IOException {
		if (debug) System.out.println("  TCPSender preparing to send data...");
		int dataLength = dataToSend.length;
		if (debug) System.out.println("  TCPSender message length: " + dataLength);
		String str = new String(dataToSend, "UTF-8");
		if (debug) System.out.println("  TCPSender message: " + str);
		dout.writeInt(dataLength);
		dout.write(dataToSend, 0, dataLength);
		dout.flush();
	}

}
