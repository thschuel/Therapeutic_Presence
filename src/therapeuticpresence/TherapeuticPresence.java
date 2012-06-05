package therapeuticpresence;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import processing.core.*;
import generativedesign.*;
import SimpleOpenNI.*;
import processing.opengl.*;
import ddf.minim.*;

public class TherapeuticPresence extends PApplet {

	private static final long serialVersionUID = 1L;
	
	// --- setup constants ---
	public static final short MAX_USER = 4;
	public static final short DRAW_SKELETONS = 1;
	public static final short DRAW_DEPTHMAP = 2;
	public static final short DRAW_TREE = 3;
	public static final short DRAW_AUDIOSKELETONS = 4;
	public static final short MIRROR_OFF = 0;
	public static final short MIRROR_LEFT = 1;
	public static final short MIRROR_RIGHT = 2;
	
	// --- static setup ---
	public static boolean fullBodyTracking = false; // control for full body tracking
	public static boolean recordFlag = true; // set to false for playback
	public static boolean debugOutput = true;
	
	// --- setup variables ---
	public short visualisation = TherapeuticPresence.DRAW_DEPTHMAP;
	public short mirrorTherapy = TherapeuticPresence.MIRROR_OFF;
	public boolean drawCamFrustrum = false; // control drawing of the kinect camera
	public boolean autoCalibration = false; // control for auto calibration of skeleton
	
	// --- interfaces to other modules ---
	// interface to talk to kinect
	protected SimpleOpenNI kinect;
	
	// interfaces to use audio visualisation
	protected Minim minim;
	protected AudioPlayer audioPlayer;
	protected AudioVisuals audioVisuals;
	
	// the skeletons that control the scene, only one user for now
	protected HashMap<Integer,Skeleton> skeletons = new HashMap<Integer,Skeleton>();
		
	// user interface
	protected GuiHud guiHud;
	
	// --- internal variables ---
	// setup scene, values can be controlled by user input
	private float translateX = 0.0f;
	private float translateY = 0.0f;
	private float translateZ = 0.0f;
	private float rotX = radians(0);  // by default rotate the hole scene 180deg around the x-axis, the data from openni comes upside down
	private float rotY = radians(0);
	private float rotZ = radians(180);

	// calculate colors for trace from image
	private int[] colors; 
	private PImage img;
	private float actColor = 0.f;
	private int	colorsSize;
	private float colorsStepSize = 10.f;
	
	// variables to calculate and draw the tree
	private float curlx = 0;
	private float curly = 0;
	private float f = sqrt(2)/2.f;
	private float delay = 20;
	private float growth = 0;
	private float growthTarget = 0;
	private int branches = 17;
	
	
	// -----------------------------------------------------------------
	public void setup() {		
		size(screenWidth-2,screenHeight-100,OPENGL);
		
		// establish connection to kinect/openni
		setupKinect();
		
		// setup Audio Controls
		setupAudio();
		
		// build color set
		calculateColorSet();

		// generate HUD
		guiHud = new GuiHud(this);
		  
	}
	
	private void setupKinect () {
		kinect = new SimpleOpenNI(this);
		
		if (recordFlag) {
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
		} else {
			// playback
			kinect.openFileRecording("./data/test.oni");
			//kinect.enableScene();
		}
	}
	
	private void setupAudio () {
		minim = new Minim(this);
		audioPlayer = minim.loadFile("../data/moan.mp3",1024);
		audioPlayer.loop();
		audioVisuals = new AudioVisuals(this);
		audioPlayer.addListener(audioVisuals);
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
				
			case TherapeuticPresence.DRAW_AUDIOSKELETONS:
				drawAudioSkeletons();
				break;
			
			default:
				drawDepthMap();
				break;
		}
		// draw the HUD
		guiHud.draw();

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

