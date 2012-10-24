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
import scenes.TunnelScene3D;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;
import utils.Agent;

public class Agent3DVisualisation extends AbstractSkeletonAudioVisualisation {
	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width, height;
	protected float centerX=0;
	protected float startTorsoX=0;
	protected float centerY=0;
	protected float startTorsoY=0;
	protected float centerZ=0;
	protected float fadeInCenterZ=0;
	protected PVector center = new PVector();
	private float orientation = 0;
	protected final float lowerZBoundary = 0.3f*TunnelScene3D.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.85f*TunnelScene3D.tunnelLength;
	
	// the ellipse to draw
	protected Agent[] agents = new Agent[100];
	
	// these values are used for drawing the audioresponsive circles
	protected final float delay = 8f;
	protected final int radiation = 30;
	protected final float scaleDC = 2f;
	protected final float scaleAC = 13f;
	protected final float strokeWeight = 1.7f;
	
	public Agent3DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(false);
	}
	
	public void setup() {
		mainApplet.colorMode(PConstants.HSB,360,100,100);
		for(int i=0; i<agents.length; i++) {
			float angle = PConstants.TWO_PI*((float)i/agents.length);
			float x = PApplet.cos(angle)*150;
			float y = PApplet.sin(angle)*150;
			agents[i]=new Agent(mainApplet,x,y,angle);
		}
		PVector torso = skeleton.getJoint(Skeleton.TORSO);
		startTorsoX=torso.x;
		startTorsoY=torso.y;
	}
	
	public void updateCanvasCoordinates () {
		width = TunnelScene3D.getTunnelWidthAt(centerZ);
		height = TunnelScene3D.getTunnelHeightAt(centerZ);
		PVector torso = skeleton.getJoint(Skeleton.TORSO);
		float mappedTorsoX = PApplet.constrain(torso.x-startTorsoX,-width/2,width/2);
		float mappedTorsoY = PApplet.constrain(torso.y-startTorsoY,-height/2,height/2);
		centerX =0;//+= (mappedTorsoX-centerX)/delay;
		centerY =0;//+= (mappedTorsoY-centerY)/delay;
	    center.set(centerX,centerY,centerZ);
		float orientationSkeleton = PVector.angleBetween(new PVector(0,0,1),skeleton.getOrientationX()) - PConstants.HALF_PI;
		// TODO: this is a hack. find solution for changing mirror kinect on the fly
		if (!TherapeuticPresence.mirrorKinect) {
			orientation += (orientationSkeleton*0.4-orientation)/delay;
		} else {
			orientation += (-orientationSkeleton*0.4-orientation)/delay;
		}
			
	}
	
	public void draw () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			float leftToY = PVector.angleBetween(skeleton.getLeftUpperArm(),skeleton.getOrientationY());
			float rightToY = PVector.angleBetween(skeleton.getRightUpperArm(),skeleton.getOrientationY());
			float leftToZ = PVector.angleBetween(skeleton.getLeftUpperArm(),skeleton.getOrientationZ());
			float rightToZ = PVector.angleBetween(skeleton.getRightUpperArm(),skeleton.getOrientationZ());
			leftToY -= PConstants.HALF_PI; // shift to -half_pi .. half_pi
			rightToY -= PConstants.HALF_PI;
			leftToY *= 0.4; // scale down
			rightToY *= 0.4; // scale down
			leftToZ -= PConstants.HALF_PI; // shift to -half_pi .. half_pi
			rightToZ -= PConstants.HALF_PI;
			leftToZ *= 0.4; // scale down
			rightToZ *= 0.4; // scale down
			
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			centerZ += (mappedDistance-centerZ)/delay;
			updateCanvasCoordinates();
			
			mainApplet.pushStyle();
			mainApplet.lightSpecular(230, 230, 230); 
			mainApplet.directionalLight(200f, 200f, 200f, 0.5f, 0.5f, -1f); 
			mainApplet.specular(mainApplet.color(200)); 
			mainApplet.shininess(5.0f);
			mainApplet.pushMatrix();
			// ------ set view ------
			mainApplet.translate(center.x,center.y,center.z); 
			mainApplet.rotateY(orientation); 
			// ------ update and draw agents ------
			for(int i=0; i<agents.length; i++) {
			    agents[i].update1(leftToY,rightToY,leftToZ,rightToZ,width+100,height+100); 
			    agents[i].draw();
			}
			mainApplet.popMatrix();
			mainApplet.popStyle();
		}
	}
	

	public short getVisualisationType() {
		return TherapeuticPresence.ELLIPSOIDAL_3D_VISUALISATION;
	}

}
