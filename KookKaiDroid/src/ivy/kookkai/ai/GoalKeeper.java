package ivy.kookkai.ai;

import android.util.Log;
import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;

public class GoalKeeper implements AITemplate {
	

	public static final int KOOKKAI_MARK = 3;
	private int state = 0;
	private double px = 0.3, ix = 0.0, dx = 0.0;
	private double py = 1.0, iy = 0.0, dy = 0.05;
	private double pz = 4.0, iz = 0.0, dz = 0;
	private double threasholdX = 40, threasholdY = 20, threasholdZ = 20;
	private double falldownThreashold = 150.0;
	private double fallCounter = 0;
	private double getupThreashold = 10.0;
	private double _x, x = 0, xx = 0, xxx, _y, y = 0, yy = 0, yyy, _z, z = 0,
			zz = 0, zzz;
	private double vx = 0, vy = 0, vz = 0;
	private double _ballx=0,_bally=0,ballx=0,bally=0;
	private double ballVX=0,ballVY=0;
	private static final double KMEAN = 3;
	private double goalPos;
	private double _ax = 0.3, _ay = 9.5, _az = 4.8;
	private double dax;
	private double day;
	private double daz;
	private double difAccel;
	private String out;
	private KookKaiAndroidAPI api;

	public GoalKeeper(KookKaiAndroidAPI api) {
		this.api = api;
	}	
	private void calculateVX(double vx){
		ballVX = (ballVX*(KMEAN-1)+vx)/KMEAN;
	}
	private void calculateVY(double vy){
		ballVY = (ballVY*(KMEAN-1)+vy)/KMEAN;
	}
	private void rememberBallPosition(){
		_ballx = ballx;
		_bally = bally;
	}
	private int calculateFallDirection(){
		if(ballx>0) return 1;
		else return -1;
	}
	private double remap_x(double x,double y){
		return 1*x+0*y;
	}
	private double remap_y(double x,double y){
		return 0*x+1*y;
	}
	public void preTrajectory(double[] targetPos) {
		_y = y;
		// original : y = GlobalVar.ballPos[1] - 40;
		y = targetPos[1] - 100;
		
		out += GlobalVar.frameHeight + ", " + targetPos[1] + "\n";
		if (_y * y <= 0)
			yy = 0;
		yy += y;
		yyy = y - _y;

		_z = z;
		// z = targetPos[0] - 27; original
		z = targetPos[0] + 28;
		if (_z * z <= 0)
			zz = 0;
		zz += z;
		zzz = z - _z;

	}
	
	public void trajectoryToBallCalculation() {
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
	
	public void strikeBall() {
		trajectoryToBallCalculation();
		api.walkingNonLimit(35, 0, 0);
	}

	@Override
	public String execute() {
		out = "";

		// calculate deference between accelerometer value that we expect and
		// actual one.
		dax = GlobalVar.ax - _ax;
		day = GlobalVar.ay - _ay;
		daz = GlobalVar.az - _az;
		difAccel = dax * dax + day * day + daz * daz;

		// // if game state is not playing so robot must stand still.
		// if (GlobalVar.gameData.state != Constants.STATE_PLAYING ) {
		// //api.stand_still();
		// api.ready();
		// return "wait for game's play state.\n";
		// }

		// if state < 0 then robot is getting up so wait until robot stand
		// successfully.
		if (state < 0) {
			gettingUp();
			return "waiting for stand up." + state;		
		}

		// if robot fall down ;
		if (difAccel > falldownThreashold) {
			startGettingUp();
			return "standing up.";
		} else {
			fallCounter = 0;
		}
		
		
		if(GlobalVar.ballPos[2]>0 && GlobalVar.ballPos[3]>5){
			double image_x = GlobalVar.ballPos[0];
			double image_y = GlobalVar.ballPos[1];
			
			ballx = remap_x(image_x,image_y);
			bally = remap_y(image_x,image_y);
//			Log.d("goalkeep_image","image:"+(int)image_x+","+(int)image_y);
//			Log.d("goalkeep_image","ball:"+(int)ballx+","+(int)bally);
			
			calculateVX(ballx-_ballx);
			calculateVY(bally-_bally);
			rememberBallPosition();
		}
		else{
			calculateVX(0);
			calculateVY(0);
		}
//		Log.d("goalkeep_image","ballVY:"+ballVY);
		if(GlobalVar.ballPos[2]>0 && GlobalVar.ballPos[3]>80){
			
			if(ballVY<-6){
				int direction = calculateFallDirection();
				if(direction==-1){
					//fall left
					Log.d("goalkeep_image","fall left!");
				}
				else if(direction==1){
					//fall right
					Log.d("goalkeep_image","fall right!");
				}
				else if(direction==0){
					Log.d("goalkeep_image","straight ball!");
				}
			}
		}
		if(GlobalVar.ballPos[2]>0)
			if(GlobalVar.ballPos[3]>200 || GlobalVar.ballPos[1]>200){
			int direction = calculateFallDirection();
			if(direction==-1){
				//fall left
				Log.d("goalkeep_image","Too close:fall left!");
			}
			else if(direction==1){
				//fall right
				Log.d("goalkeep_image","Too close:fall right!");
			}
			else if(direction==0){
				Log.d("goalkeep_image","Too close:straight ball!");
			}
		}
		return out;
		// */
	}
	
	
}
