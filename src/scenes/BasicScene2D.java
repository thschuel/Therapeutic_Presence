package scenes;

import processing.core.PApplet;

public class BasicScene2D extends AbstractScene {
	
	public BasicScene2D (PApplet _mainApplet, int _backgroundColor) {
		super(_mainApplet,_backgroundColor);
	}

	public void reset() {
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
	}

}
