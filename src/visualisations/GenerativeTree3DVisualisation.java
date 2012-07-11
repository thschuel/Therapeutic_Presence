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
	private float curlx = 0;
	private float curly = 0;
	private float downscaleStrokeLength = PApplet.sqrt(2)/2.f;
	private float delay = 20;
	private int minBranches = 5;
	private int addBranches = 5;
//	private float growth = 0;
//	private float growthTarget = 0;
//	private int branches = 17;
	
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
		if (skeleton.isUpdated && audioManager.isUpdated) {
			// center.z reacts to position of user with delay
			float mappedDistance = PApplet.map(skeleton.distanceToKinect(),0,TherapeuticPresence.maxDistanceToKinect,TherapeuticPresence.lowerZBoundary,TherapeuticPresence.upperZBoundary);
			centerz = PApplet.constrain(mappedDistance/TherapeuticPresence.maxDistanceToKinect*TunnelScene3D.tunnelLength,0,TunnelScene3D.tunnelLength);
			width = TunnelScene3D.getTunnelWidthAt(centerz);
			height = TunnelScene3D.getTunnelHeightAt(centerz);
		    center.set(0,0,centerz);

			float actualScale = skeleton.distanceToKinect()/TherapeuticPresence.maxDistanceToKinect;
			float mappedScale = mappedDistance/TherapeuticPresence.maxDistanceToKinect;
			int branchCount = minBranches+PApplet.round(addBranches*actualScale);
			float initialStrokeLength = mappedScale*height/3;
			
			// draw trunk of the tree
			mainApplet.pushMatrix();
			mainApplet.translate(center.x,-mappedScale*height/8-initialStrokeLength,center.z);
			mainApplet.stroke(strokeColor,transparency);
			float strokeWeight = audioManager.getMeanFFT(0) * initialScale;
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,0,initialStrokeLength,0);
			mainApplet.translate(0,initialStrokeLength,0);
			
			float anglelArmToBodyAxis = skeleton.angleBetween(Skeleton.LEFT_SHOULDER,Skeleton.LEFT_HAND,Skeleton.TORSO,Skeleton.NECK); 
			float anglerArmToBodyAxis = skeleton.angleBetween(Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_HAND,Skeleton.TORSO,Skeleton.NECK);
			// TODO this is a hack. find solution to switch on/off mirroring of kinect
			if (!TherapeuticPresence.mirrorKinect) {
				// trees react to body posture with a delay
				curlx += (anglerArmToBodyAxis*0.5-curlx)/delay;
				curly += (anglelArmToBodyAxis*0.5-curly)/delay;		
			} else {
				// trees react to body posture with a delay
				curlx += (anglelArmToBodyAxis*0.5-curlx)/delay;
				curly += (anglerArmToBodyAxis*0.5-curly)/delay;		
			}
			
			// colors of leafs differ in HSB-space
			colorsStepSize = colorsSize/PApplet.pow(2,branchCount); 
			actColorIndex = 0.f;
			// sizes of leafs differ according to music samples
			sampleStepSize = audioManager.getBufferSize()/PApplet.pow(2,branchCount);
			actSampleIndex = 0f;
			// start branching
			branch(initialStrokeLength*downscaleStrokeLength,branchCount,strokeWeight*downScale);
			
			mainApplet.popMatrix();
		}
		
	}
	
	private void branch(float length, int count, float strokeWeight)
	{
		length *= downscaleStrokeLength;
		count -= 1;
		if ((length > 1) && (count > 0)) {
		    // draw branch and go ahead
			mainApplet.pushMatrix();
		    
			mainApplet.rotateZ(-curlx);
			mainApplet.stroke(strokeColor,transparency);
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,0,length,0);
			mainApplet.translate(0,length,0);
		    branch(length,count,strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
	      
		    mainApplet.pushMatrix();
		    
		    mainApplet.rotateZ(curly);
			mainApplet.stroke(strokeColor,transparency);
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,0,length,0);
		    mainApplet.translate(0,length,0);
		    branch(length,count,strokeWeight*downScale);
		    
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
