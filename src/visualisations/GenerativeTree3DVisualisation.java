package visualisations;

import java.util.ArrayList;

import processing.core.*;
import scenes.TunnelScene3D;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;

public class GenerativeTree3DVisualisation extends AbstractSkeletonAudioVisualisation {

	private int strokeColor = 0;

	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width, height;
	protected float centerZ;
	protected float fadeInCenterZ=0;
	protected float fadeOutCenterZ=0;
	protected PVector center = new PVector();
	protected final float lowerZBoundary = 0.45f*TunnelScene3D.tunnelLength; // to control z position of drawing within a narrow corridor
	protected final float upperZBoundary = 0.7f*TunnelScene3D.tunnelLength;

	// variables to calculate and draw the tree
	private float curlRightLowerArm = 0;
	private float curlLeftLowerArm = 0;
	private float curlRightUpperArm = 0;
	private float curlLeftUpperArm = 0;
	private float orientationTree = 0;
	private float downscaleStrokeLength = PApplet.sqrt(2)/2.f;
	private float delay = 20;
	private int minBranches = 5;
	private int addBranches = 5;
	private int branchCount = 0;
	
	// leafs
	private ArrayList<PVector> leafsBottom = new ArrayList<PVector>();
	private ArrayList<PVector> leafsTop = new ArrayList<PVector>();
	private int[] leafColors;
	private int actColorIndex = 0;
	private int colorsSize = 360;
	private float colorsStepSize = 10.f;
	private int leafWidth = 25;
	private int leafHeight = 50;
	private boolean leafsFallDown = false;
	private boolean leafsGrow = false;
	private int frameTreeShaken = -9999;
	private float fallDownSpeed = 5f;
	
	// audio responsive tree
	protected float initialScale = 2f;
	protected float downScale = 0.9f;
	protected float transparency = 200;
	protected float sampleStepSize =10f;
	protected int actSampleIndex = 0;
	
