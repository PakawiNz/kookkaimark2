package com.example.udpfootballer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;

import android.util.Log;

public class UDPClient implements Runnable { 
	public final int DEFAULT_PORT = 50003; 
	private byte[] bytes;
	private DatagramSocket socket;
	private int serverPort;
	private ArrayDeque<InetAddress> serverIps;
	private boolean isRunning;
	private Thread thread;
		
	public UDPClient(ArrayDeque<InetAddress> serverIps) {
		this.serverIps = serverIps;
		this.serverPort = DEFAULT_PORT;
		this.isRunning = false;
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(300);
		} catch (Exception e) { return; }
		Log.d("Comm_Client", "Client created");
	}

	public UDPClient() {
		this(new ArrayDeque<InetAddress>());
	}

	public UDPClient(int serverPort) {
		this();
		this.serverPort = serverPort;
	}	

	public UDPClient(ArrayDeque<InetAddress> serverIps, int serverPort) {
		this(serverIps);
		this.serverPort = serverPort;
	}
	
	public void add(String ip) {
		try {
			this.serverIps.add(InetAddress.getByName(ip));
		} catch (UnknownHostException e) {
			Log.d("Comm_Client", "Error IP");
		}
	}

	public void run() { 
		ArrayDeque<InetAddress> serverIps = this.serverIps.clone();
		this.isRunning = true;
		while(!serverIps.isEmpty() && this.isRunning) {
			byte[] sendData = bytes;
			try {
				InetAddress serverAddr = serverIps.remove();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddr, serverPort); 
				Log.d("Comm_Client", "Package Sent");
				socket.send(sendPacket);
			} catch (Exception e) { 
				Log.d("Comm_Client", "Exception While Thread");
			}
		}
		this.isRunning = false;
	}

	public void sendMessage(byte[] bytes) {
		//Log.d("client_sendMessage", "BYTE = " + MessageBundle.byteToString(bytes[2]) + ", " + MessageBundle.byteToString(bytes[1]) + ", " + MessageBundle.byteToString(bytes[0]));
		try {
			if(this.isRunning) {
				this.isRunning = false;
				while(this.thread.isAlive());
			}
			this.bytes = bytes;
			Log.d("Comm_Client", "New Thread");
			thread = new Thread(this, "Client Thread");
			thread.start();
		} catch (Exception e) { return; }
	}
	
	public void sendMessage(String message) {
		byte[] bytes= message.getBytes(); 
		sendMessage(bytes);
	}	
} 