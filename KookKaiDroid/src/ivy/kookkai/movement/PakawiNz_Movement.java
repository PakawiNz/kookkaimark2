package ivy.kookkai.movement;

import android.R.bool;
import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.movement.MovementTemplate;
import ivy.kookkai.strategy.ChampStateFull;
import ivy.kookkai.strategy.StrategyTemplate;
import ivy.kookkai.vision.SensorInterface;

public class PakawiNz_Movement implements MovementTemplate {

	private static final int CENTER_BallX = 0;
	private static final int TRESH_BallX = 50;
	private static final int CENTER_GoalX = 0;
	private static final int TRESH_GoalX = 35;
	private static final int FINE_TRESH_GoalX = 5;

	private static final int OnLEFT = -2;
	private static final int OnCEN_L = -1;
	private static final int OnCENTER = 0;
	private static final int OnCEN_R = 1;
	private static final int OnRIGHT = 2;

	private static final int LEFT = 0;
	private static final int RIGHT = 1;

	private int ballState = 0;
	private int[] goalState;

	private String out = "";
	private KookKaiAndroidAPI api;

	public PakawiNz_Movement(KookKaiAndroidAPI api) {
		this.api = api;
		goalState = new int[2];
	}

	// -------------------------------->> STATE HANDLING <<--------------------------------//

	private void updateCurrentState() {

		if (GlobalVar.ballPos[0] - CENTER_BallX < -TRESH_BallX) {
			ballState = OnLEFT;
		} else if (GlobalVar.ballPos[0] - CENTER_BallX > TRESH_BallX) {
			ballState = OnRIGHT;
		} else {
			ballState = OnCENTER;
		}

		if (GlobalVar.goalPosL[0] - CENTER_GoalX < -TRESH_GoalX) {
			goalState[LEFT] = OnLEFT;
		} else if (GlobalVar.goalPosL[0] - CENTER_GoalX > TRESH_GoalX) {
			goalState[LEFT] = OnRIGHT;
		} else {
			if (GlobalVar.goalPosL[0] - CENTER_GoalX < -FINE_TRESH_GoalX) {
				goalState[LEFT] = OnCEN_L;
			} else if (GlobalVar.goalPosL[0] - CENTER_GoalX > FINE_TRESH_GoalX) {
				goalState[LEFT] = OnCEN_R;
			} else {
				goalState[LEFT] = OnCENTER;
			}
		}

		if (GlobalVar.goalPosR[0] - CENTER_GoalX < -TRESH_GoalX) {
			goalState[RIGHT] = OnLEFT;
		} else if (GlobalVar.goalPosR[0] - CENTER_GoalX > TRESH_GoalX) {
			goalState[RIGHT] = OnRIGHT;
		} else {
			if (GlobalVar.goalPosR[0] - CENTER_GoalX < -FINE_TRESH_GoalX) {
				goalState[RIGHT] = OnCEN_L;
			} else if (GlobalVar.goalPosR[0] - CENTER_GoalX > FINE_TRESH_GoalX) {
				goalState[RIGHT] = OnCEN_R;
			} else {
				goalState[RIGHT] = OnCENTER;
			}
		}

	}

	private boolean goal_is_on(int goal, int leftMost, int rightMost) {
		for (int i = leftMost; i <= rightMost; i++) {
			if (goalState[goal] == i)
				return true;
		}
		return false;
	}

	private int nearestGoal() {
		if (Math.abs(GlobalVar.goalPosL[0]) < Math.abs(GlobalVar.goalPosR[0])) {
			return OnLEFT;
		} else {
			return OnRIGHT;
		}
	}

	private boolean goal_is_found(int goal) {
		if(goal == LEFT)
			return GlobalVar.goalPosL[2] > 0;
		else
			return GlobalVar.goalPosR[2] > 0;
	}

	// -------------------------------->> WALKING HANDLING <<--------------------------------//

	@SuppressWarnings("unused")
	private void alignLeft() {
		out += "ALIGN LEFT\n";
		api.walkingNonLimit(-10, 0, 50);
	}

