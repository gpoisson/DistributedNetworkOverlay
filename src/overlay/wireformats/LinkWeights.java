package overlay.wireformats;

public class LinkWeights extends Message {

	int numberOfLinks;
	
	public LinkWeights (int numLinks, MessagingNodesList mnList) {
		type = 4;
		this.numberOfLinks = numLinks;
		this.identifier = mnList.toString();
	}
}
