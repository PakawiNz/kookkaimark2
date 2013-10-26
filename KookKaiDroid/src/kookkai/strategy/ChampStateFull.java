package kookkai.strategy;

import com.example.udpfootballer.KookKaiStateAndAction;

import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;

public class ChampStateFull implements StrategyTemplate{
	KookKaiAndroidAPI api;
	FetchBall fetchBall;

	private final int LEFTBALL = (int)(GlobalVar.FRAME_WIDTH * (1.0/4.0));
	private final int RIGHTBALL = GlobalVar.FRAME_WIDTH - LEFTBALL;
	private final int LEFTGOAL = (int)(GlobalVar.FRAME_WIDTH * (1.0/4.0));
	private final int RIGHTGOAL = GlobalVar.FRAME_WIDTH - LEFTGOAL;
	
	long lastTime = 0;

	private boolean lock(int ms){
		if(System.currentTimeMillis() - lastTime < ms) {
			return false;
		} else{
			lastTime = System.currentTimeMillis();
			return true;
		}
	}
	
	public ChampStateFull(FetchBall fetchBall) {
		this.fetchBall = fetchBall;
		this.api = fetchBall.api;
	}
	public String run() {			//ball pos :: 0 X : 1 Y : 2 SIZE;
		if(!lock(300)){
			if(GlobalVar.ballPos[0] < LEFTBALL){
				api.walkingNonLimit(0, 10, -200);
			}else if(GlobalVar.ballPos[0] > RIGHTBALL){
				api.walkingNonLimit(0, 10, 200);
			}else if (GlobalVar.ballPos[2] > 0) {
				if(GlobalVar.goalPos[0] > LEFTGOAL){
					api.walkingNonLimit(-10, 0, -100);
				}else if(GlobalVar.goalPos[0] < RIGHTGOAL){
					api.walkingNonLimit(10, 0, 100);
				}else {
					if(GlobalVar.isGoalDirection())
						api.walkingNonLimit(0, 40, 0);	
				}
			}
		}
		
		if (fetchBall.isStartFalling()) {
			fetchBall.startGettingUp();
		} else {
			fetchBall.resetFallCounter();
		}
		return "HOLY SHIT!!!";
		
	}

}