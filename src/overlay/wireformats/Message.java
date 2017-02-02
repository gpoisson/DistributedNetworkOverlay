package overlay.wireformats;

import java.io.UnsupportedEncodingException;


public abstract class Message {
	
	protected int type;
	protected long timestamp;
	protected String identifier;
	protected int tracker;
	protected byte[] out;
	
	public byte[] getByteArray() {
		String outString = type + " " + timestamp + " " + identifier + " " + tracker;
		out = outString.getBytes();
		return out;
	}
	
	public String toString() {
		String str;
		try {
			str = new String(out, "UTF-8");
			return str;
		} catch (UnsupportedEncodingException e) {
			System.out.println(e);
		}
		return null;
	}
	
}
