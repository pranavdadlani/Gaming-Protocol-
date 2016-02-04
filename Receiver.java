import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Receiver {
	public static int seqCount = 1;

	public static Set<Packet> receivedPackets = new TreeSet<Packet>();

	public static void main(String[] args) {

		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(4301);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		byte[] incomingData = new byte[2000];
		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

		while (true) {
			try {
				socket.receive(incomingPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] data = incomingPacket.getData();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = null;
			try {
				is = new ObjectInputStream(in);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// boolean flag = false;
			try {
				Packet receivedPacket = (Packet) is.readObject();

				is.close();
				// if (receivedPacket.getSequenceNo() != 12 && flag == false) {
				// flag = true;
				// System.out.println("ack sending for " +
				// receivedPacket.getSequenceNo());
				if (receivedPacket.getSequenceNo() == seqCount) {
					seqCount = seqCount + 1;
					receivedPackets.add(receivedPacket);

				}

				byte[] ackBytes = new Integer(seqCount).toString().getBytes();
				socket.send(new DatagramPacket(ackBytes, ackBytes.length, incomingPacket.getAddress(),
						incomingPacket.getPort()));
			
				if (receivedPacket.endBit == true && seqCount == receivedPacket.getSequenceNo() + 1) {
					// end of file
					System.out.println("File received");

					joinPackets();
					break;

				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static void joinPackets() throws FileNotFoundException, IOException {

		List<byte[]> list = new ArrayList<byte[]>();
		Iterator<Packet> receivedPacketIterator = receivedPackets.iterator();
		int temp = 0;

		while (receivedPacketIterator.hasNext()) {
			Packet currentPacket = receivedPacketIterator.next();
			System.out.println(currentPacket.getSequenceNo());
			byte[] b = currentPacket.getContent();
			list.add(b);
			temp += b.length;

		}
		System.out.println("byte array size " + temp + "   " + list.size());


		FileOutputStream out = new FileOutputStream("testIbm111.pdf");
		for (byte[] _b : list)
			out.write(_b);
		out.flush();
		out.close();
		// TODO Auto-generated method stub

	}

}
