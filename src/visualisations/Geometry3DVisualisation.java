package visualisations;

import java.util.ArrayList;
import processing.core.*;
import scenes.TunnelScene3D;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;
import utils.BezierCurve3D;
import utils.Constants;

public class Geometry3DVisualisation extends AbstractSkeletonAudioVisualisation {
	
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
	protected float width=0, height=0;
	protected float centerZ=0;
	protected float fadeInCenterZ=0;
	protected final float lowerZBoundary = 0.3f*TunnelScene3D.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.85f*TunnelScene3D.tunnelLength;
	
	// these values are used for drawing the bezier curves
	protected final float delay = 8f;
	protected final int radiation = 30;
	protected final float scaleDC = 1f;
	protected final float scaleAC = 5f;
	protected final float strokeWeight = 1.7f;
	
	public Geometry3DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(false);
	}
	
	public void setup() {
	}
	
	public void updateCanvasCoordinates () {
	    center.set(0,0,centerZ);
		width = TunnelScene3D.getTunnelWidthAt(centerZ);
		height = TunnelScene3D.getTunnelHeightAt(centerZ);
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
		left1.z += (center.z + skeleton.getJoint(Skeleton.LEFT_HAND).z-skeleton.getJoint(Skeleton.LEFT_SHOULDER).z - left1.z)/delay;
		left2.z += (center.z + skeleton.getJoint(Skeleton.LEFT_ELBOW).z-skeleton.getJoint(Skeleton.LEFT_SHOULDER).z - left2.z)/delay;
		right2.z += (center.z + skeleton.getJoint(Skeleton.RIGHT_ELBOW).z-skeleton.getJoint(Skeleton.RIGHT_SHOULDER).z - right2.z)/delay;
		right1.z += (center.z + skeleton.getJoint(Skeleton.RIGHT_HAND).z-skeleton.getJoint(Skeleton.RIGHT_SHOULDER).z - right1.z)/delay;
		anchorL1.z += (left1.z - (left2.z-left1.z)/2f - anchorL1.z)/delay;
		anchorL2.z += (left1.z + (left2.z-left1.z)/2f - anchorL2.z)/delay;
		anchorL3.z += (left2.z + (center.z-left2.z)/2f - anchorL3.z)/delay;
		anchorR3.z += (right2.z + (center.z-right2.z)/2f - anchorR3.z)/delay;
		anchorR2.z += (right1.z + (right2.z-right1.z)/2f - anchorR2.z)/delay;
		anchorR1.z += (right1.z - (right2.z-right1.z)/2f - anchorR1.z)/delay;
	}
	
	public void draw () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			centerZ += (mappedDistance-centerZ)/delay;
			updateCanvasCoordinates();
			updateBezierCurves();
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,BezierCurve3D.MAX_TRANSPARENCY);
			mainApplet.strokeWeight(strokeWeight);
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
		}
	}
	
	public boolean fadeIn () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			// center.z reacts to position of user with delay
			fadeInCenterZ+=skeleton.distanceToKinect()/mainApplet.frameRate;
			float mappedDistance = PApplet.map(fadeInCenterZ,0,TherapeuticPresence.maxDistanceToKinect,0,upperZBoundary);
			centerZ += (mappedDistance-centerZ)/delay;
			updateCanvasCoordinates();
			updateBezierCurves();
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,BezierCurve3D.MAX_TRANSPARENCY);
			mainApplet.strokeWeight(strokeWeight);
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
			if (fadeInCenterZ >= skeleton.distanceToKinect()) {
				return true;
			}
		}
		return false;
	}
	
	// Fade out method is used to blend between visualisations.
	public boolean fadeOut () {
		// clean up bezierCurves ArrayList
		for (int i=0;i<bezierCurves.size();i++) {
			if (bezierCurves.get(i).transparency <= 0) {
				bezierCurves.remove(i--);
			}
		}
		if (bezierCurves.size() == 0) {
			return true;
		} else {
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,BezierCurve3D.MAX_TRANSPARENCY);
			mainApplet.strokeWeight(strokeWeight);
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
			return false;
		}
	}
	
	private void updateBezierCurves () {
		float angleLeftUpperArm = skeleton.angleToYAxis(Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER);
		float angleRightUpperArm = skeleton.angleToYAxis(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
		float angleLeftLowerArm = skeleton.angleBetween(Skeleton.LEFT_HAND,Skeleton.LEFT_ELBOW,Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER);
		float angleRightLowerArm = skeleton.angleBetween(Skeleton.RIGHT_HAND,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
		
		// check for angles not to reach out of quadrants.
		angleLeftUpperArm = PApplet.constrain(angleLeftUpperArm,Constants.QUARTER_PI,Constants.THREE_QUARTER_PI);
		angleRightUpperArm = PApplet.constrain(angleRightUpperArm,Constants.QUARTER_PI,Constants.THREE_QUARTER_PI);
		angleLeftLowerArm = PApplet.constrain(angleLeftLowerArm,0,Constants.HALF_PI-(Constants.THREE_QUARTER_PI-angleLeftUpperArm));
		angleRightLowerArm = PApplet.constrain(angleRightLowerArm,0,Constants.HALF_PI-(Constants.THREE_QUARTER_PI-angleRightUpperArm));
		
		// use negative angles because kinect data comes upside down
		angleLeftLowerArm = -angleLeftLowerArm;
		angleRightLowerArm = -angleRightLowerArm;
		
		// shift angles of upper arms by 90 degree to use calculations in polar coordinates
		angleLeftUpperArm = (angleLeftUpperArm+Constants.HALF_PI)%Constants.PI;
		angleRightUpperArm = (angleRightUpperArm+Constants.HALF_PI)%Constants.PI;
		// shift angles of lower arms by angles of upper arms to use calculations in polar coordinates
		angleLeftLowerArm = (angleLeftLowerArm+angleLeftUpperArm)%Constants.PI;
		angleRightLowerArm = (angleRightLowerArm+angleRightUpperArm)%Constants.PI;
		
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
		// use sample data to shift offset
		float sampleValues[] = new float[11];
		for (int i=0; i<11; i++) {
			sampleValues[i] = audioManager.getMeanSampleAt((int)(i*audioManager.getBufferSize()/11))*1.5f;
		}
		
		// add BezierCurves to Array. based on the calculated coordinates and the FFT values
		for (int i=0; i<AudioManager.bands; i++) {
			float strokeOffsetGrowth;
			int color;
			for (int j=-1; j<2; j+=2) {
				if (i==0) strokeOffsetGrowth = audioManager.getMeanFFT(0)*scaleDC;
				else if (j==1) strokeOffsetGrowth = audioManager.getLeftFFT(i)*scaleAC;
				else strokeOffsetGrowth = audioManager.getRightFFT(i)*scaleAC;
				mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,BezierCurve3D.MAX_TRANSPARENCY);
				color = mainApplet.color(i,255,255,255);
				int offset = PApplet.round(j*i*radiation);
				BezierCurve3D temp = new BezierCurve3D(strokeOffsetGrowth,color);
				temp.addAnchorPoint(new PVector(anchorL1.x,anchorL1.y + (offset*sampleValues[0]),anchorL1.z));
				temp.addControlPoint(new PVector(left1.x,left1.y + (offset*sampleValues[1]),left1.z));
				temp.addAnchorPoint(new PVector(anchorL2.x,anchorL2.y + (offset*sampleValues[2]),anchorL2.z));
				temp.addControlPoint(new PVector(left2.x,left2.y + (offset*sampleValues[3]),left2.z));
				temp.addAnchorPoint(new PVector(anchorL3.x,anchorL3.y + (offset*sampleValues[4]),anchorL3.z));
				temp.addControlPoint(new PVector(center.x,center.y + (offset*sampleValues[5]),center.z));
				temp.addAnchorPoint(new PVector(anchorR3.x,anchorR3.y + (offset*sampleValues[6]),anchorR3.z));
				temp.addControlPoint(new PVector(right2.x,right2.y + (offset*sampleValues[7]),right2.z));
				temp.addAnchorPoint(new PVector(anchorR2.x,anchorR2.y + (offset*sampleValues[8]),anchorR2.z));
				temp.addControlPoint(new PVector(right1.x,right1.y + (offset*sampleValues[9]),right1.z));
				temp.addAnchorPoint(new PVector(anchorR1.x,anchorR1.y + (offset*sampleValues[10]),anchorR1.z));
				bezierCurves.add(temp);
				if (i==0) j=2; // draw dc curve only once
			}
		}
		
		// clean up bezierCurves ArrayList
		for (int i=0;i<bezierCurves.size();i++) {
			if (bezierCurves.get(i).transparency <= 0) {
				bezierCurves.remove(i--);
			}
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.GEOMETRY_3D_VISUALISATION;
	}

}
