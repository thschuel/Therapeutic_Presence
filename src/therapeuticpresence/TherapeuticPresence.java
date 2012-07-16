package therapeuticpresence;

import javax.media.opengl.GL;
import therapeuticskeleton.Skeleton;
import processing.core.*;
import SimpleOpenNI.*;
import processing.opengl.*;
import scenes.*;
import visualisations.*;

/* The main application class. 
 * TherapeuticPresence maintains interfaces to 
 *   the kinect
 *   the scene
 *   the skeleton in the scene
 *   the active visualisation
 *   the gui
 * It also contains the basic setup variables. It handles keyboard events.
 * Workflow is
 *   setting up the kinect and the gui according to setup variables
 *   set up a basic 3d scene
 *   set up depth map visualisation
 *   wait for user to enter the scene
 *   calibrate user (automatic or pose)
 *   set up skeleton from kinect data (skeleton updates itself)
 *   set up chosen scene type
 *   set up chosen visualisation and play
 *   wait for user input
 */
public class TherapeuticPresence extends PApplet {

	private static final long serialVersionUID = 1L;
	
	// --- setup constants ---
	public static final short MAX_USERS = 4;
	public static final short DEPTHMAP_VISUALISATION = 0;
	public static final short STICKFIGURE_VISUALISATION = 1;
	public static final short GENERATIVE_TREE_2D_VISUALISATION = 2;
	public static final short GENERATIVE_TREE_3D_VISUALISATION = 3;
	public static final short GEOMETRY_2D_VISUALISATION = 4;
	public static final short GEOMETRY_3D_VISUALISATION = 5;
	public static final short BASIC_SCENE2D = 0;
	public static final short BASIC_SCENE3D = 1;
	public static final short TUNNEL_SCENE2D = 2;
	public static final short TUNNEL_SCENE3D = 3;

	
	// --- static setup variables ---
	public static boolean fullBodyTracking = false; // control for full body tracking
	public static boolean calculateLocalCoordSys = true; // control for full body tracking
	public static boolean recordFlag = true; // set to false for playback
	public static boolean debugOutput = true;
	public static short initialVisualisationMethod = TherapeuticPresence.DEPTHMAP_VISUALISATION;
	public static short defaultVisualisationMethod = TherapeuticPresence.GEOMETRY_3D_VISUALISATION;
	public static short currentVisualisationMethod;
	public static short initialSceneType = TherapeuticPresence.BASIC_SCENE3D;
	public static short defaultSceneType = TherapeuticPresence.TUNNEL_SCENE3D;
	public static short currentSceneType;
	public static short mirrorTherapy = Skeleton.MIRROR_THERAPY_OFF;
	public static boolean autoCalibration = true; // control for auto calibration of skeleton
	public static boolean mirrorKinect = false;
	public static float maxDistanceToKinect = 2500f; // in mm 
	public static float lowerZBoundary = 0.45f*maxDistanceToKinect; // to control z position of drawing within a narrow corridor
	public static float upperZBoundary = 0.78f*maxDistanceToKinect;
	
	// --- interfaces to other modules ---
	// interface to talk to kinect
	protected SimpleOpenNI kinect = null;
	// interface to the Scene/Background
	protected AbstractScene scene = null;
	// interface to the chosen visualisation object
	protected AbstractVisualisation nextVisualisation = null;
	protected AbstractVisualisation visualisation = null;
	protected AbstractVisualisation lastVisualisation = null;
	// the skeleton that control the scene, only one user for now
	protected Skeleton skeleton = null;
	// user interface
	protected GuiHud guiHud = null;
	// audio interface
	protected AudioManager audioManager = null;
	// posture processing for skeleton interface
	protected PostureProcessing postureProcessing = null;
	
	
	// -----------------------------------------------------------------
	public void setup() {		
		size(screenWidth-16,screenHeight-128,OPENGL);
		
		// establish connection to kinect/openni
		setupKinect();
		  
		// start the audio interface
		audioManager = new AudioManager(this);
		audioManager.setup();
		audioManager.start();
		
		// setup Scene
		setupScene(TherapeuticPresence.initialSceneType);

		// start visualisation (default is depthMap)
		setupVisualisation(TherapeuticPresence.initialVisualisationMethod);
		
		// generate HUD
		guiHud = new GuiHud(this);
	}
	
	private void setupKinect () {
		kinect = new SimpleOpenNI(this);
		
		if (TherapeuticPresence.recordFlag) {
			// enable/disable mirror
			kinect.setMirror(mirrorKinect);
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
		}
	}

