package therapeuticpresence;

import java.text.DecimalFormat;

import processing.core.*;
import controlP5.*;
import processing.opengl.*;
import therapeuticskeleton.Skeleton;

public class GuiHud {

	// Link to main Applet to draw on
	private TherapeuticPresence mainApplet;
	private PGraphics3D g3;
	
	// The HUD controls
	private ControlP5 control;
	private ControlGroup menu;
	private Button menuButton;
	private Textarea info;
	private Textarea fps;
	private Textarea guiMessages;

	public DecimalFormat df = new DecimalFormat("0.00");
	
	public GuiHud (TherapeuticPresence _mainApplet) {

		mainApplet = _mainApplet;
		control = new ControlP5(mainApplet);
		g3 = (PGraphics3D)_mainApplet.g;
		
		createMenu();
		menu.hide();
		menuButton = control.addButton("toggleMenu",1,0,0,150,20);
		menuButton.setCaptionLabel("Toggle Menu");
		menuButton.plugTo(this);
		
		createInfoAndFPS();
		
		guiMessages = control.addTextarea("guiMessagesArea","",mainApplet.width-200,10,200,mainApplet.height-20);
	}
	
	public void draw () {
		// store camera matrix
		PMatrix3D currentCamMatrix = new PMatrix3D(g3.camera); 
		// reset camera to draw HUD
		g3.camera();
	    // update fps Text
		String shapeString = "";
		switch (PostureProcessing.activeShape) {
			case Skeleton.V_SHAPE: shapeString="V"; break;
			case Skeleton.A_SHAPE: shapeString="A"; break;
			case Skeleton.U_SHAPE: shapeString="U"; break;
			case Skeleton.N_SHAPE: shapeString="N"; break;
			case Skeleton.M_SHAPE: shapeString="M"; break;
			case Skeleton.W_SHAPE: shapeString="W"; break;
			case Skeleton.O_SHAPE: shapeString="O"; break;
			case Skeleton.I_SHAPE: shapeString="I"; break;
			default: shapeString="NO"+PostureProcessing.activeShape; break;
		}
	    fps.setText("shape: "+shapeString+" fps: "+PApplet.round(mainApplet.frameRate));
	    // toggle debug output
	    if (!TherapeuticPresence.debugOutput) {
			guiMessages.hide();
		} else {
			guiMessages.show();
		}
	    // draw the HUD
		control.draw();   
		// set camera back to old position
		g3.camera = currentCamMatrix;
	}
	
	public void sendGuiMessage (String s) {
		guiMessages.setText(guiMessages.getText()+"\n"+s);
		if (guiMessages.isScrollable()) {
			guiMessages.scroll(1);
		}
	}	
	
