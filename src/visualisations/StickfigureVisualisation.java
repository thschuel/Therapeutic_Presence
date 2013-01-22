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
import therapeuticskeleton.Skeleton;
import therapeuticpresence.TherapeuticPresence;

public class StickfigureVisualisation extends AbstractSkeletonVisualisation {

	private int strokeColor = 0;
	private int jointColor = 0;
	private int sagittalPlaneColor = 0;
	private int frontalPlaneColor = 0;
	private int transversalPlaneColor = 0;
	private int pixelColor=0;
	private float transparency=0;
	private float lengthJointOrientations = 100f;
	private float radiusJoints = 10f;
	private float scale = 1f;
	private float strokeWeight = 2f;
	private SimpleOpenNI kinect;
	
	public StickfigureVisualisation (TherapeuticPresence _mainApplet, SimpleOpenNI _kinect, Skeleton _skeleton) {
		super(_mainApplet, _skeleton);
		kinect=_kinect;
		mainApplet.colorMode(PConstants.HSB,360,100,100,100);
		pixelColor=mainApplet.color(mainApplet.random(360),100,100,100);
	}
	
	public StickfigureVisualisation (TherapeuticPresence _mainApplet, SimpleOpenNI _kinect, Skeleton _skeleton, int _pixelColor) {
		super(_mainApplet, _skeleton);
		kinect=_kinect;
		pixelColor=_pixelColor;
	}
	
	public void setup() {
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		strokeColor = mainApplet.color(0,255,255);
		jointColor = mainApplet.color(0,0,255);
		sagittalPlaneColor = mainApplet.color(0,255,255);
		frontalPlaneColor = mainApplet.color(0,0,255);
		transversalPlaneColor = mainApplet.color(255,0,255);
	}

	public boolean fadeIn () {
		if (transparency >= 255) {
			return true;
		} else {
			transparency+=10f;
			drawUserPixels();
			drawStickfigure(false);
			return false;
		}
	}
	
	public boolean fadeOut() {
		transparency-=0.5f;
		if (strokeWeight<15f) strokeWeight+=0.05f;
		if (transparency<=0) return true;
		drawStickfigure(true);
		return false;
	}
	
	public void draw() {
		drawUserPixels();
		drawStickfigure(false);
		drawJoints(false);
		//drawBodyPlanes();
		drawLocalCoordinateSystem();
	}
	
	private void drawUserPixels () {
		int[] userPixels=kinect.getUsersPixels(SimpleOpenNI.USERS_ALL);
		if(userPixels != null) {
			PVector[] realWorldDepthMap = kinect.depthMapRealWorld();
			int h = kinect.depthHeight();
			int w = kinect.depthWidth();
			int stepSize = 3;  // to speed up the drawing, draw every third point
			int index;
			
			mainApplet.stroke(pixelColor); 
			for(int y=0; y<h; y+=stepSize) {
				for(int x=0; x<w; x+=stepSize) {
					index=x+y*w;
					if(userPixels[index] == skeleton.getUserId()) { 
						// draw the projected point
						mainApplet.point(realWorldDepthMap[index].x,realWorldDepthMap[index].y,realWorldDepthMap[index].z);
					}
				} 
			}
		}
	}
	
	private void drawStickfigure (boolean drawActiveLimbsOnly) {
		
		if (!drawActiveLimbsOnly) {
			drawLineBetweenJoints(Skeleton.HEAD,Skeleton.NECK);
			drawLineBetweenJoints(Skeleton.NECK,Skeleton.LEFT_SHOULDER);
			drawLineBetweenJoints(Skeleton.NECK,Skeleton.RIGHT_SHOULDER);
			drawLineBetweenJoints(Skeleton.LEFT_SHOULDER,Skeleton.TORSO);
			drawLineBetweenJoints(Skeleton.RIGHT_SHOULDER,Skeleton.TORSO);
		}

		drawLineBetweenJoints(Skeleton.LEFT_SHOULDER,Skeleton.LEFT_ELBOW);
		drawLineBetweenJoints(Skeleton.LEFT_ELBOW,Skeleton.LEFT_HAND);
		drawLineBetweenJoints(Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_ELBOW);
		drawLineBetweenJoints(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_HAND);

		if (TherapeuticPresence.fullBodyTracking) {
			drawLineBetweenJoints(Skeleton.TORSO,Skeleton.LEFT_HIP);
			drawLineBetweenJoints(Skeleton.LEFT_HIP,Skeleton.LEFT_KNEE);
			drawLineBetweenJoints(Skeleton.LEFT_KNEE,Skeleton.LEFT_FOOT);
			drawLineBetweenJoints(Skeleton.TORSO,Skeleton.RIGHT_HIP);
			drawLineBetweenJoints(Skeleton.RIGHT_HIP,Skeleton.RIGHT_KNEE);
			drawLineBetweenJoints(Skeleton.RIGHT_KNEE,Skeleton.RIGHT_FOOT);
		}
		
	}
	
