package ivy.kookkai.data;

public class JoystickData {
	
	public boolean dUp,dDown,dLeft,dRight,dCenter;
	public boolean a,b,one,two;
	public boolean home,plus,minus;
	
	public void clearState(){
		dUp=dDown=dLeft=dRight=dCenter=false;
		a=b=one=two=false;
		home=plus=minus=false;
	}
	
	public JoystickData() {
		clearState();
	}
}