	public void setMirrorKinect (boolean _mirrorKinect) {
//		if (skeleton != null) {
//			kinect.stopTrackingSkeleton(skeleton.userId);
//		}
//		mirrorKinect = _mirrorKinect;
//		kinect.setMirror(mirrorKinect);
//		kinect.update();
//		if (skeleton != null) {
//			kinect.startTrackingSkeleton(skeleton.userId);
//		}
	}
	
	public void setupScene (short _sceneType) {
		switch (_sceneType) {
			case TherapeuticPresence.BASIC_SCENE2D:
				scene = new BasicScene2D(this,color(0,0,0));
				scene.reset();
				currentSceneType = TherapeuticPresence.BASIC_SCENE2D;
				break;
				
			case TherapeuticPresence.TUNNEL_SCENE2D:
				if (audioManager != null) {
					scene = new TunnelScene2D(this,color(0,0,0),audioManager);
					scene.reset();
					currentSceneType = TherapeuticPresence.TUNNEL_SCENE2D;
				} else {
					setupScene(TherapeuticPresence.BASIC_SCENE2D);
					debugMessage("setupScene(short): AudioManager needed for Tunnel Scene!");
				}
				break;
				
			case TherapeuticPresence.TUNNEL_SCENE3D:
				if (audioManager != null) {
					scene = new TunnelScene3D(this,color(0,0,0),audioManager);
					scene.reset();
					currentSceneType = TherapeuticPresence.TUNNEL_SCENE3D;
				} else {
					setupScene(TherapeuticPresence.BASIC_SCENE3D);
					debugMessage("setupScene(short): AudioManager needed for Tunnel Scene!");
				}
				break;
			
			default:
				scene = new BasicScene3D(this,color(0,0,0));
				scene.reset();
				currentSceneType = TherapeuticPresence.BASIC_SCENE3D;
				break;
		}
	}
	
	public void setupVisualisation (short _visualisationMethod) {
		
		// prepare for fade out
		lastVisualisation = visualisation;
		visualisation = null;
		
		// set up next visualisation for fade in
		switch (_visualisationMethod) {
			case TherapeuticPresence.STICKFIGURE_VISUALISATION:
				nextVisualisation = new StickfigureVisualisation(this,skeleton);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.STICKFIGURE_VISUALISATION;
				break;
				
			case TherapeuticPresence.GENERATIVE_TREE_2D_VISUALISATION:
				nextVisualisation = new GenerativeTree2DVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.GENERATIVE_TREE_2D_VISUALISATION;
				break;
				
			case TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION:
				nextVisualisation = new GenerativeTree3DVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION;
				break;
				
			case TherapeuticPresence.GEOMETRY_2D_VISUALISATION:
				nextVisualisation = new Geometry2DVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.GEOMETRY_2D_VISUALISATION;
				break;
				
			case TherapeuticPresence.GEOMETRY_3D_VISUALISATION:
				nextVisualisation = new Geometry3DVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.GEOMETRY_3D_VISUALISATION;
				break;
			
			default:
				nextVisualisation = new DepthMapVisualisation(this,kinect);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.DEPTHMAP_VISUALISATION;
				break;
		}
	}
	
	// -----------------------------------------------------------------
	public void draw() {
		// -------- update status --------------------------
		if (kinect != null) {
			kinect.update();
		}
		if (skeleton != null && kinect.isTrackingSkeleton(skeleton.getUserId())) {
			skeleton.update();
		}
		if (audioManager != null) {
			audioManager.update();
		}
		if (postureProcessing != null) {
			postureProcessing.updatePosture();
			postureProcessing.triggerAction();
		}
		
		// -------- drawing --------------------------------
		if (scene != null) {
			scene.reset();
		}
		
		if (lastVisualisation != null) {
			if (lastVisualisation.fadeOut()) { // true when visualisation has done fade out
				lastVisualisation = null;
			}
		}
		if (nextVisualisation != null) {
			if (nextVisualisation.fadeIn()) { // true when visualisation has done fade in
				visualisation = nextVisualisation;
				nextVisualisation = null;
			}
		}
		if (visualisation != null) { 
			visualisation.draw();
		}
		
		if (guiHud != null) {
			guiHud.draw();
		}

	}
	
	public void debugMessage (String _message) {
		guiHud.sendGuiMessage(_message);
		println(_message);
	}

	
	// -----------------------------------------------------------------
	// is triggered by SimpleOpenNI on "onEndCalibration" and by user on "loadCalibration"
	// starts tracking of a Skeleton
	private void newSkeletonFound (int _userId) {
		if (_userId < 0 || _userId > TherapeuticPresence.MAX_USERS) {
			debugMessage("newSkeletonFound: User id "+_userId+" outside range. Maximum users: "+TherapeuticPresence.MAX_USERS);
		} else {
			if (skeleton != null ) {
				debugMessage("Skeleton of user "+skeleton.getUserId()+" replaced with skeleton of user "+_userId+"!");
			}
			skeleton = new Skeleton(kinect,_userId,fullBodyTracking,calculateLocalCoordSys,mirrorTherapy);
			// start default scene and visualisation
			setupScene(defaultSceneType);
			setupVisualisation(defaultVisualisationMethod);
			postureProcessing = new PostureProcessing(this,skeleton);
		}
	}
	
