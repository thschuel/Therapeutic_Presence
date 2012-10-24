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

import processing.core.*;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;

public class GenerativeTree2DVisualisation extends AbstractSkeletonAudioVisualisation {

	private int strokeColor = 0;
	
	private float canvasWidth;
	private float canvasHeight;

	// variables to calculate and draw the tree
	private float curlx = 0;
	private float curly = 0;
	private float downscaleStrokeLength = PApplet.sqrt(2)/2.f;
	private float delay = 20;
	private int minBranches = 5;
//	private float growth = 0;
//	private float growthTarget = 0;
//	private int branches = 17;
	
	// colors of leafs
	private int[] leafColors;
	private float actColorIndex = 0.f;
	private int colorsSize = 360;
	private float colorsStepSize = 10.f;
	private int leafWidth = 15;
	private int leafHeight = 30;
	
	// audio responsive tree
	protected float initialScale = 2f;
	protected float downScale = 0.9f;
	protected float transparency = 200;
	protected float sampleStepSize =10f;
	protected float actSampleIndex = 0f;
	
	public GenerativeTree2DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super (_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(true);
		canvasWidth = mainApplet.width;
		canvasHeight = mainApplet.height;
	}
	
	public void setup() {
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		strokeColor = mainApplet.color(250,250,250);
		mainApplet.colorMode(PConstants.HSB,360,100,100,255);
		leafColors = new int[colorsSize];
		for (int i=0; i<colorsSize; i++) {
			leafColors[i] = mainApplet.color(i,100,100);
		}
	}

	public void draw() {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			float scale = skeleton.distanceToKinect()/TherapeuticPresence.maxDistanceToKinect;
			int branchCount = minBranches+PApplet.round(minBranches*scale);
			float initialStrokeLength = scale*canvasHeight/3;
			// draw trunk of the tree
			mainApplet.pushMatrix();
			mainApplet.translate(canvasWidth/2,(scale+2.5f)*canvasHeight/5+initialStrokeLength);
			mainApplet.stroke(strokeColor,transparency);
			float strokeWeight = audioManager.getMeanFFT(0) * initialScale;
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,-initialStrokeLength);
			mainApplet.translate(0,-initialStrokeLength);
			
			float anglelArmToBodyAxis = skeleton.angleBetween(Skeleton.LEFT_SHOULDER,Skeleton.LEFT_HAND,Skeleton.TORSO,Skeleton.NECK); 
			float anglerArmToBodyAxis = skeleton.angleBetween(Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_HAND,Skeleton.TORSO,Skeleton.NECK);
			// TODO this is a hack. find solution to switch on/off mirroring of kinect
			if (!TherapeuticPresence.mirrorKinect) {
				// trees react to body posture with a delay
				curlx += (anglerArmToBodyAxis*0.5-curlx)/delay;
				curly += (anglelArmToBodyAxis*0.5-curly)/delay;		
			} else {
				// trees react to body posture with a delay
				curlx += (anglelArmToBodyAxis*0.5-curlx)/delay;
				curly += (anglerArmToBodyAxis*0.5-curly)/delay;		
			}
			
			// colors of leafs differ in HSB-space
			colorsStepSize = colorsSize/PApplet.pow(2,branchCount); 
			actColorIndex = 0.f;
			// sizes of leafs differ according to music samples
			sampleStepSize = audioManager.getBufferSize()/PApplet.pow(2,branchCount);
			actSampleIndex = 0f;
			// start branching
			branch(initialStrokeLength*downscaleStrokeLength,branchCount,strokeWeight*downScale);
			
			mainApplet.popMatrix();
		}
		
	}
	
	private void branch(float length, int count, float strokeWeight)
	{
		length *= downscaleStrokeLength;
		count -= 1;
		if ((length > 1) && (count > 0)) {
		    // draw branch and go ahead
			mainApplet.pushMatrix();
		    
			mainApplet.rotate(-curlx);
			mainApplet.stroke(strokeColor,transparency);
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,-length);
			mainApplet.translate(0,-length);
		    branch(length,count,strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
	      
		    mainApplet.pushMatrix();
		    
		    mainApplet.rotate(curly);
			mainApplet.stroke(strokeColor,transparency);
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,-length);
		    mainApplet.translate(0,-length);
		    branch(length,count,strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
		} else {
			// draw leafs
			mainApplet.stroke(0,0,0);
			mainApplet.strokeWeight(1);
			mainApplet.fill(leafColors[PApplet.round(actColorIndex+=colorsStepSize)],transparency);
			float scale = audioManager.getMeanSampleAt(PApplet.round(actSampleIndex+=sampleStepSize));
			mainApplet.pushMatrix();
			mainApplet.rotate(PConstants.HALF_PI*scale);
			mainApplet.translate(0,-leafHeight/2);
			mainApplet.ellipse(0,0,leafWidth,leafHeight);
			mainApplet.popMatrix();
			mainApplet.noFill();
		}
		
	}

	public short getVisualisationType() {
		return TherapeuticPresence.GENERATIVE_TREE_2D_VISUALISATION;
	}

}
