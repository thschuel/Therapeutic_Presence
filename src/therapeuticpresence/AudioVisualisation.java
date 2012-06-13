package therapeuticpresence;

import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.FFT;

public class AudioVisualisation extends SkeletonVisualisation implements AudioListener {

	protected float[] leftChannelSamples = null;
	protected float[] rightChannelSamples = null;

	protected PGraphics tunnel;
	protected PImage tunnelImg;
	protected Minim minim;
	protected AudioPlayer audioPlayer;
	
	protected boolean calcFFT = true; 
	protected int bands = 8;
	protected FFT fft; 
	protected float maxFFT;
	protected float[] leftFFT = null;
	protected float[] rightFFT = null;
	
	// coordinates for the visualization are defined through angles of limbs
	protected int leftX, leftY;
	protected int leftQuarterX, leftQuarterY;
	protected int centerX, centerY;
	protected int rightQuarterX, rightQuarterY;
	protected int rightX, rightY;
	protected int delay = 5;
	
	
	public AudioVisualisation (PApplet _mainApplet, Skeleton _skeleton) {
		super(_mainApplet,_skeleton);
		minim = new Minim(mainApplet);
		tunnel = mainApplet.createGraphics(mainApplet.width,mainApplet.height,PConstants.P3D);
		tunnelImg = tunnel.get();
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
	    
	    centerX = mainApplet.width/2;
		leftX = 0;
		leftQuarterX = mainApplet.width/4;
		rightQuarterX = 3*mainApplet.width/4;
		rightX = mainApplet.width;	
		leftY = mainApplet.height/2;
		leftQuarterY = mainApplet.height/2;
		centerY = mainApplet.height/2;
		rightQuarterY = mainApplet.height/2;
		rightY = mainApplet.height/2;
	}
	
	public void draw () {
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
		
		if (mainApplet.frameCount%4 == 0) {
			//updateTunnel();
			//tunnelImg = tunnel.get();
		}
		//mainApplet.image(tunnelImg,0,0);
		
		//mainApplet.image(visual, 0, 0);
		if (skeleton.isUpdated && leftChannelSamples != null && rightChannelSamples != null) {
			
			updateOrientationCoordinates();
			
			if (calcFFT) {
			    fft.forward(leftChannelSamples);
			    for(int i = 0; i < bands; i++) leftFFT[i] = fft.getAvg(i);
			    fft.forward(rightChannelSamples);
			    for(int i = 0; i < bands; i++) rightFFT[i] = fft.getAvg(i);
			}
			
			int radiation = 30;
			int stepSizeSamples = 5;
			
			mainApplet.colorMode(PApplet.HSB,bands,255,255,255);
			for (int i=0; i<bands; i++) {
				int offset = i*radiation;
				int scale;
				if (i==0) scale = 4;
				else scale = 8;
				mainApplet.stroke(i,255,255,255);
				mainApplet.noFill();
				mainApplet.strokeWeight(leftFFT[i]*scale);
				mainApplet.beginShape();
				mainApplet.vertex(leftX,leftY+offset);
				mainApplet.vertex(leftQuarterX,leftQuarterY+offset);
				mainApplet.vertex(centerX,centerY+offset);
				mainApplet.vertex(rightQuarterX,rightQuarterY+offset);
				mainApplet.vertex(rightX,rightY+offset);
				mainApplet.endShape();
				mainApplet.strokeWeight(rightFFT[i]*scale);
				mainApplet.beginShape();
				mainApplet.vertex(leftX,leftY-offset);
				mainApplet.vertex(leftQuarterX,leftQuarterY-offset);
				mainApplet.vertex(centerX,centerY-offset);
				mainApplet.vertex(rightQuarterX,rightQuarterY-offset);
				mainApplet.vertex(rightX,rightY-offset);
				mainApplet.endShape();
			}
			
			mainApplet.colorMode(PApplet.RGB,255,255,255,255);
		}
	}
	
