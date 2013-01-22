/*
Copyright (c) 2012, Thomas Schueler, http://www.thomasschueler.de
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the author nor the
      names of the contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THOMAS SCHUELER BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package therapeuticpresence;

import java.io.BufferedWriter;
import java.io.FileWriter;

import javax.media.opengl.GL;
import therapeuticskeleton.*;
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
	public static final short USER_PIXEL_VISUALISATION = 1;
	public static final short STATISTICS_VISUALISATION = 2;
	public static final short STICKFIGURE_VISUALISATION = 3;
	public static final short GENERATIVE_TREE_VISUALISATION = 4;
	public static final short WAVEFORM_VISUALISATION = 5;
	public static final short ELLIPSOIDAL_VISUALISATION = 6;
	public static final short MESH_VISUALISATION = 7;
	public static final short AGENT_VISUALISATION = 8;
	public static final short BASIC_SCENE = 0;
	public static final short TUNNEL_SCENE = 1;
	public static final short LIQUID_SCENE = 2;

	// --- static setup variables ---
	public static boolean fullBodyTracking = false; // control for full body tracking
	public static boolean evaluatePostureAndGesture = true; // control for full body tracking
	public static boolean evaluateStatistics = true; // control for skeleton statistics, switched on initially
	public static boolean recordFlag = true; // set to false for playback
	public static boolean debugOutput = false;
	public static boolean showLiveStatistics = true;
	public static boolean demo=true;
	public static boolean transferSkeleton=false;
	public static short initialSceneType = TherapeuticPresence.BASIC_SCENE;
	public static short initialVisualisationMethod = TherapeuticPresence.DEPTHMAP_VISUALISATION;
	public static short defaultSceneType = TherapeuticPresence.BASIC_SCENE;
	public static short defaultVisualisationMethod = TherapeuticPresence.STICKFIGURE_VISUALISATION;
	public static short currentVisualisationMethod;
	public static short currentSceneType;
	public static short mirrorTherapy = Skeleton.MIRROR_THERAPY_OFF;
	public static boolean autoCalibration = true; // control for auto calibration of skeleton
	public static boolean mirrorKinect = false;
	public static float maxDistanceToKinect = 4000f; // in mm, is used for scaling the visuals
	public static final float cameraEyeZ = 5000f; // in mm, visuals are sensitive to this!
	public static final float DEFAULT_POSTURE_TOLERANCE = 0.3f;
	public static float postureTolerance = TherapeuticPresence.DEFAULT_POSTURE_TOLERANCE;
	public static final float DEFAULT_GESTURE_TOLERANCE = 0.4f;
	public static float gestureTolerance = TherapeuticPresence.DEFAULT_GESTURE_TOLERANCE;
	public static final float DEFAULT_SMOOTHING_SKELETON = 0.8f;
	public static float smoothingSkeleton = TherapeuticPresence.DEFAULT_SMOOTHING_SKELETON;
	public static String audioFile = "../data/moan.mp3";
	public static short initialAudioFile = 0;
	public static PVector centerOfSkeletonDetectionSpace = new PVector(0,0,maxDistanceToKinect/2f); // calibrate skeleton only for users in a defined centered space. used for stability when more users are in the scene
	public static float radiusOfSkeletonDetectionSpace = 500f;
	public static boolean structuredTaskMode = false;
	
	private int activeUserId = -1;
	private boolean skeletonDetectionStarted = false;
	private boolean drawRunning = false;
	private boolean runTherapyDraw = false; // activated after initial setup screen is done
	private boolean runInitDraw = true;
	
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
	// user interface while in therapy
	protected GuiHud guiHud = null;
	// user interface for init
	protected GuiInit guiInit = null;
	// audio interface
	protected AudioManager audioManager = null;
	// posture processing for skeleton interface
	protected PostureProcessing postureProcessing = null;
	// task manager for structured therapy
	protected TherapyTaskManager taskManager = null;
	// For statistics logging
	protected FileWriter fileWriter = null;
	protected BufferedWriter buffer = null;
	
	
	// -----------------------------------------------------------------
	public void setup() {
		size(screenWidth-16,screenHeight-128,OPENGL);
		setup_init();
	}
	private void setup_init() {
		guiInit = new GuiInit(this);
	}
	private void setup_therapy() {		

		// establish connection to kinect/openni
		setupKinect();
		
		// start the audio interface
		audioManager = new AudioManager(this);
		audioManager.setup(audioFile);
		audioManager.start();
		
		// setup Scene
		setupScene(TherapeuticPresence.initialSceneType);

		// start visualisation (default is depthMap)
		setupVisualisation(TherapeuticPresence.initialVisualisationMethod);
		
		// generate HUD
		guiHud = new GuiHud(this);
		
		runTherapyDraw = true;
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
	
	public void setupScene (short _sceneType) {
		switch (_sceneType) {
				
			case TherapeuticPresence.TUNNEL_SCENE:
				if (audioManager != null) {
					scene = new TunnelScene(this,color(0,0,0),audioManager);
					scene.reset();
					currentSceneType = TherapeuticPresence.TUNNEL_SCENE;
				} else {
					setupScene(TherapeuticPresence.BASIC_SCENE);
					debugMessage("setupScene(short): AudioManager needed for Tunnel Scene!");
				}
				break;
				
			case TherapeuticPresence.LIQUID_SCENE:
				if (audioManager != null) {
					scene = new LiquidScene(this,color(0,0,0),audioManager);
					scene.reset();
					currentSceneType = TherapeuticPresence.LIQUID_SCENE;
				} else {
					setupScene(TherapeuticPresence.BASIC_SCENE);
					debugMessage("setupScene(short): AudioManager needed for Tunnel Scene!");
				}
				break;
			
			default:
				scene = new BasicScene(this,color(0,0,0),audioManager);
				scene.reset();
				currentSceneType = TherapeuticPresence.BASIC_SCENE;
				break;
		}
		if (postureProcessing != null) {
			postureProcessing.setScene(scene);
		}
	}
	
	public void setupVisualisation (short _visualisationMethod) {
		// prepare for fade out
		lastVisualisation = visualisation;
		visualisation = null;
		
		// set up next visualisation for fade in
		switch (_visualisationMethod) {
		
			case TherapeuticPresence.USER_PIXEL_VISUALISATION: // this is only for switching to this visualisation when skeleton is already tracked!
				if (skeleton != null) {
					nextVisualisation = new UserPixelVisualisation(this,kinect,skeleton.getUserId());
					nextVisualisation.setup();
				} else {
					nextVisualisation = new UserPixelVisualisation(this,kinect,activeUserId);
					nextVisualisation.setup();
				}
				currentVisualisationMethod = TherapeuticPresence.USER_PIXEL_VISUALISATION;
				break;
				
			case TherapeuticPresence.STATISTICS_VISUALISATION:
				if (skeleton != null) {
					SkeletonStatistics statistics = skeleton.getFinalStatistics();
					if (statistics != null) {
						visualisation = new StatisticsVisualisation(this,statistics,guiHud);
						visualisation.setup();
						lastVisualisation = null; // skip fade in/fade out for statistics
						currentVisualisationMethod = TherapeuticPresence.STATISTICS_VISUALISATION;
					} else {
						visualisation = lastVisualisation;
						lastVisualisation = null;
						debugMessage("Cannot setup statistics visualisation without statistics data");
					}
				} else {
					nextVisualisation = new DepthMapVisualisation(this,kinect);
					nextVisualisation.setup();
					currentVisualisationMethod = TherapeuticPresence.DEPTHMAP_VISUALISATION;
					debugMessage("Cannot setup statistics visualisation without skeleton data");
				}
				break;
				
			case TherapeuticPresence.STICKFIGURE_VISUALISATION:
				if (lastVisualisation.getVisualisationType() == TherapeuticPresence.USER_PIXEL_VISUALISATION)
					nextVisualisation = new StickfigureVisualisation(this,kinect,skeleton,((UserPixelVisualisation)lastVisualisation).getColor());
				else
					nextVisualisation = new StickfigureVisualisation(this,kinect,skeleton);
						
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.STICKFIGURE_VISUALISATION;
				break;
				
			case TherapeuticPresence.GENERATIVE_TREE_VISUALISATION:
				nextVisualisation = new GenerativeTreeVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.GENERATIVE_TREE_VISUALISATION;
				break;
				
			case TherapeuticPresence.WAVEFORM_VISUALISATION:
				nextVisualisation = new WaveformVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.WAVEFORM_VISUALISATION;
				break;
				
			case TherapeuticPresence.ELLIPSOIDAL_VISUALISATION:
				nextVisualisation = new EllipsoidalVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.ELLIPSOIDAL_VISUALISATION;
				break;
				
			case TherapeuticPresence.MESH_VISUALISATION:
				nextVisualisation = new MeshVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.MESH_VISUALISATION;
				break;
				
			case TherapeuticPresence.AGENT_VISUALISATION:
				nextVisualisation = new AgentVisualisation(this,skeleton,audioManager);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.AGENT_VISUALISATION;
				break;
			
			default:
				nextVisualisation = new DepthMapVisualisation(this,kinect);
				nextVisualisation.setup();
				currentVisualisationMethod = TherapeuticPresence.DEPTHMAP_VISUALISATION;
				break;
		}
		if (postureProcessing != null && nextVisualisation != null) {
			postureProcessing.setVisualisation(nextVisualisation);
		}
	}
	
	public void toggleScenes () {
		if (currentSceneType == BASIC_SCENE) {
			setupScene(LIQUID_SCENE);
		} else if (currentSceneType == LIQUID_SCENE) {
			setupScene(TUNNEL_SCENE);
		} else {
			setupScene(BASIC_SCENE);
		}
		
	}
	
	public void toggleVisualisations () {
		if (demo) {
			if (currentVisualisationMethod == WAVEFORM_VISUALISATION) {
				setupScene(LIQUID_SCENE);
				setupVisualisation(GENERATIVE_TREE_VISUALISATION);
			} else if (currentVisualisationMethod == GENERATIVE_TREE_VISUALISATION) {
				setupScene(LIQUID_SCENE);
				setupVisualisation(ELLIPSOIDAL_VISUALISATION);
			}  else {
				setupScene(TUNNEL_SCENE);
				setupVisualisation(WAVEFORM_VISUALISATION);
			}
		} else {
			if (currentVisualisationMethod == WAVEFORM_VISUALISATION) {
				setupScene(LIQUID_SCENE);
				setupVisualisation(GENERATIVE_TREE_VISUALISATION);
			} else if (currentVisualisationMethod == GENERATIVE_TREE_VISUALISATION) {
				setupScene(LIQUID_SCENE);
				setupVisualisation(ELLIPSOIDAL_VISUALISATION);
			}  else if (currentVisualisationMethod == ELLIPSOIDAL_VISUALISATION) {
				setupScene(TUNNEL_SCENE);
				setupVisualisation(MESH_VISUALISATION);
			}  else if (currentVisualisationMethod == MESH_VISUALISATION) {
				setupScene(TUNNEL_SCENE);
				setupVisualisation(AGENT_VISUALISATION);
			}  else if (currentVisualisationMethod == AGENT_VISUALISATION) {
				setupScene(BASIC_SCENE);
				setupVisualisation(STICKFIGURE_VISUALISATION);
			}  else {
				setupScene(TUNNEL_SCENE);
				setupVisualisation(WAVEFORM_VISUALISATION);
			}
		}
	}
	
	// -----------------------------------------------------------------
	public void draw() {
		if (runInitDraw) {
			draw_init();
		}
		
		if (runTherapyDraw) {
			draw_therapy();
		}
	}
	
	private void draw_init () {
		if (guiInit != null) {
			guiInit.draw();
		}
		if (guiInit.startTherapy()) {
			runInitDraw = false;
			setup_therapy();
		}
	}
	
	private void draw_therapy() {
		if (!runTherapyDraw) return;
		drawRunning = true;
		
		// -------- update status --------------------------
		if (kinect != null) {
			kinect.update();
		}
		if (!skeletonDetectionStarted && skeleton == null) { // start skeleton detection in draw method to enable CoM detection 
			userDetection();
		}
		if (skeleton != null && kinect.isTrackingSkeleton(skeleton.getUserId())) {
			skeleton.update(frameCount,frameRate);
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
			pushStyle();
			scene.reset();
			popStyle();
		}
		
		if (lastVisualisation != null) {
			pushStyle();
			if (lastVisualisation.fadeOut()) { // true when visualisation has done fade out
				lastVisualisation = null;
			}
			popStyle();
		}
		if (nextVisualisation != null) {
			pushStyle();
			if (nextVisualisation.fadeIn()) { // true when visualisation has done fade in
				visualisation = nextVisualisation;
				if (postureProcessing != null) {
					postureProcessing.setVisualisation(visualisation);
				}
				nextVisualisation = null;
			}
			popStyle();
		}
		if (visualisation != null) { // while fade out and fade in visualisation is null
			pushStyle();
			visualisation.draw();
			popStyle();
		}
		
		if (guiHud != null) {
			pushStyle();
			if (showLiveStatistics && skeleton != null && currentVisualisationMethod != TherapeuticPresence.STATISTICS_VISUALISATION) {
				SkeletonStatistics temp = skeleton.getLiveStatistics();
				if (temp != null) {
					guiHud.updateLiveStatistics(temp);
				} else {
					PApplet.println("no statistics data generated");
				}
			}
			guiHud.draw();
			popStyle();
		}
		drawRunning = false;
	}
	
	public void close() {
		while (drawRunning); // wait for last draw to finish
		runTherapyDraw = false; // stop draw 
		
		// TODO: Do closing work
		if (skeleton != null && fileWriter != null && buffer != null) {
			endStatistics();
		}
		skeleton = null;
		scene = null;
		postureProcessing = null;
		if (audioManager != null)
			audioManager.stop();
		if (kinect != null)
			kinect.close();
		exit();
	}
	
	public void debugMessage (String _message) {
		guiHud.sendGuiMessage(_message);
		println(_message);
	}
	
	public void updateScore (int _score) {
		guiHud.updateScore(_score);
		println("Score: "+_score);
	}
	
	public void updateTask (short _task) {
		String taskString = "";
		switch (_task) {
			case SkeletonPosture.V_SHAPE: taskString="V"; break;
			case SkeletonPosture.A_SHAPE: taskString="A"; break;
			case SkeletonPosture.U_SHAPE: taskString="U"; break;
			case SkeletonPosture.N_SHAPE: taskString="N"; break;
			case SkeletonPosture.M_SHAPE: taskString="M"; break;
			case SkeletonPosture.W_SHAPE: taskString="W"; break;
			case SkeletonPosture.O_SHAPE: taskString="O"; break;
			case SkeletonPosture.I_SHAPE: taskString="I"; break;
			default: taskString="NO"; break;
		}
		guiHud.updateTask(taskString);
		println("Task: "+taskString);
	}

	
	// -----------------------------------------------------------------
	private void userDetection () {
		PVector userCoM = new PVector();
		int[] users = kinect.getUsers();
		for (int i=0; i<users.length; i++) {
			if (kinect.getCoM(users[i], userCoM)) {
				userCoM.y = 0f; // ignore y coordinate
				if (!skeletonDetectionStarted && PVector.dist(userCoM,centerOfSkeletonDetectionSpace) < radiusOfSkeletonDetectionSpace) {
					debugMessage("userDetection: user "+users[i]+" inside detection space. Start skeleton detection for user.");
					skeletonDetectionStarted = true;
					activeUserId = users[i];
					setupVisualisation(TherapeuticPresence.USER_PIXEL_VISUALISATION);
					if(TherapeuticPresence.autoCalibration) {
						kinect.requestCalibrationSkeleton(users[i],true);
					} else {
						kinect.startPoseDetection("Psi",users[i]);
					}
				}
			} else {
				debugMessage("userDetection: can not get center of mass for user "+users[i]+". Try again next frame.");
			}
		}
	}
	// is triggered by SimpleOpenNI on "onEndCalibration" and by user on "loadCalibration"
	// starts tracking of a Skeleton
	private void newSkeletonFound (int _userId) {
		if (_userId < 0 || _userId > TherapeuticPresence.MAX_USERS) {
			debugMessage("newSkeletonFound: User id "+_userId+" outside range. Maximum users: "+TherapeuticPresence.MAX_USERS);
		} else {
			if (skeleton != null ) {
				debugMessage("Trying to replace skeleton of user "+skeleton.getUserId()+" with skeleton of user "+_userId+"!");
			}
			skeleton = new Skeleton(kinect,_userId,fullBodyTracking);
			skeleton.setPostureTolerance(DEFAULT_POSTURE_TOLERANCE);
			skeleton.setGestureTolerance(DEFAULT_GESTURE_TOLERANCE);
			skeleton.setEvaluatePostureAndGesture(evaluatePostureAndGesture);
			skeleton.setMirrorTherapy(mirrorTherapy);
			if (evaluateStatistics) {
				beginStatistics();
			}
			kinect.setSmoothingSkeleton(smoothingSkeleton);
			// start default scene and visualisation
			postureProcessing = new PostureProcessing(this,skeleton,scene,visualisation);
			setupScene(defaultSceneType);
			setupVisualisation(defaultVisualisationMethod);
		}
	}
	
	// is triggered by SimpleOpenNi on "onLostUser"
	private void skeletonLost (int _userId) {
		if (_userId < 0 || _userId > TherapeuticPresence.MAX_USERS) {
			debugMessage("skeletonLost: User id "+_userId+" outside range. Maximum users: "+TherapeuticPresence.MAX_USERS);
		} else {
			if (evaluateStatistics) {
				endStatistics();
			}
			skeleton = null;
			postureProcessing = null;
			setupScene(initialSceneType);
			setupVisualisation(initialVisualisationMethod);
			int[] users = kinect.getUsers();
			if (users.length!=0) {
				if(TherapeuticPresence.autoCalibration) kinect.requestCalibrationSkeleton(users[0],true);
				else kinect.startPoseDetection("Psi",users[0]);
			}
		}
	}
	
	private void beginStatistics () {
		if (skeleton != null) {
			String fileName = "../log/statistics-"+PApplet.year()+"_"+PApplet.month()+"_"+PApplet.day()+"-"+PApplet.hour()+"_"+PApplet.minute()+"_"+PApplet.second()+".log";
			try {
				fileWriter = new FileWriter(fileName);
				buffer = new BufferedWriter(fileWriter);
				skeleton.setEvaluateStatistics(true,buffer);
			} catch (Exception e) {
				debugMessage("beginStatistics: could not open file "+fileName+" or buffer");
			}
		} else {
			debugMessage("beginStatistics: no skeleton found or statistics evaluation switched off");
		}
	}
	
	private void endStatistics() {
		if (skeleton != null) {
			skeleton.setEvaluateStatistics(false,null);
			if (fileWriter != null && buffer != null) {
				try {
					// close java I/O
					buffer.close();
					fileWriter.close();
					fileWriter = null;
					buffer = null;
				} catch (Exception e) {
					debugMessage("endStatistics: could not close fileWriter or buffer. "+e.getMessage());
				}
			} else {
				debugMessage("endStatistics: no file or buffer active");
			}
		} else {
			debugMessage("endStatistics: no skeleton available");
		}
	}
	
	// is used on keyboard input to restart user tracking
	private void resetKinect () {
		int[] userList = kinect.getUsers();
		if (userList != null) {
			kinect.close();
			kinect = null;
			skeleton = null;
			setupKinect();
			setupScene(initialSceneType);
			setupVisualisation(initialVisualisationMethod);
		}
	}
	
	// call back for guihud
	public void switchMirrorTherapy (short _mirrorTherapy) {
		if (_mirrorTherapy >= Skeleton.MIRROR_THERAPY_OFF && _mirrorTherapy <= Skeleton.MIRROR_THERAPY_RIGHT) {
			mirrorTherapy = _mirrorTherapy;
		} else {
			mirrorTherapy = Skeleton.MIRROR_THERAPY_OFF;
		}
		
		if (skeleton != null) {
			skeleton.setMirrorTherapy(mirrorTherapy);
		}
	}

	public void startStatistics() {
		if (skeleton != null) {
			if (evaluateStatistics) {
				debugMessage("startStatistics: new statistics object will be started");
				endStatistics();
			}
			evaluateStatistics = true;
			beginStatistics();
		} else{
			debugMessage("startStatistics: can't generate statistics without skeleton");
		}
	}
	public void stopStatistics() {
		if (skeleton != null) {
			if (!evaluateStatistics) {
				debugMessage("stopStatistics: statistics generation was switched off");
			} 
			evaluateStatistics = false;
			endStatistics();
		} else{
			debugMessage("stopStatistics: no skeleton in the scene");
		}
	}
	
	public void changePostureTolerance (float _postureTolerance) {
		if (skeleton != null) {
			if (_postureTolerance >= 0f && _postureTolerance <= 1.0f) {
				postureTolerance = _postureTolerance;
				skeleton.setPostureTolerance(postureTolerance);
			} else { 
				postureTolerance = DEFAULT_POSTURE_TOLERANCE;
				skeleton.setPostureTolerance(postureTolerance);
			}
		}
	}
	
	public void changeGestureTolerance (float _gestureTolerance) {
		if (skeleton != null) {
			if (_gestureTolerance >= 0f && _gestureTolerance <= 1.0f) {
				gestureTolerance = _gestureTolerance;
				skeleton.setGestureTolerance(gestureTolerance);
			} else { 
				gestureTolerance = DEFAULT_GESTURE_TOLERANCE;
				skeleton.setGestureTolerance(gestureTolerance);
			}
		}
	}
	
	public void changeSmoothingSkeleton (float _smoothingSkeleton) {
		if (_smoothingSkeleton >= 0f && _smoothingSkeleton <= 1.0f) {
			smoothingSkeleton = _smoothingSkeleton;
			kinect.setSmoothingSkeleton(smoothingSkeleton);
		} else { 
			smoothingSkeleton = DEFAULT_SMOOTHING_SKELETON;
			kinect.setSmoothingSkeleton(smoothingSkeleton);
		}
	}
	
	public void switchStructuredTaskMode () {
		if (taskManager == null && postureProcessing != null) {
			// switch on
			structuredTaskMode = true;
			taskManager = new TherapyTaskManager(this);
			postureProcessing.setTaskManager(taskManager);
		} else if (taskManager != null && postureProcessing != null) {
			// switch off
			structuredTaskMode = false;
			taskManager = null;
			postureProcessing.removeTaskManager();
		}
	}
	
	// -----------------------------------------------------------------
	// SimpleOpenNI user events
	public void onNewUser(int userId) {
		debugMessage("onNewUser: New User "+userId+" entered the scene.");
		// start skeleton detection in draw method to enable CoM detection
	}
	public void onLostUser(int userId) {
		debugMessage("onLostUser: Lost User " + userId);
		if (userId == skeleton.getUserId()) {
			debugMessage("onLostUser:   stop tracking user");
			this.skeletonLost(userId);
			activeUserId= -1;
		}
	}
	public void onStartCalibration(int userId) {
		debugMessage("onStartCalibration: Starting to calibrate user " + userId);
	}
	public void onEndCalibration(int userId, boolean successfull) {
		debugMessage("onEndCalibration: Ending calibration for user " + userId);
		skeletonDetectionStarted = false;
		if (successfull) { 
			debugMessage("onEndCalibration:   user calibrated successfully");
			activeUserId=userId;
			kinect.startTrackingSkeleton(userId); 
			//kinect.update(); // one update loop before going on with calculations prevents kinect to be in an unstable state
			this.newSkeletonFound(userId);
		} else { 
			debugMessage("onEndCalibration:   failed to calibrate user");
		}
	}
	public void onStartPose(String pose,int userId) {
		debugMessage("onStartPose: Start pose "+pose+" identified for user" + userId);
		debugMessage("onStartPose:   stopping pose detection and requesting calibration");
		kinect.stopPoseDetection(userId); 
		kinect.requestCalibrationSkeleton(userId, true);
	}
	
	public void onEndPose(String pose,int userId) {
		debugMessage("onEndPose: End pose "+pose+" identified for user " + userId);
	}
	
	public void onExitUser(int userId) {
		int[] userList = kinect.getUsers();
		// try to transfer skeleton to another user in the scene
		if (transferSkeleton && skeleton != null && skeleton.getUserId() == userId) {
			if (kinect.saveCalibrationDataSkeleton(userId,1)) { // save calibration data of skeleton to slot 1
				for (int i=0; i<userList.length; i++) {
					if (userList[i] != userId) { // use the first user in the list who is not the exiting user
						debugMessage("onExitUser: "+userId+". Trying to transfer calibration to user "+userList[i]);
						if (kinect.loadCalibrationDataSkeleton(userList[i],1)) { // load calibration data from slot 1 for user i
							activeUserId=userList[i];
							kinect.startTrackingSkeleton(userList[i]);
//							this.newSkeletonFound(userList[i]);
							debugMessage("onExitUser: "+userId+". Calibration successfully transferred.");
							return;
						} else {
							debugMessage("onExitUser: "+userId+". Could not load calibration data from slot 1.");
						}
					}
				}
				// reaching this point means there is no other user in the scene
				debugMessage("onExitUser: "+userId+". Could not transfer skeleton. No other user in the scene.");
			} else {
				debugMessage("onExitUser: "+userId+". Could not save calibration data to slot 1.");
			}
			debugMessage("onExitUser: "+userId+". No other user in the scene.");
		}
	}
	
	public void onReEnterUser(int userId) {
		// if another skeleton is tracked, don't transfer the skeleton 
//		if (skeleton==null) {
//			int[] userList = kinect.getUsers();
//			for (int i=0; i<userList.length; i++) {
//				if (userList[i] == userId) {
//					kinect.startTrackingSkeleton(userList[i]);
//					this.newSkeletonFound(userList[i]);
//				}
//			}
//		}
	}

	// Keyboard events
	public void keyPressed() {
		switch(key) {
			case ' ':
				toggleVisualisations();
				break;
		
		  	// save user calibration data
			case 'o':
		  		if(skeleton != null && kinect.isTrackingSkeleton(skeleton.getUserId())){
		  			if(kinect.saveCalibrationDataSkeleton(skeleton.getUserId(),"calibration"+skeleton.getUserId()+".skel"))
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
			  		if(kinect.loadCalibrationDataSkeleton(userList.get(0),"calibration"+userList.get(0)+".skel")) {
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
			    
		  	case '\n':
				setupScene(TherapeuticPresence.BASIC_SCENE);
				setupVisualisation(TherapeuticPresence.STATISTICS_VISUALISATION);
				break;
			
		  	case 'Ÿ':
		  		TherapeuticPresence.showLiveStatistics = !TherapeuticPresence.showLiveStatistics;
				break;
				
		  	case '1':
				switchMirrorTherapy(Skeleton.MIRROR_THERAPY_OFF);
		  		break;
		  		
		  	case '2':
				switchMirrorTherapy(Skeleton.MIRROR_THERAPY_LEFT);
		  		break;
		  		
		  	case '3':
				switchMirrorTherapy(Skeleton.MIRROR_THERAPY_RIGHT);
		  		break;
		  		
		  	case '4':
				startStatistics();
		  		break;
		  		
		  	case '5':
				stopStatistics();
		  		break;
		  		
		  	case '6':
				setupScene(TherapeuticPresence.BASIC_SCENE);
				setupVisualisation(TherapeuticPresence.STICKFIGURE_VISUALISATION);
		  		break;
		  		
		  	case '7':
				setupScene(TherapeuticPresence.TUNNEL_SCENE);
				setupVisualisation(TherapeuticPresence.WAVEFORM_VISUALISATION);
		  		break;
		  		
		  	case '8':
				setupScene(TherapeuticPresence.LIQUID_SCENE);
				setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_VISUALISATION);
		  		break;

		  	case '9':
				setupScene(TherapeuticPresence.LIQUID_SCENE);
				setupVisualisation(TherapeuticPresence.ELLIPSOIDAL_VISUALISATION);
		  		break;

		  	case '0':
				toggleScenes();
		  		break;

			case 'w':
				centerOfSkeletonDetectionSpace.z -= 50f;
				break;
			case 'a':
				centerOfSkeletonDetectionSpace.x += 50f;
				break;
			case 's':
				centerOfSkeletonDetectionSpace.z += 50f;
				break;
			case 'd':
				centerOfSkeletonDetectionSpace.x -= 50f;
				break;
			case 'e':
				radiusOfSkeletonDetectionSpace -= 50f;
				break;
			case 'r':
				radiusOfSkeletonDetectionSpace += 50f;
				break;
				
			case 'q':
				close();
				break;
				
		}
	}
	
	
	public static void main (String args[]) {
		PApplet.main(new String[] {"--present","therapeuticpresence.TherapeuticPresence"});
	}
	
}