		// draw the camera
		if (drawCamFrustrum) kinect.drawCamFrustum();
		popMatrix();
	} 
	
	private void drawSkeletons () {
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
					
		Iterator it = skeletons.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			int currUserId = (Integer)me.getKey();
			Skeleton currSkel = (Skeleton)me.getValue();
			
			if (kinect.isTrackingSkeleton(currUserId)) {
				currSkel.drawSkeleton(color(0,255,255));
				currSkel.drawJointOrientations(100f);
				currSkel.drawJoints(20f,color(0,0,255));
				currSkel.drawMirrorPlane(color(100,100,100));
//				float angleLeftArm = currSkel.angleBetween(Skeleton.LEFT_ELBOW,Skeleton.LEFT_HAND,Skeleton.NECK,Skeleton.TORSO);
//				guiHud.sendGuiMessage("Angle lArm to Body Axis: "+guiHud.df.format(angleLeftArm));
//				float angleBodyAxis = currSkel.angleToUpAxis(Skeleton.NECK,Skeleton.TORSO);
//				guiHud.sendGuiMessage("Angle Body to Up Axis: "+guiHud.df.format(angleBodyAxis));
//				float distance = currSkel.distanceToKinect();
//				guiHud.sendGuiMessage("Distance of Skeleton: "+guiHud.df.format(distance));
			}
		}

		// draw the camera
		if (drawCamFrustrum) kinect.drawCamFrustum();
		popMatrix();
	}
	
	public void drawAudioSkeletons () {
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
		// iterate through all skeletons in the scene			
		Iterator it = skeletons.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			int currUserId = (Integer)me.getKey();
			Skeleton currSkel = (Skeleton)me.getValue();
			if (kinect.isTrackingSkeleton(currUserId)) {
				// draw the skeleton
				currSkel.drawSkeleton(color(170,170,170));
				//currSkel.drawJoints(10f,color(50,50,50));
				audioVisuals.draw(currSkel,color(50,20,200));
			}
		}
		// draw the kinect cam
		if (drawCamFrustrum) kinect.drawCamFrustum();
		popMatrix();
	}
	
	private void drawTree () {
		background(250);
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
	// is triggered by SimpleOpenNI on "onEndCalibration"
	// starts tracking of a Skeleton
	private void newSkeletonFound (int _userId) {
		if (skeletons.containsKey(_userId)) {
			skeletons.remove(_userId);
		}
		skeletons.put(_userId, new Skeleton(kinect,_userId,this));
		visualisation = TherapeuticPresence.DRAW_AUDIOSKELETONS;
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
		guiHud.sendGuiMessage("onNewUser - userId: " + userId);
		guiHud.sendGuiMessage("  start pose detection");
	  

		if(autoCalibration)
		    kinect.requestCalibrationSkeleton(userId,true);
		else    
		    kinect.startPoseDetection("Psi",userId);
	}

	public void onLostUser(int userId)
	{
		guiHud.sendGuiMessage("onLostUser - userId: " + userId);
		this.skeletonLost(userId);
	}

	public void onStartCalibration(int userId)
	{
		guiHud.sendGuiMessage("onStartCalibration - userId: " + userId);
	}

	public void onEndCalibration(int userId, boolean successfull)
	{
		guiHud.sendGuiMessage("onEndCalibration - userId: " + userId + ", successfull: " + successfull);
	  
		if (successfull) 
		{ 
			guiHud.sendGuiMessage("  User calibrated !!!");
			kinect.startTrackingSkeleton(userId); 
			this.newSkeletonFound(userId);
		} 
		else 
		{ 
			guiHud.sendGuiMessage("  Failed to calibrate user !!!");
			guiHud.sendGuiMessage("  Start pose detection");
			kinect.startPoseDetection("Psi",userId);
		}
	}

	public void onStartPose(String pose,int userId)
	{
		guiHud.sendGuiMessage("onStartdPose - userId: " + userId + ", pose: " + pose);
		guiHud.sendGuiMessage(" stop pose detection");
	  
		kinect.stopPoseDetection(userId); 
		kinect.requestCalibrationSkeleton(userId, true);
	 
	}

	public void onEndPose(String pose,int userId)
	{
		guiHud.sendGuiMessage("onEndPose - userId: " + userId + ", pose: " + pose);
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
			case 'o':
		  		if(userList.size() < 1 ){
		  			guiHud.sendGuiMessage("There is no calibration data to save. "+userList.size()+" users active.");
			  		break;
		  		}
		  		for (int i=0; i<userList.size(); i++) {
		  			if (kinect.isTrackingSkeleton(userList.get(i))) {
		  				if(kinect.saveCalibrationDataSkeleton(userList.get(i),"calibration"+userList.get(i)+".skel"))
		  					guiHud.sendGuiMessage("Saved current calibration for user "+userList.get(i)+" to file.");      
		  				else
		  					guiHud.sendGuiMessage("Can't save calibration for user "+userList.get(i)+" to file.");      
		  			}
		  		}
		  		break;
		  	
		  	// load user calibration data
		  	case 'l':
		  		if(userList.size() < 1 ){
		  			guiHud.sendGuiMessage("There is no calibration data to load. "+userList.size()+" users active.");
			  		break;
		  		}
		  		for (int i=0; i<userList.size(); i++) {
		  			if(kinect.loadCalibrationDataSkeleton(userList.get(i),"calibration"+userList.get(i)+".skel")) {
		  				kinect.startTrackingSkeleton(userList.get(i));
		  				kinect.stopPoseDetection(userList.get(i));
		  				this.newSkeletonFound(userList.get(i));
		  				guiHud.sendGuiMessage("Loaded calibration for user "+userList.get(i)+" from file.");
		  			} else {
		  				guiHud.sendGuiMessage("Can't load calibration file for user "+userList.get(i));      
		  			}
		  		}
			    break;
			    
			case 'm':
				kinect.setMirror(!kinect.mirror());
				break;
				
			case 'w':
				translateZ -= 100.0f;
				break;
				
			case 'W':
				translateY -= 100.0f;
				break;
				
			case 'a':
				translateX += 100.0f;
				break;
				
			case 's':
				translateZ += 100.0f;
				break;
				
			case 'S':
				translateY += 100.0f;
				break;
				
			case 'd':
				translateX -= 100.0f;
				break;
		}
	    
		switch(keyCode)
		{
	    	case LEFT:
	    		rotY += 100.0f;
	    		break;
	    	case RIGHT:
	    		rotY -= 100.0f;
	    		break;
	    	case UP:
    			if(keyEvent.isShiftDown())
    				rotZ += 100.0f;
    			else
    				rotX += 100.0f;
	    		break;
	    	case DOWN:
    			if(keyEvent.isShiftDown())
    				rotZ -= 100.0f;
    			else
    				rotX -= 100.0f;
    			break;
		}
	}
	
	
	public static void main (String args[]) {
		PApplet.main(new String[] {"--present","therapeuticpresence.TherapeuticPresence"});
	}
	
}
