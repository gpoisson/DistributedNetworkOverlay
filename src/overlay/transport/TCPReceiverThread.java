package overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import overlay.wireformats.Message;
import overlay.wireformats.Register;

public class TCPReceiverThread implements Runnable {

	private Socket socket;
	private DataInputStream din;
	private boolean debug;
	
	private final int REGISTER = 0;
	private final int DEREGISTER = 1;
	
	public TCPReceiverThread(Socket socket, boolean debug) throws IOException {
		this.socket = socket;
		this.debug = debug;
		din = new DataInputStream(socket.getInputStream());
	}
	
	@Override
	public void run() {
		if (debug) System.out.println("  TCPReceiverThread spawned.");
		int dataLength;
		while (socket != null) {
			try {
				if (debug) System.out.println("  TCPReceiverThread waiting for message length...");
				dataLength = din.readInt();
				if (debug) System.out.println("  TCPReceiverThread received message of length: " + dataLength);
				if (debug) System.out.println("  TCPReceiverThread awaiting byte array message delivery...");
				byte[] data = new byte[dataLength];
				din.readFully(data,  0,  dataLength);
				String str = new String(data, "UTF-8");
				if (debug) System.out.println("  TCPReceiverThread received byte[]: " + str);
				interpret(str);
			} catch (SocketException se) {
				System.out.println(se.getMessage());
				break;
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
				break;
			}
		}
		if (debug) System.out.println("  TCPReceiverThread exiting.");
		
	}

	// Interpret the received message
	private void interpret(String str) {
		// Identify the message type
		String[] msgFields = str.split(" ");
		int msgType = Integer.parseInt(msgFields[0]);
		
		// Register message
		if (msgType == REGISTER) {
			if (debug) System.out.println("  TCPReceiver received REGISTER message...");
		}
		else if (msgType == DEREGISTER) {
			if (debug) System.out.println("  TCPReceiver received DEREGISTER message...");
		}
		else {
			if (debug) System.out.println("  TCPReceiver received unknown message.");
		}
		
	}

}