	public GenerativeTree3DVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super (_mainApplet,_skeleton,_audioManager);
		mainApplet.setMirrorKinect(true);
	}
	
	public void setup() {
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		strokeColor = mainApplet.color(250,250,250);
		mainApplet.colorMode(PConstants.HSB,360,100,100,255);
		leafColors = new int[colorsSize];
		for (int i=0; i<colorsSize; i++) {
			leafColors[i] = mainApplet.color(i,100,100);
		}
	}
	
	public boolean fadeIn () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			fadeInCenterZ+=skeleton.distanceToKinect()/mainApplet.frameRate;
			if (fadeInCenterZ >= skeleton.distanceToKinect())
				return true;
			
			// center.z reacts to position of user with delay
			centerZ = PApplet.map(fadeInCenterZ,0,TherapeuticPresence.maxDistanceToKinect,0,upperZBoundary);
			width = TunnelScene3D.getTunnelWidthAt(centerZ);
			height = TunnelScene3D.getTunnelHeightAt(centerZ);
		    center.set(0,0,centerZ);
			// get angles for drawing
			getAnglesForBranches();
			// turn the tree fast for fade in effect
			orientationTree = (fadeInCenterZ/skeleton.distanceToKinect() * 1.4f * PConstants.TWO_PI)%PConstants.TWO_PI;
			// draw
			mainApplet.pushStyle();
			drawTree(minBranches+PApplet.round(addBranches*centerZ/upperZBoundary),centerZ/upperZBoundary,centerZ/upperZBoundary*height/3);
			drawLeafs();
			mainApplet.popStyle();
		}
		return false;
	}
	
	public boolean fadeOut () {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			fadeOutCenterZ-=skeleton.distanceToKinect()/mainApplet.frameRate;
			if (fadeOutCenterZ <= 0f)
				return true;
			
			// center.z reacts to position of user with delay
			centerZ = fadeOutCenterZ;
			width = TunnelScene3D.getTunnelWidthAt(centerZ);
			height = TunnelScene3D.getTunnelHeightAt(centerZ);
		    center.set(0,0,centerZ);
			// get angles for drawing
			getAnglesForBranches();
			// turn the tree fast for fade out effect
			orientationTree = (fadeOutCenterZ/skeleton.distanceToKinect() * -1.4f * PConstants.TWO_PI)%PConstants.TWO_PI;
			// draw
			mainApplet.pushStyle();
			drawTree(minBranches+PApplet.round(addBranches*centerZ/upperZBoundary),centerZ/upperZBoundary,centerZ/upperZBoundary*height/3);
			drawLeafs();
			mainApplet.popStyle();
		}
		return false;
	}

	public void draw() {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			// center.z reacts to position of user with delay
			centerZ = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,lowerZBoundary,upperZBoundary);
			width = TunnelScene3D.getTunnelWidthAt(centerZ);
			height = TunnelScene3D.getTunnelHeightAt(centerZ);
		    center.set(0,0,centerZ);
			// get angles for drawing
			getAnglesForBranches();
			// draw
			mainApplet.pushStyle();
			drawTree(minBranches+PApplet.round(addBranches*centerZ/upperZBoundary),centerZ/upperZBoundary,centerZ/upperZBoundary*height/3);
			drawLeafs();
			mainApplet.popStyle();
			// prepare for fade out
			fadeOutCenterZ = centerZ;
			if (mainApplet.frameCount-frameTreeShaken > 70) {
				leafsFallDown = false;
			}
		}	
	}
	
	public void shakeTree () {
		frameTreeShaken = mainApplet.frameCount; 
		leafsFallDown = true;
		fallDownSpeed = 5;
	}
	
	private void getAnglesForBranches () {
//		PVector bodyAxis = skeleton.getOrientationYProjective();
//		PVector leftUpperArm = PVector.sub(skeleton.getJointProjective(Skeleton.LEFT_ELBOW),skeleton.getJointProjective(Skeleton.LEFT_SHOULDER));
//		PVector rightUpperArm = PVector.sub(skeleton.getJointProjective(Skeleton.RIGHT_ELBOW),skeleton.getJointProjective(Skeleton.RIGHT_SHOULDER));
//		PVector leftLowerArm = PVector.sub(skeleton.getJointProjective(Skeleton.LEFT_HAND),skeleton.getJointProjective(Skeleton.LEFT_ELBOW));
//		PVector rightLowerArm = PVector.sub(skeleton.getJointProjective(Skeleton.RIGHT_HAND),skeleton.getJointProjective(Skeleton.RIGHT_ELBOW));
//		float angleLeftUpperArm = PVector.angleBetween(leftUpperArm,bodyAxis);
//		float angleRightUpperArm = PVector.angleBetween(rightUpperArm,bodyAxis);
//		float angleLeftLowerArm = PVector.angleBetween(leftLowerArm,leftUpperArm);
//		float angleRightLowerArm =  PVector.angleBetween(rightLowerArm,rightUpperArm);
		float angleLeftUpperArm = skeleton.angleToLocalYAxis(Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER); 
		float angleRightUpperArm = skeleton.angleToLocalYAxis(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER);
		float angleLeftLowerArm = skeleton.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_SHOULDER,Skeleton.LEFT_HAND,Skeleton.LEFT_ELBOW); 
		float angleRightLowerArm =  skeleton.angleBetween(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_HAND,Skeleton.RIGHT_ELBOW); 
		float orientationSkeleton = PVector.angleBetween(new PVector(0,0,1),skeleton.getOrientationX()) - PConstants.HALF_PI;
		
		// TODO this is a hack. find solution to switch on/off mirroring of kinect
		if (!TherapeuticPresence.mirrorKinect) {
			// trees react to body posture with a delay
			curlLeftLowerArm += (angleRightLowerArm*0.7-curlLeftLowerArm)/delay;		
			curlLeftUpperArm += (angleRightUpperArm*0.7-curlLeftUpperArm)/delay;
			curlRightLowerArm += (angleLeftLowerArm*0.7-curlRightLowerArm)/delay;
			curlRightUpperArm += (angleLeftUpperArm*0.7-curlRightUpperArm)/delay;		
			orientationTree += (orientationSkeleton*0.8-orientationTree)/delay;
		} else {
			// trees react to body posture with a delay
			curlLeftLowerArm += (angleLeftLowerArm*0.7-curlLeftLowerArm)/delay;
			curlLeftUpperArm += (angleLeftUpperArm*0.7-curlLeftUpperArm)/delay;
			curlRightLowerArm += (angleRightLowerArm*0.7-curlRightLowerArm)/delay;		
			curlRightUpperArm += (angleRightUpperArm*0.7-curlRightUpperArm)/delay;	
			orientationTree += (-orientationSkeleton*0.8-orientationTree)/delay;
		}
	}
	
	private void drawTree(int branchDepth,float scale, float initialStrokeLength) {
		// colors of leafs differ in HSB-space
		colorsStepSize = colorsSize/PApplet.pow(2,branchDepth); 
		actColorIndex = 0;
		// leafs flicker according to music samples
		sampleStepSize = audioManager.getBufferSize()/PApplet.pow(2,branchDepth);
		actSampleIndex = 0;
		// the leafs
		if (!leafsFallDown) {
			leafsBottom.clear();
			leafsTop.clear();
		}
		
		// draw trunk of the tree
		mainApplet.pushMatrix();
		mainApplet.translate(center.x,-scale*height/8-initialStrokeLength,center.z);
		mainApplet.stroke(strokeColor,transparency);
		float strokeWeight = audioManager.getMeanFFT(0) * initialScale;
		mainApplet.strokeWeight(strokeWeight);
		mainApplet.line(0,0,0,0,initialStrokeLength,0);
		mainApplet.translate(0,initialStrokeLength,0);
		// rotate tree according to body rotation
		mainApplet.rotateY(orientationTree);
		// start branching
		branchCount = branchDepth;
		branch(initialStrokeLength*downscaleStrokeLength,branchDepth,strokeWeight*downScale);
		mainApplet.popMatrix();
	}
	
	private void drawLeafs () {
		if (leafsFallDown) {
			for (int i=0; i<leafsBottom.size(); i++) {
				PVector top = leafsTop.get(i);
				//top.z+=20;
				top.y+=fallDownSpeed;
				leafsTop.set(i,top);
				PVector bottom = leafsBottom.get(i);
				//bottom.z+=20;
				bottom.y+=fallDownSpeed;
				leafsBottom.set(i,bottom);
			}
			fallDownSpeed+=2f;
		}
		mainApplet.stroke(0,0,0);
		mainApplet.strokeWeight(1);
		mainApplet.pushMatrix();
		mainApplet.rotateY(PConstants.PI);
		mainApplet.rotateZ(PConstants.PI);
		mainApplet.translate(0,0,-TunnelScene3D.tunnelLength);
		for (int i=0; i<leafsBottom.size(); i++) {
			PVector top = leafsTop.get(i);
			PVector bottom = leafsBottom.get(i);
			mainApplet.fill(leafColors[PApplet.round(i*colorsStepSize)],transparency);
			mainApplet.pushMatrix();
			mainApplet.translate(bottom.x,bottom.y,bottom.z);
			mainApplet.rotateY(orientationTree);
			// get the orientation of the leafs by looking at the bottom and at the top point
			float y = top.y-bottom.y;
			float x = top.x-bottom.x;
			float angle = PApplet.atan2(y,x); // gives angle between -PI and PI
			mainApplet.rotateZ(angle+1.5f*PConstants.PI); // transfer to angle between 0 and 2PI with regard to the positive y axis
			mainApplet.rotateZ(audioManager.getMeanSampleAt(PApplet.round(i*sampleStepSize)));
			mainApplet.translate(0,leafHeight/2);
			mainApplet.ellipse(0,0,leafWidth,leafHeight);
			mainApplet.popMatrix();
		}
		mainApplet.popMatrix();
	}
	
	private void branch(float _strokeLength, int _branchDepth, float _strokeWeight) {
		_strokeLength *= downscaleStrokeLength;
		_branchDepth -= 1;
		if ((_strokeLength > 1) && (_branchDepth > 0)) {
		    // draw branch and go ahead
			mainApplet.pushMatrix();
		    
			if ((branchCount-_branchDepth)%2 == 0) { // alternating use of upper/lower arm
				mainApplet.rotateZ(-curlLeftLowerArm); // rotate clockwise
			} else {
				mainApplet.rotateZ(-curlLeftUpperArm); // rotate clockwise
			}
			mainApplet.stroke(strokeColor,transparency);
			mainApplet.strokeWeight(_strokeWeight);
			mainApplet.line(0,0,0,0,_strokeLength,0);
			mainApplet.translate(0,_strokeLength,0);
		    branch(_strokeLength,_branchDepth,_strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
	      
		    mainApplet.pushMatrix();

			if ((branchCount-_branchDepth)%2 == 0) { // alternating use of upper/lower arm
				mainApplet.rotateZ(curlRightLowerArm); // rotate clockwise
			} else {
				mainApplet.rotateZ(curlRightUpperArm); // rotate clockwise
			}
			mainApplet.stroke(strokeColor,transparency);
			mainApplet.strokeWeight(_strokeWeight);
			mainApplet.line(0,0,0,0,_strokeLength,0);
		    mainApplet.translate(0,_strokeLength,0);
		    branch(_strokeLength,_branchDepth,_strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
		} else {
			if (!leafsFallDown) {
				// store leaf positions to draw them later (for manipulation)
				mainApplet.pushMatrix();
				mainApplet.rotateZ(PConstants.HALF_PI*audioManager.getMeanSampleAt(actSampleIndex+=sampleStepSize));
				leafsBottom.add(new PVector(mainApplet.modelX(0,0,0),mainApplet.modelY(0,0,0),mainApplet.modelZ(0,0,0)));
				mainApplet.translate(0,leafHeight,0);
				leafsTop.add(new PVector(mainApplet.modelX(0,0,0),mainApplet.modelY(0,0,0),mainApplet.modelZ(0,0,0)));
				mainApplet.popMatrix();
			}
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION;
	}

}
