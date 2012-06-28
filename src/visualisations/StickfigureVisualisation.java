package visualisations;

import processing.core.*;
import therapeuticpresence.Skeleton;
import therapeuticpresence.TherapeuticPresence;

public class StickfigureVisualisation extends SkeletonVisualisation {

	private int strokeColor = 0;
	private int jointColor = 0;
	private int mirrorPlaneColor = 0;
	private float lengthJointOrientations = 0f;
	private float radiusJoints = 0f;
	
	public StickfigureVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton) {
		super(_mainApplet, _skeleton);
		mainApplet.setMirrorKinect(true);
	}
	
	public void setup() {
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		strokeColor = mainApplet.color(0,255,255);
		jointColor = mainApplet.color(0,0,255);
		mirrorPlaneColor = mainApplet.color(100,100,100);
		lengthJointOrientations = 100f;
		radiusJoints = 20f;
	}

	public void draw() {
		drawStickfigure();
		drawJoints(true);
		drawMirrorPlane();
	}
	
	private void drawStickfigure () {
		
		drawLineBetweenJoints(Skeleton.HEAD,Skeleton.NECK);
		drawLineBetweenJoints(Skeleton.NECK,Skeleton.LEFT_SHOULDER);
		drawLineBetweenJoints(Skeleton.NECK,Skeleton.RIGHT_SHOULDER);
		drawLineBetweenJoints(Skeleton.LEFT_SHOULDER,Skeleton.TORSO);
		drawLineBetweenJoints(Skeleton.RIGHT_SHOULDER,Skeleton.TORSO);

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
		float meanConfidence = (skeleton.getConfidenceJoint(jointType1)+skeleton.getConfidenceJoint(jointType2)) / 2.0f;
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.stroke(strokeColor,55+meanConfidence*200);
		mainApplet.line(joint1.x,joint1.y,joint1.z,joint2.x,joint2.y,joint2.z);
	}
	
	private void drawJoints (boolean drawOrientations) {
		short count;
		if (TherapeuticPresence.fullBodyTracking) count = 15;
		else count = 9;
		
		for (short i=0; i<count; i++) {
			PVector joint = skeleton.getJoint(i);
			float confidence = skeleton.getConfidenceJoint(i);
			mainApplet.pushMatrix();
			mainApplet.translate(joint.x,joint.y,joint.z);
			mainApplet.noStroke();
			mainApplet.colorMode(PConstants.RGB,255,255,255,255);
			mainApplet.fill(jointColor,55+confidence*200);
			mainApplet.sphere(radiusJoints);
			
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

	private void drawMirrorPlane () {
		// for debug: show mirror plane
		// draw plane by finding 4 points in that plane
		if (TherapeuticPresence.mirrorTherapy != TherapeuticPresence.MIRROR_OFF) {
			PVector n0MP = skeleton.getN0VectorMirrorPlane();
			PVector rMP = skeleton.getRVectorMirrorPlane();
			PVector u = n0MP.cross(new PVector(0,0,1)); // cross product of n0 with arbitrary vector -> u lies on the plane
			PVector v = n0MP.cross(u); // v is orthogonal to both N and u (again is in the plane) 
			u.normalize();
			v.normalize();
			// draw a quad centered in the neckPoint (which is always part of the mirror plane)
			PVector P0 = rMP;
			float sizePlane = 300.0f;
			PVector fu = PVector.mult(u,sizePlane);
			PVector fv = PVector.mult(v,sizePlane);
			PVector P1 = PVector.sub(P0,fu); P1.sub(fv);
			PVector P2 = PVector.add(P0,fu); P2.sub(fv);
			PVector P3 = PVector.add(P0,fu); P3.add(fv);
			PVector P4 = PVector.sub(P0,fu); P4.add(fv);
			
			// draw your vertices
			mainApplet.pushMatrix();
			mainApplet.noStroke();
			mainApplet.colorMode(PConstants.RGB,255,255,255,255);
			mainApplet.fill(mirrorPlaneColor,40);
			mainApplet.beginShape(PApplet.QUADS);
				mainApplet.vertex(P1.x,P1.y,P1.z);
				mainApplet.vertex(P2.x,P2.y,P2.z);
				mainApplet.vertex(P3.x,P3.y,P3.z);
				mainApplet.vertex(P4.x,P4.y,P4.z);
			mainApplet.endShape();
			mainApplet.popMatrix();
		}
				
	}

	public short getVisualisationType() {
		return TherapeuticPresence.STICKFIGURE_VISUALISATION;
	}

}