	private void drawLineBetweenJoints (short jointType1, short jointType2) {
		PVector joint1 = skeleton.getJoint(jointType1);
		PVector joint2 = skeleton.getJoint(jointType2);
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.stroke(strokeColor,transparency);
		mainApplet.strokeWeight(strokeWeight);
		mainApplet.line(joint1.x,joint1.y,joint1.z,joint2.x,joint2.y,joint2.z);
	}
	
	private void drawJoints (boolean drawOrientations) {
		short count;
		if (TherapeuticPresence.fullBodyTracking) count = 15;
		else count = 9;
		
		for (short i=0; i<count; i++) {
			PVector joint = skeleton.getJoint(i);
			float confidence = skeleton.getJointConfidence(i);
			float distance = skeleton.getJointDelta(i)/20;
			mainApplet.pushMatrix();
			mainApplet.translate(joint.x,joint.y,joint.z);
			mainApplet.noStroke();
			mainApplet.colorMode(PConstants.RGB,255,255,255,255);
			mainApplet.fill(jointColor,55+confidence*200);
			mainApplet.sphere(radiusJoints+radiusJoints*distance);
			
			if (drawOrientations) {
				PMatrix3D orientation = skeleton.getJointOrientation(i);
				// set the local coordsys
				mainApplet.applyMatrix(orientation);
				mainApplet.colorMode(PConstants.RGB,255,255,255,255);
			    // x - r
				mainApplet.stroke(255,0,0,55+confidence*200);
				mainApplet.line(0,0,0,lengthJointOrientations,0,0);
			    // y - g
				mainApplet.stroke(0,255,0,55+confidence*200);
				mainApplet.line(0,0,0,0,lengthJointOrientations,0);
			    // z - b    
				mainApplet.stroke(255,255,0,55+confidence*200);
				mainApplet.line(0,0,0,0,0,lengthJointOrientations);
			}
			
			mainApplet.popMatrix();
		}
	}

