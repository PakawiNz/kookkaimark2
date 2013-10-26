package com.example.udpfootballer;

//This will be implemented as singleton

public class KookKaiStateAndAction {
	private static KookKaiStateAndAction instance = new KookKaiStateAndAction();
	
	public final static int STATE_BALL_FOUND = (1 << 4);
	public final static int STATE_GOAL_FOUND = (1 << 5);
	public final static int STATE_ENEMY_FOUND = (1 << 6);
	public final static int STATE_FALLING = (1 << 7);

	public final static int ACTION_ALIGN_SELF = 1;
	public final static int ACTION_STRIKE_BALL = 2;
	public final static int ACTION_FINDING_BALL = 3;
	public final static int ACTION_STRIKE_ENEMY = 4;
	public final static int ACTION_START_GETTING_UP = 5;
	public final static int ACTION_GETTING_UP = 6;
	public final static int ACTION_STANDING_READY = 7;
	public final static int ACTION_TRACKING_BALL = 8;
	public final static int UNINITIAL = 0;
	
	private int state;
	
	protected KookKaiStateAndAction() {
		state = 0;
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
	
	public byte[] getBytes() {
		byte[] bytes = new byte[1];
		bytes[0] = (byte) state;
		return bytes;
	}
	
	public boolean isState(int action) {
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
	
	public static synchronized KookKaiStateAndAction getInstance() {
		return instance;
	}
}
