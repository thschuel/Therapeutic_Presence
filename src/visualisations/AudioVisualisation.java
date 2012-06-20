package visualisations;

import java.util.ArrayList;


import processing.core.*;
import therapeuticpresence.*;

public class AudioVisualisation extends SkeletonVisualisation {

	protected AudioManager audioManager;
	
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
	
	public AudioVisualisation (PApplet _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton);
		audioManager = _audioManager;
	}
	
	public void setup() {
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
	}
	
	public void draw () {
		if (skeleton.isUpdated && audioManager.isUpdated) {
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,255);
			mainApplet.noFill();
			updateBezierCurves();
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
			mainApplet.colorMode(PApplet.RGB,255,255,255,255);
		}
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