	// is triggered by SimpleOpenNi on "onLostUser"
	private void skeletonLost (int _userId) {
		if (_userId < 0 || _userId > TherapeuticPresence.MAX_USERS) {
			debugMessage("skeletonLost: User id "+_userId+" outside range. Maximum users: "+TherapeuticPresence.MAX_USERS);
		} else {
			skeleton = null;
			setupScene(initialSceneType);
			setupVisualisation(initialVisualisationMethod);
			int[] users = kinect.getUsers();
			if (users.length!=0) {
				if(TherapeuticPresence.autoCalibration) kinect.requestCalibrationSkeleton(users[0],true);
				else kinect.startPoseDetection("Psi",users[0]);
			}
		}
	}
	
	// call back for guihud
	public void switchMirrorTherapy (short _mirrorTherapy) {
		if (_mirrorTherapy >= Skeleton.MIRROR_THERAPY_OFF && _mirrorTherapy <= Skeleton.MIRROR_THERAPY_RIGHT) 
			mirrorTherapy = _mirrorTherapy;
		else
			mirrorTherapy = Skeleton.MIRROR_THERAPY_OFF;
		
		if (skeleton != null) 
			skeleton.setMirrorTherapy(mirrorTherapy);
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
			kinect.startTrackingSkeleton(skeleton.getUserId());
		}
	}
	public void onLostUser(int userId) {
		debugMessage("onLostUser - userId: " + userId);
		if (userId == skeleton.getUserId()) {
			this.skeletonLost(userId);
		}
	}
	public void onStartCalibration(int userId) {
		debugMessage("onStartCalibration - userId: " + userId);
	}
	public void onEndCalibration(int userId, boolean successfull) {
		debugMessage("onEndCalibration - userId: " + userId + ", successfull: " + successfull);
		if (successfull) { 
			debugMessage("  User calibrated !!!");
			kinect.startTrackingSkeleton(userId); 
			//kinect.update(); // one update loop before going on with calculations prevents kinect to be in an unstable state
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
		switch(key) {
		  	// save user calibration data
			case 'o':
		  		if(skeleton != null && kinect.isTrackingSkeleton(skeleton.getUserId())){
		  			if(kinect.saveCalibrationDataSkeleton(skeleton.getUserId(),"../data/calibration"+skeleton.getUserId()+".skel"))
	  					debugMessage("Saved current calibration for user "+skeleton.getUserId()+" to file.");      
	  				else
	  					debugMessage("Can't save calibration for user "+skeleton.getUserId()+" to file.");
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
				setMirrorKinect(!mirrorKinect);
				break;
				
		}
		
		if (scene.sceneIs3D()) {
			switch (key) {
				case 'w':
					((BasicScene3D)scene).translateZ -= 100.0f;
					break;
					
				case 'W':
					((BasicScene3D)scene).translateY -= 100.0f;
					break;
					
				case 'a':
					((BasicScene3D)scene).translateX += 100.0f;
					break;
					
				case 's':
					((BasicScene3D)scene).translateZ += 100.0f;
					break;
					
				case 'S':
					((BasicScene3D)scene).translateY += 100.0f;
					break;
					
				case 'd':
					((BasicScene3D)scene).translateX -= 100.0f;
					break;
			}
			switch(keyCode) {
		    	case LEFT:
		    		((BasicScene3D)scene).rotY += 100.0f;
		    		break;
		    	case RIGHT:
		    		((BasicScene3D)scene).rotY -= 100.0f;
		    		break;
		    	case UP:
	    			if(keyEvent.isShiftDown())
	    				((BasicScene3D)scene).rotZ += 100.0f;
	    			else
	    				((BasicScene3D)scene).rotX += 100.0f;
		    		break;
		    	case DOWN:
	    			if(keyEvent.isShiftDown())
	    				((BasicScene3D)scene).rotZ -= 100.0f;
	    			else
	    				((BasicScene3D)scene).rotX -= 100.0f;
	    			break;
			}
		}
	}
	
	
	public static void main (String args[]) {
		PApplet.main(new String[] {"--present","therapeuticpresence.TherapeuticPresence"});
	}
	
}
