package scenes;

import therapeuticpresence.TherapeuticPresence;

public abstract class AbstractScene {
	protected TherapeuticPresence mainApplet = null;
	protected int backgroundColor = 0;
	protected int defaultBackgroundColor;
	
	public AbstractScene (TherapeuticPresence _mainApplet, int _backgroundColor) {
		mainApplet = _mainApplet;
		backgroundColor = _backgroundColor;
		defaultBackgroundColor = _backgroundColor;
	}
	
	public abstract void reset ();
	public abstract short getSceneType ();
	public abstract boolean sceneIs3D ();
	public abstract void shapeActiveAlert (float _time);
	
	public void setBackgroundColor (int _backgroundColor) {
		backgroundColor = _backgroundColor;
	}
	public int getBackgroundColor () {
		return backgroundColor;
	}
}
