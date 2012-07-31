package visualisations;

import java.util.ArrayList;
import processing.core.*;
import scenes.TunnelScene3D;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;
import utils.Ellipsoid3D;

public class Ellipsoidal3DVisualisation extends AbstractSkeletonAudioVisualisation {
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
	protected final float lowerZBoundary = 0.3f*TunnelScene3D.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.85f*TunnelScene3D.tunnelLength;
	
	// the ellipse to draw
	protected ArrayList<Ellipsoid3D> ellipsoids = new ArrayList<Ellipsoid3D>();
	
	// these values are used for drawing the audioresponsive circles
	protected final float delay = 8f;
	protected final int radiation = 30;
	protected final float scaleDC = 6f;
	protected final float scaleAC = 40f;
	
	public Ellipsoidal3DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(false);
	}
	
	public void setup() {
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
		float orientationSkeleton = PVector.angleBetween(new PVector(0,-1,0),skeleton.getOrientationY()) - PConstants.HALF_PI;
		// TODO: this is a hack. find solution for changing mirror kinect on the fly
		if (!TherapeuticPresence.mirrorKinect) {
			orientation += (orientationSkeleton*0.3-orientation)/delay;
		} else {
			orientation += (-orientationSkeleton*0.3-orientation)/delay;
		}
			
	}
	
	public void draw () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			centerZ += (mappedDistance-centerZ)/delay;
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
			centerZ += (mappedDistance-centerZ)/delay;
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
		PVector lHand = skeleton.getJoint(Skeleton.LEFT_HAND);
		PVector rHand = skeleton.getJoint(Skeleton.RIGHT_HAND);
		float mappedLeftX = PApplet.constrain(lHand.x,-width/2,0);
		float mappedLeftY = PApplet.constrain(lHand.y,-height/2,height/2);
		float mappedRightX = PApplet.constrain(rHand.x,0,width/2);
		float mappedRightY = PApplet.constrain(rHand.y,-height/2,height/2);
		leftX += (mappedLeftX-leftX)/delay;
		leftY += (mappedLeftY-leftY)/delay;
		rightX += (mappedRightX-rightX)/delay;
		rightY += (mappedRightY-rightY)/delay;
		
		// use sample data to shift offset
		float sampleValues[] = new float[11];
		for (int i=0; i<11; i++) {
			sampleValues[i] = audioManager.getMeanSampleAt((int)(i*audioManager.getBufferSize()/11))*1.5f;
		}
		
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
				Ellipsoid3D temp = new Ellipsoid3D(center,leftX-offset,leftY-offset,rightX+offset,rightY+offset,orientation,color,strokeWeight);
				ellipsoids.add(temp);
				if (i==0) j=2; // draw dc curve only once
			}
		}
		
		// clean up bezierCurves ArrayList
		for (int i=0;i<ellipsoids.size();i++) {
			if (ellipsoids.get(i).transparency <= 0) {
				ellipsoids.remove(i--);
			}
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.ELLIPSOIDAL_3D_VISUALISATION;
	}

}
