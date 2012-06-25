package therapeuticpresence;

import processing.core.PApplet;
import processing.core.PVector;

public class BezierCurve3D {
	public static final int MAX_POINTS = 16;
	public static final float FADE_OUT_SECONDS = 0.15f;
	public static final float MAX_TRANSPARENCY = 255f;
	public static final float GROWTH_FACTOR = 1.9f;
	public static final float GROWTH_STEPS = 3f;
	
	private PVector anchorPoints[] = new PVector[MAX_POINTS];
	private int anchorPointsCounter = 0;
	private PVector controlPoints[] = new PVector[MAX_POINTS-1];
	private int controlPointsCounter = 0;
	
	private int strokeWeight; 
	private int color;
	public float transparency = 0.7f*MAX_TRANSPARENCY;
	
	public BezierCurve3D (int _strokeWeight, int _color) {
		strokeWeight = _strokeWeight;
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
	
	public void draw (PApplet _mainApplet) {
		if (transparency > 0f && anchorPointsCounter > 1 && controlPointsCounter > 0) {
			_mainApplet.stroke(color,transparency);
			_mainApplet.noFill();
			//_mainApplet.strokeWeight(strokeWeight*=GROWTH_FACTOR);
			// use multiple lines instead of high stroke weight. thick lines produce artifacts
			for (int j=-strokeWeight; j<=strokeWeight;j+=GROWTH_STEPS) {
				_mainApplet.beginShape();
				for (int i=0; i<anchorPointsCounter-1; i++) {
					if (i==0) _mainApplet.vertex(anchorPoints[i].x,anchorPoints[i].y+j,anchorPoints[i].z);
					_mainApplet.bezierVertex(controlPoints[i].x,controlPoints[i].y+j,controlPoints[i].z,controlPoints[i].x,controlPoints[i].y+j,controlPoints[i].z,anchorPoints[i+1].x,anchorPoints[i+1].y+j,anchorPoints[i+1].z);
				}
				_mainApplet.endShape();
			}
			strokeWeight = PApplet.round(strokeWeight*GROWTH_FACTOR);
			transparency -= MAX_TRANSPARENCY/(FADE_OUT_SECONDS*_mainApplet.frameRate);
		}
	}
}