	private void updateTunnel () {
		tunnel.beginDraw();
		tunnel.noStroke();
		tunnel.copy(tunnel.get(),10, 10, tunnel.width-20, tunnel.height-20,0,0,tunnel.width, tunnel.height ); 
		tunnel.fill(0,10);
		tunnel.rect(0,0,tunnel.width, tunnel.height );
		tunnel.pushMatrix();
		tunnel.translate( tunnel.width/2, tunnel.height/2 );
		tunnel.stroke(255);
		tunnel.strokeWeight(2);
		for( int i = 0; i < 360; i++ ) {
			tunnel.stroke( 255, mainApplet.random(255), 0); 
			tunnel.point( PApplet.cos(PApplet.radians(i)) * 70, PApplet.sin(PApplet.radians(i)) * 70);
			tunnel.point( PApplet.cos(PApplet.radians(i)) * 80, PApplet.sin(PApplet.radians(i)) * 80);
		}
		tunnel.popMatrix();
		tunnel.endDraw();
	}
	
	private void updateOrientationCoordinates () {
		float angleLeftUpperArm = skeleton.angleToYAxis(Skeleton.LEFT_SHOULDER,Skeleton.LEFT_ELBOW);
		float angleRightUpperArm = skeleton.angleToYAxis(Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_ELBOW);
		float angleLeftLowerArm = skeleton.angleBetween(Skeleton.LEFT_HAND,Skeleton.LEFT_ELBOW,Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER);
		float angleRightLowerArm = skeleton.angleBetween(Skeleton.RIGHT_HAND,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
		
		// check for angles not reach out of quadrants.
		if(angleLeftUpperArm+angleLeftLowerArm > PConstants.PI) {
			angleLeftLowerArm = PConstants.PI - angleLeftUpperArm;
		}
		if(angleRightUpperArm+angleRightLowerArm > PConstants.PI) {
			angleRightLowerArm = PConstants.PI - angleRightUpperArm;
		}
		
		// shift angles of upper arms by 90 degree to use calculations in polar coordinates
		angleLeftUpperArm = (angleLeftUpperArm+PConstants.PI/2)%PConstants.PI;
		angleRightUpperArm = (angleRightUpperArm+PConstants.PI/2)%PConstants.PI;
		// shift angles of lower arms by angles of upper arms to use calculations in polar coordinates
		angleLeftLowerArm = (angleLeftLowerArm+angleLeftUpperArm)%PConstants.PI;
		angleRightLowerArm = (angleRightLowerArm+angleRightUpperArm)%PConstants.PI;
		
		// actual coordinates
		int leftQuarterYNew = centerY+(int)((leftQuarterX-centerX)*PApplet.sin(angleLeftUpperArm)/PApplet.cos(angleLeftUpperArm));
		int rightQuarterYNew = centerY-(int)((rightQuarterX-centerX)*PApplet.sin(angleRightUpperArm)/PApplet.cos(angleRightUpperArm));
		int leftYNew = leftQuarterY+(int)((leftX-leftQuarterX)*PApplet.sin(angleLeftLowerArm)/PApplet.cos(angleLeftLowerArm));
		int rightYNew = rightQuarterY-(int)((rightX-rightQuarterX)*PApplet.sin(angleRightLowerArm)/PApplet.cos(angleRightLowerArm));
		// constrain coordinates
		leftQuarterYNew = PApplet.constrain(leftQuarterYNew,50,mainApplet.height-50);
		rightQuarterYNew = PApplet.constrain(rightQuarterYNew,50,mainApplet.height-50);
		leftYNew = PApplet.constrain(leftYNew,0,mainApplet.height);
		rightYNew = PApplet.constrain(rightYNew,0,mainApplet.height);
		// update coordinates with delay
		leftQuarterY += (leftQuarterYNew-leftQuarterY)/delay;
		rightQuarterY += (rightQuarterYNew-rightQuarterY)/delay;
		leftY += (leftYNew-leftY)/delay;
		rightY += (rightYNew-rightY)/delay;
	}
	
	public void stop () {
		audioPlayer.close();
		minim.stop();
	}

}
