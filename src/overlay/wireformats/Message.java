package overlay.wireformats;

public abstract class Message {
	
	protected int type;
	protected long timestamp = System.nanoTime();
	protected String identifier;
	protected int tracker;
	
}
