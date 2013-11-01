package ivy.kookkai.strategy;

import android.util.Log;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.movement.MovementTemplate;
import ivy.kookkai.vision.SensorInterface;

public class ChampStateFull implements StrategyTemplate {
	private long timeLeft = 0;
	private long lastTime = 0;

	private boolean lock() {
		timeLeft -= System.currentTimeMillis() - lastTime;
		if (timeLeft < 0) {
			return false;
		} else {
			lastTime = System.currentTimeMillis();
			return true;
		}
	}
	
	private MovementTemplate movement;
	private void lock(int ms) {
		lastTime = System.currentTimeMillis();
		timeLeft = ms;
	}

	private String out = "";
	private double fallCounter = 0;	
	
	private int macuState = 0;
	
	public ChampStateFull(MovementTemplate movement) {
		this.movement = movement;
	}

	public String run() { // ball pos :: 0 X : 1 Y : 2 SIZE;
		
		out = "";
		
		out += "\n";
		out += "GOAL L POSITION" + GlobalVar.goalPosL[0] + "\n";
		out += "GOAL L Y" + GlobalVar.goalPosL[1] + "\n";
		out += "GOAL R POSITION" + GlobalVar.goalPosR[0] + "\n";
		out += "GOAL R Y" + GlobalVar.goalPosR[1] + "\n";
		out += "BALL POSITION" + GlobalVar.ballPos[0] + "\n";
		out += "BALL Y " + GlobalVar.ballPos[1] + "\n";
		out += "BALL SIZE " + GlobalVar.ballPos[2] + "\n";
		out += "FALLING STATE " + GlobalVar.falling_state + "\n";
		out += "GOAL DIRECTION " + GlobalVar.isGoalDirection + "\n";
		out += "\n";

		/*if (!GlobalVar.committeeAllowMeToPlay(ai)) {
			api.ready();
			return "BLOCKED  ";
		}*/
		
		if (lock()){
			out += "[LOCK!!]\n";
			return out;
		}
		if (GlobalVar.ballPos[2] > 0) {
			if (GlobalVar.ballPos[1] < 100) {
				if(GlobalVar.ballPos[1] < 20){
					int readyToKick = movement.prepareKick();
					if (readyToKick == 1) {
						movement.playBall();
						lock(600);
					} else if (readyToKick == -1) {
						movement.changeDirection();
						lock(10000);
					} else {
						lock(300);
					}
				}else{
					movement.walkToBall();
					lock(100);
				}
			} else {
				movement.findBall();
				lock(100);
			}
		} else {
			movement.findBall();
			lock(100);
		}

		if (GlobalVar.falling_state != SensorInterface.FALL_NONE) {
			if (fallCounter < 7)
				fallCounter++;
			else {
				movement.standingUp();
				fallCounter = 0;
			}
		} else {
			fallCounter = 0;
		}
		
//		if(!x.equals("LOCK  "))
//			Log.d("pakawinz_debug",out + x);
		
		out += "[PLAYING]\n";
		out += movement.getMSG();
		return out;

	}

}