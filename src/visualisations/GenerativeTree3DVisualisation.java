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
import scenes.TunnelScene3D;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;

public class GenerativeTree3DVisualisation extends AbstractSkeletonAudioVisualisation {

	private int strokeColor = 0;

	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width, height;
	protected float centerZ;
	protected float fadeInCenterZ=0;
	protected float fadeOutCenterZ=0;
	protected PVector center = new PVector();
	protected final float lowerZBoundary = 0.45f*TunnelScene3D.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.7f*TunnelScene3D.tunnelLength;

	// variables to calculate and draw the tree
	private float curlRightLowerArm = 0;
	private float curlLeftLowerArm = 0;
	private float curlRightUpperArm = 0;
	private float curlLeftUpperArm = 0;
	private float armLeftToX = 0;
	private float armRightToX = 0;
	private float orientationTree = 0;
	private float downscaleStrokeLength = PApplet.sqrt(2)/2.f;
	private int minBranches = 5;
	private int addBranches = 5;
	private int branchCount = 0;
	
	// leafs
	private ArrayList<PVector> leafs = new ArrayList<PVector>();
	private ArrayList<Float> leafsRotationZ = new ArrayList<Float>();
	private ArrayList<PVector> leafsFalling = null;
	private ArrayList<Float> leafsRotationZFalling = null;
	private int[] leafColors;
	private int actColorIndex = 0;
	private int colorsSize = 360;
	private float colorsStepSize = 10.f;
	private int leafWidth = 25;
	private int leafHeight = 50;
	private boolean leafsFallDown = false;
	private float fallDownGravity = 5f;
	
	// audio responsive tree
	protected float initialScale = 2.5f;
	protected float downScale = 0.9f;
	protected float transparency = 200;
	protected float fallingLeafsTransparency = 200;
	protected float sampleStepSize =10f;
	protected int actSampleIndex = 0;
	
	public GenerativeTree3DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super (_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(true);
	}
	
	public void setup() {
		mainApplet.noLights();
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		strokeColor = mainApplet.color(250,250,250);
		mainApplet.colorMode(PConstants.HSB,360,100,100,255);
		leafColors = new int[colorsSize];
		angleScale1=0.6f;
		angleScale2=0.7f;
		angleScale3=0.8f;
		movementResponseDelay=20f;
		for (int i=0; i<colorsSize; i++) {
			leafColors[i] = mainApplet.color(i,100,100);
		}
	}
	
	public boolean fadeIn () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			fadeInCenterZ+=skeleton.distanceToKinect()/mainApplet.frameRate;
			if (fadeInCenterZ >= skeleton.distanceToKinect())
				return true;
			
