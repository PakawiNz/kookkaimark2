package com.example.udpfootballer;

//This will be implemented as singleton

public class KookKaiTwin {
	private static KookKaiTwin instance = new KookKaiTwin();
	private long timeStamp = 0;
	
	private int state;
	private int ballPixels;
	
	protected KookKaiTwin() {
		state = 0;
	}
	
	public void setBallPixels(int ballPixels) {
		this.ballPixels = ballPixels;
	}
	
	public int getBallPixels() {
		return ballPixels;
	}
	
	public void setAction(int action) {
		this.state &= (0xF0);
		this.state |= action;
	}
	
	public void setState(int state) {
		this.state |= state;
	}
	
	public void setStateAndAction(int state) {
		this.state = state;
	}
	
	public void unSetState(int state) {
		this.state &= ~state;
	}
	
	public int getStateAndAction() {
		return state;
	}
	
	public void markTimeStamp() {
		timeStamp = System.currentTimeMillis();
	}
	
	public long getTimestamp() {
		return timeStamp;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[1];
		bytes[0] = (byte) state;
		return bytes;
	}
	
	public boolean isState(int action) {
		if(System.currentTimeMillis() - getTimestamp() > 1000)
			return false;/*disconnect from friend more than 1 sec*/
		return ((action & state) != 0);
	}
	
	public int getAction() {
		return (this.state & 0x0F);
	}
	
	public void setBytes(byte[] bytes) {
		this.state = bytes[0];
	}
	
	public String toString() {
		String result = "";
		byte[] bytes = getBytes();
		for(int i = 0;i < 1;i++) {
			result += MessageBundle.byteToString(bytes[i]) + " ";
		}
		return result; 
	}
		
	public static synchronized KookKaiTwin getInstance() {
		return instance;
	}
}
