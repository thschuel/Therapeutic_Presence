package utils;

import processing.core.*;
import generativedesign.*;
import scenes.TunnelScene3D;
import therapeuticpresence.AudioManager;
import therapeuticpresence.TherapeuticPresence;

public class Mesh3D {
	private PApplet mainApplet;
	public static final int MAX_POINTS = 16;
	public static final float FADE_OUT_SECONDS = 0.6f;
	public static final float MAX_TRANSPARENCY = 255f;
	public static final short LINEAR_REGRESSION = 0;
	public static final short QUADRATIC_REGRESSION = 1;
	
	private PVector center = new PVector();
	private float zOffset = 0f;
	protected float leftX=0;
	protected float leftY=0;
	protected float rightX=0;
	protected float rightY=0;
	protected float orientation=0;
	public float transparency = MAX_TRANSPARENCY;
	private short regressionMode = QUADRATIC_REGRESSION;
	private int framesAlive = 0;
	private float fadeOutFrames;
	
	private Mesh mesh;
	
	public Mesh3D(PApplet _mainApplet, PVector _center, float _angleLeft, float _angleRight, float _orientation) {
		center=_center;
		orientation=_orientation;
		mainApplet=_mainApplet;
		mesh = new Mesh(mainApplet,Mesh.SINE, 200, 200, -PConstants.PI, _angleLeft, -PConstants.PI, _angleRight);
		mesh.setColorRange(192, 192, 50, 50, 50, 50, MAX_TRANSPARENCY);
	}
	public void draw (PApplet _mainApplet) {
		if (transparency > 0) {
			mesh.setMeshAlpha(transparency);
			fadeOutFrames = _mainApplet.frameRate*FADE_OUT_SECONDS;
			_mainApplet.pushMatrix();
			_mainApplet.translate(center.x,center.y,center.z-zOffset);
			_mainApplet.rotateY(orientation);
			mesh.draw();
			_mainApplet.popMatrix();
			transparency -= regression(fadeOutFrames);
			if (++framesAlive >= fadeOutFrames) transparency = 0f;
			zOffset += TunnelScene3D.tunnelLength/_mainApplet.frameRate*FADE_OUT_SECONDS;
		}
	}
	
	private float regression (float frameRate) {
		if (regressionMode == QUADRATIC_REGRESSION) {
			return PApplet.pow(transparency/fadeOutFrames,2f);
		} else {
			return MAX_TRANSPARENCY/fadeOutFrames;
		}
	}
}
