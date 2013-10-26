package ivy.kookkai.refbox;

public class TeamInfo {

	public byte teamNumber;          // unique team number
	public byte teamColour;          // colour of the team
	public byte goalColour;          // colour of the goal
	public byte score;               // team's score
	public RobotInfo[] player = new  RobotInfo[Constants.MAX_NUM_PLAYERS];       // the team's players
    
	public TeamInfo() {
		for(int i=0;i<player.length;i++)
			player[i] = new RobotInfo();
	}
	
}