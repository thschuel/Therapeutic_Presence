package scenes;

import processing.core.PApplet;
import processing.core.PConstants;
import therapeuticpresence.PostureProcessing;
import therapeuticpresence.TherapeuticPresence;

public class BasicScene2D extends AbstractScene {
	protected int alertColor;
	protected float activeTime = 0f;
	protected final float alertFadeOutTime = 1f; // seconds to fade out
	protected final float timeToFullAlert = PostureProcessing.timeHoldShapeToTrigger; // seconds to fully display alert
	
	public BasicScene2D (TherapeuticPresence _mainApplet, int _backgroundColor) {
		super(_mainApplet,_backgroundColor);
		mainApplet.colorMode(PConstants.RGB,255,255,255,100);
		alertColor = mainApplet.color(255,0,0,100);
	}

	public void reset() {
		// apply alert
		if (activeTime > timeToFullAlert) activeTime = timeToFullAlert;
		int newAlpha = PApplet.round(100f*activeTime/timeToFullAlert);
		alertColor = (alertColor&0xffffff) | newAlpha<<24;
		backgroundColor=PApplet.blendColor(defaultBackgroundColor,alertColor,PConstants.BLEND);
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
		// fade out alert
		activeTime -= alertFadeOutTime/mainApplet.frameRate;
		if (activeTime<0f) activeTime=0f;
	}

	public short getSceneType() {
		return TherapeuticPresence.BASIC_SCENE2D;
	}

	public boolean sceneIs3D() {
		return false;
	}
	
	public void shapeActiveAlert (float _activeTime) {
		activeTime = _activeTime;
	}
}
