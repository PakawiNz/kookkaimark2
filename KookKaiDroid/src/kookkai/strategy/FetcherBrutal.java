package kookkai.strategy;
import android.util.Log;

import com.example.udpfootballer.KookKaiStateAndAction;
import com.example.udpfootballer.KookKaiTwin;

import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.refbox.Constants;
import ivy.kookkai.refbox.KookKaiTeamInfo;
import ivy.kookkai.refbox.TeamInfo;

public class FetcherBrutal implements StrategyTemplate{
	private TeamInfo teaminfo;
	private KookKaiStateAndAction myState;
	private KookKaiTwin twin;
	private FetchBall fetcher;
	
	public FetcherBrutal(FetchBall fetcher) {
		myState = KookKaiStateAndAction.getInstance();
		twin = KookKaiTwin.getInstance();
		this.fetcher = fetcher;
	}
	
	public String run() {
		String out = "";

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
			if(!twin.isState(KookKaiStateAndAction.STATE_BALL_FOUND) ||
					twin.isState(KookKaiStateAndAction.STATE_FALLING)) {
				myState.setAction(KookKaiStateAndAction.ACTION_FINDING_BALL);
				fetcher.findBall();
				myState.unSetState(KookKaiStateAndAction.STATE_BALL_FOUND);
				return "STATUS : ball not found\n";		
			} else {
				if (GlobalVar.enemyPos[2] <  0) {
					fetcher.findEnemy();
				} else {
					myState.setAction(KookKaiStateAndAction.ACTION_STRIKE_ENEMY);
					fetcher.alignMeEnemyGoal();
				}
				myState.setAction(KookKaiStateAndAction.ACTION_STRIKE_ENEMY);
				return "STATUS : ball not found + Destroy enemy";
			}
		}

		myState.setState(KookKaiStateAndAction.STATE_BALL_FOUND);
		out += "STATUS : ball Found\n";
		
		if (!twin.isState(KookKaiStateAndAction.STATE_BALL_FOUND) || /*twin can't find any ball*/
				twin.isState(KookKaiStateAndAction.STATE_FALLING) || /*twin is falling*/
				GlobalVar.ballPos[3] > twin.getBallPixels() /*my ball pixels are bigger than my twin */) {
			//play by my self

			// use default goal position as the leftmost
			boolean foundGoal = false;
			out += "find correct goal: ";

			if (GlobalVar.goalPos[2] > 0) {
				// if goal found, align himself, ball, and goal as a straight line
				out += "goal found\n";
				myState.setState(KookKaiStateAndAction.STATE_GOAL_FOUND);
				foundGoal = true;
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
			//Strike enemy and believe in my twin
			if (GlobalVar.enemyPos[2] <  0) {
				fetcher.findEnemy();
			} else {
				myState.setAction(KookKaiStateAndAction.ACTION_STRIKE_ENEMY);
				fetcher.alignMeEnemyGoal();
			}
			myState.setAction(KookKaiStateAndAction.ACTION_STRIKE_ENEMY);
			out += "my twin's ball is better : Set and destroy";
		}
		return out;
	}
}
