package ivy.kookkai;

import ivy.kookkai.data.ColorPlate;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.debugview.CameraInterface;
import ivy.kookkai.vision.ColorManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

public class Network {
	// cache
	// current image
	public static final byte[] imgYUV = new byte[518400];
	private static YuvImage jpgYUV;
	private static byte[] byteYUV;
	// enum

	private static final int port = 24445;
	private DatagramSocket socket;

	private Network() {
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private final byte[] buffer = new byte[65535];
	private static final int maxdatagramsize = 50000;

	private void receiveandresponse() throws IOException, ClassNotFoundException {
		// System.out.println("waiting");
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		// System.out.println("receiving");
		byte[] recvmsg = packet.getData();
		int recvmsglen = packet.getLength();

		// /////////////////////////

		// decode
		String req = new String(recvmsg, 0, recvmsglen);
		// System.out.println("network received: " + req);

		// encode
		InetAddress address = packet.getAddress();
		int port = packet.getPort();

		if (req.contains("imgyuv")) {
			int idx = Integer.parseInt(req.substring(6));
			packet = new DatagramPacket(imgYUV, idx * maxdatagramsize,
					Math.min(maxdatagramsize, imgYUV.length - maxdatagramsize
							* idx), address, port);
		} else if (req.contains("createjpeg")) {
			createJpeg();
			byte[] imglen = new String("" + byteYUV.length).getBytes();
			packet = new DatagramPacket(imglen, imglen.length, address, port);
		} else if (req.contains("getjpeg")) {
			int idx = Integer.parseInt(req.substring(7));
			packet = new DatagramPacket(byteYUV, idx * maxdatagramsize,
					Math.min(maxdatagramsize, byteYUV.length - maxdatagramsize
							* idx), address, port);
		} else if (req.contains("setcolorlist")) {
			byte[] sendmsg = new String("ready").getBytes();
			DatagramPacket dummymsg = new DatagramPacket(sendmsg,
					sendmsg.length, address, port);
			socket.send(dummymsg);
			packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			ByteArrayInputStream bais = new ByteArrayInputStream(
					packet.getData(), 0, packet.getLength());
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object obj = ois.readObject();
			if (obj instanceof ArrayList<?>) {
				ColorManager.colorList = (ArrayList<ColorPlate>) obj;
			}
			ColorManager.createColorHashMap();
			packet = dummymsg;
		} else if (req.contains("getcolorlist")) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(ColorManager.colorList);
			byte[] msg = baos.toByteArray();
			packet = new DatagramPacket(msg, msg.length, address, port);
		} else if (req.contains("writecolorlist")) {
			ColorManager.writeColorList();
			byte[] sendmsg = new String("ready").getBytes();
			packet = new DatagramPacket(sendmsg, sendmsg.length, address, port);
		} else if (req.contains("readcolorlist")) {
			GlobalVar.initVar();
			byte[] sendmsg = new String("ready").getBytes();
			packet = new DatagramPacket(sendmsg, sendmsg.length, address, port);
		}
		socket.send(packet);
		// System.out.println("network sent: " + packet.getLength() + ", done");
	}

	public static void createJpeg() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		jpgYUV = new YuvImage(imgYUV, CameraInterface.previewformat, 640, 480,
				null);
		// jpgYUV = new YuvImage(imgYUV, ImageFormat.NV21, 720, 480, null);
		jpgYUV.compressToJpeg(new Rect(0, 0, 640, 480), 80, baos);
		byteYUV = baos.toByteArray();
	}

	private static NetworkThread thread = null;

	public static void createThread() {
		if (thread == null || !thread.isAlive()) {
			thread = new NetworkThread();
			System.out.println("network thread created");
			thread.start();
			System.out.println("setwork thread started");
		}
	}

	public static void destroyThread() {
		thread.interrupt();
		thread.instance.socket.close();
		thread = null;
		System.out.println("network thread killed");
	}

	private static class NetworkThread extends Thread {
		public Network instance;

		public void run() {
			instance = new Network();
			while (true) {
				try {
					instance.receiveandresponse();
				} catch (SocketException e) {
					e.printStackTrace();
					break;
				} catch (IOException e) {
					e.printStackTrace();
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					// break;
				}
			}
		}
	}
}
