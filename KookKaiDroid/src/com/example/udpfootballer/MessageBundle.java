package com.example.udpfootballer;

import android.util.Log;

public class MessageBundle {
	private static final int BIT0 = (1 << 0);
	private static final int BIT1 = (1 << 1);
	private static final int BIT2 = (1 << 2);
	private static final int BIT3 = (1 << 3);
	private static final int BIT4 = (1 << 4);
	private static final int BIT5 = (1 << 5);
	private static final int BIT6 = (1 << 6);
	private static final int BIT7 = (1 << 7);
	private static final int BIT8 = (1 << 8);

	private int ballPixels;
	public static final int length = 4;
	private byte[] bytes;
	private int state;
	private long timeStamp;
	
	public MessageBundle() {
		ballPixels = 0;
		state = 0;
		bytes = new byte[length];
	}

	public MessageBundle setBallPixels(int pixels) {
		Log.d("message_bundle", "set to " + pixels);
		this.ballPixels = pixels;
		bytes[0] = (byte) (pixels >> 8);
		bytes[1] = (byte) (pixels /*% BIT7*/);
		return this;
	}
	
	public MessageBundle setStateAndAction(int state) {
		this.state = state;
		bytes[2] = (byte) state;
		return this;
	}
	
	public void setBytes(byte[] bytes) {
		ballPixels = (bytes[0] & 0xFF) * BIT8 + (bytes[1] & 0xFF);
		state = bytes[2];
		this.bytes = bytes;
	}
	
	public void markTimeStamp() {
		timeStamp = System.currentTimeMillis();
	}
	
	public int getBallPixels() {
		return ballPixels;
	}

	public int getStateAndAction() {
		return state;
	}
	
	public long getMessageTimeStamp() {
		return timeStamp;
	}
	
	public byte[] getBytes() {
		Log.d("message_bundle", "Byte[0] : " + bytes[0] + ", Byte[1] : " + bytes[1]);
		return bytes;
	}
	
	public static String byteToString(byte b) {
		String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		return s1;
	}
	
	public String toString() {
		String result = "";
		for(int i = 0;i < length;i++) {
			result += byteToString(this.bytes[i]) + " ";
		}
		return result; 
	}
}
