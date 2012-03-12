package therapeuticpresence;

import java.text.DecimalFormat;

import processing.core.*;
import controlP5.*;
import processing.opengl.*;

public class GuiHud {

	// Link to main Applet to draw on
	TherapeuticPresence mainApplet;
	PGraphics3D g3;
	
	// The menu
	public ControlP5 control;
	ControlGroup menu;
	
	// Last selection of mirror therapy
	public short mirrorTherapy = Skeleton.MIRROR_OFF;
	
	// Last selection of visualisation
	public short visualisation = TherapeuticPresence.DRAW_DEPTHMAP;
	
	// font to display text
	PFont font;
	public int guiTextColor = 255;
	
	String[] guiMessages;
	int guiMessagesCount = 0;
	int guiMessagesTextSize = 24;

	public DecimalFormat df = new DecimalFormat("0.00");
	
	public GuiHud (TherapeuticPresence _mainApplet) {

		mainApplet = _mainApplet;
		control = new ControlP5(_mainApplet);
		createMenu();
		menu.hide();
		controlP5.Button b = control.addButton("toggleMenu",1,0,0,100,20);
		b.setLabel("Toggle Menu");
		b.plugTo(this);
		font = mainApplet.createFont("", 12, true);
		g3 = (PGraphics3D)_mainApplet.g;
		guiMessages = new String[mainApplet.height/guiMessagesTextSize];
	}
	
	public void draw () {

		// store camera matrix
		PMatrix3D currentCamMatrix = new PMatrix3D(g3.camera); 
		// reset camera to draw HUD
		g3.camera();
	    mainApplet.fill(guiTextColor);
	    
	    // display fps
	    mainApplet.textFont(font);
	    mainApplet.text("fps: "+mainApplet.round(mainApplet.frameRate),20,mainApplet.height-40);
	    
	    // guiMessages
	    mainApplet.textFont(font,guiMessagesTextSize);
	    mainApplet.textAlign(PApplet.RIGHT);
	    for (int i=0; i<guiMessagesCount; i++) {
	    	mainApplet.text(guiMessages[i],mainApplet.width,(i+1)*guiMessagesTextSize);
	    }
	    mainApplet.textAlign(PApplet.LEFT);
	    
	    // draw the menu
		control.draw(); 
		// set camera back to old position
		g3.camera = currentCamMatrix;
	}
	
	public void sendGuiMessage (String s) {
		// swap messages in the array
		if (guiMessagesCount == guiMessages.length) {
			int i=0;
			for (; i<guiMessages.length-1; i++) {
				guiMessages[i] = guiMessages[i+1];
			}
			guiMessages[i] = s;
		} else {
			guiMessages[guiMessagesCount++] = s;
		}	
	}	
	
	public void resetGuiMessages () {
		guiMessagesCount = 0;
	}
	
	private void createMenu() {
		// create a group to store the messageBox elements
		menu = control.addGroup("Menu",0,20,150);
		menu.setBackgroundHeight(120);
		menu.setBackgroundColor(mainApplet.color(0,100));
		menu.hideBar();
		
		controlP5.Button mirrorTherapyOff = control.addButton("switchMirrorTherapyOff",Skeleton.MIRROR_OFF,0,0,150,20);
		mirrorTherapyOff.moveTo(menu);
		mirrorTherapyOff.setColorBackground(mainApplet.color(40));
		mirrorTherapyOff.setColorActive(mainApplet.color(20));
		mirrorTherapyOff.setCaptionLabel("MT OFF");
		mirrorTherapyOff.plugTo(this);
		
		controlP5.Button mirrorTherapyLeft = control.addButton("switchMirrorTherapyLeft",Skeleton.MIRROR_LEFT,0,20,150,20);
		mirrorTherapyLeft.moveTo(menu);
		mirrorTherapyLeft.setColorBackground(mainApplet.color(40));
		mirrorTherapyLeft.setColorActive(mainApplet.color(20));
		mirrorTherapyLeft.setCaptionLabel("MT LEFT");
		mirrorTherapyLeft.plugTo(this);
		
		controlP5.Button mirrorTherapyRight = control.addButton("switchMirrorTherapyRight",Skeleton.MIRROR_RIGHT,0,40,150,20);
		mirrorTherapyRight.moveTo(menu);
		mirrorTherapyRight.setColorBackground(mainApplet.color(40));
		mirrorTherapyRight.setColorActive(mainApplet.color(20));
		mirrorTherapyRight.setCaptionLabel("MT RIGHT");
		mirrorTherapyRight.plugTo(this);
		
		controlP5.Button drawDepthMap = control.addButton("switchVisualisationDepthMap",TherapeuticPresence.DRAW_DEPTHMAP,0,60,150,20);
		drawDepthMap.moveTo(menu);
		drawDepthMap.setColorBackground(mainApplet.color(40));
		drawDepthMap.setColorActive(mainApplet.color(20));
		drawDepthMap.setCaptionLabel("Draw DepthMap");
		drawDepthMap.plugTo(this);
		
		controlP5.Button drawSkeletons = control.addButton("switchVisualisationSkeletons",TherapeuticPresence.DRAW_SKELETONS,0,80,150,20);
		drawSkeletons.moveTo(menu);
		drawSkeletons.setColorBackground(mainApplet.color(40));
		drawSkeletons.setColorActive(mainApplet.color(20));
		drawSkeletons.setCaptionLabel("Draw Skeletons");
		drawSkeletons.plugTo(this);
		
		controlP5.Button drawTree = control.addButton("switchVisualisationTree",TherapeuticPresence.DRAW_TREE,0,100,150,20);
		drawTree.moveTo(menu);
		drawTree.setColorBackground(mainApplet.color(40));
		drawTree.setColorActive(mainApplet.color(20));
		drawTree.setCaptionLabel("Draw Tree");
		drawTree.plugTo(this);
		
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
		mirrorTherapy = Skeleton.MIRROR_OFF;
		mainApplet.switchMirrorTherapy();
	}
	
	private void switchMirrorTherapyLeft (int theValue) {
		mirrorTherapy = Skeleton.MIRROR_LEFT;
		mainApplet.switchMirrorTherapy();
	}
	
	private void switchMirrorTherapyRight (int theValue) {
		mirrorTherapy = Skeleton.MIRROR_RIGHT;
		mainApplet.switchMirrorTherapy();
	}
	
	private void switchVisualisationDepthMap (int theValue) {
		visualisation = TherapeuticPresence.DRAW_DEPTHMAP;
		mainApplet.switchVisualisation();
	}
	
	private void switchVisualisationSkeletons (int theValue) {
		visualisation = TherapeuticPresence.DRAW_SKELETONS;
		mainApplet.switchVisualisation();
	}
	
	private void switchVisualisationTree (int theValue) {
		visualisation = TherapeuticPresence.DRAW_TREE;
		mainApplet.switchVisualisation();
	}

	
	
}
