package ivy.kookkai.refbox;




public class GameData {

public byte[] header = new byte[4];           // header to identify the structure
public int version;             // version of the data structure
public byte playersPerTeam;       // The number of players on a team
public byte state;                // state of the game (STATE_READY, STATE_PLAYING, etc)
public byte firstHalf;            // 1 = game in first half, 0 otherwise
public byte kickOffTeam;          // the next team to kick off
public byte secondaryState;       // Extra state information - (STATE2_NORMAL, STATE2_PENALTYSHOOT, etc)
public byte dropInTeam;           // team that caused last drop in
public short dropInTime;          // number of seconds passed since the last drop in.  -1 before first dropin
public int secsRemaining;       // estimate of number of seconds remaining in the half
public TeamInfo[] teams = new TeamInfo[2];

public GameData() {
	for(int i=0;i<teams.length;i++)
		teams[i] = new TeamInfo();
}


}


/*
// data structure header
static final int GAMECONTROLLER_RETURN_STRUCT_HEADER    "RGrt"

static final int GAMECONTROLLER_RETURN_STRUCT_VERSION   1

static final int GAMECONTROLLER_RETURN_MSG_MAN_PENALISE 0
static final int GAMECONTROLLER_RETURN_MSG_MAN_UNPENALISE 1
static final int GAMECONTROLLER_RETURN_MSG_ALIVE 2

struct RoboCupGameControlReturnData {
    char    header[4];
    uint32  version;
    uint16  team;
    uint16  player;             // player number - 1 based
    uint32  message;
};

*/








