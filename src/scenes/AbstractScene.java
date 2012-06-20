package scenes;

import processing.core.PApplet;

public abstract class AbstractScene {
	protected PApplet mainApplet = null;
	protected int backgroundColor = 0;
	
	public AbstractScene (PApplet _mainApplet, int _backgroundColor) {
		mainApplet = _mainApplet;
		backgroundColor = _backgroundColor;
	}
	
	public abstract void reset ();
}
