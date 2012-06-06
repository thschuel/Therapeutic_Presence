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
	public static final short MAX_USERS = 4;
	public static final short DRAW_SKELETON = 1;
	public static final short DRAW_DEPTHMAP = 2;
	public static final short DRAW_TREE = 3;
	public static final short DRAW_AUDIOSKELETONS = 4;
	public static final short MIRROR_OFF = 0;
	public static final short MIRROR_LEFT = 1;
	public static final short MIRROR_RIGHT = 2;
	
	// --- static setup variables ---
	public static boolean fullBodyTracking = false; // control for full body tracking
	public static boolean recordFlag = true; // set to false for playback
	public static boolean debugOutput = true;
	public static short visualisationMethod = TherapeuticPresence.DRAW_DEPTHMAP;
	public static short mirrorTherapy = TherapeuticPresence.MIRROR_OFF;
	public static boolean drawCamFrustum = false; // control drawing of the kinect camera
	public static boolean autoCalibration = false; // control for auto calibration of skeleton
	
	// --- interfaces to other modules ---
	// interface to talk to kinect
	protected SimpleOpenNI kinect;
	
	// interface to the chosen visualisation object
	protected Visualisation visualisation = null;
	
	// the skeleton that control the scene, only one user for now
	protected Skeleton skeleton = null;
		
	// user interface
	protected GuiHud guiHud;
	
	
	// -----------------------------------------------------------------
	public void setup() {		
		size(screenWidth-2,screenHeight-100,OPENGL);
		
		// establish connection to kinect/openni
		setupKinect();

		// start visualisation (default is depthMap)
		setupVisualisation();
		
		// generate HUD
		guiHud = new GuiHud(this);
		  
	}
	
	private void setupKinect () {
		kinect = new SimpleOpenNI(this);
		
		if (TherapeuticPresence.recordFlag) {
			// enable/disable mirror
			kinect.setMirror(true);
			// enable depthMap generation 
			kinect.enableDepth();
			if (TherapeuticPresence.fullBodyTracking) {
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
	
	public void setupVisualisation () {
		switch (TherapeuticPresence.visualisationMethod) {
			case TherapeuticPresence.DRAW_SKELETON:
				visualisation = new StickfigureVisualisation(this,skeleton);
				visualisation.setup();
				break;
				
			case TherapeuticPresence.DRAW_TREE:
				visualisation = new GenerativeTreeVisualisation(this,skeleton);
				visualisation.setup();
				break;
				
			case TherapeuticPresence.DRAW_AUDIOSKELETONS:
				visualisation = new AudioVisuals(this,skeleton);
				visualisation.setup();
				break;
			
			default:
				visualisation = new DepthMapVisualisation(this,kinect);
				visualisation.setup();
				break;
		}
	}
	
	// -----------------------------------------------------------------
	public void draw() {
		// -------- update status --------------------------
		if (kinect != null)
			kinect.update();
		if (skeleton != null && kinect.isTrackingSkeleton(skeleton.userId))
			skeleton.updateSkeleton();
		
		// -------- drawing --------------------------------
		if (visualisation != null)
			visualisation.draw();
		
		// draw the camera
		if (TherapeuticPresence.drawCamFrustum) 
			kinect.drawCamFrustum();
		
		if (guiHud != null)
			guiHud.draw();

	}

	
	// -----------------------------------------------------------------
	// is triggered by SimpleOpenNI on "onEndCalibration" and by user on "loadCalibration"
	// starts tracking of a Skeleton
	private void newSkeletonFound (int _userId) {
		if (_userId < 0 || _userId > TherapeuticPresence.MAX_USERS) {
			guiHud.sendGuiMessage("newSkeletonFound: User id "+_userId+" outside range. Maximum users: "+TherapeuticPresence.MAX_USERS);
		} else {
			if (skeleton != null ) {
				guiHud.sendGuiMessage("Skeleton of user "+skeleton.userId+" replaced with skeleton of user "+_userId+"!");
			}
			skeleton = new Skeleton(kinect,_userId);
			TherapeuticPresence.visualisationMethod = TherapeuticPresence.DRAW_SKELETON;
			setupVisualisation();
		}
	}
	
	// is triggered by SimpleOpenNi on "onLostUser"
	private void skeletonLost (int _userId) {
		if (_userId < 0 || _userId > TherapeuticPresence.MAX_USERS) {
			guiHud.sendGuiMessage("skeletonLost: User id "+_userId+" outside range. Maximum users: "+TherapeuticPresence.MAX_USERS);
		} else {
			skeleton = null;
			TherapeuticPresence.visualisationMethod = TherapeuticPresence.DRAW_DEPTHMAP;
			setupVisualisation();
		}
	}
	
	
	// -----------------------------------------------------------------
	// SimpleOpenNI user events
	public void onNewUser(int userId)
	{
		guiHud.sendGuiMessage("onNewUser - userId: " + userId);
		guiHud.sendGuiMessage("  start pose detection");
	  

		if(TherapeuticPresence.autoCalibration)
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
		switch(key)
		{
		  	// save user calibration data
			case 'o':
		  		if(skeleton != null && kinect.isTrackingSkeleton(skeleton.userId)){
		  			if(kinect.saveCalibrationDataSkeleton(skeleton.userId,"../data/calibration"+skeleton.userId+".skel"))
	  					guiHud.sendGuiMessage("Saved current calibration for user "+skeleton.userId+" to file.");      
	  				else
	  					guiHud.sendGuiMessage("Can't save calibration for user "+skeleton.userId+" to file.");
		  		} else {
		  			guiHud.sendGuiMessage("There is no calibration data to save. No skeleton found.");
			  	}
		  		break;
		  	
		  	// load user calibration data
		  	case 'l':
		  		IntVector userList = new IntVector();
		  		kinect.getUsers(userList);
		  		if (userList.size() > 0) {
			  		if(kinect.loadCalibrationDataSkeleton(userList.get(0),"../data/calibration"+userList.get(0)+".skel")) {
		  				kinect.startTrackingSkeleton(userList.get(0));
		  				kinect.stopPoseDetection(userList.get(0));
		  				this.newSkeletonFound(userList.get(0));
		  				guiHud.sendGuiMessage("Loaded calibration for user "+userList.get(0)+" from file.");
		  			} else {
		  				guiHud.sendGuiMessage("Can't load calibration file for user "+userList.get(0));      
		  			}
		  		} else {
		  			guiHud.sendGuiMessage("No calibration data loaded. You need at least one active user!");
		  		}
			    break;
			    
			case 'm':
				kinect.setMirror(!kinect.mirror());
				break;
				
			case 'w':
				visualisation.translateZ -= 100.0f;
				break;
				
			case 'W':
				visualisation.translateY -= 100.0f;
				break;
				
			case 'a':
				visualisation.translateX += 100.0f;
				break;
				
			case 's':
				visualisation.translateZ += 100.0f;
				break;
				
			case 'S':
				visualisation.translateY += 100.0f;
				break;
				
			case 'd':
				visualisation.translateX -= 100.0f;
				break;
		}
	    
		switch(keyCode)
		{
	    	case LEFT:
	    		visualisation.rotY += 100.0f;
	    		break;
	    	case RIGHT:
	    		visualisation.rotY -= 100.0f;
	    		break;
	    	case UP:
    			if(keyEvent.isShiftDown())
    				visualisation.rotZ += 100.0f;
    			else
    				visualisation.rotX += 100.0f;
	    		break;
	    	case DOWN:
    			if(keyEvent.isShiftDown())
    				visualisation.rotZ -= 100.0f;
    			else
    				visualisation.rotX -= 100.0f;
    			break;
		}
	}
	
	
	public static void main (String args[]) {
		PApplet.main(new String[] {"--present","therapeuticpresence.TherapeuticPresence"});
	}
	
}
