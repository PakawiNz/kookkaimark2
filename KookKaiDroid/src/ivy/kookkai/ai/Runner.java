package ivy.kookkai.ai;

import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.refbox.Constants;

public class Runner implements AITemplate{
	
	KookKaiAndroidAPI api;
	
	public Runner(KookKaiAndroidAPI api) {
		this.api = api;
	}

	public String execute() {
		String out="";
		
		/*if (GlobalVar.gameData.state != Constants.STATE_PLAYING ) {
			//api.stand_still();
			api.ready();		
			return "wait for game's play state.\n";
		}*/
		int vx=0,vy=40,vz=0;
		api.walkingNonLimit(vx, vy, vz);
		out +="GOO JA RUN WOYY\n";
		out += "vx:"+vx+" \nvy:" + vy + " \nvz:" + vz + "\n";
		
		return out;
	}

}
