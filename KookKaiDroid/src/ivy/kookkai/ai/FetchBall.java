package ivy.kookkai.ai;

import kookkai.strategy.ChampStateFull;
import kookkai.strategy.FetcherBrutal;
import kookkai.strategy.FetcherPeaceful;
import kookkai.strategy.FetcherSinglePeaceful;
import kookkai.strategy.FetcherTest;
import kookkai.strategy.StrategyTemplate;
import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.refbox.Constants;
import ivy.kookkai.refbox.KookKaiTeamInfo;
import ivy.kookkai.refbox.TeamInfo;


public class FetchBall implements AITemplate {
	private int state = 0;
	private double px = 0.3, ix = 0.0, dx = 0.0;
	private double py = 1.0, iy = 0.0, dy = 0.05;
	private double pz = 4.0, iz = 0.0, dz = 0;
	private double thresholdY_level1 = 100, thresholdZ_level1 = 50;
	private double thresholdY_level2 = 70, thresholdZ_level2 = 25;
	private double falldownThreashold = 150.0;
	private double fallCounter = 0;
	private double getupThreashold = 10.0;
	public double _x, x = 0, xx = 0, xxx, _y, y = 0, yy = 0, yyy, _z, z = 0,
			zz = 0, zzz;
	public double vx = 0, vy = 0, vz = 0;
	private double goalPos;
	private double _ax = 0.3, _ay = 9.5, _az = 4.8;
	private double dax;
	private double day;
	private double daz;
	private double difAccel;
	public KookKaiAndroidAPI api;
	private StrategyTemplate strategy;
	
	public FetchBall(KookKaiAndroidAPI api) {
		this.api = api;
		this.strategy = new FetcherSinglePeaceful(this);
	}

	public void preTrajectoryToBall(double[] targetPos) {
		_y = y;
		// original : y = GlobalVar.ballPos[1] - 40;
		y = targetPos[1] - 45;
		
		if (_y * y <= 0)
			yy = 0;
		yy += y;
		yyy = y - _y;

		_z = z;
		// z = targetPos[0] - 27; original
		z = targetPos[0] - 26;
		if (_z * z <= 0)
			zz = 0;
		zz += z;
		zzz = z - _z;

	}
	
	public void trajectoryToBallCalculation() {
		preTrajectoryToBall(GlobalVar.ballPos);
		_x = x;
		x = GlobalVar.ballPos[0] - goalPos;
		
		if (_x * x <= 0)
			xx = 0;
		xx += x;
		xxx = x - _x;
		
		vx = px * x + ix * xx + dx * xxx;
		vy = py * y + iy * yy + dy * yyy;
		vz = pz * z + iz * zz + dz * zzz;
	}
	
	public void trajectoryToEnemyCalculation() {
		_y = y;
		// original : y = GlobalVar.ballPos[1] - 40;
		y = GlobalVar.enemyPos[1] - 250;
		
		if (_y * y <= 0)
			yy = 0;
		yy += y;
		yyy = y - _y;

		_z = z;
		// z = targetPos[0] - 27; original
		z = GlobalVar.enemyPos[0] ;
		if (_z * z <= 0)
			zz = 0;
		zz += z;
		zzz = z - _z;
		
		_x = x;
		x = GlobalVar.enemyPos[0]- goalPos;
		if (_x * x <= 0)
			xx = 0;
		xx += x;
		xxx = x - _x;
		
		vx = px * x + ix * xx + dx * xxx;
		vy = py * y + iy * yy + dy * yyy;
		vz = pz * z + iz * zz + dz * zzz;
	}
	
