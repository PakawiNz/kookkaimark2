package ivy.kookkai.ai;

import ivy.kookkai.ai.AITemplate;
import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.strategy.ChampStateFull;
import ivy.kookkai.strategy.StrategyTemplate;

public class PakawiNz_AI implements AITemplate {

	private double fallCounter = 0;	
	
	private StrategyTemplate strategy;
	public KookKaiAndroidAPI api;
	
	private String out = "";
	
	public PakawiNz_AI(KookKaiAndroidAPI api) {
		this.api = api;
		this.strategy = new ChampStateFull(this);
	}
	
	private final int BallX_CENTER = 0;
	private final int BallX_TRESH = 50;
	private final int GoalX_CENTER = 0;
	private final int GoalX_TRESH = 35;
	
	private final int onLEFT = -1;
	private final int onCENTER = 0;
	private final int onRIGHT = 1;
	
	private int ballState = 0;
	private int goalState = 0;
	
	//-------------------------------->> STATE HANDLING <<--------------------------------//
	
	public void updateCurrentState(){
		
		if(GlobalVar.ballPos[0] - BallX_CENTER < -BallX_TRESH){
			ballState = onLEFT;
		}else if(GlobalVar.ballPos[0] - BallX_CENTER > BallX_TRESH){
			ballState = onRIGHT;
		}else {
			ballState = onCENTER;
		}
		
		if(GlobalVar.goalPos[0] - GoalX_CENTER < -GoalX_TRESH){
			goalState = onLEFT;
		}else if(GlobalVar.goalPos[0] - GoalX_CENTER > GoalX_TRESH){
			goalState = onRIGHT;
		}else {
			goalState = onCENTER;
		}
		
	}
	
	public void updateFineBallState(){
		
//		if(GlobalVar.ballPos[0] - BallX_CENTER < -2){
//			ballState = onLEFT;
//		}else if(GlobalVar.ballPos[0] - BallX_CENTER > 15){
//			ballState = onRIGHT;
//		}else {
//			ballState = onCENTER;
//		}
		
		if(GlobalVar.ballPos[0] - BallX_CENTER < -20){
			ballState = onLEFT;
		}else if(GlobalVar.ballPos[0] - BallX_CENTER > 20){
			ballState = onRIGHT;
		}else {
			ballState = onCENTER;
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
	
	//-------------------------------->> MOVEMENT HANDLING <<--------------------------------//
	
	public void findBall() {
		out += "FINDBALL > ";
		if(ballState == onLEFT){
			rotateLeft();
		}else if(ballState == onRIGHT){
			rotateRight();
		}else if(ballState == onCENTER){
			goStraight();
		}
	}

	public void trackBall() {
		out += "TRACKBALL > ";
		if(ballState == onLEFT){
			//folllowLeft();
			rotateLeft();
		}else if(ballState == onRIGHT){
			//followRight();
			rotateRight();
		}else if(ballState == onCENTER){
			goStraight();
		}
	}
	
	public void runToBall() {
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
	
	public void changeDirection() {
		out += "CHANGE DIRECTION > ";
		//api.playSaveMotion(2);
		slideRight();
	}
	
	public int prepareKick() {
		out += "Prepare!!\n";
		updateFineBallState();
		if(ballState == onLEFT){
			rotateLeft();
		}else if(ballState == onRIGHT){
			rotateRight();
		}else {
			if(goalState == onLEFT){
				slideRight();
			}else if(goalState == onRIGHT){
				slideLeft();
			} else {
				if(GlobalVar.isCorrectDirection()){
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

	//-------------------------------->> FALLING HANDLING <<--------------------------------//
	
	public void startGettingUp() {
		// if robot is detected in falldown stage in a period of time will
		// call stand up motion.
		out += "Getting Up!!\n";
		if (fallCounter < 7)
			fallCounter++;// original = 20
		else {
			if (GlobalVar.ay < 0) { // stand up from front-down position.
				api.playSaveMotion(1); // flip
				api.playSaveMotion(0); // stand up
			} else { // stand up from back-down position.
				api.playSaveMotion(0); // stand up
			}
			fallCounter = 0;
		}	
	}

	public void resetFallCounter() {
		fallCounter = 0;
	}

	//-------------------------------->> CONTROL <<--------------------------------//
	
	public void forceReady() {
		api.ready();
	}
	
	public String execute() {
		
		out = "";
		out += "\n";
		out += "GOAL L POSITION" + GlobalVar.goalPosL[0] + "\n";
		out += "GOAL L Y" + GlobalVar.goalPosL[1] + "\n";
		out += "GOAL R POSITION" + GlobalVar.goalPosR[0] + "\n";
		out += "GOAL R Y" + GlobalVar.goalPosR[1] + "\n";
		out += "BALL POSITION" + GlobalVar.ballPos[0] + "\n";
		out += "BALL Y " + GlobalVar.ballPos[1] + "\n";
		out += "BALL SIZE " + GlobalVar.ballPos[2] + "\n";
		out += "\n";
		String x =  strategy.run();
		
//		if(!x.equals("LOCK  "))
//			Log.d("pakawinz_debug",out + x);

		return out + x;
	}
}