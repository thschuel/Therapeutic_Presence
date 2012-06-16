package therapeuticpresence;

import processing.core.*;
import SimpleOpenNI.*;
import processing.opengl.*;

/* The main application class. 
 * TherapeuticPresence maintains interfaces to 
 *   the kinect
 *   the skeleton in the scene
 *   the active visualisation
 *   the gui
 * It also contains the basic setup variables. It handles keyboard events.
 * Workflow is
 *   setting up the kinect and the gui according to setup variables
 *   set up depth map visualisation
 *   wait for user to enter the scene
 *   calibrate user (automatic or pose)
 *   set up skeleton from kinect data (skeleton updates itself)
 *   set up chosen visualisation and play
 *   wait for user input
 */
public class TherapeuticPresence extends PApplet {

	private static final long serialVersionUID = 1L;
	
	// --- setup constants ---
	public static final short MAX_USERS = 4;
	public static final short DRAW_SKELETON = 1;
	public static final short DRAW_DEPTHMAP = 2;
	public static final short DRAW_TREE = 3;
	public static final short DRAW_AUDIOSKELETON = 4;
	public static final short MIRROR_OFF = 0;
	public static final short MIRROR_LEFT = 1;
	public static final short MIRROR_RIGHT = 2;
	
	// --- static setup variables ---
	public static boolean fullBodyTracking = false; // control for full body tracking
	public static boolean recordFlag = true; // set to false for playback
	public static boolean debugOutput = true;
	public static short visualisationMethod = TherapeuticPresence.DRAW_DEPTHMAP;
	public static short mirrorTherapy = TherapeuticPresence.MIRROR_OFF;
	public static boolean autoCalibration = true; // control for auto calibration of skeleton
	
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
			if (kinect.openFileRecording("../data/test.oni") == false) {
				println("fehler");
			}
			//skeleton = new Skeleton(kinect,1);
		}
	}
	
	public void setupVisualisation () {
		this.setupVisualisation(TherapeuticPresence.visualisationMethod);
	}
	
	public void setupVisualisation (short _visualisationMethod) {
		TherapeuticPresence.visualisationMethod = _visualisationMethod;
		switch (TherapeuticPresence.visualisationMethod) {
			case TherapeuticPresence.DRAW_SKELETON:
				visualisation = new StickfigureVisualisation(this,skeleton);
				visualisation.setup();
				break;
				
			case TherapeuticPresence.DRAW_TREE:
				visualisation = new GenerativeTreeVisualisation(this,skeleton);
				visualisation.setup();
				break;
				
			case TherapeuticPresence.DRAW_AUDIOSKELETON:
				visualisation = new AudioVisualisation(this,skeleton);
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
		
		if (guiHud != null)
			guiHud.draw();

	}
	
	public void debugMessage (String _message) {
		if (TherapeuticPresence.debugOutput) {
			guiHud.sendGuiMessage(_message);
		} else {
			// TODO: write text-file output
		}
	}

	
	// -----------------------------------------------------------------
	// is triggered by SimpleOpenNI on "onEndCalibration" and by user on "loadCalibration"
	// starts tracking of a Skeleton
	private void newSkeletonFound (int _userId) {
		if (_userId < 0 || _userId > TherapeuticPresence.MAX_USERS) {
			debugMessage("newSkeletonFound: User id "+_userId+" outside range. Maximum users: "+TherapeuticPresence.MAX_USERS);
		} else {
			if (skeleton != null ) {
				debugMessage("Skeleton of user "+skeleton.userId+" replaced with skeleton of user "+_userId+"!");
			}
			skeleton = new Skeleton(kinect,_userId);
			setupVisualisation(TherapeuticPresence.DRAW_AUDIOSKELETON);
		}
	}
	
	// is triggered by SimpleOpenNi on "onLostUser"
	private void skeletonLost (int _userId) {
		if (_userId < 0 || _userId > TherapeuticPresence.MAX_USERS) {
			debugMessage("skeletonLost: User id "+_userId+" outside range. Maximum users: "+TherapeuticPresence.MAX_USERS);
		} else {
			skeleton = null;
			setupVisualisation(TherapeuticPresence.DRAW_DEPTHMAP);
		}
	}
	
	
	// -----------------------------------------------------------------
	// SimpleOpenNI user events
	public void onNewUser(int userId) {
		debugMessage("New User "+userId+" entered the scene.");
		if (skeleton == null) {
			debugMessage("  start pose detection");
			if(TherapeuticPresence.autoCalibration) kinect.requestCalibrationSkeleton(userId,true);
			else kinect.startPoseDetection("Psi",userId);
		} else {
			debugMessage("  no pose detection, skeleton is already tracked");
		}
	}
	public void onLostUser(int userId) {
		debugMessage("onLostUser - userId: " + userId);
		this.skeletonLost(userId);
	}
	public void onStartCalibration(int userId) {
		debugMessage("onStartCalibration - userId: " + userId);
	}
	public void onEndCalibration(int userId, boolean successfull) {
		debugMessage("onEndCalibration - userId: " + userId + ", successfull: " + successfull);
		if (successfull) { 
			debugMessage("  User calibrated !!!");
			kinect.startTrackingSkeleton(userId); 
			this.newSkeletonFound(userId);
		} else { 
			debugMessage("  Failed to calibrate user !!!");
			debugMessage("  Start pose detection");
			kinect.startPoseDetection("Psi",userId);
		}
	}
	public void onStartPose(String pose,int userId) {
		debugMessage("onStartdPose - userId: " + userId + ", pose: " + pose);
		debugMessage(" stop pose detection");
		kinect.stopPoseDetection(userId); 
		kinect.requestCalibrationSkeleton(userId, true);
	}
	public void onEndPose(String pose,int userId) {
		debugMessage("onEndPose - userId: " + userId + ", pose: " + pose);
	}

	
	// Keyboard events
	public void keyPressed() {
		switch(key)
		{
		  	// save user calibration data
			case 'o':
		  		if(skeleton != null && kinect.isTrackingSkeleton(skeleton.userId)){
		  			if(kinect.saveCalibrationDataSkeleton(skeleton.userId,"../data/calibration"+skeleton.userId+".skel"))
	  					debugMessage("Saved current calibration for user "+skeleton.userId+" to file.");      
	  				else
	  					debugMessage("Can't save calibration for user "+skeleton.userId+" to file.");
		  		} else {
		  			debugMessage("There is no calibration data to save. No skeleton found.");
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
		  				debugMessage("Loaded calibration for user "+userList.get(0)+" from file.");
		  			} else {
		  				debugMessage("Can't load calibration file for user "+userList.get(0));      
		  			}
		  		} else {
		  			debugMessage("No calibration data loaded. You need at least one active user!");
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
	    
		switch(keyCode) {
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
