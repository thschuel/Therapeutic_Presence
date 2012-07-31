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
	public float cameraX = 0.0f;
	public float cameraY = 0.0f;
	public float cameraZ = 0.0f;
	public float upX = 0.0f; 
	public float upY = -1.0f; // kinect data comes upside down
	public float upZ = 0.0f;

	public BasicScene3D (TherapeuticPresence _mainApplet, int _backgroundColor) {
		super(_mainApplet,_backgroundColor);
		mainApplet.colorMode(PConstants.RGB,255,255,255,100);
		alertColor = mainApplet.color(255,0,0,100);
		cameraZ=TherapeuticPresence.cameraEyeZ;
	}

	public void reset () {
		// apply alert
		if (activeTime > timeToFullAlert) activeTime = timeToFullAlert;
		int newAlpha = PApplet.round(90f*activeTime/timeToFullAlert);
		alertColor = (alertColor&0xffffff) | newAlpha<<24;
		backgroundColor=PApplet.blendColor(defaultBackgroundColor,alertColor,PConstants.BLEND);
		// reset the scene
		mainApplet.background(backgroundColor);
		// set the camera to the user specified position, facing towards the origin
		mainApplet.camera(cameraX,cameraY,cameraZ,0,0,0,upX,upY,upZ);
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
