package ivy.kookkai.strategy;

import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.movement.JamornSCurve;
import ivy.kookkai.vision.BlobObject;

import com.example.udpfootballer.UDPClient;

public class JamornSCurvePlan implements StrategyTemplate{
	UDPClient messenger;
	int state;
	JamornSCurve player;
	
	String out;
	
	boolean nearestPoleOnLeft;
	boolean nearestPoleOnRight;
	double lkpp_x, lkpp_y;

	
	public JamornSCurvePlan(JamornSCurve player) {
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
		/*switch(state) {
		 *	//start
			case 0: {
				
			}
			//pass first pole
			case 1: {
				if(nearestPoleOnRight) {
					player.goAroundLeftFaceRight();
					out += "goAroundLeft - FaceRight\n";
					break;
				}
				state += 1;
			}
			//pass second pole
			case 2: {
				player.goStraight();
			}
		}*/
		
		messenger.sendMessage(out);
		return out;
	}
	
}