	@SuppressWarnings("unused")
	private void alignRight() {
		out += "ALIGN RIGHT\n";
		api.walkingNonLimit(10, 0, -50);
	}

	@SuppressWarnings("unused")
	private void folllowLeft() {
		out += "FOLLOW LEFT\n";
		api.walkingNonLimit(5, 30, 0);
	}

	@SuppressWarnings("unused")
	private void followRight() {
		out += "FOLLOW RIGHT\n";
		api.walkingNonLimit(-5, 30, 0);
	}

	private void rotateLeft() {
		out += "ROTATE LEFT\n";
		api.walkingNonLimit(0, 0, -80);
	}

	private void rotateRight() {
		out += "ROTATE RIGHT\n";
		api.walkingNonLimit(0, 0, 80);
	}

	private void slideLeft() {
		out += "SLIDE LEFT\n";
		api.walkingNonLimit(10, 0, 0);
	}

	private void slideRight() {
		out += "SLIDE RIGHT\n";
		api.walkingNonLimit(-10, 0, 0);
	}

	private void goStraight() {
		out += "GO STRAIGHT\n";
		api.walkingNonLimit(0, 30, 0);
	}

	private void goSlow() {
		api.walkingNonLimit(0, 10, 0);
	}

	// -------------------------------->> MOVEMENT COMMAND <<--------------------------------//

	public void findBall() {
		updateCurrentState();
		out += "FINDBALL > ";
		if (ballState == OnLEFT) {
			rotateLeft();
		} else if (ballState == OnRIGHT) {
			rotateRight();
		} else if (ballState == OnCENTER) {
			goStraight();
		}
	}

	public void trackBall() {
		updateCurrentState();
		out += "TRACKBALL > ";
		if (ballState == OnLEFT) {
			// folllowLeft();
			rotateLeft();
		} else if (ballState == OnRIGHT) {
			// followRight();
			rotateRight();
		} else if (ballState == OnCENTER) {
			goStraight();
		}
	}

	public void walkToBall() {
		out += "RUN TO BALL > ";
		if (ballState == OnLEFT) {
			rotateLeft();
		} else if (ballState == OnRIGHT) {
			rotateRight();
		} else if (ballState == OnCENTER) {
			goSlow();
		}
	}

	public void playBall() {
		out += "PLAY BALL > ";
		goStraight();
	}

	public void kickBall() {
		out += "KICK BALL > ";
		api.playSaveMotion(3);
		// goStraight();
	}

	public void walkToSetupPosition() {
		goStraight();
	}

	public void changeDirection() {
		out += "CHANGE DIRECTION > ";
		// api.playSaveMotion(2);
		slideRight();
	}

	public int prepareKick() {
		out += "Prepare!!\n";
		updateCurrentState();
		if (!GlobalVar.isGoalDirection) return -1;
		if (goal_is_found(LEFT) && goal_is_found(RIGHT)) {
			return 1;
		} else if (goal_is_found(LEFT)) {
			if (goal_is_on(LEFT, OnLEFT, OnCEN_L)) {
				return 1;
			}else{
				alignRight();
			}
		} else if (goal_is_found(RIGHT)) {
			if (goal_is_on(RIGHT, OnCEN_R, OnRIGHT)) {
				return 1;
			}else{
				alignLeft();
			}
		} else {
			if(nearestGoal() == OnLEFT){
				alignRight();
			}else{
				alignLeft();
			}
		}
		return prepareKick();
	}

	public void standingUp() {
		if (GlobalVar.falling_state != SensorInterface.FALL_FACEUP) {
			api.playSaveMotion(0); // stand up
		}
		if (GlobalVar.falling_state != SensorInterface.FALL_FACEDOWN) {
			api.playSaveMotion(1); // flip
			api.playSaveMotion(0); // stand up
		}
	}

	public void forceReady() {
		api.ready();
	}

	public String getMSG() {
		String a = out;
		out = "";
		return a;
	}
}