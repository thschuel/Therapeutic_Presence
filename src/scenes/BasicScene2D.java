package scenes;

import therapeuticpresence.TherapeuticPresence;

public class BasicScene2D extends AbstractScene {
	
	public BasicScene2D (TherapeuticPresence _mainApplet, int _backgroundColor) {
		super(_mainApplet,_backgroundColor);
	}

	public void reset() {
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
	}

	public short getSceneType() {
		return TherapeuticPresence.BASIC_SCENE2D;
	}

	public boolean sceneIs3D() {
		return false;
	}

}
