package Visuals;

import java.util.ArrayList;


import processing.core.*;
import therapeuticpresence.AudioManager;
import therapeuticpresence.BezierCurve;
import therapeuticpresence.Skeleton;
import therapeuticpresence.TherapeuticPresence;
import ddf.minim.*;
import ddf.minim.analysis.FFT;

public class AudioVisualisation extends SkeletonVisualisation {

	protected AudioManager audioManager;
	
	// animate the background: tunnel effect
	protected PImage[] backgroundImg = new PImage[4];
	protected float numberOfCirclesForTunnel = 10f;
	protected float circlesOffset = 0f; // between 0 and 1
	protected float circlesStrokeWeight = 4f;
	protected int circlesColor;
	// these values are used to control the background tinting
	public static float backgroundTintHueMax = 255f;
	public static float backgroundTintBrightnessMax = 20f; // should depend on expected max value of FFT DC
	public static float backgroundTintSaturationMax = 30f; // should depend on expected max value of FFT DC
	protected int backgroundTintHue = 120;
	protected float backgroundDelay = 12f;
	protected float fftDCValue=0f;
	protected float fftDCValueDelayed=0f;
	
	// coordinates for the visualization are defined through angles of limbs
	// bezier curves used for drawing. anchor points and control points.
	protected int anchorL1X, anchorL1Y;
	protected int left1X, left1Y;
	protected int anchorL2X, anchorL2Y;
	protected int left2X, left2Y;
	protected int anchorL3X, anchorL3Y;
	protected int centerX, centerY;
	protected int anchorR3X, anchorR3Y;
	protected int right2X, right2Y;
	protected int anchorR2X, anchorR2Y;
	protected int right1X, right1Y;
	protected int anchorR1X, anchorR1Y;
	
	protected ArrayList<BezierCurve> bezierCurves = new ArrayList<BezierCurve>();
	
	// these values are used for drawing the bezier curves
	protected float delay = 8f;
	protected int radiation = 40;
	protected float scaleDC = 1f;
	protected float scaleAC = 12f;
	
	protected GenerativeTreeVisualisation gtv = null;
	
	public AudioVisualisation (PApplet _mainApplet, Skeleton _skeleton) {
		super(_mainApplet,_skeleton);
		audioManager = new AudioManager(mainApplet);
	}
	
	public AudioVisualisation (PApplet _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton);
		audioManager = _audioManager;
	}
	
	public void setup() {

		// setting up the background images
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,backgroundTintSaturationMax,backgroundTintBrightnessMax,1);
		backgroundColor = mainApplet.color(backgroundTintHue,0,0,1);
		circlesColor = mainApplet.color(backgroundTintHue,8f*backgroundTintSaturationMax/10f,backgroundTintBrightnessMax,0.18f);
