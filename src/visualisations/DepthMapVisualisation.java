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

import SimpleOpenNI.SimpleOpenNI;
import processing.core.*;
import therapeuticpresence.TherapeuticPresence;

public class DepthMapVisualisation extends AbstractVisualisation {

	protected SimpleOpenNI kinect;
	
	public DepthMapVisualisation (TherapeuticPresence _mainApplet, SimpleOpenNI _kinect) {
		super(_mainApplet);
		mainApplet.setMirrorKinect(true);
		kinect = _kinect;
	}

	public void setup() {
	}

	public void draw() {
		
		int[] depthMap = kinect.depthMap();
		PVector[] realWorldDepthMap = kinect.depthMapRealWorld();
		int h = kinect.depthHeight();
		int w = kinect.depthWidth();
		int stepSize = 3;  // to speed up the drawing, draw every third point
		int index;
		
		mainApplet.colorMode(PConstants.RGB,255,255,255,100);
		for(int y=0; y<h; y+=stepSize) {
			for(int x=0; x<w; x+=stepSize) {
				index=x+y*w;
				if(depthMap[index] > 0) { 
					PVector testCoM = new PVector (realWorldDepthMap[index].x,0f,realWorldDepthMap[index].z);
					if (PVector.dist(TherapeuticPresence.centerOfSkeletonDetectionSpace, testCoM) < TherapeuticPresence.radiusOfSkeletonDetectionSpace) {
						mainApplet.stroke(255,255,255,100); 
					} else if (PVector.dist(new PVector(0,0,0),realWorldDepthMap[index]) > TherapeuticPresence.maxDistanceToKinect) {
						mainApplet.stroke(20,255,20,40);
					} else {
						mainApplet.stroke(255,255,255,40);
					}
					mainApplet.point(realWorldDepthMap[index].x,realWorldDepthMap[index].y,realWorldDepthMap[index].z);
				}
			} 
		}
		// draw skeleton detection space
		mainApplet.stroke(200,10,10,30);
		mainApplet.noFill();
		mainApplet.strokeWeight(6f);
		float tubeHeight = 800f;
		PVector center = TherapeuticPresence.centerOfSkeletonDetectionSpace;
		float radius = TherapeuticPresence.radiusOfSkeletonDetectionSpace;
		mainApplet.pushMatrix();
		mainApplet.translate(center.x,center.y,center.z);
		mainApplet.rotateY(PConstants.PI);
		mainApplet.pushMatrix();
		mainApplet.translate(0,tubeHeight,0);
		mainApplet.rotateX(PConstants.HALF_PI);
		mainApplet.ellipse(0,0,2*radius,2*radius);
		mainApplet.popMatrix();
		mainApplet.pushMatrix();
		mainApplet.translate(0,-tubeHeight,0);
		mainApplet.rotateX(PConstants.HALF_PI);
		mainApplet.ellipse(0,0,2*radius,2*radius);
		mainApplet.popMatrix();
		mainApplet.line(-radius,tubeHeight,-radius,-tubeHeight);
		mainApplet.line(radius,tubeHeight,radius,-tubeHeight);
		mainApplet.popMatrix();
	}

	public short getVisualisationType() {
		return TherapeuticPresence.DEPTHMAP_VISUALISATION;
	}
}
