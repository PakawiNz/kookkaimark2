package kookkai.strategy;

import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.refbox.TeamInfo;

import com.example.udpfootballer.KookKaiStateAndAction;
import com.example.udpfootballer.KookKaiTwin;

public class FetcherPeaceful implements StrategyTemplate{
	private KookKaiStateAndAction myState;
	private KookKaiTwin twin;
	private FetchBall fetcher;
	
	long timeStart = 0;
	boolean timeSet = false;
	boolean timeReady = false;
	
	public FetcherPeaceful(FetchBall fetcher) {
		myState = KookKaiStateAndAction.getInstance();
		twin = KookKaiTwin.getInstance();
		this.fetcher = fetcher;		
	}

	public String run() {
		String out = "";
		if (!GlobalVar.committeeAllowMeToPlay(fetcher)) {
			fetcher.forceReady();
			return "[REF BOX BLOCK]";
		}
		
		// if state < 0 then robot is getting up so wait until robot stand
		// successfully.
		if (fetcher.isFalling()) {
			fetcher.gettingUp();
			myState.setState(KookKaiStateAndAction.STATE_FALLING);
			myState.setAction(KookKaiStateAndAction.ACTION_GETTING_UP);
			return "STATUS : waiting for stand up.";
		}

		// if robot fall down ;
		if (fetcher.isStartFalling()) {
			fetcher.startGettingUp();
			myState.setState(KookKaiStateAndAction.STATE_FALLING);
			myState.setAction(KookKaiStateAndAction.ACTION_START_GETTING_UP);
			return "STATUS : standing up.";
		} else {
			fetcher.resetFallCounter();
		}
		
		myState.unSetState(KookKaiStateAndAction.STATE_FALLING);
		
		if (GlobalVar.ballPos[2] < 0 /* not found ball */) {
			fetcher.findBall();
			myState.setAction(KookKaiStateAndAction.ACTION_FINDING_BALL);
			return "STATUS : ball not found finding ball";
		}

		myState.setState(KookKaiStateAndAction.STATE_BALL_FOUND);
		out += "STATUS : ball Found\n";
		
		if (!twin.isState(KookKaiStateAndAction.STATE_BALL_FOUND) || /*twin can't find any ball*/
				twin.isState(KookKaiStateAndAction.STATE_FALLING) || /*twin is falling*/
				GlobalVar.ballPos[3] > twin.getBallPixels() /*my ball pixels are bigger than my twin */) {
			//play by my self

			// use default goal position as the leftmost
			if (GlobalVar.goalPos[2] > 0) {
				// if goal found, align himself, ball, and goal as a straight line
				out += "goal found\n";
				myState.setState(KookKaiStateAndAction.STATE_GOAL_FOUND);
			} else {
				// if not just walk toward ball, and turn a little to try to locate goal
				out += "goal not found\n";
				myState.unSetState(KookKaiStateAndAction.STATE_GOAL_FOUND);
			}

			if (fetcher.readyToKickBall()) {
				myState.setAction(KookKaiStateAndAction.ACTION_STRIKE_BALL);
				fetcher.runToBall();
			} else {
				myState.setAction(KookKaiStateAndAction.ACTION_ALIGN_SELF);
				fetcher.alignMeMyBallGoal();
			}
		} else {
			myState.setAction(KookKaiStateAndAction.ACTION_TRACKING_BALL);
			fetcher.trackBall();
			out += "found ball set in ready position";
		}
		return out;
	}
}
