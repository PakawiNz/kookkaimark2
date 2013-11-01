package ivy.kookkai.vision;

import java.util.ArrayList;

import android.util.Log;
import ivy.kookkai.data.GlobalVar;

public class BlobAnalyser {
	
	private static final double filterRatio = 0.2;
	private static final double intersectRatio = 0.7;
	private static final int ballCut = 20;
	
	private boolean yellowInBall(BlobObject yellow,BlobObject ball){
		int left = Math.max(yellow.posRect.left,ball.posRect.left);
		int right = Math.min(yellow.posRect.right,ball.posRect.right);
		int top = Math.max(yellow.posRect.top,ball.posRect.top);
		int bottom = Math.min(yellow.posRect.bottom,ball.posRect.bottom);
		
		float w = right - left;
		float h = bottom - top;
		
		float percentage = w * h / (float)yellow.getSize();
		
		Log.d("blobAnalyse","Intersect " + percentage + "\n");
		
		if(w < 0 || h < 0) return false;
		return percentage > intersectRatio;
	}
	
	public String execute(){
		GlobalVar.ballPos[2] = -1;
		GlobalVar.goalPosL[2] = -1;
		GlobalVar.goalPosR[2] = -1;
		GlobalVar.polePos[2] = -1;
		
		ArrayList<BlobObject> goals = new ArrayList<BlobObject>();
		BlobObject blob ,ball = null;
		
		for (int i = 0; i < GlobalVar.mergeResult.size(); i++) {
			blob = GlobalVar.mergeResult.get(i);
			switch (blob.tag) {
			case GlobalVar.BALL :
				ball = blob;
				GlobalVar.ballPos[0] = blob.posRect.centerX() - GlobalVar.frameWidth / 2;
				GlobalVar.ballPos[1] = GlobalVar.frameHeight - blob.posRect.bottom;
				GlobalVar.ballPos[2] = blob.getSize();
				break;
			case GlobalVar.GOAL :
				goals.add(blob);
				break;
			}
		}
		
		if(goals.size() > 0){
		
			int minimumFragmentSize = (int)(filterRatio * goals.get(goals.size()-1).getSize());
			blob = goals.get(goals.size() - 1);
			
			for( int i = 0; i < goals.size(); i++){
				BlobObject b = goals.get(i);
				if(b.posRect.bottom > blob.posRect.bottom){
					blob = b;
				}
				if(goals.get(i).getSize() < minimumFragmentSize) {
					Log.d("blobAnalyse","Too small Fragment\n");
					goals.remove(i);
					i--;
				} else if(ball != null) {
					if(b.posRect.bottom - ball.posRect.bottom > ballCut) {
						Log.d("blobAnalyse","GOAL BEFORE BALL\n");
						goals.remove(i);
						i--;
					}else {
						if(yellowInBall(b, ball)){
							goals.remove(i);
							i--;
						}
					}
				}
				if(ball != null && yellowInBall(b, ball)){
					GlobalVar.blobResult.remove(b);
					GlobalVar.mergeResult.remove(b);
				}
			}
			
			GlobalVar.polePos[0] = blob.posRect.centerX() - GlobalVar.frameWidth / 2;
			GlobalVar.polePos[1] = GlobalVar.frameHeight - blob.posRect.bottom;
			GlobalVar.polePos[2] = blob.getSize();
		}

		if(goals.size() == 2){
			BlobObject blob1 = goals.get(1);
			BlobObject blob2 = goals.get(0);
			if(blob1.posRect.centerX() < blob2.posRect.centerX());
			else{
				blob = blob1;
				blob1 = blob2;
				blob2 = blob;
			}
			GlobalVar.goalPosL[0] = blob1.posRect.centerX() - GlobalVar.frameWidth / 2;
			GlobalVar.goalPosL[1] = GlobalVar.frameHeight - blob1.posRect.bottom;
			GlobalVar.goalPosL[2] = blob1.getSize();
			
			GlobalVar.goalPosR[0] = blob2.posRect.centerX() - GlobalVar.frameWidth / 2;
			GlobalVar.goalPosR[1] = GlobalVar.frameHeight - blob2.posRect.bottom;
			GlobalVar.goalPosR[2] = blob2.getSize();
			
		}else if(goals.size() == 1){
			BlobObject blob1 = goals.get(0);
			double delta1 = GlobalVar.goalPosL[0] - (blob1.posRect.centerX() - GlobalVar.frameWidth / 2);
			double delta2 = GlobalVar.goalPosR[0] - (blob1.posRect.centerX() - GlobalVar.frameWidth / 2);
			delta1 = Math.abs(delta1);
			delta2 = Math.abs(delta2);
			if(delta1 < delta2){
				GlobalVar.goalPosL[0] = blob1.posRect.centerX() - GlobalVar.frameWidth / 2;
				GlobalVar.goalPosL[1] = GlobalVar.frameHeight - blob1.posRect.bottom;
				GlobalVar.goalPosL[2] = blob1.getSize();
			}else{
				GlobalVar.goalPosR[0] = blob1.posRect.centerX() - GlobalVar.frameWidth / 2;
				GlobalVar.goalPosR[1] = GlobalVar.frameHeight - blob1.posRect.bottom;
				GlobalVar.goalPosR[2] = blob1.getSize();
			}
			
		}else if(goals.size() == 0){
			if(GlobalVar.goalPosL[0] < -80 && GlobalVar.goalPosR[0] < -80){
				GlobalVar.goalPosL[0] = 0;
				GlobalVar.goalPosR[0] = -100;
			}else if(GlobalVar.goalPosL[0] > 80 && GlobalVar.goalPosR[0] > 80){
				GlobalVar.goalPosL[0] = 100;
				GlobalVar.goalPosR[0] = 0;
			}
		}
		
		/*String log = 	"Goals Count : " + goals.size() + "\n" +
						"GoalPosL[0] : " + GlobalVar.goalPosL[0] + "\n" +
						"GoalPosL[1] : " + GlobalVar.goalPosL[1] + "\n" +
						"GoalPosL[2] : " + GlobalVar.goalPosL[2] + "\n" +
						"GoalPosR[0] : " + GlobalVar.goalPosR[0] + "\n" +
						"GoalPosR[1] : " + GlobalVar.goalPosR[1] + "\n" +
						"GoalPosR[2] : " + GlobalVar.goalPosR[2] + "\n";
		
		String log = 	"Goal Count : " + goals.size() + "\n";
		for(int i = 0; i < goals.size(); i++){
			log += "Goal" + i + " Size : " + goals.get(i).getSize() + "\n";
		}
		
		Log.d("pakawinz_debug",log);
		*/
		
		return "";
	}

}