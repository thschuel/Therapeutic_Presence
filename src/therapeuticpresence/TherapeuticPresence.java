package therapeuticpresence;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import processing.core.*;
import generativedesign.*;
import SimpleOpenNI.*;
import processing.opengl.*;

public class TherapeuticPresence extends PApplet {

	private static final long serialVersionUID = 1L;
	
	public static final short MAX_USER = 4;
	public static final short DRAW_SKELETONS = 1;
	public static final short DRAW_DEPTHMAP = 2;
	public static final short DRAW_TREE = 3;
	
	SimpleOpenNI kinect;
	
	// setup scene, values can be controlled by user input
	float translateX = 0.0f;
	float translateY = 0.0f;
	float translateZ = 0.0f;
	float rotX = radians(0);  // by default rotate the hole scene 180deg around the x-axis, the data from openni comes upside down
	float rotY = radians(0);
	float rotZ = radians(180);
	
	short visualisation = TherapeuticPresence.DRAW_DEPTHMAP;
	
	// control drawing of the kinect camera
	boolean drawCamFrustrum = false;
	
	// control for full body tracking
	boolean fullBodyTracking = false;

	// calculate colors for trace from image
	int[] colors; 
	PImage img;
	float actColor = 0.f;
	int	colorsSize;
	float colorsStepSize = 10.f;
	
	// variables to calculate and draw the tree
	float curlx = 0;
	float curly = 0;
	float f = sqrt(2)/2.f;
	float delay = 20;
	float growth = 0;
	float growthTarget = 0;
	int branches = 17;
	
	// the skeleton that controls the scene, only one user for now
	HashMap<Integer,Skeleton> skeletons = new HashMap<Integer,Skeleton>();
	
	GuiHud guiHud;
	
	
	// -----------------------------------------------------------------
	public void setup() {		
		size(screenWidth-2,screenHeight-100,OPENGL);  // strange, get drawing error in the cameraFrustum if i use P3D, in opengl there is no problem
		
		// establish connection to kinect/openni
		setupKinect();
		
		// build color set
		calculateColorSet();

		// generate HUD
		guiHud = new GuiHud(this);
		
		  
	}
	
	private void setupKinect () {
		kinect = new SimpleOpenNI(this);
		// enable/disable mirror
		kinect.setMirror(true);
		// enable depthMap generation 
		kinect.enableDepth();
		if (fullBodyTracking) {
			// enable skeleton generation for all joints
			kinect.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
		} else {
			// enable skeleton generation for upper joints
			kinect.enableUser(SimpleOpenNI.SKEL_PROFILE_UPPER);
		}
		
}
	
	private void calculateColorSet () {
		// load Image and calculate histogram
		/*img = loadImage("data/pic1.jpg");
		colorsSize = (img.width/stepSize)*(img.height/stepSize);
		colors = new int[colorsSize];
		int i = 0; 
		for (int py=0; py<img.height-stepSize; py+=stepSize) {
		  for (int px=0; px<img.width-stepSize; px+=stepSize) {
		    colors[i] = img.get(px, py);
		    i++;
		  }
		}
		// sort colors
		colors = GenerativeDesign.sortColors(this, colors, GenerativeDesign.BRIGHTNESS);
		*/
		colorMode(HSB,360,100,100);
		colorsSize = 360;
		colors = new int[colorsSize];
		for (int i=0; i<colorsSize; i++) {
			colors[i] = color(i,100,100);
		}
		colorMode(RGB,255,255,255);
	}

	
	// -----------------------------------------------------------------
	public void draw() {
		
		// -------- update status --------------------------
		// update the kinect
		kinect.update();
		
		// update skeletons in the scene
		Iterator it = skeletons.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			int currUserId = (Integer)me.getKey();
			Skeleton currSkel = (Skeleton)me.getValue();
			
			if (kinect.isTrackingSkeleton(currUserId)) {
				currSkel.updateSkeleton();
			}
		}
		