//		for (int i=0; i<4; i++) {
//			backgroundImg[i] = mainApplet.loadImage("../data/backgroundpic"+(i+1)+".jpg");
//			backgroundImg[i].resize(mainApplet.width,mainApplet.height);
//		}
	    // fixed x values
		left1X = 0;
		left2X = mainApplet.width/4;
	    centerX = mainApplet.width/2;
		right2X = 3*mainApplet.width/4;
		right1X = mainApplet.width;	
		anchorL1X = -mainApplet.width/8;
		anchorL2X = mainApplet.width/8;
		anchorL3X = 3*mainApplet.width/8;
		anchorR3X = 5*mainApplet.width/8;
		anchorR2X = 7*mainApplet.width/8;
		anchorR1X = 9*mainApplet.width/8;
		// y values change according to movement
		left1Y = mainApplet.height/2;
		left2Y = mainApplet.height/2;
		centerY = mainApplet.height/2;
		right2Y = mainApplet.height/2;
		right1Y = mainApplet.height/2;
		anchorL1Y = left1Y + ((left2Y-left1Y)/2);
		anchorL2Y = left1Y + ((left2Y-left1Y)/2);
		anchorL3Y = centerY + ((left2Y-centerY)/2);
		anchorR3Y = centerY + ((right2Y-centerY)/2);
		anchorR2Y = right1Y + ((right2Y-right1Y)/2);
		anchorR1Y = right1Y + ((right2Y-right1Y)/2);

		gtv = new GenerativeTreeVisualisation(mainApplet,skeleton,audioManager);
		gtv.setup();
	}

	public void reset() {
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
		drawTunnel();
	}
	
	public void draw () {
		
		if (gtv != null) gtv.draw();
		
		if (skeleton.isUpdated && audioManager.isUpdated) {
			fftDCValue = audioManager.getMeanFFT(0);
			if (fftDCValueDelayed == 0) fftDCValueDelayed=fftDCValue;
			else fftDCValueDelayed += (fftDCValue-fftDCValueDelayed)/backgroundDelay;
			mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,backgroundTintSaturationMax,backgroundTintBrightnessMax,1);
			backgroundColor = mainApplet.color(backgroundTintHue,backgroundTintSaturationMax,fftDCValueDelayed,1);
//			circlesStrokeWeight = fftDCValueDelayed;
			
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,255);
			mainApplet.noFill();
			updateBezierCurves();
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
			mainApplet.colorMode(PApplet.RGB,255,255,255,255);
		}
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
	
	private void updateBezierCurves () {
		float angleLeftUpperArm = skeleton.angleToYAxis(Skeleton.LEFT_SHOULDER,Skeleton.LEFT_ELBOW);
		float angleRightUpperArm = skeleton.angleToYAxis(Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_ELBOW);
		float angleLeftLowerArm = skeleton.angleBetween(Skeleton.LEFT_HAND,Skeleton.LEFT_ELBOW,Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER);
		float angleRightLowerArm = skeleton.angleBetween(Skeleton.RIGHT_HAND,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
		
		// check for angles not to reach out of quadrants.
		if(angleLeftUpperArm+angleLeftLowerArm > PConstants.PI) {
			angleLeftLowerArm = PConstants.PI - angleLeftUpperArm;
		}
		if(angleRightUpperArm+angleRightLowerArm > PConstants.PI) {
			angleRightLowerArm = PConstants.PI - angleRightUpperArm;
		}
		
		// shift angles of upper arms by 90 degree to use calculations in polar coordinates
		angleLeftUpperArm = (angleLeftUpperArm+PConstants.PI/2)%PConstants.PI;
		angleRightUpperArm = (angleRightUpperArm+PConstants.PI/2)%PConstants.PI;
		// shift angles of lower arms by angles of upper arms to use calculations in polar coordinates
		angleLeftLowerArm = (angleLeftLowerArm+angleLeftUpperArm)%PConstants.PI;
		angleRightLowerArm = (angleRightLowerArm+angleRightUpperArm)%PConstants.PI;
		
		// actual coordinates
		int leftQuarterYNew = centerY+(int)((left2X-centerX)*PApplet.sin(angleLeftUpperArm)/PApplet.cos(angleLeftUpperArm));
		int rightQuarterYNew = centerY-(int)((right2X-centerX)*PApplet.sin(angleRightUpperArm)/PApplet.cos(angleRightUpperArm));
		int leftYNew = left2Y+(int)((left1X-left2X)*PApplet.sin(angleLeftLowerArm)/PApplet.cos(angleLeftLowerArm));
		int rightYNew = right2Y-(int)((right1X-right2X)*PApplet.sin(angleRightLowerArm)/PApplet.cos(angleRightLowerArm));
		// constrain coordinates
		leftQuarterYNew = PApplet.constrain(leftQuarterYNew,50,mainApplet.height-50);
		rightQuarterYNew = PApplet.constrain(rightQuarterYNew,50,mainApplet.height-50);
		leftYNew = PApplet.constrain(leftYNew,0,mainApplet.height);
		rightYNew = PApplet.constrain(rightYNew,0,mainApplet.height);
		
		// update coordinates with delay
		left2Y += (leftQuarterYNew-left2Y)/delay;
		right2Y += (rightQuarterYNew-right2Y)/delay;
		left1Y += (leftYNew-left1Y)/delay;
		right1Y += (rightYNew-right1Y)/delay;
		// update anchorpoints based on controlpoints
		anchorL1Y = left1Y + ((left2Y-left1Y)/2);
		anchorL2Y = left1Y + ((left2Y-left1Y)/2);
		anchorL3Y = centerY + ((left2Y-centerY)/2);
		anchorR3Y = centerY + ((right2Y-centerY)/2);
		anchorR2Y = right1Y + ((right2Y-right1Y)/2);
		anchorR1Y = right1Y + ((right2Y-right1Y)/2);
		
		// add BezierCurves to Array. based on the calculated coordinates and the FFT values
		for (int i=0; i<AudioManager.bands; i++) {
			float strokeWeight;
			int color;
			if (i==0) {
				strokeWeight = audioManager.getMeanFFT(0)*scaleDC;
				color = mainApplet.color(i,255,255);
				BezierCurve temp = new BezierCurve(strokeWeight,color);
				temp.addAnchorPoint(anchorL1X, anchorL1Y);
				temp.addControlPoint(left1X,left1Y);
				temp.addAnchorPoint(anchorL2X, anchorL2Y);
				temp.addControlPoint(left2X,left2Y);
				temp.addAnchorPoint(anchorL3X, anchorL3Y);
				temp.addControlPoint(centerX,centerY);
				temp.addAnchorPoint(anchorR3X, anchorR3Y);
				temp.addControlPoint(right2X,right2Y);
				temp.addAnchorPoint(anchorR2X, anchorR2Y);
				temp.addControlPoint(right1X,right1Y);
				temp.addAnchorPoint(anchorR1X, anchorR1Y);
				bezierCurves.add(temp);
			} else {
				color = mainApplet.color(i,255,255);
				for (int j=-1; j<2; j+=2) {
					if (j==1) strokeWeight = audioManager.getLeftFFT(i)*scaleAC;
					else strokeWeight = audioManager.getRightFFT(i)*scaleAC;
					int offset = j*i*radiation;
					BezierCurve temp = new BezierCurve(strokeWeight,color);
					temp.addAnchorPoint(anchorL1X, anchorL1Y+offset);
					temp.addControlPoint(left1X,left1Y+offset);
					temp.addAnchorPoint(anchorL2X, anchorL2Y+offset);
					temp.addControlPoint(left2X,left2Y+offset);
					temp.addAnchorPoint(anchorL3X, anchorL3Y+offset);
					temp.addControlPoint(centerX,centerY+offset);
					temp.addAnchorPoint(anchorR3X, anchorR3Y+offset);
					temp.addControlPoint(right2X,right2Y+offset);
					temp.addAnchorPoint(anchorR2X, anchorR2Y+offset);
					temp.addControlPoint(right1X,right1Y+offset);
					temp.addAnchorPoint(anchorR1X, anchorR1Y+offset);
					bezierCurves.add(temp);
				}
			}
		}
		// clean up bezierCurves ArrayList
		for (int i=0;i<bezierCurves.size();i++) {
			if (bezierCurves.get(i).transparency <= 0) {
				bezierCurves.remove(i);
			}
		}
	}

}
