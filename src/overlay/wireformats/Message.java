package overlay.wireformats;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Calendar;

public abstract class Message {
	
	protected int type;
	protected Timestamp time;
	protected long timestamp;
	protected String identifier;
	protected int tracker;
	protected byte[] out;
	
	public byte[] getByteArray() {
		setTime();
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
	
	public void setTime() {

		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();
		this.time = new java.sql.Timestamp(now.getTime());
		timestamp = this.time.getTime();
	}
	
}
