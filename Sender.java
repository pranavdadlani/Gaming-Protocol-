import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Sender {
	public static Map<Integer, Boolean> ackMap = new TreeMap<>();
	public static int ssThresh = 20;
	public static int cwnd = 2;
	public static int timeout = 100; // timeout change later
	public static ArrayList<Packet> chunks = new ArrayList<>();
	public static ArrayList<Packet> congestionWindow = new ArrayList<>();
	public static boolean flagFinishSendingAll = false;
	public static boolean sendFlag = false;

	public static void main(String[] args) throws IOException, InterruptedException {
		DatagramSocket socket = null;

		InetAddress destinationIp = null;
		FileInputStream fin = null;
		BufferedInputStream bis = null;
		InetAddress ipaddress = null;

		try {
			ipaddress = InetAddress.getByName("localhost");
			destinationIp = InetAddress.getByName("localhost");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			socket = new DatagramSocket(4300, ipaddress);
			new ReceiverThread(socket).start();
		} catch (SocketException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		File file = new File("/Users/pranavdadlani/Desktop/Resume/PranavDadlaniResume.pdf");
		System.out.println("Length is " + file.length());
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		bis = new BufferedInputStream(fin);
		byte[] buffer = new byte[1024];

		int bytesRead;
		int currentSeqno = 0;
		int count = 0;
		int numberOfChunks = 0;
		if (file.length() % 1024 == 0)
			numberOfChunks = (int) (file.length() / 1024);
		else
			numberOfChunks = (int) (file.length() / 1024) + 1;

		System.out.println("No of chunks" + numberOfChunks);
		int currentCwd = 1;
		int a = 0;
		while ((bytesRead = bis.read(buffer)) != -1) {
			currentSeqno++;
			Packet tempPacket = new Packet(Arrays.copyOfRange(buffer, 0, bytesRead), currentSeqno);
			buffer = new byte[1024];
			a += bytesRead;
			chunks.add(tempPacket);
		}
		System.out.println(a);
		send(socket, destinationIp);

	}

	private static void send(DatagramSocket socket, InetAddress destinationIp)
			throws IOException, InterruptedException {
		int index = 0;

		while (index < chunks.size()) {
			System.out.println("index " + index);

			synchronized (congestionWindow) {
				System.out.println("cwnd size -> " + cwnd);
				congestionWindow.clear();
				// flagFinishSendingAll = false;

				for (int j = index; j < index + cwnd; j++) {
					if (j == chunks.size() - 1) {
						chunks.get(j).endBit = true;
						congestionWindow.add(chunks.get(j));

						break;
					}
					congestionWindow.add(chunks.get(j));
				}
				// congestionWindow.size()=cwd
				for (int j = 0; j < congestionWindow.size(); j++) {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(congestionWindow.get(j));
					byte[] data = outputStream.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(data, data.length, destinationIp, 4301);
			//		if (congestionWindow.get(j).getSequenceNo() == 5 && sendFlag == false) // if
					// 5,
					// then do not send

				//	{

					//	sendFlag = true;

			//		} else {
						socket.send(sendPacket);
				//	}

					os.flush();
					os.close();

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				try {
					// flagFinishSendingAll = true;

					// System.out.println("window sent");
					congestionWindow.notifyAll();
					congestionWindow.wait();
					// System.out.println("below wait");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			index = ReceiverThread.mostRecent.get(ReceiverThread.mostRecent.size() - 1) - 1;
		}

	}

}
