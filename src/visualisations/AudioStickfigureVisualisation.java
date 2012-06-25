package visualisations;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import therapeuticpresence.AudioManager;
import therapeuticpresence.BezierCurve3D;
import therapeuticpresence.Skeleton;
import therapeuticpresence.TherapeuticPresence;

public class AudioStickfigureVisualisation extends SkeletonVisualisation {

	protected AudioManager audioManager;
	
	// these values are used for drawing the bezier curves
	protected float delay = 8f;
	protected int radiation = 40;
	protected float scaleDC = 1f;
	protected float scaleAC = 12f;
	
	protected ArrayList<BezierCurve3D> bezierCurves = new ArrayList<BezierCurve3D>();
	
	public AudioStickfigureVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super(_mainApplet, _skeleton);
		audioManager = _audioManager;
	}
	
	public void setup() {
	}

	public void draw() {
		if (skeleton.isUpdated && audioManager.isUpdated) {
			updateBezierCurves();
			for (int i=0; i<bezierCurves.size(); i++) {
				bezierCurves.get(i).draw(mainApplet);
			}
		}
	}
	
	private void updateBezierCurves () {
		// add BezierCurves to Array. based on the calculated coordinates and the FFT values
		for (int i=0; i<AudioManager.bands; i++) {
			int strokeWeight;
			int color;
			for (int j=-1; j<2; j+=2) {
				if (i==0) strokeWeight = PApplet.round(audioManager.getMeanFFT(0)*scaleDC);
				else if (j==1) strokeWeight = PApplet.round(audioManager.getLeftFFT(i)*scaleAC);
				else strokeWeight = PApplet.round(audioManager.getRightFFT(i)*scaleAC);
				mainApplet.colorMode(PConstants.HSB,AudioManager.bands,255,255,100);
				color = mainApplet.color(i,255,255);
				int offset = PApplet.round(j*i*radiation);
				BezierCurve3D temp = new BezierCurve3D(strokeWeight,color);
				PVector leftHand = skeleton.getJoint(Skeleton.LEFT_HAND);
				leftHand.y+=offset;
				PVector leftElbow = skeleton.getJoint(Skeleton.LEFT_ELBOW);
				leftElbow.y+=offset;
				PVector leftShoulder = skeleton.getJoint(Skeleton.LEFT_SHOULDER);
				leftShoulder.y+=offset;
				PVector rightShoulder = skeleton.getJoint(Skeleton.RIGHT_SHOULDER);
				rightShoulder.y+=offset;
				PVector rightElbow = skeleton.getJoint(Skeleton.RIGHT_ELBOW);
				rightElbow.y+=offset;
				PVector rightHand = skeleton.getJoint(Skeleton.RIGHT_HAND);
				rightHand.y+=offset;
				temp.addAnchorPoint(leftHand);
				temp.addControlPoint(PVector.div(PVector.add(leftHand,leftElbow),2f));
				temp.addAnchorPoint(leftElbow);
				temp.addControlPoint(PVector.div(PVector.add(leftElbow,leftShoulder),2f));
				temp.addAnchorPoint(leftShoulder);
				temp.addControlPoint(PVector.div(PVector.add(leftShoulder,rightShoulder),2f));
				temp.addAnchorPoint(rightShoulder);
				temp.addControlPoint(PVector.div(PVector.add(rightShoulder,rightElbow),2f));
				temp.addAnchorPoint(rightElbow);
				temp.addControlPoint(PVector.div(PVector.add(rightElbow,rightHand),2f));
				temp.addAnchorPoint(rightHand);
				bezierCurves.add(temp);
				if (i==0) j=2; // draw dc curve only once
			}
		}
		// clean up bezierCurves ArrayList
		for (int i=0;i<bezierCurves.size();i++) {
			if (bezierCurves.get(i).transparency <= 0) {
				bezierCurves.remove(i);
			}
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.AUDIO_STICKFIGURE_VISUALISATION;
	}

}
