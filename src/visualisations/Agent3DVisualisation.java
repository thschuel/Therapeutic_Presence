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
			float left = PVector.angleBetween(skeleton.getLeftUpperArm(),skeleton.getOrientationY());
			float right = PVector.angleBetween(skeleton.getRightUpperArm(),skeleton.getOrientationY());
			left -= PConstants.HALF_PI; // shift to -half_pi .. half_pi
			right -= PConstants.HALF_PI;
			left *= 0.8; // scale down
			right *= 0.8; // scale down
			
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
			    agents[i].update1(left,right,width+100,height+100); 
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
