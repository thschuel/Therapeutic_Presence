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

import java.text.DecimalFormat;

import peasy.PeasyCam;
import processing.core.*;
import controlP5.*;
import processing.opengl.*;
import scenes.AbstractScene;
import therapeuticskeleton.Skeleton;
import visualisations.AbstractSkeletonAudioVisualisation;
import visualisations.AbstractSkeletonVisualisation;

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
	private Textarea score;
	private Textarea task;
	private Textarea guiMessages;

	public DecimalFormat df = new DecimalFormat("0.00");
	
	public GuiHud (TherapeuticPresence _mainApplet) {

		mainApplet = _mainApplet;
		control = new ControlP5(mainApplet);
		g3 = (PGraphics3D)_mainApplet.g;
		
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		createMenu();
		menu.hide();
		menuButton = control.addButton("toggleMenu",1,0,0,150,20);
		menuButton.setCaptionLabel("Toggle Menu");
		menuButton.plugTo(this);
		
		createInfoElements();
	}
	
	public void draw () {
		// store camera matrix
		PMatrix3D currentCamMatrix = new PMatrix3D(g3.camera); 
		// reset camera to draw HUD
		g3.camera();
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.noLights();
		updateDisplay();
	    // draw the HUD
		control.draw();   
		// set camera back to old position
		g3.camera = currentCamMatrix;
	}
	
	private void updateDisplay () {
	    // update fps Text
		String shapeString = "";
		switch (PostureProcessing.activePosture) {
			case Skeleton.V_SHAPE: shapeString="V"; break;
			case Skeleton.A_SHAPE: shapeString="A"; break;
			case Skeleton.U_SHAPE: shapeString="U"; break;
			case Skeleton.N_SHAPE: shapeString="N"; break;
			case Skeleton.M_SHAPE: shapeString="M"; break;
			case Skeleton.W_SHAPE: shapeString="W"; break;
			case Skeleton.O_SHAPE: shapeString="O"; break;
			case Skeleton.I_SHAPE: shapeString="I"; break;
			case Skeleton.HANDS_FORWARD_DOWN_POSE: shapeString="HF"; break;
			default: shapeString="NO"+PostureProcessing.activePosture; break;
		}
	    fps.setText("pG "+Skeleton.pushGestureStartCycle+" g "+PostureProcessing.currentGesture+" shape: "+shapeString+" fps: "+PApplet.round(mainApplet.frameRate));
	    // toggle debug output
	    if (TherapeuticPresence.debugOutput) {
			guiMessages.show();
			info.show();
		} else {
			guiMessages.hide();
			info.hide();
		}
	    if (TherapeuticPresence.structuredTaskMode) {
	    	score.show();
	    	task.show();
	    } else {
	    	score.hide();
	    	task.hide();
	    }
	}
	
	public void sendGuiMessage (String s) {
		guiMessages.setText(guiMessages.getText()+"\n"+s);
		if (guiMessages.isScrollable()) {
			guiMessages.scroll(1);
		}
	}	
	
	public void updateScore (int _score) {
		score.setText(""+_score);
	}	
	
	public void updateTask (String _task) {
		task.setText("Next pose: "+_task);
	}
	
	private void createInfoElements() {
		info = control.addTextarea("InfoLabel",
									"Controls: \n" +
									"w/a/s/d = pos detection space \n" +
									"e/r = radius detection space \n" +
									"o/l = save/load calibration data \n" +
									"m = toggle mirror kinect data \n"+
									"\nColor coding: \n"+
									"green: outside max distance to kinect \n"+
									"gray: outside skeleton detection space\n"+
									"white: inside skeleton detection space",
									3,mainApplet.height-150,500,150);
		
		fps = control.addTextarea("fpsArea","",3,mainApplet.height-20,500,50);
		
		guiMessages = control.addTextarea("guiMessagesArea","",mainApplet.width-200,10,200,mainApplet.height-20);

		score = control.addTextarea("scoreArea","",mainApplet.width/2-5,20,70,70);
		score.setFont(mainApplet.createFont("Helvetica",30));
		score.setText(""+0);
		
		task = control.addTextarea("taskArea","",mainApplet.width/2-305,20,300,70);
		task.setFont(mainApplet.createFont("Helvetica",30));
		task.setText(""+0);
	}
	
	
	private void createMenu() {
		// create a group to store the menu elements
		menu = control.addGroup("Menu",0,20,200);
		menu.setBackgroundHeight(460);
		menu.setBackgroundColor(mainApplet.color(70,70));
		menu.hideBar();
		
		int positionY=0;
		
		controlP5.Button mirrorTherapyOff = control.addButton("switchMirrorTherapyOff",Skeleton.MIRROR_THERAPY_OFF,0,positionY,200,20);
		mirrorTherapyOff.moveTo(menu);
		mirrorTherapyOff.setCaptionLabel("Mirror off");
		mirrorTherapyOff.plugTo(this);
		positionY += 20;
		
		controlP5.Button mirrorTherapyLeft = control.addButton("switchMirrorTherapyLeft",Skeleton.MIRROR_THERAPY_LEFT,0,positionY,200,20);
		mirrorTherapyLeft.moveTo(menu);
		mirrorTherapyLeft.setCaptionLabel("Mirror right");
		mirrorTherapyLeft.plugTo(this);
		positionY += 20;
		
		controlP5.Button mirrorTherapyRight = control.addButton("switchMirrorTherapyRight",Skeleton.MIRROR_THERAPY_RIGHT,0,positionY,200,20);
		mirrorTherapyRight.moveTo(menu);
		mirrorTherapyRight.setCaptionLabel("Mirror left");
		mirrorTherapyRight.plugTo(this);
		positionY += 20;
		
		controlP5.Button switchScene = control.addButton("switchScene",TherapeuticPresence.TUNNEL_SCENE3D,0,positionY,200,20);
		switchScene.moveTo(menu);
		switchScene.setCaptionLabel("Switch Scenes");
		switchScene.plugTo(this);
		positionY += 20;
		
		controlP5.Button drawDepthMap = control.addButton("switchVisualisationDepthMap",TherapeuticPresence.DEPTHMAP_VISUALISATION,0,positionY,200,20);
		drawDepthMap.moveTo(menu);
		drawDepthMap.setCaptionLabel("Draw DepthMap");
		drawDepthMap.plugTo(this);
		positionY += 20;
		
		controlP5.Button drawSkeletons = control.addButton("switchVisualisationSkeletons",TherapeuticPresence.STICKFIGURE_VISUALISATION,0,positionY,200,20);
		drawSkeletons.moveTo(menu);
		drawSkeletons.setCaptionLabel("Draw Skeletons");
		drawSkeletons.plugTo(this);
		positionY += 20;
		
		controlP5.Button drawEllipsoid3D = control.addButton("switchVisualisationEllipsoid3D",TherapeuticPresence.ELLIPSOIDAL_3D_VISUALISATION,0,positionY,200,20);
		drawEllipsoid3D.moveTo(menu);
		drawEllipsoid3D.setCaptionLabel("Draw Ellipsoid 3D");
		drawEllipsoid3D.plugTo(this);
		positionY += 20;
		
		controlP5.Button drawTree3D = control.addButton("switchVisualisationTree3D",TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION,0,positionY,200,20);
		drawTree3D.moveTo(menu);
		drawTree3D.setCaptionLabel("Draw Tree 3D");
		drawTree3D.plugTo(this);
		positionY += 20;
		
		controlP5.Button drawGeometry3D = control.addButton("switchVisualisationGeometry3D",TherapeuticPresence.GEOMETRY_3D_VISUALISATION,0,positionY,200,20);
		drawGeometry3D.moveTo(menu);
		drawGeometry3D.setCaptionLabel("Draw Geometry3D");
		drawGeometry3D.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea switchTaskModeLabel = control.addTextarea("switchTaskModeLabel","Structured Task Mode",2,positionY+4,178,16);
		switchTaskModeLabel.moveTo(menu);
		controlP5.Toggle switchTaskMode = control.addToggle("switchTaskMode",TherapeuticPresence.structuredTaskMode,180,positionY,20,20);
		switchTaskMode.moveTo(menu);
		switchTaskMode.setLabelVisible(false);
		switchTaskMode.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea demoLabel = control.addTextarea("demoLabel","Toggle Demo",2,positionY+4,178,16);
		demoLabel.moveTo(menu);
		controlP5.Toggle demo = control.addToggle("switchDemo",TherapeuticPresence.demo,180,positionY,20,20);
		demo.moveTo(menu);
		demo.setLabelVisible(false);
		demo.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea debugOutputLabel = control.addTextarea("debugOutputLabel","Toggle debugOutput",2,positionY+4,178,16);
		debugOutputLabel.moveTo(menu);
		controlP5.Toggle debugOuput = control.addToggle("switchDebugOuput",TherapeuticPresence.debugOutput,180,positionY,20,20);
		debugOuput.moveTo(menu);
		debugOuput.setLabelVisible(false);
		debugOuput.plugTo(this);
		positionY += 20;
		
		controlP5.Slider fftGain = control.addSlider("changeFFTGain",0.0f,1.0f,AudioManager.gain,0,positionY,108,20);
		fftGain.moveTo(menu);
		fftGain.setCaptionLabel("FFT Gain");
		fftGain.plugTo(this);
		positionY += 20;
		
		controlP5.Slider maxDistanceToKinect = control.addSlider("changeMaxDistanceToKinect",0.0f,5000.0f,TherapeuticPresence.maxDistanceToKinect,0,positionY,108,20);
		maxDistanceToKinect.moveTo(menu);
		maxDistanceToKinect.setCaptionLabel("Max dist");
		maxDistanceToKinect.plugTo(this);
		positionY += 20;
		
		controlP5.Slider postureTolerance = control.addSlider("changePostureTolerance",0.0f,1.0f,TherapeuticPresence.postureTolerance,0,positionY,108,20);
		postureTolerance.moveTo(menu);
		postureTolerance.setCaptionLabel("Posture Tolerance");
		postureTolerance.plugTo(this);
		positionY += 20;
		
		controlP5.Slider gestureTolerance = control.addSlider("changeGestureTolerance",0.0f,1.0f,TherapeuticPresence.gestureTolerance,0,positionY,108,20);
		gestureTolerance.moveTo(menu);
		gestureTolerance.setCaptionLabel("Gesture Tolerance");
		gestureTolerance.plugTo(this);
		positionY += 20;
		
		controlP5.Slider smoothingSkeleton = control.addSlider("changeSmoothingSkeleton",0.0f,1.0f,TherapeuticPresence.smoothingSkeleton,0,positionY,108,20);
		smoothingSkeleton.moveTo(menu);
		smoothingSkeleton.setCaptionLabel("Skeleton Smoothing");
		smoothingSkeleton.plugTo(this);
		positionY += 20;
		
		controlP5.Slider sceneAnimationSpeed = control.addSlider("changeSceneAnimationSpeed",0.0f,10.0f,AbstractScene.animationSpeed,0,positionY,108,20);
		sceneAnimationSpeed.moveTo(menu);
		sceneAnimationSpeed.setCaptionLabel("Scene Animation Speed");
		sceneAnimationSpeed.plugTo(this);
		positionY += 20;
		
		controlP5.Slider movementResponseDelay = control.addSlider("changeMovementResponseDelay",0.0f,20.0f,AbstractSkeletonAudioVisualisation.movementResponseDelay,0,positionY,108,20);
		movementResponseDelay.moveTo(menu);
		movementResponseDelay.setCaptionLabel("Movement Response");
		movementResponseDelay.plugTo(this);
		positionY += 20;
		
		controlP5.Slider angleScale1 = control.addSlider("changeAngleScale1",0.0f,1.0f,AbstractSkeletonVisualisation.angleScale1,0,positionY,108,20);
		angleScale1.moveTo(menu);
		angleScale1.setCaptionLabel("Angle Scale 1");
		angleScale1.plugTo(this);
		positionY += 20;
		
		controlP5.Slider angleScale2 = control.addSlider("changeAngleScale2",0.0f,1.0f,AbstractSkeletonVisualisation.angleScale2,0,positionY,108,20);
		angleScale2.moveTo(menu);
		angleScale2.setCaptionLabel("Angle Scale 2");
		angleScale2.plugTo(this);
		positionY += 20;
		
		controlP5.Slider angleScale3 = control.addSlider("changeAngleScale3",0.0f,1.0f,AbstractSkeletonVisualisation.angleScale3,0,positionY,108,20);
		angleScale3.moveTo(menu);
		angleScale3.setCaptionLabel("Angle Scale 3");
		angleScale3.plugTo(this);
		positionY += 20;
		
		controlP5.Slider kinectTiltDegree = control.addSlider("changeKinectTilt",-45f,45f,TherapeuticPresence.kinectTiltDegree,0,positionY,108,20);
		kinectTiltDegree.moveTo(menu);
		kinectTiltDegree.setCaptionLabel("Kinect Tilt");
		kinectTiltDegree.plugTo(this);
		positionY += 20;
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
		menu.hide();
	}
	
	private void switchMirrorTherapyLeft (int theValue) {
		mainApplet.switchMirrorTherapy(Skeleton.MIRROR_THERAPY_LEFT);
		menu.hide();
	}
	
	private void switchMirrorTherapyRight (int theValue) {
		mainApplet.switchMirrorTherapy(Skeleton.MIRROR_THERAPY_RIGHT);
		menu.hide();
	}

	private void switchScene (int theValue) {
		if (TherapeuticPresence.currentSceneType == TherapeuticPresence.TUNNEL_SCENE3D) {
			mainApplet.setupScene(TherapeuticPresence.LIQUID_SCENE3D);
		} else {
			mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
		}
		menu.hide();
	}
	
	private void switchVisualisationDepthMap (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.DEPTHMAP_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationSkeletons (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.STICKFIGURE_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationGeometry3D (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.GEOMETRY_3D_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationTree3D (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.LIQUID_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_3D_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationEllipsoid3D (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.LIQUID_SCENE3D);
		mainApplet.setupVisualisation(TherapeuticPresence.ELLIPSOIDAL_3D_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationGeometry2D (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE2D);
		mainApplet.setupVisualisation(TherapeuticPresence.GEOMETRY_2D_VISUALISATION);
		menu.hide();
	}
	
	private void switchTaskMode (int theValue) {
		mainApplet.switchStructuredTaskMode();
	}
	
	private void switchDemo (int theValue) {
		TherapeuticPresence.demo = !TherapeuticPresence.demo;
	}
	
	private void switchDebugOuput (int theValue) {
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

	private void changeGestureTolerance (float theValue) {
		mainApplet.changeGestureTolerance(theValue);
	}
	private void changeSmoothingSkeleton (float theValue) {
		mainApplet.changeSmoothingSkeleton(theValue);
	}
	private void changeSceneAnimationSpeed (float theValue) {
		AbstractScene.animationSpeed = theValue;
	}
	private void changeMovementResponseDelay (float theValue) {
		AbstractSkeletonAudioVisualisation.movementResponseDelay = theValue;
	}
	private void changeAngleScale1 (float theValue) {
		AbstractSkeletonVisualisation.angleScale1 = theValue;
	}
	private void changeAngleScale2 (float theValue) {
		AbstractSkeletonVisualisation.angleScale2 = theValue;
	}
	private void changeAngleScale3 (float theValue) {
		AbstractSkeletonVisualisation.angleScale3 = theValue;
	}
	private void changeKinectTilt (float theValue) {
		mainApplet.changeKinectTilt(theValue);
	}
	
	
}
