package ivy.kookkai.localization;

public class Localization {
	
	
	public Humanoid ourHumanoid,oppHumanoid;
	public Ball ball;
	
	public Localization(){
		ourHumanoid = new Humanoid();
		oppHumanoid = new Humanoid();

		ball = new Ball();
	}
}
