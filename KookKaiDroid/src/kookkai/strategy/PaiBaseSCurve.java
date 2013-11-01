package kookkai.strategy;

import ivy.kookkai.ai.PakawiNz_AI;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.vision.BlobObject;

import com.example.udpfootballer.UDPClient;

public class PaiBaseSCurve implements StrategyTemplate{
	UDPClient messenger;
	int state;
	PakawiNz_AI player;
	
	String out;
	
	boolean nearestPoleOnLeft;
	boolean nearestPoleOnRight;
	double lkpp_x, lkpp_y;

	
	public PaiBaseSCurve(PakawiNz_AI player) {
		this.player = player;
		this.state = 0;
		messenger = new UDPClient(8324);
		messenger.add("192.168.1.106");
	}
	
	public void poleCheck() {
		double min_x = 1000;
		double min_y = 1000;
		boolean foundPole = false;
		for(BlobObject b : GlobalVar.mergeResult) {
			/*out += "Rectangle Position : " + b.posRect + "\n";
			out += "Centroid Position : " + b.centroidX + "\n";
			out += "PixelCount : " + b.pixelCount + "\n";
			out += "Tag : " + b.tag + "\n";
			out += "\n";*/
			if(b.posRect.exactCenterY() < min_y) {
				min_x = b.posRect.exactCenterX();
				min_y = b.posRect.exactCenterY();
				foundPole = true;
			}
		}		
		out += "Goal POS X : " + min_x;
		if(!foundPole) {
			nearestPoleOnLeft = false;
			nearestPoleOnRight = false;
			return;
		}
		if(min_x > 120) {
			nearestPoleOnLeft = false;
			nearestPoleOnRight = true;
			return;
		} else {
			nearestPoleOnLeft = true;
			nearestPoleOnRight = false;
			return;
		}
	}
	
	public String run() {
		out = "";
		out += "[Start]\n";
		out += "[End]\n";
		poleCheck();
		switch(state) {
			case 0: {
				if(nearestPoleOnLeft) {
					player.goAroundRightFaceLeft();
					out += "goAroundRight - FaceLeft\n";
					break;
				}
				state += 1;
				break;
			}
			case 1: {
				if(nearestPoleOnRight) {
					player.goAroundLeftFaceRight();
					out += "goAroundLeft - FaceRight\n";
					break;
				}
				state += 1;
			}
			case 2: {
				player.goStraight();
			}
		}
		
		messenger.sendMessage(out);
		return out;
	}
	
}