			// center.z reacts to position of user with delay
			centerZ = PApplet.map(fadeInCenterZ,0,TherapeuticPresence.maxDistanceToKinect,0,upperZBoundary);
			width = TunnelScene3D.getTunnelWidthAt(centerZ);
			height = TunnelScene3D.getTunnelHeightAt(centerZ);
		    center.set(0,0,centerZ);
			// get angles for drawing
			getAnglesForBranches();
			// turn the tree fast for fade in effect
			orientationTree = (fadeInCenterZ/skeleton.distanceToKinect() * -1.4f * PConstants.TWO_PI)%PConstants.TWO_PI;
			// draw
			mainApplet.pushStyle();
			drawTree(minBranches+PApplet.round(addBranches*centerZ/upperZBoundary),centerZ/upperZBoundary,centerZ/upperZBoundary*height/3);
			drawLeafs();
			mainApplet.popStyle();
		}
		return false;
	}
	
	public boolean fadeOut () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			fadeOutCenterZ-=skeleton.distanceToKinect()/mainApplet.frameRate;
			if (fadeOutCenterZ <= 0f)
				return true;
			
			// center.z reacts to position of user with delay
			centerZ = fadeOutCenterZ;
			width = TunnelScene3D.getTunnelWidthAt(centerZ);
			height = TunnelScene3D.getTunnelHeightAt(centerZ);
		    center.set(0,0,centerZ);
			// get angles for drawing
			getAnglesForBranches();
			// turn the tree fast for fade out effect
			orientationTree = (fadeOutCenterZ/skeleton.distanceToKinect() * -1.4f * PConstants.TWO_PI)%PConstants.TWO_PI;
			// draw
			mainApplet.pushStyle();
			drawTree(minBranches+PApplet.round(addBranches*centerZ/upperZBoundary),centerZ/upperZBoundary,centerZ/upperZBoundary*height/3);
			drawLeafs();
			mainApplet.popStyle();
		}
		return false;
	}

	public void draw() {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			// center.z reacts to position of user with delay
			centerZ = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			width = TunnelScene3D.getTunnelWidthAt(centerZ);
			height = TunnelScene3D.getTunnelHeightAt(centerZ);
		    center.set(0,0,centerZ);
			// get angles for drawing
			getAnglesForBranches();
			// draw
			mainApplet.pushStyle();
			float initialStrokeLength = /*(PApplet.abs(armLeftToX)/PConstants.PI)*(PApplet.abs(armRightToX)/PConstants.PI) * */ (centerZ/upperZBoundary*height/3);
			drawTree(minBranches+PApplet.round(addBranches*centerZ/upperZBoundary),centerZ/upperZBoundary,initialStrokeLength);
			drawLeafs();
			mainApplet.popStyle();
			// prepare for fade out
			fadeOutCenterZ = centerZ;
			// reset leaf falling
			if (fallingLeafsTransparency <= 0) {
				leafsFallDown = false;
				leafsFalling = null;
				leafsRotationZFalling = null;
				fallingLeafsTransparency = transparency;
			}
		}	
	}
	
	public void shakeTree () {
		leafsFallDown = true;
	}
	
	private void getAnglesForBranches () {
		float angleLeftUpperArm = skeleton.angleToLocalYAxis(Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER); 
		float angleRightUpperArm = skeleton.angleToLocalYAxis(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
		float angleLeftLowerArm = skeleton.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER,Skeleton.LEFT_HAND,Skeleton.LEFT_ELBOW); 
		float angleRightLowerArm =  skeleton.angleBetween(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_HAND,Skeleton.RIGHT_ELBOW); 
		float orientationSkeleton = PVector.angleBetween(new PVector(0,0,1),skeleton.getOrientationX()) - PConstants.HALF_PI;

		// use distance to body plane measure
//		float angleLeftUpperArmToX = skeleton.angleToLocalXAxis(Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER);
//		float angleRightUpperArmToX = skeleton.angleToLocalXAxis(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
		// shift to -PI - PI
//		angleLeftUpperArmToX -= PConstants.HALF_PI;
//		angleRightUpperArmToX -= PConstants.HALF_PI;
		
		
		// TODO this is a hack. find solution to switch on/off mirroring of kinect
		if (!TherapeuticPresence.mirrorKinect) {
			// trees react to body posture with a delay
			curlLeftLowerArm += (angleRightLowerArm*angleScale1-curlLeftLowerArm)/movementResponseDelay;		
			curlLeftUpperArm += (angleRightUpperArm*angleScale2-curlLeftUpperArm)/movementResponseDelay;
			curlRightLowerArm += (angleLeftLowerArm*angleScale1-curlRightLowerArm)/movementResponseDelay;
			curlRightUpperArm += (angleLeftUpperArm*angleScale2-curlRightUpperArm)/movementResponseDelay;		
			orientationTree += (orientationSkeleton*angleScale3-orientationTree)/movementResponseDelay;
		} else {
			// trees react to body posture with a delay
			curlLeftLowerArm += (angleLeftLowerArm*angleScale1-curlLeftLowerArm)/movementResponseDelay;
			curlLeftUpperArm += (angleLeftUpperArm*angleScale2-curlLeftUpperArm)/movementResponseDelay;
			curlRightLowerArm += (angleRightLowerArm*angleScale1-curlRightLowerArm)/movementResponseDelay;		
			curlRightUpperArm += (angleRightUpperArm*angleScale2-curlRightUpperArm)/movementResponseDelay;	
			orientationTree += (-orientationSkeleton*angleScale3-orientationTree)/movementResponseDelay;
		}
		
		
	}
	
	private void drawTree(int branchDepth,float scale, float initialStrokeLength) {
		// colors of leafs differ in HSB-space
		colorsStepSize = colorsSize/PApplet.pow(2,branchDepth); 
		actColorIndex = 0;
		// leafs flicker according to music samples
		sampleStepSize = audioManager.getBufferSize()/PApplet.pow(2,branchDepth);
		actSampleIndex = 0;
		// the leafs
		if (leafsFallDown && leafsFalling == null) {
			leafsFalling = (ArrayList<PVector>)leafs.clone();
			leafsRotationZFalling = (ArrayList<Float>)leafsRotationZ.clone();
		}
		leafs.clear();
		leafsRotationZ.clear();

		// the strokeWeight reacts to the volume of the audio
		float strokeWeight = audioManager.getMeanFFT(0) * initialScale;
		
		// draw trunk of the tree
		mainApplet.pushMatrix();
		mainApplet.translate(center.x,-scale*height/8-initialStrokeLength,center.z);
		// rotate tree according to body rotation
		mainApplet.rotateY(orientationTree);
		
		mainApplet.pushStyle();
		mainApplet.noStroke();
		mainApplet.fill(strokeColor,transparency);
		mainApplet.beginShape(PConstants.QUAD_STRIP);
		mainApplet.vertex(-strokeWeight,0f,0f);
		mainApplet.vertex(strokeWeight,0f,0f);
		mainApplet.vertex(-strokeWeight,initialStrokeLength,0f);
		mainApplet.vertex(strokeWeight,initialStrokeLength,0f);
		mainApplet.endShape();
		mainApplet.translate(0,initialStrokeLength,0);
		// start branching
		branchCount = branchDepth;
		branch(initialStrokeLength*downscaleStrokeLength,branchDepth,strokeWeight*downScale);
		mainApplet.popStyle();
		mainApplet.popMatrix();
	}
	
	private void drawLeafs () {
		mainApplet.noStroke();
		mainApplet.pushMatrix();
		if (leafsFallDown && leafsFalling != null) {
			// draw falling leafs
			for (int i=0; i<leafsFalling.size(); i++) {
				float rand = mainApplet.random(-1f,1f);
				PVector leafFalling = leafsFalling.get(i);
				leafFalling.y-=fallDownGravity+rand*fallDownGravity;
				leafFalling.x+=rand*10;
				leafsFalling.set(i,leafFalling);
				mainApplet.fill(leafColors[PApplet.round(i*colorsStepSize)],fallingLeafsTransparency);
				mainApplet.pushMatrix();
				mainApplet.translate(leafFalling.x,leafFalling.y,leafFalling.z);
				mainApplet.rotateY(orientationTree);
				float angle = leafsRotationZFalling.get(i);
				angle += mainApplet.random(-1f,1f)*PApplet.radians(30);
				mainApplet.rotateZ(angle+1.5f*PConstants.PI); // transfer to angle between 0 and 2PI with regard to the positive y axis
				leafsRotationZFalling.set(i,angle);
				mainApplet.ellipse(0,0,leafWidth,leafHeight);
				mainApplet.popMatrix();
			}
			fallingLeafsTransparency -= fallDownGravity/1.5f;
		}
		// draw current leafs
		for (int i=0; i<leafs.size(); i++) {
			PVector leaf = leafs.get(i);
			mainApplet.fill(leafColors[PApplet.round(i*colorsStepSize)],transparency);
			mainApplet.pushMatrix();
			mainApplet.translate(leaf.x,leaf.y,leaf.z);
			mainApplet.rotateY(orientationTree);
			float angle = leafsRotationZ.get(i);
			mainApplet.rotateZ(angle+1.5f*PConstants.PI); // transfer to angle between 0 and 2PI with regard to the positive y axis
			mainApplet.ellipse(0,0,leafWidth,leafHeight);
			mainApplet.popMatrix();
		}
		mainApplet.popMatrix();
	}
	
	private void branch(float _strokeLength, int _branchDepth, float _strokeWeight) {
		_strokeLength *= downscaleStrokeLength;
		_branchDepth -= 1;
		if ((_strokeLength > 1) && (_branchDepth > 0)) {
		    // draw branch and go ahead
			mainApplet.pushMatrix();
		    
			if ((branchCount-_branchDepth)%2 == 0) { // alternating use of upper/lower arm
				mainApplet.rotateZ(-curlLeftLowerArm); // rotate clockwise
			} else {
				mainApplet.rotateZ(-curlLeftUpperArm); // rotate clockwise
			}
//			mainApplet.stroke(strokeColor,transparency);
//			mainApplet.strokeWeight(_strokeWeight);
//			mainApplet.line(0,0,0,0,_strokeLength,0);
			mainApplet.beginShape(PConstants.QUAD_STRIP);
			mainApplet.vertex(-_strokeWeight,0f,0f);
			mainApplet.vertex(_strokeWeight,0f,0f);
			mainApplet.vertex(-_strokeWeight,_strokeLength,0f);
			mainApplet.vertex(_strokeWeight,_strokeLength,0f);
			mainApplet.endShape();
			mainApplet.translate(0,_strokeLength,0);
		    branch(_strokeLength,_branchDepth,_strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
	      
		    mainApplet.pushMatrix();

			if ((branchCount-_branchDepth)%2 == 0) { // alternating use of upper/lower arm
				mainApplet.rotateZ(curlRightLowerArm); // rotate clockwise
			} else {
				mainApplet.rotateZ(curlRightUpperArm); // rotate clockwise
			}
//			mainApplet.stroke(strokeColor,transparency);
//			mainApplet.strokeWeight(_strokeWeight);
//			mainApplet.line(0,0,0,0,_strokeLength,0);
			mainApplet.beginShape(PConstants.QUAD_STRIP);
			mainApplet.vertex(-_strokeWeight,0f,0f);
			mainApplet.vertex(_strokeWeight,0f,0f);
			mainApplet.vertex(-_strokeWeight,_strokeLength,0f);
			mainApplet.vertex(_strokeWeight,_strokeLength,0f);
			mainApplet.endShape();
		    mainApplet.translate(0,_strokeLength,0);
		    branch(_strokeLength,_branchDepth,_strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
		} else {
			// store leaf positions to draw them later (for manipulation)
			mainApplet.pushMatrix();
			mainApplet.rotateZ(PConstants.HALF_PI*audioManager.getMeanSampleAt(actSampleIndex+=sampleStepSize));
			PVector leaf = new PVector(mainApplet.modelX(0,0,0),mainApplet.modelY(0,0,0),mainApplet.modelZ(0,0,0)); 
			leafs.add(leaf);
			mainApplet.translate(0,leafHeight,0);
			// get the orientation of the leafs by looking at the bottom and at the top point
			float y = mainApplet.modelY(0,0,0)-leaf.y;
			float x = mainApplet.modelX(0,0,0)-leaf.x;
			float angle = PApplet.atan2(y,x); // gives angle between -PI and PI
			leafsRotationZ.add(angle);
			mainApplet.popMatrix();	
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION;
	}

}
