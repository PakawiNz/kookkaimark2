package kookkai.strategy;

import ivy.kookkai.ai.PakawiNz_AI;
import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;

public class ChampStateFull implements StrategyTemplate{
	KookKaiAndroidAPI api;
	PakawiNz_AI ai;

	long timeLeft = 0;
	long lastTime = 0;

	private boolean lock(){
		timeLeft -= System.currentTimeMillis() - lastTime;
		if(timeLeft < 0) {
			return false;
		} else{
			lastTime = System.currentTimeMillis();
			return true;
		}
	}

	private void lock(int ms){
		lastTime = System.currentTimeMillis();
		timeLeft = ms;
	}

	public ChampStateFull(PakawiNz_AI ai) {
		this.ai = ai;
		this.api = ai.api;
	}
	public String run() {			//ball pos :: 0 X : 1 Y : 2 SIZE;
		ai.updateCurrentState();
		if(lock()) return "LOCK";

		if(GlobalVar.ballPos[2] > 0){
			if(GlobalVar.ballPos[1] < 100){
				int readyToKick = ai.prepareKick();
				if(readyToKick == 1){
					ai.kickBall();
					lock(2000);
				}else if(readyToKick == -1){
					ai.changeDirection();
					lock(10000);
				}else {
					lock(300);
				}
			}else {
				ai.trackBall();
				lock(100);
			}
		} else {
			ai.findBall();
			lock(100);
		}


		if (ai.isStartFalling()) {
			ai.startGettingUp();
		} else {
			ai.resetFallCounter();
		}

		return "HOLY SHIT!!!";

	}

}