package visualisations;

import processing.core.*;
import therapeuticpresence.*;

public class GenerativeTreeVisualisation extends SkeletonVisualisation {

	private int strokeColor = 0;

	// variables to calculate and draw the tree
	private float curlx = 0;
	private float curly = 0;
	private float f = PApplet.sqrt(2)/2.f;
	private float delay = 20;
//	private float growth = 0;
//	private float growthTarget = 0;
//	private int branches = 17;
	
	// colors of leafs
	private int[] leafColors;
	private float actColor = 0.f;
	private int colorsSize = 360;
	private float colorsStepSize = 10.f;
	
	// audio responsive tree
	protected AudioManager audioManager = null;
	
	protected float initialScale = 3f;
	protected float downScale = 0.9f;
	protected float transparency = 150;
	
	public GenerativeTreeVisualisation (TherapeuticPresence _mainApplet, Skeleton _skeleton, AudioManager _audioManager) {
		super (_mainApplet,_skeleton);
		audioManager = _audioManager;
	}
	
	public void setup() {
		mainApplet.colorMode(PConstants.HSB,360,100,100);
		strokeColor = mainApplet.color(0,0,100);
		leafColors = new int[colorsSize];
		for (int i=0; i<colorsSize; i++) {
			leafColors[i] = mainApplet.color(i,100,100);
		}
	}

	public void draw() {
		if (skeleton.isUpdated && audioManager.isUpdated) {
			
			// draw trunk of the tree
			mainApplet.pushMatrix();
			mainApplet.translate(mainApplet.width/2,mainApplet.height);
			mainApplet.stroke(strokeColor,transparency);
			float strokeWeight = audioManager.getMeanFFT(0) * initialScale;
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,-mainApplet.height/3);
			mainApplet.translate(0,-mainApplet.height/3);
			
			float anglelArmToBodyAxis = skeleton.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_HAND,Skeleton.TORSO,Skeleton.NECK); 
			float anglerArmToBodyAxis = skeleton.angleBetween(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_HAND,Skeleton.TORSO,Skeleton.NECK);
			 
			// trees react to body posture with a delay
			curlx += (anglelArmToBodyAxis*0.75-curlx)/delay;
			curly += (anglerArmToBodyAxis*0.75-curly)/delay;				
			int branchCount = (int)(13*skeleton.distanceToKinect()/3000f);
			// colors of leafs differ in HSB-space
			colorsStepSize = colorsSize/PApplet.pow(2,branchCount); 
			actColor = 0.f;
			// start branching
			branch(mainApplet.height/4.f,branchCount,strokeWeight*downScale);
			
			mainApplet.popMatrix();
		}
		
	}
	
	private void branch(float length, int count, float strokeWeight)
	{
		length *= f;
		count -= 1;
		if ((length > 1) && (count > 0)) {
		    // draw branch and go ahead
			mainApplet.pushMatrix();
		    
			mainApplet.rotate(-curlx);
			mainApplet.stroke(strokeColor,transparency);
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,-length);
			mainApplet.translate(0,-length);
		    branch(length,count,strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
	      
		    mainApplet.pushMatrix();
		    
		    mainApplet.rotate(curly);
			mainApplet.stroke(strokeColor,transparency);
			mainApplet.strokeWeight(strokeWeight);
			mainApplet.line(0,0,0,-length);
		    mainApplet.translate(0,-length);
		    branch(length,count,strokeWeight*downScale);
		    
		    mainApplet.popMatrix();
		} else {
			// draw leafs
			mainApplet.strokeWeight(1);
			mainApplet.fill(leafColors[PApplet.round(actColor+=colorsStepSize)],transparency);
			mainApplet.translate(0,-5);
			mainApplet.ellipse(0,0,15,30);
			mainApplet.noFill();
		}
		
	}

	public short getVisualisationType() {
		return TherapeuticPresence.GENERATIVE_TREE_VISUALISATION;
	}

}
