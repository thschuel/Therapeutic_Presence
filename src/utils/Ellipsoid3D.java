package utils;

import processing.core.*;
import scenes.TunnelScene3D;
import shapes3d.Tube;
import therapeuticpresence.AudioManager;
import therapeuticpresence.TherapeuticPresence;

public class Ellipsoid3D {
	public static final int MAX_POINTS = 16;
	public static final float FADE_OUT_SECONDS = 0.6f;
	public static final float MAX_TRANSPARENCY = 255f;
	public static final short LINEAR_REGRESSION = 0;
	public static final short QUADRATIC_REGRESSION = 1;
	
	private PVector center = new PVector();
	private float zOffset = 0f;
	protected float innerRadius=0;
	protected float leftY=0;
	protected float rightX=0;
	protected float rightY=0;
	protected float orientation=0;
	private int strokeColor;
	private float strokeWeight;
	public float transparency = MAX_TRANSPARENCY;
	private short regressionMode = QUADRATIC_REGRESSION;
	private int framesAlive = 0;
	private float fadeOutFrames;
	private Tube theTube;
	
	public Ellipsoid3D(PVector _center, float _innerRadius, float _outerRadius, float _rightX, float _rightY, float _orientation, int _strokeColor, float _strokeWeight) {
		center=_center;
		innerRadius=_innerRadius;
		leftY=_outerRadius;
		rightX=_rightX;
		rightY=_rightY;
		strokeColor=_strokeColor;
		strokeWeight=_strokeWeight;
		orientation=_orientation;
		//theTube=new Tube();
	}
	public void draw (TherapeuticPresence _mainApplet) {
		if (transparency > 0) {
			fadeOutFrames = _mainApplet.frameRate*FADE_OUT_SECONDS;
			_mainApplet.pushStyle();
			_mainApplet.colorMode(PApplet.HSB,AudioManager.bands,255,255,Ellipsoid3D.MAX_TRANSPARENCY);
			_mainApplet.strokeWeight(strokeWeight);
			_mainApplet.stroke(strokeColor,transparency);
			_mainApplet.noFill();
			_mainApplet.ellipseMode(PConstants.CORNERS);
			_mainApplet.pushMatrix();
			_mainApplet.translate(center.x,center.y,center.z);
			_mainApplet.rotateX(orientation);
			_mainApplet.rotateX(PConstants.HALF_PI);
			_mainApplet.pushMatrix();
			_mainApplet.translate(0,0,-zOffset);
			_mainApplet.ellipse(innerRadius,leftY,rightX,rightY);
			_mainApplet.popMatrix();
			_mainApplet.pushMatrix();
			_mainApplet.translate(0,0,zOffset);
			_mainApplet.ellipse(innerRadius,leftY,rightX,rightY);
			_mainApplet.popMatrix();
			_mainApplet.popMatrix();
			_mainApplet.popStyle();
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
