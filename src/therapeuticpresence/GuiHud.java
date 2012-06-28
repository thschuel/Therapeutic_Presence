package therapeuticpresence;

import java.text.DecimalFormat;

import processing.core.*;
import controlP5.*;
import processing.opengl.*;

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
		if (!TherapeuticPresence.debugOutput) {
			guiMessages.hide();
		}
	}
	
	public void draw () {
		// store camera matrix
		PMatrix3D currentCamMatrix = new PMatrix3D(g3.camera); 
		// reset camera to draw HUD
		g3.camera();
	    // update fps Text
	    fps.setText("fps: "+PApplet.round(mainApplet.frameRate));
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
									3,mainApplet.height-170,145,150);
		fps = control.addTextarea("fpsArea","",3,mainApplet.height-20,145,20);
	}
	
	
	private void createMenu() {
		// create a group to store the menu elements
		menu = control.addGroup("Menu",0,20,150);
		menu.setBackgroundHeight(200);
		menu.setBackgroundColor(mainApplet.color(70,70));
		menu.hideBar();
		
		controlP5.Button mirrorTherapyOff = control.addButton("switchMirrorTherapyOff",TherapeuticPresence.MIRROR_OFF,0,0,150,20);
		mirrorTherapyOff.moveTo(menu);
		mirrorTherapyOff.setCaptionLabel("MT OFF");
		mirrorTherapyOff.plugTo(this);
		
		controlP5.Button mirrorTherapyLeft = control.addButton("switchMirrorTherapyLeft",TherapeuticPresence.MIRROR_LEFT,0,20,150,20);
		mirrorTherapyLeft.moveTo(menu);
		mirrorTherapyLeft.setCaptionLabel("MT LEFT");
		mirrorTherapyLeft.plugTo(this);
		
		controlP5.Button mirrorTherapyRight = control.addButton("switchMirrorTherapyRight",TherapeuticPresence.MIRROR_RIGHT,0,40,150,20);
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

		controlP5.Button drawTree = control.addButton("switchVisualisationTree",TherapeuticPresence.GENERATIVE_TREE_VISUALISATION,0,100,150,20);
		drawTree.moveTo(menu);
		drawTree.setCaptionLabel("Draw Tree");
		drawTree.plugTo(this);

		controlP5.Button drawAudioSkeletons = control.addButton("switchVisualisationGeometry2D",TherapeuticPresence.GEOMETRY_2D_VISUALISATION,0,120,150,20);
		drawAudioSkeletons.moveTo(menu);
		drawAudioSkeletons.setCaptionLabel("Draw Geometry2D");
		drawAudioSkeletons.plugTo(this);

		controlP5.Button drawGeometry3D = control.addButton("switchVisualisationGeometry3D",TherapeuticPresence.GEOMETRY_3D_VISUALISATION,0,140,150,20);
		drawGeometry3D.moveTo(menu);
		drawGeometry3D.setCaptionLabel("Draw Geometry3D");
		drawGeometry3D.plugTo(this);

		controlP5.Textarea autoCalibrationLabel = control.addTextarea("autoCalibrationLabel","Toggle AutoCalibration",2,164,128,16);
		autoCalibrationLabel.moveTo(menu);
		
		controlP5.Toggle autoCalibration = control.addToggle("switchAutoCalibration",TherapeuticPresence.autoCalibration,130,160,20,20);
		autoCalibration.moveTo(menu);
		autoCalibration.setLabelVisible(false);
		autoCalibration.plugTo(this);
		
		controlP5.Slider fftGain = control.addSlider("changeFFTGain",0.0f,1.0f,AudioManager.gain,0,180,108,20);
		fftGain.moveTo(menu);
		fftGain.setCaptionLabel("FFT Gain");
		fftGain.plugTo(this);
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
		TherapeuticPresence.mirrorTherapy = TherapeuticPresence.MIRROR_OFF;
	}
	
	private void switchMirrorTherapyLeft (int theValue) {
		TherapeuticPresence.mirrorTherapy = TherapeuticPresence.MIRROR_LEFT;
	}
	
	private void switchMirrorTherapyRight (int theValue) {
		TherapeuticPresence.mirrorTherapy = TherapeuticPresence.MIRROR_RIGHT;
	}
	
	private void switchVisualisationDepthMap (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.DEPTHMAP_VISUALISATION);
	}
	
	private void switchVisualisationSkeletons (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.STICKFIGURE_VISUALISATION);
	}
	
	private void switchVisualisationTree (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE2D);
		mainApplet.setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_VISUALISATION);
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
		TherapeuticPresence.autoCalibration = !mainApplet.autoCalibration;
	}
	
	private void changeFFTGain (float theValue) {
		AudioManager.gain = theValue;
	}
	

}
