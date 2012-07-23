package visualisations;

import generativedesign.*;

import java.util.ArrayList;
import processing.core.*;
import scenes.TunnelScene3D;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;

public class Mesh3DVisualisation extends AbstractSkeletonAudioVisualisation {
	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width, height;
	protected float centerX=0;
	protected float centerY=0;
	protected float centerZ;
	protected float leftX=0;
	protected float leftY=0;
	protected float rightX=0;
	protected float rightY=0;
	protected float fadeInCenterZ=0;
	protected PVector center = new PVector();
	private float orientation = 0;
	protected final float lowerZBoundary = 0.3f*TunnelScene3D.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.85f*TunnelScene3D.tunnelLength;
	
	// the ellipse to draw
	protected Mesh mesh;
	
	// these values are used for drawing the audioresponsive circles
	protected final float delay = 8f;
	protected final int radiation = 30;
	protected final float scaleDC = 2f;
	protected final float scaleAC = 13f;
	protected final float strokeWeight = 1.7f;
	
	public Mesh3DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(false);
	}
	
	public void setup() {
	}
	
	public void updateCanvasCoordinates () {
		width = TunnelScene3D.getTunnelWidthAt(centerZ);
		height = TunnelScene3D.getTunnelHeightAt(centerZ);
		PVector torso = skeleton.getJoint(Skeleton.TORSO);
		float mappedTorsoX = PApplet.constrain(torso.x,-width/2,width/2);
		float mappedTorsoY = PApplet.constrain(torso.y,-height/2,height/2);
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

			mainApplet.lightSpecular(230, 230, 230); 
			mainApplet.directionalLight(200f, 200f, 200f, 0.5f, 0.5f, -1f); 
			mainApplet.specular(mainApplet.color(200)); 
			mainApplet.shininess(5.0f);
			
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			centerZ += (mappedDistance-centerZ)/delay;
			updateCanvasCoordinates();
			updateMesh();
			// rotate tree according to body rotation
			mainApplet.pushStyle();
			mainApplet.pushMatrix();
			mainApplet.translate(center.x,center.y,center.z);
			mainApplet.rotateY(orientation);
			mainApplet.scale(100);
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

		mesh = new Mesh(mainApplet,Mesh.FIGURE8TORUS,20,20,0,2f*left,0,2f*right);
		mesh.setColorRange(100, 192, 50, 80, 30, 50, 100);
		
		/*
		// add BezierCurves to Array. based on the calculated coordinates and the FFT values
		for (int i=0; i<AudioManager.bands; i++) {
			float strokeWeight;
			int color;
			for (int j=-1; j<2; j+=2) {
				if (i==0) strokeWeight = audioManager.getMeanFFT(0)*scaleDC;
				else if (j==1) strokeWeight = audioManager.getLeftFFT(i)*scaleAC;
				else strokeWeight = audioManager.getRightFFT(i)*scaleAC;
				mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,Ellipsoid3D.MAX_TRANSPARENCY);
				color = mainApplet.color(i,255,255,255);
				int offset = PApplet.round(j*i*radiation);
				Mesh3D temp = new Mesh3D(mainApplet,center,left,right,orientation);
				meshes.add(temp);
				if (i==0) j=2; // draw dc curve only once
			}
		}*/
	}

	public short getVisualisationType() {
		return TherapeuticPresence.ELLIPSOIDAL_3D_VISUALISATION;
	}

}
