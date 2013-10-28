package kookkai.strategy;

import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.refbox.TeamInfo;

import com.example.udpfootballer.KookKaiStateAndAction;
import com.example.udpfootballer.KookKaiTwin;

public class FetcherSinglePeaceful implements StrategyTemplate{
	private KookKaiStateAndAction myState;
	private FetchBall player;

	long timeStart = 0;
	boolean timeSet = false;
	boolean timeReady = false;

	
	public FetcherSinglePeaceful(FetchBall player) {
		myState = KookKaiStateAndAction.getInstance();
		this.player = player;		
	}

	public String run() {
		String out = "";
		out += "Ball Distance : " + GlobalVar.ballPos[1] + "\n";
		if (!GlobalVar.committeeAllowMeToPlay(player)) {
			player.forceReady();
			return "[REF BOX BLOCK]\n";
		}
		
		/*if(!timeSet) {
			timeStart = System.currentTimeMillis();
			fetcher.forceReady();
			timeSet = true;
			return out + "TIME SET";
		}
		if (!timeReady) {
			if(System.currentTimeMillis() - timeStart < 20000) {
				fetcher.forceReady();
				return out + "TIME COUNTING";
			} else {
				timeReady = true;
				return out + "TIME READY";
			}
		}*/

		/*if (!GlobalVar.committeeAllowMeToPlay(fetcher)) {
			fetcher.forceReady();
			return "[REF BOX BLOCK]";
		}*/
		// if state < 0 then robot is getting up so wait until robot stand
		// successfully.
		out += "[REF BOX PASS]\n";
		
		if (player.isFalling()) {
			player.gettingUp();
			myState.setState(KookKaiStateAndAction.STATE_FALLING);
			myState.setAction(KookKaiStateAndAction.ACTION_GETTING_UP);
			return "STATUS : waiting for stand up.";
		}

		// if robot fall down ;
		if (player.isStartFalling()) {
			player.startGettingUp();
			myState.setState(KookKaiStateAndAction.STATE_FALLING);
			myState.setAction(KookKaiStateAndAction.ACTION_START_GETTING_UP);
			return "STATUS : standing up.";
		} else {
			player.resetFallCounter();
		}
		
		myState.unSetState(KookKaiStateAndAction.STATE_FALLING);
		
		if (GlobalVar.ballPos[2] < 0 /* not found ball */) {
			player.findBall();
			myState.setAction(KookKaiStateAndAction.ACTION_FINDING_BALL);
			myState.unSetState(KookKaiStateAndAction.STATE_BALL_FOUND);
			return "STATUS : ball not found finding ball";
		}

		myState.setState(KookKaiStateAndAction.STATE_BALL_FOUND);
		out += "STATUS : ball Found\n";
		
		if (GlobalVar.goalPos[2] > 0) {
			// if goal found, align himself, ball, and goal as a straight line
			out += "goal found\n";
			myState.setState(KookKaiStateAndAction.STATE_GOAL_FOUND);
		} else {
			// if not just walk toward ball, and turn a little to try to locate goal
			out += "goal not found\n";
			myState.unSetState(KookKaiStateAndAction.STATE_GOAL_FOUND);
		}

		if (player.readyToKickBall()) {
			myState.setAction(KookKaiStateAndAction.ACTION_STRIKE_BALL);
			out += "running to Ball\n";
			player.walkToBall();
		} else {
			myState.setAction(KookKaiStateAndAction.ACTION_ALIGN_SELF);
			player.alignMeMyBallGoal();
			out += "aligning myself\n";
		}
		/*out+="x:"+(int)player.x+",y:"+(int)player.y+"z:"+(int)player.z+"\n";
		out+="vx:"+(int)player.vx+",vy:"+(int)player.vy+"vz:"+(int)player.vz+"\n";*/
		return out;
	}
}
