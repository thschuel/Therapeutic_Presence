package scenes;

import therapeuticpresence.TherapeuticPresence;

public abstract class AbstractScene {
	protected TherapeuticPresence mainApplet = null;
	protected int backgroundColor = 0;
	
	public AbstractScene (TherapeuticPresence _mainApplet, int _backgroundColor) {
		mainApplet = _mainApplet;
		backgroundColor = _backgroundColor;
	}
	
	public abstract void reset ();
	public abstract short getSceneType ();
	public abstract boolean sceneIs3D ();
}
