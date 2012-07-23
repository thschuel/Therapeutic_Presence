package scenes;

import processing.core.*;
import therapeuticpresence.PostureProcessing;
import therapeuticpresence.TherapeuticPresence;

public class BasicScene3D extends AbstractScene {
	// variables for alerts
	protected int alertColor;
	protected float activeTime = 0f;
	protected final float alertFadeOutTime = 1f; // seconds to fade out
	protected final float timeToFullAlert = PostureProcessing.timeHoldShapeToTrigger; // seconds to fully display alert
	
	// variables to move through the scene. used only by some visualisations
	public float translateX = 0.0f;
	public float translateY = 0.0f;
	public float translateZ = 0.0f;
	public float rotX = PApplet.radians(0); 
	public float rotY = PApplet.radians(0);
	public float rotZ = PApplet.radians(180);  // by default rotate the hole scene 180deg around the z-axis, the data from openni comes upside down

	public BasicScene3D (TherapeuticPresence _mainApplet, int _backgroundColor) {
		super(_mainApplet,_backgroundColor);
		mainApplet.colorMode(PConstants.RGB,255,255,255,100);
		alertColor = mainApplet.color(255,0,0,100);
	}

	public void reset () {
		// apply alert
		if (activeTime > timeToFullAlert) activeTime = timeToFullAlert;
		int newAlpha = PApplet.round(90f*activeTime/timeToFullAlert);
		alertColor = (alertColor&0xffffff) | newAlpha<<24;
		backgroundColor=PApplet.blendColor(defaultBackgroundColor,alertColor,PConstants.BLEND);
		// reset the scene
		mainApplet.background(backgroundColor);
		// set the camera to the position of the kinect, facing towards the scene
		mainApplet.camera(0,0,0,0,0,1,0,1,0);
		// rotate the scene: kinect data comes upside down! values can be controlled by user
		mainApplet.rotateX(rotX);
		mainApplet.rotateY(rotY);
		mainApplet.rotateZ(rotZ);
		mainApplet.translate(translateX,translateY,translateZ);
		mainApplet.noLights();
		// fade out alert
		activeTime -= alertFadeOutTime/mainApplet.frameRate;
		if (activeTime<0f) activeTime=0f;
	}

	public short getSceneType() {
		return TherapeuticPresence.BASIC_SCENE3D;
	}

	public boolean sceneIs3D() {
		return true;
	}
	
	public void shapeActiveAlert (float _activeTime) {
		activeTime = _activeTime;
	}

}
