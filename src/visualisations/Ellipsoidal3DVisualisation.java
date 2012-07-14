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
	protected float centerz;
	protected PVector center = new PVector();
	
	// the ellipse to draw
	protected ArrayList<Ellipsoid3D> ellipsoids = new ArrayList<Ellipsoid3D>();
	
	// these values are used for drawing the audioresponsive circles
	protected final float delay = 8f;
	protected final int radiation = 30;
	protected final float scaleDC = 1f;
	protected final float scaleAC = 5f;
	protected final float strokeWeight = 1.7f;
	
	public Ellipsoidal3DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(false);
	}
	
	public void setup() {
		centerz = PApplet.constrain(skeleton.distanceToKinect()/TherapeuticPresence.maxDistanceToKinect*TunnelScene3D.tunnelLength,0,TunnelScene3D.tunnelLength);
		width = TunnelScene3D.getTunnelWidthAt(centerz);
		height = TunnelScene3D.getTunnelHeightAt(centerz);
		// coordinates based on canvas size
		updateCanvasCoordinates();
	}
	
	public void updateCanvasCoordinates () {
		// center.z reacts to position of user with delay
		centerz += (skeleton.distanceToKinect()/TherapeuticPresence.maxDistanceToKinect*TunnelScene3D.tunnelLength-centerz)/delay;
		centerz = PApplet.constrain(centerz,0,TunnelScene3D.tunnelLength);
	    center.set(0,0,centerz);
		width = TunnelScene3D.getTunnelWidthAt(centerz);
		height = TunnelScene3D.getTunnelHeightAt(centerz);
	}
	
	public void draw () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			updateCanvasCoordinates();
			updateEllipsoids();
			for (int i=0; i<ellipsoids.size(); i++) {
				ellipsoids.get(i).draw(mainApplet);
			}
		}
	}
	
	public boolean fadeIn () {
		return true;
	}
	
	// Fade out method is used to blend between visualisations. it should be over in 20 frames!!!
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
		float sizeX = (PVector.sub(skeleton.getJoint(Skeleton.LEFT_ELBOW),skeleton.getJoint(Skeleton.RIGHT_ELBOW))).mag();
		float sizeY = (PVector.sub(skeleton.getJoint(Skeleton.LEFT_HAND),skeleton.getJoint(Skeleton.TORSO))).mag();
		
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
				Ellipsoid3D temp = new Ellipsoid3D(sizeX+offset,sizeY+offset,color,strokeWeight);
				temp.center.x = 0;
				temp.center.y = 0;
				temp.center.z = center.z;
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
