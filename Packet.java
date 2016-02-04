import java.io.Serializable;

public class Packet implements Serializable, Comparable<Packet> {

	private byte[] content;
	private int sequenceNo;
	boolean endBit;
	public long sendTime;
	public long receiveTime;

	Packet() {
		endBit = false;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(int sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	Packet(byte[] content, int sequenceNo) {
		this.content = content;
		this.sequenceNo = sequenceNo;
	}

	@Override
	public int compareTo(Packet o) {
		int compareQuantity = ((Packet) o).getSequenceNo();

		// TODO Auto-generated method stub
		return this.getSequenceNo() - compareQuantity;
	}

}
