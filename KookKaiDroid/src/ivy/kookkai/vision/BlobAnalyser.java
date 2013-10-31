package ivy.kookkai.vision;

import ivy.kookkai.data.GlobalVar;

public class BlobAnalyser {
	
	public String execute(){
		GlobalVar.ballPos[2] = -1;
		GlobalVar.goalPos[2] = -1;
		for (int i = 0; i < GlobalVar.mergeResult.size(); i++) {
			BlobObject b = GlobalVar.mergeResult.get(i);
			
			switch (b.tag) {
			case GlobalVar.BALL :
					
			case GlobalVar.GOAL :
			
			
			}
		
		}
		
		return "";
	}

}