	private void drawBodyPlanes () {
		// for debug: show body planes (sagittal plane is mirror plane)
		// draw plane by finding 4 points in that plane
		PVector n0 = skeleton.getN0VectorSagittalPlane();
		PVector r = skeleton.getRVectorSagittalPlane();
		PVector u = n0.cross(new PVector(0,0,1)); // cross product of n0 with arbitrary vector -> u lies on the plane
		PVector v = n0.cross(u); // v is orthogonal to both N and u (again is in the plane) 
		u.normalize();
		v.normalize();
		// draw a quad centered in the torso point (which is always part of the planes -> r)
		PVector P0 = r;
		float sizePlane = 300.0f;
		PVector fu = PVector.mult(u,sizePlane);
		PVector fv = PVector.mult(v,sizePlane);
		PVector P1 = PVector.sub(P0,fu); P1.sub(fv);
		PVector P2 = PVector.add(P0,fu); P2.sub(fv);
		PVector P3 = PVector.add(P0,fu); P3.add(fv);
		PVector P4 = PVector.sub(P0,fu); P4.add(fv);
		// draw vertices
		mainApplet.pushMatrix();
		mainApplet.noStroke();
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.fill(sagittalPlaneColor,200);
		mainApplet.beginShape(PApplet.QUADS);
			mainApplet.vertex(P1.x,P1.y,P1.z);
			mainApplet.vertex(P2.x,P2.y,P2.z);
			mainApplet.vertex(P3.x,P3.y,P3.z);
			mainApplet.vertex(P4.x,P4.y,P4.z);
		mainApplet.endShape();
		mainApplet.popMatrix();
		
		// draw frontal plane by finding 4 points in that plane
		n0 = skeleton.getN0VectorFrontalPlane();
		r = skeleton.getRVectorFrontalPlane();
		u = n0.cross(new PVector(0,0,1)); // cross product of n0 with arbitrary vector -> u lies on the plane
		v = n0.cross(u); // v is orthogonal to both N and u (again is in the plane) 
		u.normalize();
		v.normalize();
		// draw a quad centered in the torso point (which is always part of the planes -> r)
		P0 = r;
		sizePlane = 300.0f;
		fu = PVector.mult(u,sizePlane);
		fv = PVector.mult(v,sizePlane);
		P1 = PVector.sub(P0,fu); P1.sub(fv);
		P2 = PVector.add(P0,fu); P2.sub(fv);
		P3 = PVector.add(P0,fu); P3.add(fv);
		P4 = PVector.sub(P0,fu); P4.add(fv);
		// draw vertices
		mainApplet.pushMatrix();
		mainApplet.noStroke();
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.fill(frontalPlaneColor,200);
		mainApplet.beginShape(PApplet.QUADS);
			mainApplet.vertex(P1.x,P1.y,P1.z);
			mainApplet.vertex(P2.x,P2.y,P2.z);
			mainApplet.vertex(P3.x,P3.y,P3.z);
			mainApplet.vertex(P4.x,P4.y,P4.z);
		mainApplet.endShape();
		mainApplet.popMatrix();
		
		// draw transversal plane by finding 4 points in that plane
		n0 = skeleton.getN0VectorTransversalPlane();
		r = skeleton.getRVectorTransversalPlane();
		u = n0.cross(new PVector(0,0,1)); // cross product of n0 with arbitrary vector -> u lies on the plane
		v = n0.cross(u); // v is orthogonal to both N and u (again is in the plane) 
		u.normalize();
		v.normalize();
		// draw a quad centered in the torso point (which is always part of the planes -> r)
		P0 = r;
		sizePlane = 300.0f;
		fu = PVector.mult(u,sizePlane);
		fv = PVector.mult(v,sizePlane);
		P1 = PVector.sub(P0,fu); P1.sub(fv);
		P2 = PVector.add(P0,fu); P2.sub(fv);
		P3 = PVector.add(P0,fu); P3.add(fv);
		P4 = PVector.sub(P0,fu); P4.add(fv);
		// draw vertices
		mainApplet.pushMatrix();
		mainApplet.noStroke();
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.fill(transversalPlaneColor,200);
		mainApplet.beginShape(PApplet.QUADS);
			mainApplet.vertex(P1.x,P1.y,P1.z);
			mainApplet.vertex(P2.x,P2.y,P2.z);
			mainApplet.vertex(P3.x,P3.y,P3.z);
			mainApplet.vertex(P4.x,P4.y,P4.z);
		mainApplet.endShape();
		mainApplet.popMatrix();
	}
	
	private void drawLocalCoordinateSystem () {
		PVector origin = skeleton.getOrigin();
		PVector xVector = new PVector();
		xVector.set(skeleton.getOrientationX());
		PVector yVector = new PVector();
		yVector.set(skeleton.getOrientationY());
		PVector zVector = new PVector();
		zVector.set(skeleton.getOrientationZ());
		PVector sagittalN0Vector = new PVector();
		sagittalN0Vector.set(skeleton.getN0VectorSagittalPlane());
		PVector frontalN0Vector = new PVector();
		frontalN0Vector.set(skeleton.getN0VectorFrontalPlane());
		PVector transversalN0Vector = new PVector();
		transversalN0Vector.set(skeleton.getN0VectorTransversalPlane());
		xVector.mult(1000);
		yVector.mult(1000);
		zVector.mult(1000);
		sagittalN0Vector.mult(500);
		frontalN0Vector.mult(500);
		transversalN0Vector.mult(500);
		mainApplet.pushMatrix();
		mainApplet.translate(origin.x,origin.y,origin.z);
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.strokeWeight(4);
		mainApplet.stroke(255,0,0,180);
		mainApplet.line(0,0,0,xVector.x,xVector.y,xVector.z);
		mainApplet.stroke(0,255,0,180);
		mainApplet.line(0,0,0,yVector.x,yVector.y,yVector.z);
		mainApplet.stroke(0,0,255,180);
		mainApplet.line(0,0,0,zVector.x,zVector.y,zVector.z);
		mainApplet.stroke(255,0,0,255);
		mainApplet.line(0,0,0,sagittalN0Vector.x,sagittalN0Vector.y,sagittalN0Vector.z);
		mainApplet.stroke(0,255,0,255);
		mainApplet.line(0,0,0,transversalN0Vector.x,transversalN0Vector.y,transversalN0Vector.z);
		mainApplet.stroke(0,0,255,255);
		mainApplet.line(0,0,0,frontalN0Vector.x,frontalN0Vector.y,frontalN0Vector.z);
		mainApplet.popMatrix();
	}

	public short getVisualisationType() {
		return TherapeuticPresence.STICKFIGURE_VISUALISATION;
	}

}