	public void trajectoryToSetupPosition() {
		_y = y;
		// original : y = GlobalVar.ballPos[1] - 40;
		// y = GlobalVar.ballPos[1] - 60;// but if less than 240 ready
		y = 30;

		if (_y * y <= 0)
			yy = 0;
		yy += y;
		yyy = y - _y;

		_z = z;
		// z = targetPos[0] - 27; original
		z = GlobalVar.ballPos[0] - 26;
		if (_z * z <= 0)
			zz = 0;
		zz += z;
		zzz = z - _z;

		_x = x;
		// x = GlobalVar.ballPos[0] - goalPos;
		x = 0;
		if (_x * x <= 0)
			xx = 0;
		xx += x;
		xxx = x - _x;

		vx = px * x + ix * xx + dx * xxx;
		vy = py * y + iy * yy + dy * yyy;
		vz = pz * z + iz * zz + dz * zzz;
	}
	
	
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
	
	public void findBall() {
		api.walking(0, 10, -125);
		x = xx = y = yy = z = zz = 0;	
	}
	public void trajectoryToTrackBall(){
		double threshold_z_trackball = 25;
		if(Math.abs(GlobalVar.ballPos[0])>threshold_z_trackball){
			_y = y;
			// original : y = GlobalVar.ballPos[1] - 40;
	//		y = GlobalVar.ballPos[1] - 60;
			y=0;
			
			if (_y * y <= 0)
				yy = 0;
			yy += y;
			yyy = y - _y;
	
			_z = z;
			// z = targetPos[0] - 27; original
			z = GlobalVar.ballPos[0];
			if (_z * z <= 0)
				zz = 0;
			zz += z;
			zzz = z - _z;
			
			
			_x = x;
			if(z>0)	x = 15;
			else x=-15;
			
			if (_x * x <= 0)
				xx = 0;
			xx += x;
			xxx = x - _x;
			
			vx = px * x + ix * xx + dx * xxx;
			vy = py * y + iy * yy + dy * yyy;
			vz = pz * z + iz * zz + dz * zzz;
		}
	}
	public void trackBall() {
		trajectoryToTrackBall();
		state = 0;
		api.walking((int) vx, (int) vy, (int) vz);
	}

	public void findEnemy(){
		api.walking(0, 10, -125);
		x = xx = y = yy = z = zz = 0;
	}
	
	public void gettingUp() {
		if (state < -20 && difAccel < getupThreashold) {
			state = -20;
		}
		state++;
	}

	public void alignMeMyBallGoal() {
		trajectoryToBallCalculation();
		state = 0;
		api.walking((int) vx, (int) vy, (int) vz);		
	}
	
	
	public void walkToSetupPosition() {
		if(GlobalVar.ballPos[1] < 215) {
			vx = vy = vz = 0;
			api.ready();
		} else {
			trajectoryToSetupPosition();
			state = 0;
			api.walking((int)vx, (int)vy, (int)vz);
		}
	}
	
	public void runToBall() {
		//trajectoryToBallCalculation();
		vx = 0;
		vy = 35;
		vz = 0;
		api.walkingNonLimit(0, 35, 0);
	}

	public void alignMeEnemyGoal() {
		trajectoryToEnemyCalculation();
		api.walking((int) vx, (int) vy, (int) vz);
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
	
	public void foundGoalHandle() {
		goalPos = GlobalVar.goalPos[0];
	}
	
	public void notFoundGoalHandle() {
		goalPos = -GlobalVar.frameWidth;
	}
	
	public void forceStandStill() {
		api.stand_still();
	}
	
	public void forceReady() {
		api.ready();
	}
	
	public boolean readyToKickBall() {
		
		return (GlobalVar.ballPos[1] < thresholdY_level1 && Math.abs(z) < thresholdZ_level1 && (GlobalVar.goalPos[2] > 0))
			|| 	(GlobalVar.ballPos[1] < thresholdY_level2 && Math.abs(z) < thresholdZ_level2 && (GlobalVar.goalPos[2] > 0));
	}
	
	public String execute() {
		dax = GlobalVar.ax - _ax;
		day = GlobalVar.ay - _ay;
		daz = GlobalVar.az - _az;
		difAccel = dax * dax + day * day + daz * daz;
		String out = "";
		out += "X = " + vx + "\n";
		out += "Y = " + vy + "\n";
		out += "Z = " + vz + "\n";
		return strategy.run();
	}
}
