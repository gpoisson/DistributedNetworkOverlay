package overlay.wireformats;

public class TaskInitiate extends Message {

	public TaskInitiate(int numRounds) {
		type = 6;
		identifier = numRounds + "";
	}
}
