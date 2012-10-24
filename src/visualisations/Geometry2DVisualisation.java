/*
Copyright (c) 2012, Thomas Schueler, http://www.thomasschueler.de
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the author nor the
      names of the contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THOMAS SCHUELER BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package visualisations;

import java.util.ArrayList;
import therapeuticskeleton.Skeleton;

import processing.core.*;
import therapeuticpresence.*;
import utils.BezierCurve2D;

public class Geometry2DVisualisation extends AbstractSkeletonAudioVisualisation {
	
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
	protected ArrayList<BezierCurve2D> bezierCurves = new ArrayList<BezierCurve2D>();
	
	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected int width, height;
	protected float scale = 1f;
	
	// these values are used for drawing the bezier curves
	protected float delay = 8f;
	protected int radiation = 40;
	protected float scaleDC = 1f;
	protected float scaleAC = 12f;
	
	public Geometry2DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(true);
	}
	
	public void setup() {
		width = mainApplet.width;
		height = mainApplet.height;
		// fix center point
	    centerX = width/2;
		centerY = height/2;
		// coordinates based on canvas size
		updateCanvasCoordinates();
		// variable coordinates, will be controlled by user movement
		left1Y = height/2;
		left2Y = height/2;
		right2Y = height/2;
		right1Y = height/2;
		anchorL1Y = left1Y + ((left2Y-left1Y)/2);
		anchorL2Y = left1Y + ((left2Y-left1Y)/2);
		anchorL3Y = centerY + ((left2Y-centerY)/2);
		anchorR3Y = centerY + ((right2Y-centerY)/2);
		anchorR2Y = right1Y + ((right2Y-right1Y)/2);
		anchorR1Y = right1Y + ((right2Y-right1Y)/2);
	}
	
	private void updateCanvasCoordinates () {
		scale = skeleton.distanceToKinect()/TherapeuticPresence.maxDistanceToKinect;
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
	}
	
	public void draw () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
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
		updateCanvasCoordinates();
		
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
				int offset = PApplet.round(j*i*radiation*scale);
				BezierCurve2D temp = new BezierCurve2D(strokeWeight,color);
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
		return TherapeuticPresence.GEOMETRY_2D_VISUALISATION;
	}

}
