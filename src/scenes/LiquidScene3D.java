package scenes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import therapeuticpresence.*;
import utils.GridSolver;
import shapes3d.*;

public class LiquidScene3D extends BasicScene3D {
	protected AudioManager audioManager = null;
	
	protected int backgroundTintColor = 0;
	protected int backgroundTintHueMax = 360;
	protected int backgroundTintHue = 207;
	protected int defaultBackgroundHighlightColor = 0x10ffffff;
	protected float audioReactionDelay = 12f;
	protected float fftDCValue=0f;
	protected float fftDCValueDelayed=0f;

	private float angle=0;
	private float lightPosX=0;
	private float lightPosY=0;
	private Ellipsoid ellipsoid;
	
	public LiquidScene3D (TherapeuticPresence _mainApplet, int _backgroundColor, AudioManager _audioManager) {
		super (_mainApplet,_backgroundColor);
		audioManager = _audioManager;
		// set up for third person view
		cameraZ = TherapeuticPresence.cameraEyeZ;

		ellipsoid = new Ellipsoid(mainApplet,20,20);
		ellipsoid.setTexture("../data/smoketex.jpg");
		ellipsoid.setRadius(10000f,10000f,1000f);
		ellipsoid.moveTo(0,0,-1000f);
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,100,100,100);
		ellipsoid.fill(mainApplet.color(207,92,100,100));
		ellipsoid.drawMode(Shape3D.SOLID|Shape3D.TEXTURE);
	}
	
	public void reset () {
		mainApplet.pushStyle();
		// change colors based on audio stream
		fftDCValue = audioManager.getMeanFFT(0);
		fftDCValueDelayed += (fftDCValue-fftDCValueDelayed)/audioReactionDelay;
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,100,audioManager.getMaxFFT()/3f,100);
		backgroundTintColor = mainApplet.color(backgroundTintHue,100,fftDCValueDelayed,100);
		defaultBackgroundColor = PApplet.blendColor(backgroundTintColor,defaultBackgroundHighlightColor,PConstants.BLEND);
		backgroundTintColor = PApplet.blendColor(backgroundTintColor,alertColor,PConstants.BLEND);
		super.reset();
  
		lightPosX = PApplet.cos(PApplet.radians(angle)) * 6000f;
		lightPosY = PApplet.sin(PApplet.radians(angle)) * 6000f;
		angle++;
		//mainApplet.ambientLight(10,140,245);
		mainApplet.lights();
		mainApplet.directionalLight(backgroundTintHue,92,fftDCValueDelayed,lightPosX,lightPosY,6000f);
		mainApplet.lightSpecular(backgroundTintHue,92,fftDCValueDelayed);
		mainApplet.specular(backgroundTintColor);
		mainApplet.shininess(5.0f);
		ellipsoid.draw();
		mainApplet.popStyle();
	}

	public short getSceneType() {
		return TherapeuticPresence.LIQUID_SCENE3D;
	}
}
