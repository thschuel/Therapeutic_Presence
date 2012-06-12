package therapeuticpresence;

import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.FFT;

public class AudioVisualisation extends SkeletonVisualisation implements AudioListener {

	private float[] leftChannelSamples = null;
	private float[] rightChannelSamples = null;
	
	protected Minim minim;
	protected AudioPlayer audioPlayer;
	
	protected boolean calcFFT = false; 
	protected int bands = 8;
	protected FFT fft; 
	protected float maxFFT;
	protected float[] leftFFT = null;
	protected float[] rightFFT = null;
	  
	
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
		float gain = .125f;
	    fft = new FFT(audioPlayer.bufferSize(), audioPlayer.sampleRate());
	    maxFFT =  audioPlayer.sampleRate() / audioPlayer.bufferSize() * gain;
	    fft.window(FFT.HAMMING);
		leftFFT = new float[bands];
		rightFFT = new float[bands];
	    fft.linAverages(bands);
	}
	
	public void draw () {
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
		
		//mainApplet.image(visual, 0, 0);
		if (skeleton.isUpdated && leftChannelSamples != null && rightChannelSamples != null) {
			
			if (calcFFT) {
			    fft.forward(leftChannelSamples);
			    for(int i = 0; i < bands; i++) leftFFT[i] = fft.getAvg(i);
			    fft.forward(rightChannelSamples);
			    for(int i = 0; i < bands; i++) rightFFT[i] = fft.getAvg(i);
			}
			
			float angleLeftUpperArm = skeleton.angleToYAxis(Skeleton.LEFT_SHOULDER,Skeleton.LEFT_ELBOW);
			float angleRightUpperArm = skeleton.angleToYAxis(Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_ELBOW);
			float angleLeftLowerArm = skeleton.angleBetween(Skeleton.LEFT_HAND,Skeleton.LEFT_ELBOW,Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER);
			float angleRightLowerArm = skeleton.angleBetween(Skeleton.RIGHT_HAND,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
			
			// shift angles of upper arms by 90 degree to use polar coordinates
			angleLeftUpperArm = (angleLeftUpperArm+PApplet.PI/2)%PApplet.PI;
			angleRightUpperArm = (angleRightUpperArm+PApplet.PI/2)%PApplet.PI;
			// shift angles of lower arms by angles of upper arms to use polar coordinates
			angleLeftLowerArm = (angleLeftLowerArm+angleLeftUpperArm)%PApplet.PI;
			angleRightLowerArm = (angleRightLowerArm+angleRightUpperArm)%PApplet.PI;
			
			int centerX = mainApplet.width/2;
			int centerY = mainApplet.height/2;
			
			int leftX = 0;
			int leftQuarterX = mainApplet.width/4;
			int rightQuarterX = 3*mainApplet.width/4;
			int rightX = mainApplet.width;	
			
			int leftQuarterY = centerY+(int)((leftQuarterX-centerX)*PApplet.sin(angleLeftUpperArm)/PApplet.cos(angleLeftUpperArm));
			int rightQuarterY = centerY-(int)((rightQuarterX-centerX)*PApplet.sin(angleRightUpperArm)/PApplet.cos(angleRightUpperArm));
			int leftY = leftQuarterY+(int)((leftX-leftQuarterX)*PApplet.sin(angleLeftLowerArm)/PApplet.cos(angleLeftLowerArm));
			int rightY = rightQuarterY-(int)((rightX-rightQuarterX)*PApplet.sin(angleRightLowerArm)/PApplet.cos(angleRightLowerArm));
			
			float radiation = 200f;
			float lineLength = 250f;
			int stepSizeSamples = 5;
			
			mainApplet.colorMode(PApplet.RGB,leftChannelSamples.length/stepSizeSamples,255,255,255);
			mainApplet.stroke(255,0,255);
			mainApplet.strokeWeight(3);
			mainApplet.line(centerX,centerY,leftQuarterX,leftQuarterY);
			mainApplet.line(centerX,centerY,rightQuarterX,rightQuarterY);
			mainApplet.line(leftQuarterX,leftQuarterY,leftX,leftY);
			mainApplet.line(rightQuarterX,rightQuarterY,rightX,rightY);
//			for (int i=0; i<rightChannelSamples.length; i+=stepSizeSamples) {
//				mainApplet.stroke(i,70,70,50);
//				float amp = rightChannelSamples[i];
//				mainApplet.line(-amp*radiation,0,-amp*radiation,lineLength);
//			}
//			mainApplet.translate(0,lineLength);
//			mainApplet.rotate(angleLeftLowerArm);
//			for (int i=0; i<leftChannelSamples.length; i+=stepSizeSamples) {
//				mainApplet.stroke(i,70,70,50);
//				float amp = leftChannelSamples[i];
//				mainApplet.line(amp*radiation,0,amp*radiation,lineLength);
//			}
//			for (int i=0; i<rightChannelSamples.length; i+=stepSizeSamples) {
//				mainApplet.stroke(i,70,70,50);
//				float amp = rightChannelSamples[i];
//				mainApplet.line(-amp*radiation,0,-amp*radiation,lineLength);
//			}
//			mainApplet.popMatrix();
//			
//			mainApplet.pushMatrix();
//			mainApplet.translate(mainApplet.width/2,mainApplet.height/2);
//			mainApplet.rotate(-angleRightUpperArm);
//			for (int i=0; i<leftChannelSamples.length; i+=stepSizeSamples) {
//				mainApplet.stroke(i,70,70,10);
//				float amp = leftChannelSamples[i];
//				mainApplet.line(amp*radiation,0,amp*radiation,lineLength);
//			}
//			for (int i=0; i<rightChannelSamples.length; i+=stepSizeSamples) {
//				mainApplet.stroke(i,70,70,10);
//				float amp = rightChannelSamples[i];
//				mainApplet.line(-amp*radiation,0,-amp*radiation,lineLength);
//			}
//			mainApplet.translate(0,lineLength);
//			mainApplet.rotate(-angleRightLowerArm);
//			for (int i=0; i<leftChannelSamples.length; i+=stepSizeSamples) {
//				mainApplet.stroke(i,70,70,10);
//				float amp = leftChannelSamples[i];
//				mainApplet.line(amp*radiation,0,amp*radiation,lineLength);
//			}
//			for (int i=0; i<rightChannelSamples.length; i+=stepSizeSamples) {
//				mainApplet.stroke(i,70,70,10);
//				float amp = rightChannelSamples[i];
//				mainApplet.line(-amp*radiation,0,-amp*radiation,lineLength);
//			}
			
			mainApplet.colorMode(PApplet.RGB,255,255,255,255);

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

	    if(mainApplet.frameCount % 4 == 0 ) {
			mainApplet.image(mainApplet.get(),-5,-5,mainApplet.width+8,mainApplet.height+8);
	    }
	}
	
	public void stop () {
		audioPlayer.close();
		minim.stop();
	}

}
