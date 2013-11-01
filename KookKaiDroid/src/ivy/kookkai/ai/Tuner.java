package ivy.kookkai.ai;

import ivy.kookkai.api.KookKaiAndroidAPI;

import com.example.udpfootballer.ReceiverListener;
import com.example.udpfootballer.UDPServer;

public class Tuner implements AITemplate{
	private UDPServer udpserver;
	private int vx;
	private int vy;
	private int vz;
	private KookKaiAndroidAPI api;

	public Tuner(KookKaiAndroidAPI api) {
		this.api = api;
		udpserver = new UDPServer(8863);
		udpserver.start();
		udpserver.setOnReceiveListener(new ReceiverListener() {
			public void onReceive(byte[] a) {
				vx = (((a[0] & 0xFF)- 100) * 2);
				vy = (((a[1] & 0xFF) - 100) * 2);
				vz = (((a[2] & 0xFF)- 100) * 2);
			}
		});
	}
	
	public String execute() {
		String out = "";
		out += "VX : " + vx + "\n";
		out += "VY : " + vy + "\n";
		out += "VZ : " + vz + "\n";
		api.walkingNonLimit(vx, vy, vz);
		return out;
	}
	
}