	private void createInfoAndFPS() {
		info = control.addTextarea("InfoLabel",
									"Controls: \n" +
									"w/s = +/-translateZ \n" +
									"d/a = +/-translateX \n" +
									"W/S = +/-translateY \n" +
									"up/down = +/-rotateX \n" +
									"left/right = +/-rotateY \n" +
									"shift+up/+down = +/-rotateZ \n" +
									"o/l = save/load calibration data \n" +
									"m = toggle mirror kinect data",
									3,mainApplet.height-200,145,150);
		fps = control.addTextarea("fpsArea","",3,mainApplet.height-50,300,50);
		fps.setFont(mainApplet.createFont("Arial",34));
	}
	
	
	private void createMenu() {
		// create a group to store the menu elements
		menu = control.addGroup("Menu",0,20,150);
		menu.setBackgroundHeight(300);
		menu.setBackgroundColor(mainApplet.color(70,70));
		menu.hideBar();
		
		controlP5.Button mirrorTherapyOff = control.addButton("switchMirrorTherapyOff",Skeleton.MIRROR_THERAPY_OFF,0,0,150,20);
		mirrorTherapyOff.moveTo(menu);
		mirrorTherapyOff.setCaptionLabel("MT OFF");
		mirrorTherapyOff.plugTo(this);
		
		controlP5.Button mirrorTherapyLeft = control.addButton("switchMirrorTherapyLeft",Skeleton.MIRROR_THERAPY_LEFT,0,20,150,20);
		mirrorTherapyLeft.moveTo(menu);
		mirrorTherapyLeft.setCaptionLabel("MT LEFT");
		mirrorTherapyLeft.plugTo(this);
		
		controlP5.Button mirrorTherapyRight = control.addButton("switchMirrorTherapyRight",Skeleton.MIRROR_THERAPY_RIGHT,0,40,150,20);
		mirrorTherapyRight.moveTo(menu);
		mirrorTherapyRight.setCaptionLabel("MT RIGHT");
		mirrorTherapyRight.plugTo(this);
		
		controlP5.Button drawDepthMap = control.addButton("switchVisualisationDepthMap",TherapeuticPresence.DEPTHMAP_VISUALISATION,0,60,150,20);
		drawDepthMap.moveTo(menu);
		drawDepthMap.setCaptionLabel("Draw DepthMap");
		drawDepthMap.plugTo(this);
		
		controlP5.Button drawSkeletons = control.addButton("switchVisualisationSkeletons",TherapeuticPresence.STICKFIGURE_VISUALISATION,0,80,150,20);
		drawSkeletons.moveTo(menu);
		drawSkeletons.setCaptionLabel("Draw Skeletons");
		drawSkeletons.plugTo(this);

		controlP5.Button drawTree2D = control.addButton("switchVisualisationTree2D",TherapeuticPresence.GENERATIVE_TREE_2D_VISUALISATION,0,100,150,20);
		drawTree2D.moveTo(menu);
		drawTree2D.setCaptionLabel("Draw Tree 2D");
		drawTree2D.plugTo(this);

		controlP5.Button drawTree3D = control.addButton("switchVisualisationTree3D",TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION,0,120,150,20);
		drawTree3D.moveTo(menu);
		drawTree3D.setCaptionLabel("Draw Tree 3D");
		drawTree3D.plugTo(this);

		controlP5.Button drawAudioSkeletons = control.addButton("switchVisualisationGeometry2D",TherapeuticPresence.GEOMETRY_2D_VISUALISATION,0,140,150,20);
		drawAudioSkeletons.moveTo(menu);
		drawAudioSkeletons.setCaptionLabel("Draw Geometry2D");
		drawAudioSkeletons.plugTo(this);

		controlP5.Button drawGeometry3D = control.addButton("switchVisualisationGeometry3D",TherapeuticPresence.GEOMETRY_3D_VISUALISATION,0,160,150,20);
		drawGeometry3D.moveTo(menu);
		drawGeometry3D.setCaptionLabel("Draw Geometry3D");
		drawGeometry3D.plugTo(this);

		controlP5.Textarea autoCalibrationLabel = control.addTextarea("autoCalibrationLabel","Toggle AutoCalibration",2,184,128,16);
		autoCalibrationLabel.moveTo(menu);
		
		controlP5.Toggle autoCalibration = control.addToggle("switchAutoCalibration",TherapeuticPresence.autoCalibration,130,180,20,20);
		autoCalibration.moveTo(menu);
		autoCalibration.setLabelVisible(false);
		autoCalibration.plugTo(this);

		controlP5.Textarea debugOutputLabel = control.addTextarea("debugOutputLabel","Toggle debugOutput",2,204,128,16);
		debugOutputLabel.moveTo(menu);
		
		controlP5.Toggle debugOuput = control.addToggle("switchDebugOuput",TherapeuticPresence.debugOutput,130,200,20,20);
		debugOuput.moveTo(menu);
		debugOuput.setLabelVisible(false);
		debugOuput.plugTo(this);
		
		controlP5.Slider fftGain = control.addSlider("changeFFTGain",0.0f,1.0f,AudioManager.gain,0,220,108,20);
		fftGain.moveTo(menu);
		fftGain.setCaptionLabel("FFT Gain");
		fftGain.plugTo(this);
		
		controlP5.Slider maxDistanceToKinect = control.addSlider("changeMaxDistanceToKinect",0.0f,4000.0f,TherapeuticPresence.maxDistanceToKinect,0,240,108,20);
		maxDistanceToKinect.moveTo(menu);
		maxDistanceToKinect.setCaptionLabel("Max dist");
		maxDistanceToKinect.plugTo(this);
		
		controlP5.Slider postureTolerance = control.addSlider("changePostureTolerance",0.0f,1.0f,TherapeuticPresence.postureTolerance,0,260,108,20);
		postureTolerance.moveTo(menu);
		postureTolerance.setCaptionLabel("Posture Tolerance");
		postureTolerance.plugTo(this);
		
		controlP5.Slider smoothingSkeleton = control.addSlider("changeSmoothingSkeleton",0.0f,1.0f,TherapeuticPresence.smoothingSkeleton,0,280,108,20);
		smoothingSkeleton.moveTo(menu);
		smoothingSkeleton.setCaptionLabel("Skeleton Smoothing");
		smoothingSkeleton.plugTo(this);
	}
	
	
	// call back functions ----------------------
	// call back function for menu button "toggle"
	private void toggleMenu(int theValue) {
		if(menu.isVisible()) {
			menu.hide();
		} else {
			menu.show();
		}
	}
	
	private void switchMirrorTherapyOff (int theValue) {
		mainApplet.switchMirrorTherapy(Skeleton.MIRROR_THERAPY_OFF);
	}
	
	private void switchMirrorTherapyLeft (int theValue) {
		mainApplet.switchMirrorTherapy(Skeleton.MIRROR_THERAPY_LEFT);
	}
	
	private void switchMirrorTherapyRight (int theValue) {
		mainApplet.switchMirrorTherapy(Skeleton.MIRROR_THERAPY_RIGHT);
	}
	
	private void switchVisualisationDepthMap (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.DEPTHMAP_VISUALISATION);
	}
	
	private void switchVisualisationSkeletons (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.STICKFIGURE_VISUALISATION);
	}
	
	private void switchVisualisationTree2D (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE2D);
		mainApplet.setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_2D_VISUALISATION);
	}
	
	private void switchVisualisationTree3D (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION);
	}
	
	private void switchVisualisationGeometry2D (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE2D);
		mainApplet.setupVisualisation(TherapeuticPresence.GEOMETRY_2D_VISUALISATION);
	}
	
	private void switchVisualisationGeometry3D (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.GEOMETRY_3D_VISUALISATION);
	}
	
	private void switchAutoCalibration (int theValue) {
		TherapeuticPresence.autoCalibration = !TherapeuticPresence.autoCalibration;
	}
	
	private void switchDebugOutput (int theValue) {
		TherapeuticPresence.debugOutput = !TherapeuticPresence.debugOutput;
	}
	
	private void changeFFTGain (float theValue) {
		AudioManager.gain = theValue;
	}
	
	private void changeMaxDistanceToKinect (float theValue) {
		TherapeuticPresence.maxDistanceToKinect = theValue;
	}

	private void changePostureTolerance (float theValue) {
		mainApplet.changePostureTolerance(theValue);
	}
	private void changeSmoothingSkeleton (float theValue) {
		mainApplet.changeSmoothingSkeleton(theValue);
	}
}
