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
import therapeuticskeleton.*;
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
	private Textarea liveStatistics;
	private Textarea finalStatistics;
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
			case SkeletonPosture.V_SHAPE: shapeString="V"; break;
			case SkeletonPosture.A_SHAPE: shapeString="A"; break;
			case SkeletonPosture.U_SHAPE: shapeString="U"; break;
			case SkeletonPosture.N_SHAPE: shapeString="N"; break;
			case SkeletonPosture.M_SHAPE: shapeString="M"; break;
			case SkeletonPosture.W_SHAPE: shapeString="W"; break;
			case SkeletonPosture.O_SHAPE: shapeString="O"; break;
			case SkeletonPosture.I_SHAPE: shapeString="I"; break;
			case SkeletonPosture.HANDS_FORWARD_DOWN_POSE: shapeString="HF"; break;
			default: shapeString="NO"+PostureProcessing.activePosture; break;
		}
	    fps.setText("pG "+SkeletonGesture.pushGestureStartCycle+" g "+PostureProcessing.currentGesture+" shape: "+shapeString+" fps: "+PApplet.round(mainApplet.frameRate));
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
	    if (TherapeuticPresence.showLiveStatistics) {
	    	liveStatistics.show();
	    } else {
	    	liveStatistics.hide();
	    }
	    if (TherapeuticPresence.currentVisualisationMethod == TherapeuticPresence.STATISTICS_VISUALISATION) {
	    	liveStatistics.hide();
	    	finalStatistics.show();
	    } else {
	    	finalStatistics.hide();
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
	
	public void updateLiveStatistics (float dPSLeftHand, float dPSLeftElbow, float dPSRightHand, float dPSRightElbow) {
		DecimalFormat df = new DecimalFormat ("0.00");
		liveStatistics.setText("mm per second\nLH: "+df.format(dPSLeftHand)+"\nLE: "+df.format(dPSLeftElbow)+"\nRH: "+df.format(dPSRightHand)+"\nRE: "+df.format(dPSRightElbow));
	}
	
	public void setFinalStatistics (float dLeftHand, float dLeftElbow, float dRightHand, float dRightElbow) {
		DecimalFormat df = new DecimalFormat ("0.00");
		finalStatistics.setText("distance in mm\nLH: "+df.format(dLeftHand)+"\nLE: "+df.format(dLeftElbow)+"\nRH: "+df.format(dRightHand)+"\nRE: "+df.format(dRightElbow));
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

		liveStatistics = control.addTextarea("liveStatisticsArea","",mainApplet.width-400,10,200,mainApplet.height-20);
		liveStatistics.setFont(mainApplet.createFont("Courier",20));
		liveStatistics.setText("");
		finalStatistics = control.addTextarea("finalStatisticsArea","",mainApplet.width-400,10,200,mainApplet.height-20);
		finalStatistics.setFont(mainApplet.createFont("Courier",20));
		finalStatistics.setText("");
		
		guiMessages = control.addTextarea("guiMessagesArea","",mainApplet.width-200,10,200,mainApplet.height-20);

		score = control.addTextarea("scoreArea","",mainApplet.width/2-100,20,70,70);
		score.setFont(mainApplet.createFont("Helvetica",30));
		score.setText(""+0);
		
		task = control.addTextarea("taskArea","",mainApplet.width/2-400,20,300,70);
		task.setFont(mainApplet.createFont("Helvetica",30));
		task.setText(""+0);
	}
	
	
	private void createMenu() {
		// create a group to store the menu elements
		menu = control.addGroup("Menu",0,20,200);
		menu.setBackgroundHeight(500);
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
		mirrorTherapyLeft.setCaptionLabel("Mirror left");
		mirrorTherapyLeft.plugTo(this);
		positionY += 20;
		
		controlP5.Button mirrorTherapyRight = control.addButton("switchMirrorTherapyRight",Skeleton.MIRROR_THERAPY_RIGHT,0,positionY,200,20);
		mirrorTherapyRight.moveTo(menu);
		mirrorTherapyRight.setCaptionLabel("Mirror right");
		mirrorTherapyRight.plugTo(this);
		positionY += 20;
		
		controlP5.Button switchScene = control.addButton("switchScene",TherapeuticPresence.TUNNEL_SCENE,0,positionY,200,20);
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
		
		controlP5.Button drawWaveform = control.addButton("switchVisualisationWaveform",TherapeuticPresence.WAVEFORM_VISUALISATION,0,positionY,200,20);
		drawWaveform.moveTo(menu);
		drawWaveform.setCaptionLabel("Draw Waveform");
		drawWaveform.plugTo(this);
		positionY += 20;
		
		controlP5.Button drawTree = control.addButton("switchVisualisationTree",TherapeuticPresence.GENERATIVE_TREE_VISUALISATION,0,positionY,200,20);
		drawTree.moveTo(menu);
		drawTree.setCaptionLabel("Draw Tree");
		drawTree.plugTo(this);
		positionY += 20;
		
		controlP5.Button drawEllipsoid = control.addButton("switchVisualisationEllipsoid",TherapeuticPresence.ELLIPSOIDAL_VISUALISATION,0,positionY,200,20);
		drawEllipsoid.moveTo(menu);
		drawEllipsoid.setCaptionLabel("Draw Ellipsoid");
		drawEllipsoid.plugTo(this);
		positionY += 20;
		
		controlP5.Button drawStatistics = control.addButton("switchVisualisationStatistics",TherapeuticPresence.STATISTICS_VISUALISATION,0,positionY,200,20);
		drawStatistics.moveTo(menu);
		drawStatistics.setCaptionLabel("Draw Statistics");
		drawStatistics.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea showLiveStatisticsLabel = control.addTextarea("showLiveStatisticsLabel","Live Statistics",2,positionY+4,178,16);
		showLiveStatisticsLabel.moveTo(menu);
		controlP5.Toggle showLiveStatistics = control.addToggle("showLiveStatistics",TherapeuticPresence.showLiveStatistics,180,positionY,20,20);
		showLiveStatistics.moveTo(menu);
		showLiveStatistics.setLabelVisible(false);
		showLiveStatistics.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea switchTaskModeLabel = control.addTextarea("switchTaskModeLabel","Task Mode",2,positionY+4,178,16);
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
		
		controlP5.Button closeApplication = control.addButton("closeApplication",0,0,positionY,200,20);
		closeApplication.setColorBackground(mainApplet.color(150,0,0));
		closeApplication.setColorForeground(mainApplet.color(200,0,0));
		closeApplication.moveTo(menu);
		closeApplication.setCaptionLabel("Close Application");
		closeApplication.plugTo(this);
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
		mainApplet.toggleScenes();
		menu.hide();
	}
	
	private void switchVisualisationDepthMap (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE);
		mainApplet.setupVisualisation(TherapeuticPresence.DEPTHMAP_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationSkeletons (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE);
		mainApplet.setupVisualisation(TherapeuticPresence.STICKFIGURE_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationWaveform (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.TUNNEL_SCENE);
		mainApplet.setupVisualisation(TherapeuticPresence.WAVEFORM_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationTree (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.LIQUID_SCENE);
		mainApplet.setupVisualisation(TherapeuticPresence.GENERATIVE_TREE_VISUALISATION);
		menu.hide();
	}
	
	private void switchVisualisationEllipsoid (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.LIQUID_SCENE);
		mainApplet.setupVisualisation(TherapeuticPresence.ELLIPSOIDAL_VISUALISATION);
		menu.hide();
	}

	private void switchVisualisationStatistics (int theValue) {
		mainApplet.setupScene(TherapeuticPresence.BASIC_SCENE);
		mainApplet.setupVisualisation(TherapeuticPresence.STATISTICS_VISUALISATION);
		menu.hide();
	}
	
	private void showLiveStatistics (int theValue) {
		TherapeuticPresence.showLiveStatistics = !TherapeuticPresence.showLiveStatistics;
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
	private void closeApplication (float theValue) {
		mainApplet.close();
	}
	
	
}
