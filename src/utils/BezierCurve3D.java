package utils;

import processing.core.PApplet;
import processing.core.PVector;
import scenes.TunnelScene3D;
import therapeuticpresence.TherapeuticPresence;

public class BezierCurve3D {
	public static final int MAX_POINTS = 16;
	public static final float FADE_OUT_SECONDS = 0.6f;
	public static final float MAX_TRANSPARENCY = 255f;
	public static final short LINEAR_REGRESSION = 0;
	public static final short QUADRATIC_REGRESSION = 1;
	
	private PVector anchorPoints[] = new PVector[MAX_POINTS];
	private int anchorPointsCounter = 0;
	private PVector controlPoints[] = new PVector[MAX_POINTS-1];
	private int controlPointsCounter = 0;
	
	// use growing stroke weight or multiple lines for fade out
	private boolean useStrokeWeight = true;
	private float strokeOffset = 0f; 
	private float strokeOffsetGrowth = 1.2f;
	private float strokeWeight = 1f;
	private final float strokeWeightGrowth = 1.2f;
	
	public float zOffset = 0f;
	public float centerZ = 0f;
	
	private int color;
	public float transparency = MAX_TRANSPARENCY;
	private short regressionMode = QUADRATIC_REGRESSION;
	private int framesAlive = 0;
	private float fadeOutFrames;
	
	public BezierCurve3D (float _initialStrokeShape, int _color) {
		strokeWeight = _initialStrokeShape;
		strokeOffset = _initialStrokeShape;
		color = _color;
	}
	public void addAnchorPoint (PVector _anchorPoint) {
		if (anchorPointsCounter < MAX_POINTS) {
			anchorPoints[anchorPointsCounter++] = _anchorPoint;
		}
	}
	public void addControlPoint (PVector _controlPoint) {
		if (controlPointsCounter < MAX_POINTS) {
			controlPoints[controlPointsCounter++] = _controlPoint;
		}
	}
	
	public void draw (TherapeuticPresence _mainApplet) {
		if (transparency > 0f && anchorPointsCounter > 1 && controlPointsCounter > 0) {
			fadeOutFrames = _mainApplet.frameRate*FADE_OUT_SECONDS;
			
			_mainApplet.stroke(color,transparency);
			_mainApplet.noFill();
			
			if (useStrokeWeight) 
				drawUsingStrokeWeight(_mainApplet);
			else 
				drawUsingStrokeOffset(_mainApplet);
			
			transparency -= regression(fadeOutFrames);
			if (++framesAlive >= fadeOutFrames) transparency = 0f;
			zOffset += TunnelScene3D.tunnelLength/_mainApplet.frameRate*FADE_OUT_SECONDS;
			centerZ=controlPoints[controlPointsCounter/2].z;
		}
	}
	
	private void drawUsingStrokeWeight (TherapeuticPresence _mainApplet) {
		_mainApplet.strokeWeight(strokeWeight*=strokeWeightGrowth);
		_mainApplet.beginShape();
		for (int i=0; i<anchorPointsCounter-1; i++) {
			if (i==0) _mainApplet.vertex(anchorPoints[i].x,anchorPoints[i].y,anchorPoints[i].z-zOffset);
			_mainApplet.bezierVertex(controlPoints[i].x,controlPoints[i].y,controlPoints[i].z-zOffset,controlPoints[i].x,controlPoints[i].y,controlPoints[i].z-zOffset,anchorPoints[i+1].x,anchorPoints[i+1].y,anchorPoints[i+1].z-zOffset);
		}
		_mainApplet.endShape();
	}
	
	private void drawUsingStrokeOffset (TherapeuticPresence _mainApplet) {
		for (float j=-strokeOffset; j<=strokeOffset;j+=strokeOffset) {
			_mainApplet.beginShape();
			for (int i=0; i<anchorPointsCounter-1; i++) {
				if (i==0) _mainApplet.vertex(anchorPoints[i].x,anchorPoints[i].y+j,anchorPoints[i].z-zOffset);
				_mainApplet.bezierVertex(controlPoints[i].x,controlPoints[i].y+j,controlPoints[i].z-zOffset,controlPoints[i].x,controlPoints[i].y+j,controlPoints[i].z-zOffset,anchorPoints[i+1].x,anchorPoints[i+1].y+j,anchorPoints[i+1].z-zOffset);
			}
			_mainApplet.endShape();
			if (strokeOffset == 0) break;
		}
		strokeOffset *= strokeOffsetGrowth;
	}
	
	private float regression (float frameRate) {
		if (regressionMode == QUADRATIC_REGRESSION) {
			return PApplet.pow(transparency/fadeOutFrames,2f);
		} else {
			return MAX_TRANSPARENCY/fadeOutFrames;
		}
	}
}
