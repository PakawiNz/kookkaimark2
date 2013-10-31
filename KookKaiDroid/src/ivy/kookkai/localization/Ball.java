package ivy.kookkai.localization;

import ivy.kookkai.debugview.FieldView;
import android.graphics.Point;

public class Ball extends Point{
	
	public Ball() {
		super();
		this.x=(int)(FieldView.FIELD_LENGTH*Math.random());
		this.y=(int)(FieldView.FIELD_WIDTH*Math.random());
		// TODO Auto-generated constructor stub
	}

	public Ball(int x, int y) {
		super(x, y);
		// TODO Auto-generated constructor stub
	}
	
}
