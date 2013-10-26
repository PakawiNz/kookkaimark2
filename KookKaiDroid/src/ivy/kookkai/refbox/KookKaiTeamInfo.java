package ivy.kookkai.refbox;
import ivy.kookkai.data.GlobalVar;
public class KookKaiTeamInfo {
	private static KookKaiTeamInfo instance = new KookKaiTeamInfo();
	private TeamInfo teaminfo;
	protected KookKaiTeamInfo() {
		teaminfo = GlobalVar.gameData.teams[0];
	}
	public void setTeamInfo(int n){
		this.teaminfo=GlobalVar.gameData.teams[n];
	}
	public TeamInfo getTeamInfo(){
		return this.teaminfo;
	}
	public static synchronized KookKaiTeamInfo getInstance() {
		return instance;
	}
	
}