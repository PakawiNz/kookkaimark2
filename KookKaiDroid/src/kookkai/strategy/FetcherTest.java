package kookkai.strategy;

import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.data.GlobalVar;

import com.example.udpfootballer.KookKaiStateAndAction;
import com.example.udpfootballer.KookKaiTwin;

public class FetcherTest implements StrategyTemplate{
	private FetchBall fetcher;
	private long time;
	private final static long TIME_PERIOD = 5000;

	public FetcherTest(FetchBall fetcher) {
		this.fetcher = fetcher;
		time = System.currentTimeMillis();
	}
	
	public String allTest() {
		String out = "[TESTING]\n";
		if(System.currentTimeMillis() - time < TIME_PERIOD) {
			if(fetcher.isFalling()) {
				fetcher.gettingUp();
				return out + "GETTING UP\n";
			} else if(fetcher.isStartFalling()){
				fetcher.startGettingUp();
				return out + "START GETTING UP\n";
			}
			return out + "STANDING STEADY\n";
		}
		if(System.currentTimeMillis() - time < TIME_PERIOD * 2) {
			fetcher.forceReady();
			return out + "FORCE READY\n";
		}
		if(System.currentTimeMillis() - time < TIME_PERIOD * 5) {
			out += "FINDING AND TRACKING BALL\n";
			if(GlobalVar.ballPos[2] < 0) {
				fetcher.findBall();
				return out + "FINDING BALL\n";
			} else {
				fetcher.trackBall();
				return out + "TRACKING BALL\n";

			}
		}
		if(System.currentTimeMillis() - time < TIME_PERIOD * 8) {
			out += "STRIKE THE BALL\n";
			if(fetcher.readyToKickBall()) {
				fetcher.runToBall();
				return out + "STRIKING BALL\n";
			} else {
				fetcher.alignMeMyBallGoal();
				return out + "ALIGNGING MySELF\n";
			}
		}
		if(System.currentTimeMillis() - time > TIME_PERIOD * 8) {
			time = System.currentTimeMillis();
		}

		return out;		
	}

	public void testStrikeBall() {
		fetcher.runToBall();
	}
	
	public String run() {
		testStrikeBall();
		return "";
	}
}