		// -------- drawing --------------------------------
		// draw chosen visualisation (depth map is default)
		switch (visualisation) {
			case TherapeuticPresence.DRAW_SKELETONS:
				drawSkeletons();
				break;
				
			case TherapeuticPresence.DRAW_TREE:
				drawTree();
				break;
			
			default:
				drawDepthMap();
				break;
		}
		// draw the HUD
		guiHud.draw();
		guiHud.resetGuiMessages();
	}
	
	private void drawSkeletons () {
		// reset the scene
		background(0,0,0);
		guiHud.guiTextColor = 255;
		// set the camera to the position of the kinect, facing towards the scene
		camera(0,0,0,0,0,1,0,1,0);
		// rotate the scene: kinect data comes upside down!
		pushMatrix();
		rotateX(rotX);
		rotateY(rotY);
		rotateZ(rotZ);
		translate(translateX,translateY,translateZ);
					
		Iterator it = skeletons.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			int currUserId = (Integer)me.getKey();
			Skeleton currSkel = (Skeleton)me.getValue();
			
			if (kinect.isTrackingSkeleton(currUserId)) {
				currSkel.drawSkeleton(this);
				currSkel.drawJointOrientations(100f,this);
				currSkel.drawJoints(20f,this);
				currSkel.drawMirrorPlane(this);
				float angleLeftArm = currSkel.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_HAND,Skeleton.NECK,Skeleton.TORSO);
				guiHud.sendGuiMessage("Angle lArm to Body Axis: "+guiHud.df.format(angleLeftArm));
				float angleBodyAxis = currSkel.angleToUpAxis(Skeleton.NECK,Skeleton.TORSO);
				guiHud.sendGuiMessage("Angle Body to Up Axis: "+guiHud.df.format(angleBodyAxis));
				float distance = currSkel.distanceToKinect();
				guiHud.sendGuiMessage("Distance of Skeleton: "+guiHud.df.format(distance));
			}
		}
		
		if (drawCamFrustrum) kinect.drawCamFrustum();
		
		popMatrix();
	}
	
	private void drawDepthMap () {
		// reset the scene
		background(0,0,0);
		// set the camera to the position of the kinect, facing towards the scene
		camera(0,0,0,0,0,1,0,1,0);
		// rotate the scene: kinect data comes upside down!
		pushMatrix();
		rotateX(rotX);
		rotateY(rotY);
		rotateZ(rotZ);
		translate(translateX,translateY,translateZ);
		
		int[]   depthMap = kinect.depthMap();
		int     steps   = 3;  // to speed up the drawing, draw every third point
		int     index;
		PVector realWorldPoint;

		stroke(70,70,70); 
		for(int y=0;y < kinect.depthHeight();y+=steps) {
			for(int x=0;x < kinect.depthWidth();x+=steps) {
				index = x + y * kinect.depthWidth();
				if(depthMap[index] > 0) { 
					// draw the projected point
					realWorldPoint = kinect.depthMapRealWorld()[index];
					point(realWorldPoint.x,realWorldPoint.y,realWorldPoint.z);
				}
			} 
		}
		popMatrix();
	} 
	
	private void drawTree () {
		background(250);
		guiHud.guiTextColor = 0;
		camera();
		
		Iterator it = skeletons.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			int currUserId = (Integer)me.getKey();
			Skeleton currSkel = (Skeleton)me.getValue();
			
			if (kinect.isTrackingSkeleton(currUserId)) {
				
				// different colors for different skeletons
				stroke((int)(255*(currUserId-1)/4));
				// different positions for different skeletons
				translate(width/2,height);
				//float angleBodyAxis = currSkel.angleToUpAxis(Skeleton.NECK,Skeleton.TORSO);
				/*PVector neck = currSkel.getJoint(Skeleton.NECK);
				PVector torso = currSkel.getJoint(Skeleton.TORSO);
				PVector body = PVector.sub(neck,torso);
				float angleBodyAxis = PVector.angleBetween(new PVector(body.x,body.y,0), new PVector(0,1,0));
				if (neck.x < torso.x) {
					angleBodyAxis *= -1;
				}
				rotate(angleBodyAxis);*/
				line (0,0,0,-height/3);
				translate(0,-height/3);
				
				float anglelArmToBodyAxis = currSkel.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_HAND,Skeleton.TORSO,Skeleton.NECK); 
				float anglerArmToBodyAxis = currSkel.angleBetween(Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_HAND,Skeleton.TORSO,Skeleton.NECK);
				
				
				float angleLArmToRArm = currSkel.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_HAND,Skeleton.RIGHT_ELBOW,Skeleton.RIGHT_HAND);
				 
				// trees react to body posture
				curlx += (anglelArmToBodyAxis*0.75-curlx)/delay;
				curly += (anglerArmToBodyAxis*0.75-curly)/delay;				
				int branchCount = (int)(13*currSkel.distanceToKinect()/3000f);
				// colors of leafs differ in HSB-space
				colorsStepSize = colorsSize/pow(2,branchCount); 
				actColor = 0.f;
				
				branch(height/4.f,branchCount);
				
				// for debug
