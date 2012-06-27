package therapeuticpresence;

import processing.core.PApplet;
import processing.core.PVector;

public class BezierCurve3D {
	public static final int MAX_POINTS = 16;
	public static final float FADE_OUT_SECONDS = 0.8f;
	public static final float MAX_TRANSPARENCY = 255f;
	public static final short LINEAR_DEGRESSION = 0;
	public static final short EXPONENTIAL_DEGRESSION = 1;
	
	private PVector anchorPoints[] = new PVector[MAX_POINTS];
	private int anchorPointsCounter = 0;
	private PVector controlPoints[] = new PVector[MAX_POINTS-1];
	private int controlPointsCounter = 0;
	
	private float strokeOffset = 0f; 
	private float strokeOffsetGrowth = 1.7f;
	private int color;
	public float transparency = MAX_TRANSPARENCY;
	private short degressionMode = LINEAR_DEGRESSION;
	private float scaleDegression = 2f;
	
	public BezierCurve3D (float _strokeOffsetGrowth, int _color) {
		strokeOffsetGrowth = _strokeOffsetGrowth;
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
			_mainApplet.stroke(color,transparency);
			_mainApplet.noFill();
			//_mainApplet.strokeWeight(strokeWeight*=GROWTH_FACTOR);
			// use multiple lines instead of high stroke weight. thick lines produce artifacts
			for (float j=-strokeOffset; j<=strokeOffset;j+=strokeOffset) {
				if (strokeOffset != 0 && j == 0) {
					continue;
				}
				_mainApplet.beginShape();
				for (int i=0; i<anchorPointsCounter-1; i++) {
					if (i==0) _mainApplet.vertex(anchorPoints[i].x,anchorPoints[i].y+j,anchorPoints[i].z);
					_mainApplet.bezierVertex(controlPoints[i].x,controlPoints[i].y+j,controlPoints[i].z,controlPoints[i].x,controlPoints[i].y+j,controlPoints[i].z,anchorPoints[i+1].x,anchorPoints[i+1].y+j,anchorPoints[i+1].z);
				}
				_mainApplet.endShape();
				if (strokeOffset == 0) j+=1;
			}
			strokeOffset += strokeOffsetGrowth;
			transparency -= degression(_mainApplet.frameRate);
		}
	}
	
	private float degression (float frameRate) {
		return MAX_TRANSPARENCY/(frameRate*FADE_OUT_SECONDS);
	}
}
