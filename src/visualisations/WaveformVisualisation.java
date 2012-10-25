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
import processing.core.*;
import scenes.TunnelScene;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;
import utils.BezierCurve;

public class WaveformVisualisation extends AbstractSkeletonAudioVisualisation {
	
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
	protected ArrayList<BezierCurve> bezierCurves = new ArrayList<BezierCurve>();
	
	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width=0, height=0;
	protected float centerZ=0;
	protected float fadeInCenterZ=0;
	protected final float lowerZBoundary = 0.3f*TunnelScene.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.85f*TunnelScene.tunnelLength;
	
	// these values are used for drawing the bezier curves
	protected final int radiation = 30;
	protected final float scaleDC = 1f;
	protected final float scaleAC = 5f;
	protected final float strokeWeight = 1.7f;
	
	public WaveformVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
	}
	
	public void setup() {
		mainApplet.noLights();
		movementResponseDelay=8f;
	}
	
	// TODO: change calculation so that higher degree of freedom of movements is possible
	// use spherical coordinates? r = width/2, angle1 = angle of arm to body axis, angle2 = angle of arm segment to reference axis in reference plane
	private void updateCanvasCoordinates () {
	    center.set(0,0,centerZ);
		width = TunnelScene.getTunnelWidthAt(centerZ);
		height = TunnelScene.getTunnelHeightAt(centerZ);
		// width/4 == distance between control points
		// control points +- width/8 == anchor points
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
		left1.z += (center.z + skeleton.getJoint(Skeleton.LEFT_HAND).z-skeleton.getJoint(Skeleton.LEFT_SHOULDER).z - left1.z)/movementResponseDelay;
		left2.z += (center.z + skeleton.getJoint(Skeleton.LEFT_ELBOW).z-skeleton.getJoint(Skeleton.LEFT_SHOULDER).z - left2.z)/movementResponseDelay;
		right2.z += (center.z + skeleton.getJoint(Skeleton.RIGHT_ELBOW).z-skeleton.getJoint(Skeleton.RIGHT_SHOULDER).z - right2.z)/movementResponseDelay;
		right1.z += (center.z + skeleton.getJoint(Skeleton.RIGHT_HAND).z-skeleton.getJoint(Skeleton.RIGHT_SHOULDER).z - right1.z)/movementResponseDelay;
		anchorL1.z += (left1.z - (left2.z-left1.z)/2f - anchorL1.z)/movementResponseDelay;
		anchorL2.z += (left1.z + (left2.z-left1.z)/2f - anchorL2.z)/movementResponseDelay;
		anchorL3.z += (left2.z + (center.z-left2.z)/2f - anchorL3.z)/movementResponseDelay;
		anchorR3.z += (right2.z + (center.z-right2.z)/2f - anchorR3.z)/movementResponseDelay;
		anchorR2.z += (right1.z + (right2.z-right1.z)/2f - anchorR2.z)/movementResponseDelay;
		anchorR1.z += (right1.z - (right2.z-right1.z)/2f - anchorR1.z)/movementResponseDelay;
	}
	
	private void updateBezierCurves () {
		float angleLeftUpperArm = skeleton.getAngleLeftUpperArm();
		float angleRightUpperArm = skeleton.getAngleRightUpperArm();
		float angleLeftLowerArm = skeleton.getAngleLeftLowerArm();
		float angleRightLowerArm = skeleton.getAngleRightLowerArm();
		
		// check for angles not to reach out of quadrants.
		angleLeftUpperArm = PApplet.constrain(angleLeftUpperArm,PConstants.QUARTER_PI,3*PConstants.QUARTER_PI);
		angleRightUpperArm = PApplet.constrain(angleRightUpperArm,PConstants.QUARTER_PI,3*PConstants.QUARTER_PI);
		angleLeftLowerArm = PApplet.constrain(angleLeftLowerArm,0,PApplet.radians(120)*((angleLeftUpperArm-PConstants.QUARTER_PI)/PConstants.HALF_PI));
		angleRightLowerArm = PApplet.constrain(angleRightLowerArm,0,PApplet.radians(120)*((angleRightUpperArm-PConstants.QUARTER_PI)/PConstants.HALF_PI));
		
		// use negative angles because kinect data comes upside down
		angleLeftLowerArm = -angleLeftLowerArm;
		angleRightLowerArm = -angleRightLowerArm;
		
		// shift angles of upper arms by 90 degree to use calculations in polar coordinates
		angleLeftUpperArm = (angleLeftUpperArm+PConstants.HALF_PI)%PConstants.PI;
		angleRightUpperArm = (angleRightUpperArm+PConstants.HALF_PI)%PConstants.PI;
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
		left2.y += (left2YNew-left2.y)/movementResponseDelay;
		right2.y += (right2YNew-right2.y)/movementResponseDelay;
		left1.y += (left1YNew-left1.y)/movementResponseDelay;
		right1.y += (right1YNew-right1.y)/movementResponseDelay;
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
				mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,BezierCurve.MAX_TRANSPARENCY);
				color = mainApplet.color(i,255,255,255);
				int offset = PApplet.round(j*i*radiation);
				BezierCurve temp = new BezierCurve(strokeOffsetGrowth,color);
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
	
	public void draw () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			centerZ += (mappedDistance-centerZ)/movementResponseDelay;
			updateCanvasCoordinates();
			updateBezierCurves();
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,BezierCurve.MAX_TRANSPARENCY);
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
			centerZ += (mappedDistance-centerZ)/movementResponseDelay;
			updateCanvasCoordinates();
			updateBezierCurves();
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,BezierCurve.MAX_TRANSPARENCY);
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
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,BezierCurve.MAX_TRANSPARENCY);
			mainApplet.strokeWeight(strokeWeight);
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
			return false;
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.WAVEFORM_VISUALISATION;
	}

}
