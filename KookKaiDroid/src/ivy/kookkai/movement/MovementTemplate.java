package ivy.kookkai.movement;

public interface MovementTemplate {

	public void findBall();
	public void trackBall();
	public void walkToBall();
	public void playBall();
	public void kickBall();
	public void walkToSetupPosition();
	public void changeDirection();
	public int prepareKick();
	public void standingUp();
	public void forceReady();
	
	public String getMSG();
}
