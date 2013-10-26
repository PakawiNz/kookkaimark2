package ivy.kookkai.ai;

import kookkai.strategy.ChampStateFull;
import kookkai.strategy.FetcherBrutal;
import kookkai.strategy.FetcherPeaceful;
import kookkai.strategy.FetcherSinglePeaceful;
import kookkai.strategy.FetcherTest;
import kookkai.strategy.StrategyTemplate;
import ivy.kookkai.ai.AITemplate;
import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.refbox.Constants;
import ivy.kookkai.refbox.KookKaiTeamInfo;
import ivy.kookkai.refbox.TeamInfo;


public class PakawiNZ extends FetchBall implements AITemplate {
	private int state = 0;

	private double threasholdZ = 30;
	private double falldownThreashold = 150.0;
	private double fallCounter = 0;
	private double getupThreashold = 10.0;
	private double goalPos;
	private double _ax = 0.3, _ay = 9.5, _az = 4.8;
	private double dax;
	private double day;
	private double daz;
	private double difAccel;
	public KookKaiAndroidAPI api;
	private StrategyTemplate strategy;
	String out = "";
	
	public PakawiNZ(KookKaiAndroidAPI api) {
		super(api);
		this.api = api;
		this.strategy = new FetcherSinglePeaceful(this);
	}
	
	private final int LEFTBALL = - GlobalVar.FRAME_WIDTH / 4;
	private final int RIGHTBALL = GlobalVar.FRAME_WIDTH / 4;
	private final int LEFTGOAL = 44;
	private final int RIGHTGOAL = 64;
	
	//-------------------------------->> WALKING HANDLING <<--------------------------------//
	
	private void alignLeft(){
		out += "ALIGN LEFT\n";
		api.walkingNonLimit(10, 0, -50);
	}

	private void alignRight(){
		out += "ALIGN RIGHT\n";
		api.walkingNonLimit(-10, 0, 50);
	}

	private void rotateLeft(){
		out += "ROTATE LEFT\n";
		api.walkingNonLimit(0, 0, -100);
	}

	private void rotateRight(){
		out += "ROTATE RIGHT\n";
		api.walkingNonLimit(0, 0, 100);
	}
	
	private void folllowLeft(){
		out += "FOLLOW LEFT\n";
		api.walkingNonLimit(5, 30, 0);
	}

	private void followRight(){
		out += "FOLLOW RIGHT\n";
		api.walkingNonLimit(-5, 30, 0);
	}
	
	private void goStraight(){
		out += "GO STRAIGHT\n";
		api.walkingNonLimit(0, 30, 0);
	}
	
	private void goSprint(){
		api.walkingNonLimit(0, 30, 0);
	}
	
	//-------------------------------->> MOVEMENT HANDLING <<--------------------------------//
	
	public void findBall() {
		if(GlobalVar.ballPos[0] < LEFTBALL){
			rotateLeft();
		}else if(GlobalVar.ballPos[0] > RIGHTBALL){
			rotateRight();
		}else {
			goStraight();
		}
	}

	public void trackBall() {
		if(GlobalVar.ballPos[0] < LEFTBALL){
			folllowLeft();
		}else if(GlobalVar.ballPos[0] > RIGHTBALL){
			followRight();
		}else {
			goStraight();
		}
	}
	
	public void runToBall() {
		out += "RUN TO BALL\n";
		goStraight();
	}
	
	public void kickBall() {
		//api.playSaveMotion(2);
		out += "KICK BALL";
		goStraight();
	}
	
	public void alignMeMyBallGoal() {
		out += "ALIGN MYSELF\n";
		if(GlobalVar.goalPos[0] < LEFTGOAL){
			if(GlobalVar.ballPos[0] < 0)
				alignLeft();
			else
				alignRight();
		}else if(GlobalVar.goalPos[0] > RIGHTGOAL){
			if(GlobalVar.ballPos[0] < 0)
				alignRight();
			else
				alignLeft();
		}else {
			goStraight();
		}
	}

	//-------------------------------->> FALLING HANDLING <<--------------------------------//
	
	public void startGettingUp() {
		// if robot is detected in falldown stage in a period of time will
		// call stand up motion.
		if (fallCounter < 7)
			fallCounter++;// original = 20
		else {
			if (GlobalVar.ay < 0) { // stand up from front-down position.
				api.playSaveMotion(1); // flip
				api.playSaveMotion(0); // stand up
				state = -100;
			} else { // stand up from back-down position.
				api.playSaveMotion(0); // stand up
				state = -60;
			}
			fallCounter = 0;
		}	
	}
	
	public void gettingUp() {
		if (state < -20 && difAccel < getupThreashold) {
			state = -20;
		}
		state++;
	}

	public void resetFallCounter() {
		fallCounter = 0;
	}
	public boolean isFalling() {
		return state < 0;
	}
	
	public boolean isStartFalling() {
		return difAccel > falldownThreashold;
	}
	
	@Deprecated
	public void foundGoalHandle() {
		goalPos = GlobalVar.goalPos[0];
	}
	
	@Deprecated
	public void notFoundGoalHandle() {
		goalPos = -GlobalVar.frameWidth;
	}
	
	@Deprecated
	public void forceStandStill() {
		api.stand_still();
	}
	
	public void forceReady() {
		api.ready();
	}
	
	public boolean readyToKickBall() {
		return GlobalVar.ballPos[1] < 100 && Math.abs(GlobalVar.ballPos[0] - GlobalVar.frameWidth/2) < threasholdZ 
				&& (GlobalVar.goalPos[2] > 0) && GlobalVar.goalPos[0] > LEFTGOAL && GlobalVar.goalPos[0] < RIGHTGOAL;
	}
	
	public String execute() {
		dax = GlobalVar.ax - _ax;
		day = GlobalVar.ay - _ay;
		daz = GlobalVar.az - _az;
		difAccel = dax * dax + day * day + daz * daz;
		out = "";
		out += "GOAL POSITION" + GlobalVar.goalPos[0] + "\n";
		out += "BALL POSITION" + GlobalVar.ballPos[0] + "\n";
		String x =  strategy.run();
		return out + x;
	}
}
