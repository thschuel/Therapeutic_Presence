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
	
	protected int width, height;
	
	protected ArrayList<BezierCurve> bezierCurves = new ArrayList<BezierCurve>();
	
	// these values are used for drawing the bezier curves
	protected float delay = 8f;
	protected int radiation = 40;
	protected float scaleDC = 1f;
	protected float scaleAC = 12f;
	
	public AudioVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton);
		audioManager = _audioManager;
	}
	
	public void setup() {
		// fix center point
	    centerX = mainApplet.width/2;
		centerY = mainApplet.height/2;
		width = mainApplet.width;
		height = mainApplet.height;
		left1Y = height/2;
		left2Y = height/2;
		right2Y = height/2;
		right1Y = height/2;
		updateFixCoordinates();
	}
	
	private void updateFixCoordinates () {
		left1X = centerX-width/2;
		left2X = centerX-width/4;
		right2X = centerX+width/4;
		right1X = centerX+width/2;	
		anchorL1X = centerX-5*width/8;
		anchorL2X = centerX-3*width/8;
		anchorL3X = centerX-width/8;
		anchorR3X = centerX+width/8;
		anchorR2X = centerX+3*width/8;
		anchorR1X = centerX+5*width/8;
		// y values change according to movement
		anchorL1Y = left1Y + ((left2Y-left1Y)/2);
		anchorL2Y = left1Y + ((left2Y-left1Y)/2);
		anchorL3Y = centerY + ((left2Y-centerY)/2);
		anchorR3Y = centerY + ((right2Y-centerY)/2);
		anchorR2Y = right1Y + ((right2Y-right1Y)/2);
		anchorR1Y = right1Y + ((right2Y-right1Y)/2);
		
	}
	
	public void draw () {
		if (skeleton.isUpdated && audioManager.isUpdated) {
			updateBezierCurves();
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
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
		
		// scale canvas for drawing according to distance to kinect
		float scale = skeleton.distanceToKinect()/3000f;
		width = PApplet.round(mainApplet.width*scale);
		height = PApplet.round(mainApplet.height*scale);
		updateFixCoordinates();
		
		// actual coordinates
		int left2YNew = centerY+(int)((left2X-centerX)*PApplet.sin(angleLeftUpperArm)/PApplet.cos(angleLeftUpperArm));
		int right2YNew = centerY-(int)((right2X-centerX)*PApplet.sin(angleRightUpperArm)/PApplet.cos(angleRightUpperArm));
		int left1YNew = left2YNew+(int)((left1X-left2X)*PApplet.sin(angleLeftLowerArm)/PApplet.cos(angleLeftLowerArm));
		int right1YNew = right2YNew-(int)((right1X-right2X)*PApplet.sin(angleRightLowerArm)/PApplet.cos(angleRightLowerArm));
		// constrain coordinates
		left2YNew = PApplet.constrain(left2YNew,centerY-height/2+50,centerY+height/2-50);
		right2YNew = PApplet.constrain(right2YNew,centerY-height/2+50,centerY+height/2-50);
		left1YNew = PApplet.constrain(left1YNew,centerY-height/2,centerY+height/2);
		right1YNew = PApplet.constrain(right1YNew,centerY-height/2,centerY+height/2);

		// update coordinates with delay
		left2Y += (left2YNew-left2Y)/delay;
		right2Y += (right2YNew-right2Y)/delay;
		left1Y += (left1YNew-left1Y)/delay;
		right1Y += (right1YNew-right1Y)/delay;
		// update anchorpoints based on controlpoints
		anchorL1Y = left1Y + ((left2Y-left1Y)/2);
		anchorL2Y = left1Y + ((left2Y-left1Y)/2);
		anchorL3Y = centerY + ((left2Y-centerY)/2);
		anchorR3Y = centerY + ((right2Y-centerY)/2);
		anchorR2Y = right1Y + ((right2Y-right1Y)/2);
		anchorR1Y = right1Y + ((right2Y-right1Y)/2);
		
		// Curves react to the waveform
		int bezierCurvePointCount = 11;
		int waveformOffsets[] = new int[bezierCurvePointCount];
		for (int i=0; i<bezierCurvePointCount; i++) {
			int index = PApplet.round(i*audioManager.getBufferSize()/bezierCurvePointCount);
			waveformOffsets[i] = 0;//PApplet.round(audioManager.getMeanSampleAt(index)*radiation);
		}
		
		// add BezierCurves to Array. based on the calculated coordinates and the FFT values
		for (int i=0; i<AudioManager.bands; i++) {
			float strokeWeight;
			int color;
			for (int j=-1; j<2; j+=2) {
				if (i==0) strokeWeight = audioManager.getMeanFFT(0)*scaleDC;
				else if (j==1) strokeWeight = audioManager.getLeftFFT(i)*scaleAC;
				else strokeWeight = audioManager.getRightFFT(i)*scaleAC;
				mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,100);
				color = mainApplet.color(i,255,255);
				int offset = PApplet.round(j*i*radiation*scale);
				BezierCurve temp = new BezierCurve(strokeWeight,color);
				temp.addAnchorPoint(anchorL1X, anchorL1Y+offset+waveformOffsets[0]);
				temp.addControlPoint(left1X,left1Y+offset+waveformOffsets[1]);
				temp.addAnchorPoint(anchorL2X, anchorL2Y+offset+waveformOffsets[2]);
				temp.addControlPoint(left2X,left2Y+offset+waveformOffsets[3]);
				temp.addAnchorPoint(anchorL3X, anchorL3Y+offset+waveformOffsets[4]);
				temp.addControlPoint(centerX,centerY+offset+waveformOffsets[5]);
				temp.addAnchorPoint(anchorR3X, anchorR3Y+offset+waveformOffsets[6]);
				temp.addControlPoint(right2X,right2Y+offset+waveformOffsets[7]);
				temp.addAnchorPoint(anchorR2X, anchorR2Y+offset+waveformOffsets[8]);
				temp.addControlPoint(right1X,right1Y+offset+waveformOffsets[9]);
				temp.addAnchorPoint(anchorR1X, anchorR1Y+offset+waveformOffsets[10]);
				bezierCurves.add(temp);
				if (i==0) j=2;
			}
		}
		
		// clean up bezierCurves ArrayList
		for (int i=0;i<bezierCurves.size();i++) {
			if (bezierCurves.get(i).transparency <= 0) {
				bezierCurves.remove(i);
			}
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.GEOMETRIC_AUDIO_VISUALISATION;
	}

}
