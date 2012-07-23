package utils;

import processing.core.*;
import scenes.TunnelScene3D;
import therapeuticpresence.AudioManager;
import therapeuticpresence.TherapeuticPresence;

public class Ellipsoid3D {
	public static final int MAX_POINTS = 16;
	public static final float FADE_OUT_SECONDS = 0.6f;
	public static final float MAX_TRANSPARENCY = 255f;
	public static final short LINEAR_REGRESSION = 0;
	public static final short QUADRATIC_REGRESSION = 1;
	
	public PVector center = new PVector();
	public float zOffset = 0f;
	public float width;
	public float height;
	public int strokeColor;
	public float strokeWeight;
	public float transparency = MAX_TRANSPARENCY;
	private short regressionMode = QUADRATIC_REGRESSION;
	private int framesAlive = 0;
	private float fadeOutFrames;
	
	public Ellipsoid3D(float _width, float _height, int _strokeColor, float _strokeWeight) {
		width=_width;
		height=_height;
		strokeColor=_strokeColor;
		strokeWeight=_strokeWeight;
	}
	public void draw (TherapeuticPresence _mainApplet) {
		if (transparency > 0) {
			fadeOutFrames = _mainApplet.frameRate*FADE_OUT_SECONDS;
			_mainApplet.pushMatrix();
			_mainApplet.translate(center.x,center.y,center.z-zOffset);
			_mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,Ellipsoid3D.MAX_TRANSPARENCY);
			_mainApplet.strokeWeight(strokeWeight);
			_mainApplet.stroke(strokeColor,transparency);
			_mainApplet.noFill();
			_mainApplet.ellipseMode(PConstants.CENTER);
			_mainApplet.ellipse(0,0,width,height);
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
