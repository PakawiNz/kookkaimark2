package com.example.udpfootballer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.util.Log;


public class UDPServer implements Runnable {
	public int DEFAULT_PORT = 50003;
	private ReceiverListener listener;
	private final Thread t;
	private int port;

	public UDPServer() {
		t = new Thread(this); 
		//Create blank receiverListener to prevent null pointer exception
		listener = new ReceiverListener() {
			@Override
			public void onReceive(byte[] a) {
				//Do nothing
			}
		};
		port = DEFAULT_PORT;
	}
	
	public UDPServer(int port) {
		this();
		this.port = port;
	}
	
	@Override
	public void run() { 
		while (true) {
			Log.d("Comm_Server", "Server Open");
			try { 
				DatagramSocket serverSocket = new DatagramSocket(port);
				serverSocket.setBroadcast(true);
				while (true) {
					byte[] receiveData = new byte[4];
					DatagramPacket receivePacket = new DatagramPacket(receiveData,
							receiveData.length);
					serverSocket.receive(receivePacket);
					listener.onReceive(receiveData);
					//InetAddress IPAddress = receivePacket.getAddress();
					//int port = receivePacket.getPort();
				}
			} catch (Exception e) {
				Log.d("Comm_Server", "Exception Happened");
			} 
		}
	} 
	
	public void setOnReceiveListener(ReceiverListener listener) {
		this.listener = listener;
	}
	
	public void start() {
		t.start();
	}
	
} 