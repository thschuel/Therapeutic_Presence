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

import generativedesign.*;

import java.util.ArrayList;
import processing.core.*;
import scenes.TunnelScene;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;
import utils.TransformableMesh;

public class MeshVisualisation extends AbstractSkeletonAudioVisualisation {
	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width, height;
	protected float centerX=0;
	protected float startTorsoX=0;
	protected float centerY=0;
	protected float startTorsoY=0;
	protected float centerZ;
	protected float leftX=0;
	protected float leftY=0;
	protected float rightX=0;
	protected float rightY=0;
	protected float fadeInCenterZ=0;
	protected PVector center = new PVector();
	private float orientation = 0;
	protected final float lowerZBoundary = 0.3f*TunnelScene.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.85f*TunnelScene.tunnelLength;
	
	// the mesh to draw
	protected TransformableMesh mesh;
	
	// these values are used for audioresponsiveness
	protected final float delay = 8f;
	protected final int radiation = 30;
	protected final float scaleDC = 2f;
	protected final float scaleAC = 13f;
	protected final float strokeWeight = 1.7f;
	
	public MeshVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
	}
	
	public void setup() {
		mainApplet.pushStyle();
		mesh = new TransformableMesh(mainApplet);
		mesh.setUCount(100);
		mesh.setVCount(100);
		mesh.setColorRange(100, 192, 50, 80, 30, 50, 100);
		PVector torso = skeleton.getJoint(Skeleton.TORSO);
		startTorsoX=torso.x;
		startTorsoY=torso.y;
		mainApplet.popStyle();
	}
	
	public void updateCanvasCoordinates () {
		width = TunnelScene.getTunnelWidthAt(centerZ);
		height = TunnelScene.getTunnelHeightAt(centerZ);
		PVector torso = skeleton.getJoint(Skeleton.TORSO);
		float mappedTorsoX = PApplet.constrain(torso.x-startTorsoX,-width/2,width/2);
		float mappedTorsoY = PApplet.constrain(torso.y-startTorsoY,-height/2,height/2);
		centerX += (mappedTorsoX-centerX)/delay;
		centerY += (mappedTorsoY-centerY)/delay;
	    center.set(centerX,centerY,centerZ);
		float orientationSkeleton = PVector.angleBetween(new PVector(0,0,1),skeleton.getOrientationX()) - PConstants.HALF_PI;
		// TODO: this is a hack. find solution for changing mirror kinect on the fly
		if (!TherapeuticPresence.mirrorKinect) {
			orientation += (orientationSkeleton*0.8-orientation)/delay;
		} else {
			orientation += (-orientationSkeleton*0.8-orientation)/delay;
		}
			
	}
	
	public void draw () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {			
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			centerZ += (mappedDistance-centerZ)/delay;
			updateCanvasCoordinates();
			updateMesh();
			// rotate tree according to body rotation
			mainApplet.pushStyle();
			mainApplet.lightSpecular(230, 230, 230); 
			mainApplet.directionalLight(200f, 200f, 200f, 0.5f, 0.5f, -1f); 
			mainApplet.specular(mainApplet.color(200)); 
			mainApplet.shininess(5.0f);
			mainApplet.pushMatrix();
			mainApplet.translate(center.x,center.y,center.z);
			mainApplet.rotateY(PConstants.HALF_PI+orientation);
			mainApplet.scale(160);
			mainApplet.noStroke();
			mainApplet.fill(0);
			mesh.draw();
			mainApplet.popMatrix();
			mainApplet.popStyle();
		}
	}
	
	public boolean fadeIn () {
		return true;
	}
	
	// Fade out method is used to blend between visualisations.
	public boolean fadeOut () {
		return true;
	}
	
	private void updateMesh () {
		float left = PVector.angleBetween(skeleton.getLeftUpperArm(),skeleton.getOrientationY());
		float right = PVector.angleBetween(skeleton.getRightUpperArm(),skeleton.getOrientationY());
		
		// use sample data to shift offset
		float sampleValues[] = new float[11];
		for (int i=0; i<11; i++) {
			sampleValues[i] = audioManager.getMeanSampleAt((int)(i*audioManager.getBufferSize()/11))*1.5f;
		}

	    // recalculate points and draw mesh
		mesh.setParam(1, left/PConstants.PI);
	    mesh.setParam(2, right/PConstants.PI);
	    mesh.update();
		
	}

	public short getVisualisationType() {
		return TherapeuticPresence.ELLIPSOIDAL_VISUALISATION;
	}

}
