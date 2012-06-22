package scenes;

import processing.core.*;
import therapeuticpresence.TherapeuticPresence;

public class BasicScene3D extends AbstractScene {
	// variables to move through the scene. used only by some visualisations
	public float translateX = 0.0f;
	public float translateY = 0.0f;
	public float translateZ = 0.0f;
	public float rotX = PApplet.radians(0); 
	public float rotY = PApplet.radians(0);
	public float rotZ = PApplet.radians(180);  // by default rotate the hole scene 180deg around the z-axis, the data from openni comes upside down

	public BasicScene3D (TherapeuticPresence _mainApplet, int _backgroundColor) {
		super(_mainApplet,_backgroundColor);
	}
	
	public void setBackgroundColor (int _backgroundColor) {
		backgroundColor = _backgroundColor;
	}

	public void reset () {
		// reset the scene
		mainApplet.background(backgroundColor);
		// set the camera to the position of the kinect, facing towards the scene
		mainApplet.camera(0,0,0,0,0,1,0,1,0);
		// rotate the scene: kinect data comes upside down! values can be controlled by user
		mainApplet.rotateX(rotX);
		mainApplet.rotateY(rotY);
		mainApplet.rotateZ(rotZ);
		mainApplet.translate(translateX,translateY,translateZ);
	}

	public short getSceneType() {
		return TherapeuticPresence.BASIC_SCENE3D;
	}

}
