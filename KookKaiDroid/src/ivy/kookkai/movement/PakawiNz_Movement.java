package ivy.kookkai.movement;

import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.movement.MovementTemplate;
import ivy.kookkai.strategy.ChampStateFull;
import ivy.kookkai.strategy.StrategyTemplate;
import ivy.kookkai.vision.SensorInterface;

public class PakawiNz_Movement implements MovementTemplate {

	private static final int BallX_CENTER = 0;
	private static final int BallX_TRESH = 50;
	private static final int GoalX_CENTER = 0;
	private static final int GoalX_TRESH = 35;
	
	private static final int OnLEFT = -1;
	private static final int OnCENTER = 0;
	private static final int OnRIGHT = 1;
	
	private int ballState = 0;
	private int goalState = 0;
	
	private String out = "";
	private KookKaiAndroidAPI api;
	
	public PakawiNz_Movement(KookKaiAndroidAPI api) {
		this.api = api;
	}
	
	//-------------------------------->> STATE HANDLING <<--------------------------------//

	private void updateCurrentState(){
		
		if(GlobalVar.ballPos[0] - BallX_CENTER < -BallX_TRESH){
			ballState = OnLEFT;
		}else if(GlobalVar.ballPos[0] - BallX_CENTER > BallX_TRESH){
			ballState = OnRIGHT;
		}else {
			ballState = OnCENTER;
		}
		
		if(GlobalVar.goalPos[0] - GoalX_CENTER < -GoalX_TRESH){
			goalState = OnLEFT;
		}else if(GlobalVar.goalPos[0] - GoalX_CENTER > GoalX_TRESH){
			goalState = OnRIGHT;
		}else {
			goalState = OnCENTER;
		}
		
	}
	
	private void updateFineBallState(){
		
//		if(GlobalVar.ballPos[0] - BallX_CENTER < -2){
//			ballState = onLEFT;
//		}else if(GlobalVar.ballPos[0] - BallX_CENTER > 15){
//			ballState = onRIGHT;
//		}else {
//			ballState = onCENTER;
//		}
		
		if(GlobalVar.ballPos[0] - BallX_CENTER < -20){
			ballState = OnLEFT;
		}else if(GlobalVar.ballPos[0] - BallX_CENTER > 20){
			ballState = OnRIGHT;
		}else {
			ballState = OnCENTER;
		}
	}
	
	//-------------------------------->> WALKING HANDLING <<--------------------------------//
	
	@SuppressWarnings("unused")
	private void alignLeft(){
		out += "ALIGN LEFT\n";
		api.walkingNonLimit(-10, 0, 50);
	}

	@SuppressWarnings("unused")
	private void alignRight(){
		out += "ALIGN RIGHT\n";
		api.walkingNonLimit(10, 0, -50);
	}
	
	@SuppressWarnings("unused")
	private void folllowLeft(){
		out += "FOLLOW LEFT\n";
		api.walkingNonLimit(5, 30, 0);
	}

	@SuppressWarnings("unused")
	private void followRight(){
		out += "FOLLOW RIGHT\n";
		api.walkingNonLimit(-5, 30, 0);
	}

	private void rotateLeft(){
		out += "ROTATE LEFT\n";
		api.walkingNonLimit(0, 0, -80);
	}

	private void rotateRight(){
		out += "ROTATE RIGHT\n";
		api.walkingNonLimit(0, 0, 80);
	}
	
	private void slideLeft(){
		out += "SLIDE LEFT\n";
		api.walkingNonLimit(10, 0, 0);
	}

	private void slideRight(){
		out += "SLIDE RIGHT\n";
		api.walkingNonLimit(-10, 0, 0);
	}
	
	private void goStraight(){
		out += "GO STRAIGHT\n";
		api.walkingNonLimit(0, 25, -30);
	}

	private void goSlow(){
		api.walkingNonLimit(0, 10, -20);
	}
	
	//-------------------------------->> MOVEMENT COMMAND <<--------------------------------//
	
	public void findBall() {
		updateCurrentState();
		out += "FINDBALL > ";
		if(ballState == OnLEFT){
			rotateLeft();
		}else if(ballState == OnRIGHT){
			rotateRight();
		}else if(ballState == OnCENTER){
			goStraight();
		}
	}

	public void trackBall() {
		updateCurrentState();
		out += "TRACKBALL > ";
		if(ballState == OnLEFT){
			//folllowLeft();
			rotateLeft();
		}else if(ballState == OnRIGHT){
			//followRight();
			rotateRight();
		}else if(ballState == OnCENTER){
			goStraight();
		}
	}
	
	public void walkToBall() {
		out += "RUN TO BALL > ";
		goSlow();
	}
	
	public void playBall() {
		out += "PLAY BALL > ";
		goStraight();
	}
	
	public void kickBall() {
		out += "KICK BALL > ";
		api.playSaveMotion(3);
		//goStraight();
	}
	
	public void walkToSetupPosition() {
		goStraight();
	}
	
	public void changeDirection() {
		out += "CHANGE DIRECTION > ";
		//api.playSaveMotion(2);
		slideRight();
	}
	
	public int prepareKick() {
		out += "Prepare!!\n";
		updateFineBallState();
		if(ballState == OnLEFT){
			rotateLeft();
		}else if(ballState == OnRIGHT){
			rotateRight();
		}else {
			if(goalState == OnLEFT){
				slideRight();
			}else if(goalState == OnRIGHT){
				slideLeft();
			} else {
				if(GlobalVar.isGoalDirection){
					return 1;
				}else{
					return -1;
				}
			}
		}
		
//		if(goalState == onLEFT){
//			if(ballState == onLEFT){
//				rotateLeft();
//			}else if(ballState == onRIGHT){
//				rotateRight();
//			}else if(ballState == onCENTER){
//				//alignLeft();
//				slideRight();
//			}
//		}else if(goalState == onRIGHT){
//			if(ballState == onLEFT){
//				rotateLeft();
//			}else if(ballState == onRIGHT){
//				rotateRight();
//			}else if(ballState == onCENTER){
//				//alignRight();
//				slideLeft();
//			}
//		} else {
//			if(GlobalVar.isGoalDirection()){
//				if(ballState == onLEFT){
//					rotateLeft();
//				}else if(ballState == onRIGHT){
//					rotateRight();
//				}else if(ballState == onCENTER){
//					return 1;
//				}
//			}else{
//				return -1;
//			}
//		}
		return 0;
	}

	public void standingUp() {
		if(GlobalVar.falling_state != SensorInterface.FALL_FACEUP){
			api.playSaveMotion(0); // stand up
		}if(GlobalVar.falling_state != SensorInterface.FALL_FACEDOWN){
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