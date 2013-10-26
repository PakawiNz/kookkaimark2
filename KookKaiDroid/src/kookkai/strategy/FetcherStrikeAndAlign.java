package kookkai.strategy;

import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.data.GlobalVar;

import com.example.udpfootballer.KookKaiStateAndAction;

public class FetcherStrikeAndAlign implements StrategyTemplate{
	FetchBall fetcher;
	public FetcherStrikeAndAlign(FetchBall fetcher) {
		this.fetcher = fetcher;
	}
	public String run() {
		if (GlobalVar.ballPos[2] > 0) {
			if (fetcher.readyToKickBall()) {
				fetcher.runToBall();
			} else {
				fetcher.alignMeMyBallGoal();
			}
		}	
		return "FUCK";
	}
	
}
