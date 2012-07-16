package visualisations;

import processing.core.*;
import scenes.TunnelScene3D;
import therapeuticpresence.*;
import therapeuticskeleton.Skeleton;

public class GenerativeTree3DVisualisation extends AbstractSkeletonAudioVisualisation {

	private int strokeColor = 0;

	// size of drawing canvas for bezier curves. is controlled by distance of user.
	protected float width, height;
	protected float centerz;
	protected PVector center = new PVector();

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
	
	// colors of leafs
	private int[] leafColors;
	private float actColorIndex = 0.f;
	private int colorsSize = 360;
	private float colorsStepSize = 10.f;
	private int leafWidth = 25;
	private int leafHeight = 50;
	
	// audio responsive tree
	protected float initialScale = 2f;
	protected float downScale = 0.9f;
	protected float transparency = 200;
	protected float sampleStepSize =10f;
	protected float actSampleIndex = 0f;
	
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

	public void draw() {
		if (skeleton.isUpdated() && audioManager.isUpdated()) {
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,TherapeuticPresence.lowerZBoundary,TherapeuticPresence.upperZBoundary);
			centerz = PApplet.constrain(mappedDistance/TherapeuticPresence.maxDistanceToKinect*TunnelScene3D.tunnelLength,0,TunnelScene3D.tunnelLength);
			width = TunnelScene3D.getTunnelWidthAt(centerz);
			height = TunnelScene3D.getTunnelHeightAt(centerz);
		    center.set(0,0,centerz);

			float actualScale = skeleton.distanceToKinect()/TherapeuticPresence.maxDistanceToKinect;
			float mappedScale = mappedDistance/TherapeuticPresence.maxDistanceToKinect;
			int branchDepth = minBranches+PApplet.round(addBranches*actualScale);
			float initialStrokeLength = mappedScale*height/3;
			
			// get angles for drawing
//			PVector bodyAxis = skeleton.getOrientationYProjective();
//			PVector leftUpperArm = PVector.sub(skeleton.getJointProjective(Skeleton.LEFT_ELBOW),skeleton.getJointProjective(Skeleton.LEFT_SHOULDER));
//			PVector rightUpperArm = PVector.sub(skeleton.getJointProjective(Skeleton.RIGHT_ELBOW),skeleton.getJointProjective(Skeleton.RIGHT_SHOULDER));
//			PVector leftLowerArm = PVector.sub(skeleton.getJointProjective(Skeleton.LEFT_HAND),skeleton.getJointProjective(Skeleton.LEFT_ELBOW));
//			PVector rightLowerArm = PVector.sub(skeleton.getJointProjective(Skeleton.RIGHT_HAND),skeleton.getJointProjective(Skeleton.RIGHT_ELBOW));
//			float angleLeftUpperArm = PVector.angleBetween(leftUpperArm,bodyAxis);
//			float angleRightUpperArm = PVector.angleBetween(rightUpperArm,bodyAxis);
//			float angleLeftLowerArm = PVector.angleBetween(leftLowerArm,leftUpperArm);
//			float angleRightLowerArm =  PVector.angleBetween(rightLowerArm,rightUpperArm);
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
			
			// draw trunk of the tree
			mainApplet.pushMatrix();
			mainApplet.translate(center.x,-mappedScale*height/8-initialStrokeLength,center.z);
			mainApplet.stroke(strokeColor,transparency);
			float strokeWeight = audioManager.getMeanFFT(0) * initialScale;
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,0,initialStrokeLength,0);
			mainApplet.translate(0,initialStrokeLength,0);

			mainApplet.rotateY(orientationTree);
			
			// colors of leafs differ in HSB-space
			colorsStepSize = colorsSize/PApplet.pow(2,branchDepth); 
			actColorIndex = 0.f;
			// sizes of leafs differ according to music samples
			sampleStepSize = audioManager.getBufferSize()/PApplet.pow(2,branchDepth);
			actSampleIndex = 0f;

			// start branching
			branchCount = branchDepth;
			branch(initialStrokeLength*downscaleStrokeLength,branchDepth,strokeWeight*downScale);

			mainApplet.popMatrix();
		}
		
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
			// draw leafs
			mainApplet.stroke(0,0,0);
			mainApplet.strokeWeight(1);
			mainApplet.fill(leafColors[PApplet.round(actColorIndex+=colorsStepSize)],transparency);
			float scale = audioManager.getMeanSampleAt(PApplet.round(actSampleIndex+=sampleStepSize));
			mainApplet.pushMatrix();
			mainApplet.rotateZ(PConstants.HALF_PI*scale);
			mainApplet.translate(0,leafHeight/2,0);
			mainApplet.ellipse(0,0,leafWidth,leafHeight);
			mainApplet.popMatrix();
			mainApplet.noFill();
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.GENERATIVE_TREE_2D_VISUALISATION;
	}

}