//				guiHud.sendGuiMessage("Angle lArm to rArm Axis: "+guiHud.df.format(angleLArmToRArm));
//				guiHud.sendGuiMessage("Angle lArm to Body Axis: "+guiHud.df.format(anglelArmToBodyAxis));
//				guiHud.sendGuiMessage("Angle rArm to Body Axis: "+guiHud.df.format(anglerArmToBodyAxis));
				//guiHud.sendGuiMessage("Angle Body to Up Axis: "+guiHud.df.format(angleBodyAxis));
			}
		}
	}
	  
	private void branch(float length, int count)
	{
		length *= f;
		count -= 1;
		if ((length > 1) && (count > 0)) {
		    // draw branch and go ahead
			pushMatrix();
		    
		    rotate(-curlx);
		    line(0,0,0,-length);
		    translate(0,-length);
		    branch(length,count);
		    
		    popMatrix();
	      
		    pushMatrix();
		    
		    rotate(curly);
		    line(0,0,0,-length);
		    translate(0,-length);
		    branch(length,count);
		    
		    popMatrix();
		} else {
			// draw leafs
			colorMode(HSB,360,100,100);
			fill(colors[round(actColor+=colorsStepSize)],200);
			translate(0,-5);
			ellipse(0,0,5,10);
			colorMode(RGB,255,255,255);
		}
	}
	
	
	// -----------------------------------------------------------------
	// call back function for menu event, called by guiHud
	public void switchMirrorTherapy () {
		Iterator it = skeletons.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			Skeleton currSkel = (Skeleton)me.getValue();
			currSkel.switchMirrorTherapy(guiHud.mirrorTherapy);
		}
	}
	
	public void switchVisualisation () {
		visualisation = guiHud.visualisation;
	}
	
	
	// -----------------------------------------------------------------
	// is triggered by SimpleOpenNI on "onEndCalibration"
	// starts tracking of a Skeleton
	private void newSkeletonFound (int _userId) {
		if (skeletons.containsKey(_userId)) {
			skeletons.remove(_userId);
		}
		skeletons.put(_userId, new Skeleton(kinect,_userId,fullBodyTracking));
		skeletons.get(_userId).switchMirrorTherapy(guiHud.mirrorTherapy);
		visualisation = TherapeuticPresence.DRAW_TREE;
	}
	
	// is triggered by SimpleOpenNi on "onLostUser"
	private void skeletonLost (int _userId) {
		skeletons.remove(_userId);
		if (skeletons.isEmpty()) {
			visualisation = TherapeuticPresence.DRAW_DEPTHMAP;
		}
	}
	
	
	// -----------------------------------------------------------------
	// SimpleOpenNI user events

	public void onNewUser(int userId)
	{
		println("onNewUser - userId: " + userId);
		println("  start pose detection");
	  
		kinect.startPoseDetection("Psi",userId);
	}

	public void onLostUser(int userId)
	{
		println("onLostUser - userId: " + userId);
		this.skeletonLost(userId);
	}

	public void onStartCalibration(int userId)
	{
		println("onStartCalibration - userId: " + userId);
	}

	public void onEndCalibration(int userId, boolean successfull)
	{
		println("onEndCalibration - userId: " + userId + ", successfull: " + successfull);
	  
		if (successfull) 
		{ 
			println("  User calibrated !!!");
			kinect.startTrackingSkeleton(userId); 
			this.newSkeletonFound(userId);
		} 
		else 
		{ 
			println("  Failed to calibrate user !!!");
			println("  Start pose detection");
			kinect.startPoseDetection("Psi",userId);
		}
	}

	public void onStartPose(String pose,int userId)
	{
		println("onStartdPose - userId: " + userId + ", pose: " + pose);
		println(" stop pose detection");
	  
		kinect.stopPoseDetection(userId); 
		kinect.requestCalibrationSkeleton(userId, true);
	 
	}

	public void onEndPose(String pose,int userId)
	{
		println("onEndPose - userId: " + userId + ", pose: " + pose);
	}

	
	// Keyboard events

	public void keyPressed()
	{
		
		// get active users
		IntVector userList = new IntVector();
		kinect.getUsers(userList);
		
		switch(key)
		{
		  	// save user calibration data
			case 's':
		  		if(userList.size() < 1 ){
		  			println("There is no calibration data to save. "+userList.size()+" users active.");
			  		break;
		  		}
		  		for (int i=0; i<userList.size(); i++) {
		  			if (kinect.isTrackingSkeleton(userList.get(i))) {
		  				if(kinect.saveCalibrationDataSkeleton(userList.get(i),"calibration"+userList.get(i)+".skel"))
		  					println("Saved current calibration for user "+userList.get(i)+" to file.");      
		  				else
		  					println("Can't save calibration for user "+userList.get(i)+" to file.");      
		  			}
		  		}
		  		break;
		  	
		  	// load user calibration data
		  	case 'l':
		  		if(userList.size() < 1 ){
		  			println("There is no calibration data to load. "+userList.size()+" users active.");
			  		break;
		  		}
		  		for (int i=0; i<userList.size(); i++) {
		  			if(kinect.loadCalibrationDataSkeleton(userList.get(i),"calibration"+userList.get(i)+".skel")) {
		  				kinect.startTrackingSkeleton(userList.get(i));
		  				kinect.stopPoseDetection(userList.get(i));
		  				this.newSkeletonFound(userList.get(i));
		  				println("Loaded calibration for user "+userList.get(i)+" from file.");
		  			} else {
		  				println("Can't load calibration file for user "+userList.get(i));      
		  			}
		  		}
			    break;
			    
			case ' ':
				kinect.setMirror(!kinect.mirror());
				break;
		}
	    
		switch(keyCode)
		{
	    	case LEFT:
	    		if (keyEvent.isControlDown())
	    			translateX += 100.0f;
	    		else
	    			rotX += 100.0f;
	    		break;
	    	case RIGHT:
	    		if (keyEvent.isControlDown())
	    			translateX -= 100.0f;
	    		else
	    			rotX -= 100.0f;
	    		break;
	    	case UP:
	    		if (keyEvent.isControlDown()) {
	    			if(keyEvent.isShiftDown())
	    				translateZ -= 100.0f;
	    			else
	    				translateY += 100.0f;
	    		} else {
	    			if(keyEvent.isShiftDown())
	    				rotZ += 100.0f;
	    			else
	    				rotY += 100.0f;
	    		}
	    		break;
	    	case DOWN:
	    		if (keyEvent.isControlDown()) {
	    			if(keyEvent.isShiftDown())
	    				translateZ += 100.0f;
	    			else
	    				translateY -= 100.0f;
	    		} else {
	    			if(keyEvent.isShiftDown())
	    				rotZ -= 100.0f;
	    			else
	    				rotY -= 100.0f;
	    			break;
	    		}
	    	
		}
	}
	
	
	public static void main (String args[]) {
		PApplet.main(new String[] {"--present","therapeuticpresence.TherapeuticPresence"});
	}
	
}
