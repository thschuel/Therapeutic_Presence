package scenes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import therapeuticpresence.AudioManager;
import therapeuticpresence.TherapeuticPresence;

public class TunnelScene2D extends BasicScene2D {

	// tunnel effect is animated through audio
	protected AudioManager audioManager = null;
	
	// animate the background: tunnel effect
	protected float numberOfCirclesForTunnel = 10f;
	protected float circlesOffset = 0f; // between 0 and 1
	protected float circlesStrokeWeight = 4f;
	protected int circlesColor = 0;
	protected float transitionSpeedSec = 1.0f;
	protected int backgroundTintHueMax = 255;
	protected int backgroundTintHue = 120;
	// background colors are controlled by audio stream
	protected float audioReactionDelay = 12f;
	protected float fftDCValue=0f;
	protected float fftDCValueDelayed=0f;
	
	public TunnelScene2D (TherapeuticPresence _mainApplet, int _backgroundColor, AudioManager _audioManager) {
		super(_mainApplet,_backgroundColor);
		audioManager = _audioManager;
		mainApplet.colorMode(PConstants.HSB,backgroundTintHueMax,10,10,100);
		circlesColor = mainApplet.color(backgroundTintHue,7,1,30); // transparent slightly less saturated and dark background Color
	}

	public void reset() {
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
		drawTunnel();
		// change colors based on audio stream
		fftDCValue = audioManager.getMeanFFT(0);
		fftDCValueDelayed += (fftDCValue-fftDCValueDelayed)/audioReactionDelay;
		mainApplet.colorMode(PConstants.HSB,backgroundTintHueMax,1,audioManager.maxFFT,1);
		backgroundColor = mainApplet.color(backgroundTintHue,1,fftDCValueDelayed,1);
		// transition effect
		circlesOffset += 1.0f/mainApplet.frameRate;
		if (circlesOffset > 1.0f) circlesOffset = 0.0f;
	}
	
	private void drawTunnel () {
		// draw circles for tunnel effect
		mainApplet.stroke(circlesColor);
		mainApplet.strokeWeight(circlesStrokeWeight);
		mainApplet.noFill();
		float ellipseHeight = 0.3f*mainApplet.height*2/numberOfCirclesForTunnel;
		float ellipseWidth = 0.3f*mainApplet.width*2/numberOfCirclesForTunnel;
		float firstEllipseB = ellipseHeight/2;
		float firstEllipseA = ellipseWidth/2;
		mainApplet.ellipse(mainApplet.width/2, mainApplet.height/2, ellipseWidth, ellipseHeight);
		for (float i=0.33f; i<=numberOfCirclesForTunnel; i*=1.7f) {
			ellipseHeight = (i*(mainApplet.height*2)/numberOfCirclesForTunnel);
			ellipseHeight +=  (((i*1.7f)*(mainApplet.height*2)/numberOfCirclesForTunnel)-ellipseHeight)*circlesOffset;
			ellipseWidth = (i*(mainApplet.width*2)/numberOfCirclesForTunnel);
			ellipseWidth +=  (((i*1.7f)*(mainApplet.width*2)/numberOfCirclesForTunnel)-ellipseWidth)*circlesOffset;
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

//		protected PImage backgroundImg[] = new PImage[4];
//		for (int i=0; i<4; i++) {
//			backgroundImg[i] = mainApplet.loadImage("../data/backgroundpic"+(i+1)+".jpg");
//			backgroundImg[i].resize(mainApplet.width,mainApplet.height);
//		}
//		PImage actBackgroundImg=backgroundImg[mainApplet.frameCount%4];
//		mainApplet.tint(backgroundColor);
//		mainApplet.image(actBackgroundImg, 0, actBackgroundImg.height/2-mainApplet.height/2,mainApplet.width,mainApplet.height);
//		mainApplet.tint(255,0);
//		backgroundImg.copy(backgroundImg, 5,5,backgroundImg.width-5,backgroundImg.height-5, 0,0,backgroundImg.width,backgroundImg.height);
//		mainApplet.image(backgroundImg,0,0);
	}

	public short getSceneType() {
		return TherapeuticPresence.TUNNEL_SCENE2D;
	}

}
