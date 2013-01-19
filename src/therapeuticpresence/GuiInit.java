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
import javax.swing.*;

public class GuiInit {

	// Link to main Applet to draw on
	private TherapeuticPresence mainApplet;
	private PGraphics3D g3;
	
	// The HUD controls
	private ControlP5 control;
	private ControlGroup menu;
	
	private boolean startTherapy = false;

	public DecimalFormat df = new DecimalFormat("0.00");
	
	public GuiInit (TherapeuticPresence _mainApplet) {

		mainApplet = _mainApplet;
		control = new ControlP5(mainApplet);
		g3 = (PGraphics3D)_mainApplet.g;
		
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		
		createMenu();
	}
	
	public void draw () {
		// store camera matrix
		PMatrix3D currentCamMatrix = new PMatrix3D(g3.camera); 
		// reset camera to draw HUD
		g3.camera();
		mainApplet.colorMode(PConstants.RGB,255,255,255,255);
		mainApplet.noLights();
	    // draw the HUD
		control.draw();   
		// set camera back to old position
		g3.camera = currentCamMatrix;
	}
	
	public boolean startTherapy () {
		return startTherapy;
	}
	
	
	private void createMenu() {
		// create a group to store the menu elements
		menu = control.addGroup("Menu",mainApplet.width/2-200,50,400);
		menu.setBackgroundHeight(380);
		menu.setBackgroundColor(mainApplet.color(70,70));
		menu.hideBar();
		
		int positionY=0;

		controlP5.Button pickMusicTrackButton = control.addButton("pickMusicTrack",0,0,positionY,400,20);
		pickMusicTrackButton.moveTo(menu);
		pickMusicTrackButton.setCaptionLabel("Pick music track");
		pickMusicTrackButton.plugTo(this);
		positionY += 20;
		
		controlP5.Button mirrorTherapyOff = control.addButton("switchMirrorTherapyOff",Skeleton.MIRROR_THERAPY_OFF,0,positionY,400,20);
		mirrorTherapyOff.moveTo(menu);
		mirrorTherapyOff.setCaptionLabel("1: Mirror off");
		mirrorTherapyOff.plugTo(this);
		positionY += 20;
		
		controlP5.Button mirrorTherapyLeft = control.addButton("switchMirrorTherapyLeft",Skeleton.MIRROR_THERAPY_LEFT,0,positionY,400,20);
		mirrorTherapyLeft.moveTo(menu);
		mirrorTherapyLeft.setCaptionLabel("2: Mirror left");
		mirrorTherapyLeft.plugTo(this);
		positionY += 20;
		
		controlP5.Button mirrorTherapyRight = control.addButton("switchMirrorTherapyRight",Skeleton.MIRROR_THERAPY_RIGHT,0,positionY,400,20);
		mirrorTherapyRight.moveTo(menu);
		mirrorTherapyRight.setCaptionLabel("3: Mirror right");
		mirrorTherapyRight.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea showLiveStatisticsLabel = control.addTextarea("showLiveStatisticsLabel","Live Statistics",2,positionY+4,378,16);
		showLiveStatisticsLabel.moveTo(menu);
		controlP5.Toggle showLiveStatistics = control.addToggle("showLiveStatistics",TherapeuticPresence.showLiveStatistics,180,positionY,20,20);
		showLiveStatistics.moveTo(menu);
		showLiveStatistics.setLabelVisible(false);
		showLiveStatistics.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea switchTaskModeLabel = control.addTextarea("switchTaskModeLabel","Task Mode",2,positionY+4,378,16);
		switchTaskModeLabel.moveTo(menu);
		controlP5.Toggle switchTaskMode = control.addToggle("switchTaskMode",TherapeuticPresence.structuredTaskMode,180,positionY,20,20);
		switchTaskMode.moveTo(menu);
		switchTaskMode.setLabelVisible(false);
		switchTaskMode.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea demoLabel = control.addTextarea("demoLabel","Toggle Demo",2,positionY+4,378,16);
		demoLabel.moveTo(menu);
		controlP5.Toggle demo = control.addToggle("switchDemo",TherapeuticPresence.demo,180,positionY,20,20);
		demo.moveTo(menu);
		demo.setLabelVisible(false);
		demo.plugTo(this);
		positionY += 20;
		
		controlP5.Textarea debugOutputLabel = control.addTextarea("debugOutputLabel","Toggle debugOutput",2,positionY+4,378,16);
		debugOutputLabel.moveTo(menu);
		controlP5.Toggle debugOuput = control.addToggle("switchDebugOuput",TherapeuticPresence.debugOutput,180,positionY,20,20);
		debugOuput.moveTo(menu);
		debugOuput.setLabelVisible(false);
		debugOuput.plugTo(this);
		positionY += 20;
		
		controlP5.Slider fftGain = control.addSlider("changeFFTGain",0.0f,1.0f,AudioManager.gain,0,positionY,308,20);
		fftGain.moveTo(menu);
		fftGain.setCaptionLabel("FFT Gain");
		fftGain.plugTo(this);
		positionY += 20;
		
		controlP5.Slider maxDistanceToKinect = control.addSlider("changeMaxDistanceToKinect",0.0f,5000.0f,TherapeuticPresence.maxDistanceToKinect,0,positionY,308,20);
		maxDistanceToKinect.moveTo(menu);
		maxDistanceToKinect.setCaptionLabel("Max dist");
		maxDistanceToKinect.plugTo(this);
		positionY += 20;
		
		controlP5.Slider postureTolerance = control.addSlider("changePostureTolerance",0.0f,1.0f,TherapeuticPresence.postureTolerance,0,positionY,308,20);
		postureTolerance.moveTo(menu);
		postureTolerance.setCaptionLabel("Posture Tolerance");
		postureTolerance.plugTo(this);
		positionY += 20;
		
		controlP5.Slider gestureTolerance = control.addSlider("changeGestureTolerance",0.0f,1.0f,TherapeuticPresence.gestureTolerance,0,positionY,308,20);
		gestureTolerance.moveTo(menu);
		gestureTolerance.setCaptionLabel("Gesture Tolerance");
		gestureTolerance.plugTo(this);
		positionY += 20;
		
		controlP5.Slider smoothingSkeleton = control.addSlider("changeSmoothingSkeleton",0.0f,1.0f,TherapeuticPresence.smoothingSkeleton,0,positionY,308,20);
		smoothingSkeleton.moveTo(menu);
		smoothingSkeleton.setCaptionLabel("Skeleton Smoothing");
		smoothingSkeleton.plugTo(this);
		positionY += 20;
		
		controlP5.Slider sceneAnimationSpeed = control.addSlider("changeSceneAnimationSpeed",0.0f,10.0f,AbstractScene.animationSpeed,0,positionY,308,20);
		sceneAnimationSpeed.moveTo(menu);
		sceneAnimationSpeed.setCaptionLabel("Scene Animation Speed");
		sceneAnimationSpeed.plugTo(this);
		positionY += 20;
		
		controlP5.Slider movementResponseDelay = control.addSlider("changeMovementResponseDelay",4f,10f,AbstractSkeletonAudioVisualisation.movementResponseDelay,0,positionY,308,20);
		movementResponseDelay.moveTo(menu);
		movementResponseDelay.setCaptionLabel("Movement Response");
		movementResponseDelay.plugTo(this);
		positionY += 20;
		
		controlP5.Slider angleScale1 = control.addSlider("changeAngleScale1",0.0f,1.0f,AbstractSkeletonVisualisation.angleScale1,0,positionY,308,20);
		angleScale1.moveTo(menu);
		angleScale1.setCaptionLabel("Angle Scale 1");
		angleScale1.plugTo(this);
		positionY += 20;
		
		controlP5.Slider angleScale2 = control.addSlider("changeAngleScale2",0.0f,1.0f,AbstractSkeletonVisualisation.angleScale2,0,positionY,308,20);
		angleScale2.moveTo(menu);
		angleScale2.setCaptionLabel("Angle Scale 2");
		angleScale2.plugTo(this);
		positionY += 20;
		
		controlP5.Slider angleScale3 = control.addSlider("changeAngleScale3",0.0f,1.0f,AbstractSkeletonVisualisation.angleScale3,0,positionY,308,20);
		angleScale3.moveTo(menu);
		angleScale3.setCaptionLabel("Angle Scale 3");
		angleScale3.plugTo(this);
		positionY += 20;
		
		controlP5.Button startTherapy = control.addButton("startTherapy",0,0,positionY,400,20);
		startTherapy.setColorBackground(mainApplet.color(150,0,0));
		startTherapy.setColorForeground(mainApplet.color(200,0,0));
		startTherapy.moveTo(menu);
		startTherapy.setCaptionLabel("Start Therapy");
		startTherapy.plugTo(this);
		positionY += 20;
	}
	
	private void pickMusicTrack (int theValue) {
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) { 
			e.printStackTrace();  
		} 
		
		// create a file chooser 
		final JFileChooser fc = new JFileChooser(); 
			 
		// in response to a button click: 
		int returnVal = fc.showOpenDialog(mainApplet); 
			 
		if (returnVal == JFileChooser.APPROVE_OPTION) { 
			String fileName = fc.getSelectedFile().getPath(); 
			// see if it's an audiotrack  
			if (fileName.endsWith("mp3")) { 
			    PApplet.println("pickMusicTrack: chosen track is "+fileName);
			    TherapeuticPresence.audioFile = fileName;
			} else { 
			    PApplet.println("pickMusicTrack: wrong filetype chosen track is "+fileName); 
			} 
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
	private void startTherapy (float theValue) {
		startTherapy = true;
		menu.hide();
	}
	
	
}
