package therapeuticpresence;

import processing.core.*;
import ddf.minim.*;

public class AudioVisualisation extends SkeletonVisualisation implements AudioListener {

	private float[] leftChannelSamples = null;
	private float[] rightChannelSamples = null;
	
	protected Minim minim;
	protected AudioPlayer audioPlayer;
	
	public AudioVisualisation (PApplet _mainApplet, Skeleton _skeleton) {
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
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // set the camera to the position of the kinect, facing towards the scene
	}
	
	public void draw () {

		
		if (skeleton.isUpdated && leftChannelSamples != null && rightChannelSamples != null) {
			
			float angleLeftUpperArm = skeleton.angleToUpAxis(Skeleton.LEFT_SHOULDER,Skeleton.LEFT_ELBOW);
			float angleRightUpperArm = skeleton.angleToUpAxis(Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_ELBOW);
			float angleLeftLowerArm = skeleton.angleBetween(Skeleton.LEFT_HAND,Skeleton.LEFT_ELBOW,Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER);
			float angleRightLowerArm = skeleton.angleBetween(Skeleton.RIGHT_HAND,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
			
			float radiation = 200f;
			float lineLength = 250;
			
			mainApplet.colorMode(PApplet.HSB,1024,100,100,100);
			mainApplet.pushMatrix();
			mainApplet.translate(mainApplet.width/2,mainApplet.height/2);
			mainApplet.rotate(angleLeftUpperArm);
			for (int i=0; i<leftChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,50);
				float amp = leftChannelSamples[i];
				mainApplet.line(amp*radiation,0,amp*radiation,lineLength);
			}
			for (int i=0; i<rightChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,50);
				float amp = rightChannelSamples[i];
				mainApplet.line(-amp*radiation,0,-amp*radiation,lineLength);
			}
			mainApplet.translate(0,lineLength);
			mainApplet.rotate(angleLeftLowerArm);
			for (int i=0; i<leftChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,50);
				float amp = leftChannelSamples[i];
				mainApplet.line(amp*radiation,0,amp*radiation,lineLength);
			}
			for (int i=0; i<rightChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,50);
				float amp = rightChannelSamples[i];
				mainApplet.line(-amp*radiation,0,-amp*radiation,lineLength);
			}
			mainApplet.popMatrix();
			
			mainApplet.pushMatrix();
			mainApplet.translate(mainApplet.width/2,mainApplet.height/2);
			mainApplet.rotate(-angleRightUpperArm);
			for (int i=0; i<leftChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,10);
				float amp = leftChannelSamples[i];
				mainApplet.line(amp*radiation,0,amp*radiation,lineLength);
			}
			for (int i=0; i<rightChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,10);
				float amp = rightChannelSamples[i];
				mainApplet.line(-amp*radiation,0,-amp*radiation,lineLength);
			}
			mainApplet.translate(0,lineLength);
			mainApplet.rotate(-angleRightLowerArm);
			for (int i=0; i<leftChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,10);
				float amp = leftChannelSamples[i];
				mainApplet.line(amp*radiation,0,amp*radiation,lineLength);
			}
			for (int i=0; i<rightChannelSamples.length; i++) {
				mainApplet.stroke(i,70,70,10);
				float amp = rightChannelSamples[i];
				mainApplet.line(-amp*radiation,0,-amp*radiation,lineLength);
			}
			mainApplet.popMatrix();
			mainApplet.colorMode(PConstants.RGB,255,255,255,255);
			

		   // if(mainApplet.frameCount % 2 == 0 ) {
		    	mainApplet.image(mainApplet.get(),-5,-5,mainApplet.width+8,mainApplet.height+8);
		    //}

//			float r = PApplet.map(i,0,1024,0,PApplet.TWO_PI*rotations);
//			float x1 = lHand.x + amp*PApplet.sin(r)*PApplet.cos(PApplet.PI/2)*radiation;
//			float y1 = lHand.y + amp*PApplet.sin(r)*PApplet.sin(PApplet.PI/2)*radiation;
//			float z1 = lHand.z + amp*PApplet.cos(r)*radiation;
//			float x2 = lElbow.x + amp*PApplet.sin(r)*PApplet.cos(PApplet.PI/2)*radiation;
//			float y2 = lElbow.y + amp*PApplet.sin(r)*PApplet.sin(PApplet.PI/2)*radiation;
//			float z2 = lElbow.z + amp*PApplet.cos(r)*radiation;
//			for (int i=0; i<rightChannelSamples.length; i++) {
//				mainApplet.stroke(i,70,70,50);
//				float amp = rightChannelSamples[i];
//				mainApplet.line(lHand.x-amp*radiation,lHand.y-amp*radiation,lHand.z-amp*radiation,
//						lElbow.x-amp*radiation,lElbow.y-amp*radiation,lElbow.z-amp*radiation);
//				mainApplet.line(rHand.x-amp*radiation,rHand.y-amp*radiation,rHand.z-amp*radiation,
//						rElbow.x-amp*radiation,rElbow.y-amp*radiation,rElbow.z-amp*radiation);
//			}
			
		}
	}
	
	public void stop () {
		audioPlayer.close();
		minim.stop();
	}

}
