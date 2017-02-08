package overlay.wireformats;

public class LinkWeights extends Message {

	
	public LinkWeights (MessagingNodesList mnList) {
		type = 4;
		this.identifier = mnList.toString();
	}
}
