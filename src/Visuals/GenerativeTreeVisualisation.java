package Visuals;

import processing.core.*;
import therapeuticpresence.Skeleton;

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
	private int	colorsSize = 360;
	private float colorsStepSize = 10.f;
	
	
	public GenerativeTreeVisualisation (PApplet _mainApplet, Skeleton _skeleton) {
		super (_mainApplet,_skeleton);
	}
	
	public void setup() {
		mainApplet.colorMode(PConstants.HSB,360,100,100);
		strokeColor = mainApplet.color(0,0,0);
		leafColors = new int[colorsSize];
		for (int i=0; i<colorsSize; i++) {
			leafColors[i] = mainApplet.color(i,100,100);
		}
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
	}

	public void draw() {
		mainApplet.background(250,250,250);
		mainApplet.camera();
		// different colors for different skeletons
		mainApplet.stroke(strokeColor);
		// different positions for different skeletons
		mainApplet.translate(mainApplet.width/2,mainApplet.height);
		//float angleBodyAxis = currSkel.angleToUpAxis(Skeleton.NECK,Skeleton.TORSO);
		/*PVector neck = currSkel.getJoint(Skeleton.NECK);
		PVector torso = currSkel.getJoint(Skeleton.TORSO);
		PVector body = PVector.sub(neck,torso);
		float angleBodyAxis = PVector.angleBetween(new PVector(body.x,body.y,0), new PVector(0,1,0));
		if (neck.x < torso.x) {
			angleBodyAxis *= -1;
		}
		rotate(angleBodyAxis);*/
		mainApplet.line(0,0,0,-mainApplet.height/3);
		mainApplet.translate(0,-mainApplet.height/3);
		
		float anglelArmToBodyAxis = skeleton.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_HAND,Skeleton.TORSO,Skeleton.NECK); 
		float anglerArmToBodyAxis = skeleton.angleBetween(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_HAND,Skeleton.TORSO,Skeleton.NECK);
		float angleLArmToRArm = skeleton.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_HAND,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_HAND);
		 
		// trees react to body posture
		curlx += (anglelArmToBodyAxis*0.75-curlx)/delay;
		curly += (anglerArmToBodyAxis*0.75-curly)/delay;				
		int branchCount = (int)(13*skeleton.distanceToKinect()/3000f);
		// colors of leafs differ in HSB-space
		colorsStepSize = colorsSize/PApplet.pow(2,branchCount); 
		actColor = 0.f;
		
		branch(mainApplet.height/4.f,branchCount);
		
		// for debug
//		guiHud.sendGuiMessage("Angle lArm to rArm Axis: "+guiHud.df.format(angleLArmToRArm));
//		guiHud.sendGuiMessage("Angle lArm to Body Axis: "+guiHud.df.format(anglelArmToBodyAxis));
//		guiHud.sendGuiMessage("Angle rArm to Body Axis: "+guiHud.df.format(anglerArmToBodyAxis));
		//guiHud.sendGuiMessage("Angle Body to Up Axis: "+guiHud.df.format(angleBodyAxis));

	}
	
	private void branch(float length, int count)
	{
		length *= f;
		count -= 1;
		if ((length > 1) && (count > 0)) {
		    // draw branch and go ahead
			mainApplet.pushMatrix();
		    
			mainApplet.rotate(-curlx);
			mainApplet.line(0,0,0,-length);
			mainApplet.translate(0,-length);
		    branch(length,count);
		    
		    mainApplet.popMatrix();
	      
		    mainApplet.pushMatrix();
		    
		    mainApplet.rotate(curly);
		    mainApplet.line(0,0,0,-length);
		    mainApplet.translate(0,-length);
		    branch(length,count);
		    
		    mainApplet.popMatrix();
		} else {
			// draw leafs
			mainApplet.fill(leafColors[PApplet.round(actColor+=colorsStepSize)],200);
			mainApplet.translate(0,-5);
			mainApplet.ellipse(0,0,5,10);
		}
		
	}

}
