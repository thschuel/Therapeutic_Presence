package scenes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import therapeuticpresence.AudioManager;

public class TunnelScene2D extends BasicScene2D {

	// tunnel effect is animated through audio
	protected AudioManager audioManager = null;
	
	// animate the background: tunnel effect
	protected PImage[] backgroundImg = new PImage[4];
	protected float numberOfCirclesForTunnel = 10f;
	protected float circlesOffset = 0f; // between 0 and 1
	protected float circlesStrokeWeight = 4f;
	protected int circlesColor;
	// these values are used to control the background tinting
	public static final float backgroundTintHueMax = 255f;
	public static final float backgroundTintBrightnessMax = 20f; // should depend on expected max value of FFT DC
	public static final float backgroundTintSaturationMax = 30f; // should depend on expected max value of FFT DC
	protected int backgroundTintHue = 120;
	protected float backgroundDelay = 12f;
	protected float fftDCValue=0f;
	protected float fftDCValueDelayed=0f;
	
	public TunnelScene2D (PApplet _mainApplet, int _backgroundColor, AudioManager _audioManager) {
		super(_mainApplet,_backgroundColor);
		audioManager = _audioManager;

		// setting up the background images
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,backgroundTintSaturationMax,backgroundTintBrightnessMax,1);
		backgroundColor = mainApplet.color(backgroundTintHue,0,0,1);
		circlesColor = mainApplet.color(backgroundTintHue,8f*backgroundTintSaturationMax/10f,backgroundTintBrightnessMax,0.18f);
//		for (int i=0; i<4; i++) {
//			backgroundImg[i] = mainApplet.loadImage("../data/backgroundpic"+(i+1)+".jpg");
//			backgroundImg[i].resize(mainApplet.width,mainApplet.height);
//		}
		mainApplet.colorMode(PApplet.RGB,255,255,255,255);
	}

	public void reset() {
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
		drawTunnel();
		fftDCValue = audioManager.getMeanFFT(0);
		if (fftDCValueDelayed == 0) fftDCValueDelayed=fftDCValue;
		else fftDCValueDelayed += (fftDCValue-fftDCValueDelayed)/backgroundDelay;
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,backgroundTintSaturationMax,backgroundTintBrightnessMax,1);
		backgroundColor = mainApplet.color(backgroundTintHue,backgroundTintSaturationMax,fftDCValueDelayed,1);
		mainApplet.colorMode(PApplet.RGB,255,255,255,255);
	}
	
	private void drawTunnel () {
		// draw circles for tunnel effect
		mainApplet.stroke(circlesColor);
		mainApplet.strokeWeight(circlesStrokeWeight);
		circlesOffset += 1.0f/mainApplet.frameRate;
		if (circlesOffset > 1.0f) circlesOffset = 0.0f;
		float ellipseHeight = (0.1f*(mainApplet.height+500)/numberOfCirclesForTunnel);
		float ellipseWidth = (0.1f*(mainApplet.width+500)/numberOfCirclesForTunnel);
		float firstEllipseB = ellipseHeight/2;
		float firstEllipseA = ellipseWidth/2;
		mainApplet.ellipse(mainApplet.width/2, mainApplet.height/2, ellipseWidth, ellipseHeight);
		for (float i=0.2f; i<=numberOfCirclesForTunnel; i*=1.7f) {
			ellipseHeight = (i*(mainApplet.height+500)/numberOfCirclesForTunnel);
			ellipseHeight +=  (((i*1.7f)*(mainApplet.height+500)/numberOfCirclesForTunnel)-ellipseHeight)*circlesOffset;
			ellipseWidth = (i*(mainApplet.width+500)/numberOfCirclesForTunnel);
			ellipseWidth +=  (((i*1.7f)*(mainApplet.width+500)/numberOfCirclesForTunnel)-ellipseWidth)*circlesOffset;
			mainApplet.ellipse(mainApplet.width/2, mainApplet.height/2, ellipseWidth, ellipseHeight);
		}
		float lastEllipseB = ellipseHeight/2;
		float lastEllipseA = ellipseWidth/2;
		for (float i=1; i<=numberOfCirclesForTunnel; i++) { 
			float angle = i*PConstants.TWO_PI/numberOfCirclesForTunnel;
			float lineX1 = mainApplet.width/2 + firstEllipseA*PApplet.cos(angle);
			float lineY1 = mainApplet.height/2 + firstEllipseB*PApplet.sin(angle);
			float lineX2 = mainApplet.width/2 + lastEllipseA*PApplet.cos(angle);
			float lineY2 = mainApplet.height/2 + lastEllipseB*PApplet.sin(angle);
			mainApplet.line(lineX1, lineY1, lineX2, lineY2);
		}
		
//		PImage actBackgroundImg=backgroundImg[mainApplet.frameCount%4];
//		mainApplet.tint(backgroundColor);
//		mainApplet.image(actBackgroundImg, 0, actBackgroundImg.height/2-mainApplet.height/2,mainApplet.width,mainApplet.height);
		//mainApplet.tint(255,0);
		//backgroundImg.copy(backgroundImg, 5,5,backgroundImg.width-5,backgroundImg.height-5, 0,0,backgroundImg.width,backgroundImg.height);
		//mainApplet.image(backgroundImg,0,0);
	}

}
