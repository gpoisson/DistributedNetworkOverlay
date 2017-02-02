package overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TCPReceiverThread implements Runnable {

	private Socket socket;
	private DataInputStream din;
	private boolean debug;
	
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
				dataLength = din.readInt();
				
				byte[] data = new byte[dataLength];
				din.readFully(data,  0,  dataLength);
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

}
