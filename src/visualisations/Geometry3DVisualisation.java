package visualisations;

import java.util.ArrayList;
import processing.core.*;
import therapeuticpresence.*;

// TODO: Implement 3d version of geometry class

public class Geometry3DVisualisation extends SkeletonVisualisation {

	protected AudioManager audioManager;
	
	// coordinates for the visualization are defined through angles of limbs
	// bezier curves used for drawing. anchor points and control points.
	protected PVector anchorL1 = new PVector();
	protected PVector left1 = new PVector();
	protected PVector anchorL2 = new PVector();
	protected PVector left2 = new PVector();
	protected PVector anchorL3 = new PVector();
	protected PVector center = new PVector();
	protected PVector anchorR3 = new PVector();
	protected PVector right2 = new PVector();
	protected PVector anchorR2 = new PVector();
	protected PVector right1 = new PVector();
	protected PVector anchorR1 = new PVector();
	protected ArrayList<BezierCurve3D> bezierCurves = new ArrayList<BezierCurve3D>();
	
	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width, height;
	
	// these values are used for drawing the bezier curves
	protected float delay = 8f;
	protected int radiation = 40;
	protected float scaleDC = 1f;
	protected float scaleAC = 12f;
	
	public Geometry3DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton);
		audioManager = _audioManager;
	}
	
	public void setup() {
		// coordinates based on canvas size
		updateCanvasCoordinates(1000f,1000f);
	}
	
	public void updateCanvasCoordinates (float _width, float _height) {
	    center = skeleton.getJoint(Skeleton.LEFT_SHOULDER);
		width = 200f+(5000f-center.z)*1800f/5000f;
		height = 200f+(5000f-center.z)*1800f/5000f;
		left1.x = center.x-width/2;
		left2.x = center.x-width/4;
		right2.x = center.x+width/4;
		right1.x = center.x+width/2;	
		anchorL1.x = center.x-5*width/8;
		anchorL2.x = center.x-3*width/8;
		anchorL3.x = center.x-width/8;
		anchorR3.x = center.x+width/8;
		anchorR2.x = center.x+3*width/8;
		anchorR1.x = center.x+5*width/8;

		left1.z = center.z;
		left2.z = center.z;
		right2.z = center.z;
		right1.z = center.z;	
		anchorL1.z = center.z;
		anchorL2.z = center.z;
		anchorL3.z = center.z;
		anchorR3.z = center.z;
		anchorR2.z = center.z;
		anchorR1.z = center.z;
	}
	
	public void draw () {
		if (skeleton.isUpdated && audioManager.isUpdated) {
			mainApplet.pushMatrix();
			mainApplet.rotateZ(PConstants.PI);
//			mainApplet.rotateX(PConstants.PI);
			updateCanvasCoordinates(1000f,1000f);
			updateBezierCurves();
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
			mainApplet.popMatrix();
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
		float left2YNew = center.y+(left2.x-center.x)*PApplet.sin(angleLeftUpperArm)/PApplet.cos(angleLeftUpperArm);
		float right2YNew = center.y-(right2.x-center.x)*PApplet.sin(angleRightUpperArm)/PApplet.cos(angleRightUpperArm);
		float left1YNew = left2YNew+(left1.x-left2.x)*PApplet.sin(angleLeftLowerArm)/PApplet.cos(angleLeftLowerArm);
		float right1YNew = right2YNew-(right1.x-right2.x)*PApplet.sin(angleRightLowerArm)/PApplet.cos(angleRightLowerArm);
		// constrain coordinates
		left2YNew = PApplet.constrain(left2YNew,center.y-height/2+50,center.y+height/2-50);
		right2YNew = PApplet.constrain(right2YNew,center.y-height/2+50,center.y+height/2-50);
		left1YNew = PApplet.constrain(left1YNew,center.y-height/2,center.y+height/2);
		right1YNew = PApplet.constrain(right1YNew,center.y-height/2,center.y+height/2);

		// update coordinates with delay
		left2.y += (left2YNew-left2.y)/delay;
		right2.y += (right2YNew-right2.y)/delay;
		left1.y += (left1YNew-left1.y)/delay;
		right1.y += (right1YNew-right1.y)/delay;
		// update anchorpoints based on controlpoints
		anchorL1.y = left1.y + ((left2.y-left1.y)/2);
		anchorL2.y = left1.y + ((left2.y-left1.y)/2);
		anchorL3.y = center.y + ((left2.y-center.y)/2);
		anchorR3.y = center.y + ((right2.y-center.y)/2);
		anchorR2.y = right1.y + ((right2.y-right1.y)/2);
		anchorR1.y = right1.y + ((right2.y-right1.y)/2);
		// update z values
		
		
		
		// add BezierCurves to Array. based on the calculated coordinates and the FFT values
		for (int i=0; i<AudioManager.bands; i++) {
			int strokeWeight;
			int color;
			for (int j=-1; j<2; j+=2) {
				if (i==0) strokeWeight = PApplet.round(audioManager.getMeanFFT(0)*scaleDC);
				else if (j==1) strokeWeight = PApplet.round(audioManager.getLeftFFT(i)*scaleAC);
				else strokeWeight = PApplet.round(audioManager.getRightFFT(i)*scaleAC);
				mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,100);
				color = mainApplet.color(i,255,255);
				int offset = PApplet.round(j*i*radiation);
				BezierCurve3D temp = new BezierCurve3D(strokeWeight,color);
				PVector tempVector = new PVector();
				tempVector.set(anchorL1.x,anchorL1.y + offset,anchorL1.z);
				temp.addAnchorPoint(tempVector);
				tempVector.set(left1.x,left1.y + offset,left1.z);
				temp.addControlPoint(tempVector);
				tempVector.set(anchorL2.x,anchorL2.y + offset,anchorL2.z);
				temp.addAnchorPoint(tempVector);
				tempVector.set(left2.x,left2.y + offset,left2.z);
				temp.addControlPoint(tempVector);
				tempVector.set(anchorL3.x,anchorL3.y + offset,anchorL3.z);
				temp.addAnchorPoint(tempVector);
				tempVector.set(center.x,center.y + offset,center.z);
				temp.addControlPoint(tempVector);
				tempVector.set(anchorR3.x,anchorR3.y + offset,anchorR3.z);
				temp.addAnchorPoint(tempVector);
				tempVector.set(right2.x,right2.y + offset,right2.z);
				temp.addControlPoint(tempVector);
				tempVector.set(anchorR2.x,anchorR2.y + offset,anchorR2.z);
				temp.addAnchorPoint(tempVector);
				tempVector.set(right1.x,right1.y + offset,right1.z);
				temp.addControlPoint(tempVector);
				tempVector.set(anchorR1.x,anchorR1.y + offset,anchorR1.z);
				temp.addAnchorPoint(tempVector);
				bezierCurves.add(temp);
				if (i==0) j=2; // draw dc curve only once
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
		return TherapeuticPresence.GEOMETRY_3D_VISUALISATION;
	}

}
