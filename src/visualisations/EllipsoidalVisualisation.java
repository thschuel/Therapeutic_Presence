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
import utils.Ellipsoid;

public class EllipsoidalVisualisation extends AbstractSkeletonAudioVisualisation {
	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width, height;
	protected float centerX=0;
	protected float startTorsoX=0;
	protected float centerY=0;
	protected float startTorsoY=0;
	protected float centerZ;
	protected float angleLeftLower=0;
	protected float angleRightLower=0;
	protected float angleLeftUpper=0;
	protected float angleRightUpper=0;
	protected float lHandX=0;
	protected float lHandY=0;
	protected float lHandZ=0;
	protected float rHandX=0;
	protected float rHandY=0;
	protected float rHandZ=0;
	protected float fadeInCenterZ=0;
	protected PVector center = new PVector();
	private float orientation = 0;
	protected final float lowerZBoundary = 0.3f*TunnelScene.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.85f*TunnelScene.tunnelLength;
	
	// the ellipse to draw
	protected ArrayList<Ellipsoid> ellipsoids = new ArrayList<Ellipsoid>();
	
	// these values are used for drawing the audioresponsive circles
	protected final float radiation = 80f;
	protected final float scaleDC = 2f;
	protected final float scaleAC = 20f;
	
	public EllipsoidalVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
	}
	
	public void setup() {
		PVector torso = skeleton.getJoint(Skeleton.TORSO);
		startTorsoX=torso.x;
		startTorsoY=torso.y;
		angleScale1=0.1f;
		angleScale2=0.9f;
		angleScale3=0.8f;
		movementResponseDelay=8f;
	}
	
	private void updateCanvasCoordinates () {
		width = TunnelScene.getTunnelWidthAt(centerZ);
		height = TunnelScene.getTunnelHeightAt(centerZ);
		PVector torso = skeleton.getJoint(Skeleton.NECK);
		float mappedTorsoX = PApplet.constrain(torso.x-startTorsoX,-width/2,width/2);
		float mappedTorsoY = PApplet.constrain(torso.y-startTorsoY,-height/2,height/2);
		centerX += (mappedTorsoX-centerX)/movementResponseDelay;
		centerY += (mappedTorsoY-centerY)/movementResponseDelay;
	    center.set(centerX,centerY,centerZ);
		float orientationSkeleton = PVector.angleBetween(new PVector(0,1,0),skeleton.getOrientationY());
		orientation += (orientationSkeleton*0.8-orientation)/movementResponseDelay;
			
	}
	
	public void draw () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			centerZ += (mappedDistance-centerZ)/movementResponseDelay;
			updateCanvasCoordinates();
			updateEllipsoids();
			for (int i=0; i<ellipsoids.size(); i++) {
				ellipsoids.get(i).draw(mainApplet);
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
			updateEllipsoids();
			for (int i=0; i<ellipsoids.size(); i++) {
				ellipsoids.get(i).draw(mainApplet);
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
		for (int i=0;i<ellipsoids.size();i++) {
			if (ellipsoids.get(i).transparency <= 0) {
				ellipsoids.remove(i--);
			}
		}
		if (ellipsoids.size() == 0) {
			return true;
		} else {
			for (int i=0; i<ellipsoids.size(); i++) {
				ellipsoids.get(i).draw(mainApplet);
			}
			return false;
		}
	}
	
	private void updateEllipsoids () {
		PVector lHand = skeleton.getJointLCS(Skeleton.LEFT_HAND);
		lHand.x = PApplet.map(lHand.x,0,900f,0,width/2);
		PVector rHand = skeleton.getJointLCS(Skeleton.RIGHT_HAND);
		rHand.x = PApplet.map(rHand.x,-900f,0f,-width/2,0);
//		PVector lHandrHand = PVector.sub(lHand,rHand);
		float distanceMapped = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,0f,1f);
		float angleLeftLowerNew = -skeleton.getAngleLeftLowerArm()*angleScale1;
		float angleRightLowerNew = -skeleton.getAngleRightLowerArm()*angleScale1;
		float angleLeftUpperNew = -skeleton.getAngleLeftUpperArm()*angleScale2;
		float angleRightUpperNew = -skeleton.getAngleRightUpperArm()*angleScale2;
		angleLeftLower += (angleLeftLowerNew-angleLeftLower)/movementResponseDelay;
		angleRightLower += (angleRightLowerNew-angleRightLower)/movementResponseDelay;
		angleLeftUpper += (angleLeftUpperNew-angleLeftUpper)/movementResponseDelay;
		angleRightUpper += (angleRightUpperNew-angleRightUpper)/movementResponseDelay;
		lHandX += (lHand.x-lHandX)/movementResponseDelay;
		lHandY += (lHand.y-lHandY)/movementResponseDelay;
		lHandZ += (lHand.z-lHandZ)/movementResponseDelay;
		rHandX += (rHand.x-rHandX)/movementResponseDelay;
		rHandY += (rHand.y-rHandY)/movementResponseDelay;
		rHandZ += (rHand.z-rHandZ)/movementResponseDelay;
		
		// use sample data to shift offset
//		float sampleValues[] = new float[11];
//		for (int i=0; i<11; i++) {
//			sampleValues[i] = audioManager.getMeanSampleAt((int)(i*audioManager.getBufferSize()/11))*1.5f;
//		}
		
		// add Ellipsoids to Array. based on the calculated coordinates and the FFT values
		for (int i=0; i<AudioManager.bands; i++) {
			float strokeWeight;
			int color;
			if (i==0) strokeWeight = audioManager.getMeanFFT(0)*scaleDC;
			else strokeWeight = audioManager.getMeanFFT(i)*scaleAC;
			mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,Ellipsoid.MAX_TRANSPARENCY);
			color = mainApplet.color(i,255,255,Ellipsoid.MAX_TRANSPARENCY);
			float offset = i*radiation;
			float radius = PApplet.map(angleLeftUpper,0,PConstants.PI,0f,1f)*1500f;//distanceMapped*1500f;
			Ellipsoid temp = new Ellipsoid(center,new PVector(lHandX,lHandY,lHandZ),radius+offset,strokeWeight,angleLeftLower,color,audioManager.getMeanFFT(0)/audioManager.getMaxFFT(),true);
			ellipsoids.add(temp);
			radius = PApplet.map(angleRightUpper,0,PConstants.PI,0f,1f)*1500f;
			temp = new Ellipsoid(center,new PVector(rHandX,rHandY,rHandZ),radius+offset,strokeWeight,angleRightLower,color,audioManager.getMeanFFT(0)/audioManager.getMaxFFT(),true);
			ellipsoids.add(temp);
			//color = mainApplet.color(i,255,255,Ellipsoid3D.MAX_TRANSPARENCY/2f);
			//temp = new Ellipsoid3D(center,distanceMapped+offset,strokeWeight,orientation,color,0,false);
			//ellipsoids.add(temp);
		}
		
		
		// clean up bezierCurves ArrayList
		for (int i=0;i<ellipsoids.size();i++) {
			if (ellipsoids.get(i).transparency <= 0) {
				ellipsoids.remove(i--);
			}
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.ELLIPSOIDAL_VISUALISATION;
	}

}
