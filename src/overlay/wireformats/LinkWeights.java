package overlay.wireformats;

public class LinkWeights extends Message {

	
	public LinkWeights (MessagingNodesList mnList) {
		type = 5;
		this.identifier = mnList.toString();
	}
}
