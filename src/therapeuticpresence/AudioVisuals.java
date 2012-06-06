package therapeuticpresence;

import processing.core.*;
import ddf.minim.*;

public class AudioVisuals extends SkeletonVisualisation implements AudioListener {

	private float[] leftChannelSamples = null;
	private float[] rightChannelSamples = null;
	
	protected Minim minim;
	protected AudioPlayer audioPlayer;
	
	public AudioVisuals (PApplet _mainApplet, Skeleton _skeleton) {
		super(_mainApplet,_skeleton);
		minim = new Minim(mainApplet);
	}
	
	public void samples(float[] samp) {
		leftChannelSamples = samp;
	}

	public void samples(float[] sampL, float[] sampR) {
		leftChannelSamples = sampL;
		rightChannelSamples = sampR;
	}
	

	public void setup() {
		audioPlayer = minim.loadFile("../data/moan.mp3",1024);
		audioPlayer.loop();
		audioPlayer.addListener(this);
	}
	
	public void draw () {

		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(0,0,0,0,0,1,0,1,0); // set the camera to the position of the kinect, facing towards the scene
		// rotate the scene: kinect data comes upside down!
		mainApplet.pushMatrix();
		mainApplet.rotateX(rotX);
		mainApplet.rotateY(rotY);
		mainApplet.rotateZ(rotZ);
		mainApplet.translate(translateX,translateY,translateZ);
		
		if (skeleton.isUpdated && leftChannelSamples != null && rightChannelSamples != null) {
			
			PVector lHand = skeleton.getJoint(Skeleton.LEFT_HAND);
			PVector lElbow = skeleton.getJoint(Skeleton.LEFT_ELBOW);
			PVector lShoulder = skeleton.getJoint(Skeleton.LEFT_SHOULDER);
			PVector rHand = skeleton.getJoint(Skeleton.RIGHT_HAND);
			PVector rElbow = skeleton.getJoint(Skeleton.RIGHT_ELBOW);
			PVector rShoulder = skeleton.getJoint(Skeleton.RIGHT_SHOULDER);
			
			float radiation = 200f;
			float rotations = 20;

			mainApplet.colorMode(PApplet.HSB,1024,100,100,100);
			for (int i=0; i<leftChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,50);
				float amp = leftChannelSamples[i];
				float r = PApplet.map(i,0,1024,0,PApplet.TWO_PI*rotations);
				float x1 = lHand.x + amp*PApplet.sin(r)*PApplet.cos(PApplet.PI/2)*radiation;
				float y1 = lHand.y + amp*PApplet.sin(r)*PApplet.sin(PApplet.PI/2)*radiation;
				float z1 = lHand.z + amp*PApplet.cos(r)*radiation;
				float x2 = lElbow.x + amp*PApplet.sin(r)*PApplet.cos(PApplet.PI/2)*radiation;
				float y2 = lElbow.y + amp*PApplet.sin(r)*PApplet.sin(PApplet.PI/2)*radiation;
				float z2 = lElbow.z + amp*PApplet.cos(r)*radiation;
				mainApplet.line(x1,y1,z1,x2,y2,z2);
				
				x1 = rHand.x + amp*PApplet.sin(r)*PApplet.cos(PApplet.PI/2)*radiation;
				y1 = rHand.y + amp*PApplet.sin(r)*PApplet.sin(PApplet.PI/2)*radiation;
				z1 = rHand.z + amp*PApplet.cos(r)*radiation;
				x2 = rElbow.x + amp*PApplet.sin(r)*PApplet.cos(PApplet.PI/2)*radiation;
				y2 = rElbow.y + amp*PApplet.sin(r)*PApplet.sin(PApplet.PI/2)*radiation;
				z2 = rElbow.z + amp*PApplet.cos(r)*radiation;
				mainApplet.line(x1,y1,z1,x2,y2,z2);
			}
			/*
			for (int i=0; i<rightChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,50);
				float amp = rightChannelSamples[i];
				mainApplet.line(lHand.x-amp*radiation,lHand.y-amp*radiation,lHand.z-amp*radiation,
						lElbow.x-amp*radiation,lElbow.y-amp*radiation,lElbow.z-amp*radiation);
				mainApplet.line(rHand.x-amp*radiation,rHand.y-amp*radiation,rHand.z-amp*radiation,
						rElbow.x-amp*radiation,rElbow.y-amp*radiation,rElbow.z-amp*radiation);
			}*/
			mainApplet.colorMode(PConstants.RGB,255,255,255,255);
			
		}
		
		mainApplet.popMatrix();
	}
	
	public void stop () {
		audioPlayer.close();
		minim.stop();
	}

}
