import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReceiverThread extends Thread {
	DatagramSocket sock;
	public static ArrayList<Integer> mostRecent = new ArrayList<Integer>();
	public static int recentlySentCwd = 2;
	public static int receivedAcksCount = 0;

	ReceiverThread(DatagramSocket sock) {
		this.sock = sock;
	}

	public void run() {
		while (true) {
			byte[] ack = new byte[512];
			DatagramPacket receivedAckPacket = new DatagramPacket(ack, ack.length);

			try {
				sock.receive(receivedAckPacket);
				String receivedSeqNo = new String(receivedAckPacket.getData());
				mostRecent.add(Integer.parseInt(receivedSeqNo.trim()));
				System.out.println("Ack received for " + receivedSeqNo.trim());
				receivedAcksCount++;
				try {
					this.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(receivedAcksCount + "  " + recentlySentCwd);
				if (receivedAcksCount == recentlySentCwd) {

					synchronized (Sender.congestionWindow) {

						// System.out.println("Getting lock");
						int tempSize = mostRecent.size();

						if (mostRecent.get(tempSize - 1) == mostRecent.get(tempSize - 2)) {
							Sender.ssThresh = (int) Sender.cwnd / 2;
							Sender.cwnd = 2;

							// add that duplicate packet to the window
						} else {
							if (Sender.cwnd * 2 <= Sender.ssThresh) {
								Sender.cwnd = Sender.cwnd * 2;

							} else
								Sender.cwnd = Sender.cwnd + 2;
						}

						recentlySentCwd = Sender.cwnd;
						receivedAcksCount = 0;
						Sender.congestionWindow.notifyAll();
					}

				} else {
					int tempSize = mostRecent.size();
					if (tempSize >= 2) {
						if (mostRecent.get(tempSize - 1) == mostRecent.get(tempSize - 2)) {
							synchronized (Sender.congestionWindow) {

								Sender.ssThresh = (int) Sender.cwnd / 2;
								Sender.cwnd = 2;
								recentlySentCwd = Sender.cwnd;
								receivedAcksCount = 0;
								Sender.congestionWindow.notifyAll();

							}
						}
					}
					// add that duplicate packet to the window

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
