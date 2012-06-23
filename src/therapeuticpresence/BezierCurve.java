package therapeuticpresence;

import processing.core.PApplet;

public class BezierCurve {
	public static final int MAX_POINTS = 16;
	public static final float FADE_OUT_SECONDS = 0.15f;
	public static final float MAX_TRANSPARENCY = 255f;
	public static final float GROWTH_FACTOR = 1.9f;
	public static final float GROWTH_STEPS = 3f;
	private int anchorPointsX[] = new int[MAX_POINTS], anchorPointsY[] = new int[MAX_POINTS];
	private int anchorPointsCounter = 0;
	private int controlPointsX[] = new int[MAX_POINTS-1], controlPointsY[] = new int[MAX_POINTS-1];
	private int controlPointsCounter = 0;
	
	private int linesCount; 
	private int color;
	public float transparency = 0.7f*MAX_TRANSPARENCY;
	
	public BezierCurve (int _linesCount, int _color) {
		linesCount = _linesCount;
		color = _color;
	}
	public void addAnchorPoint (int _x, int _y) {
		if (anchorPointsCounter < MAX_POINTS) {
			anchorPointsX[anchorPointsCounter] = _x;
			anchorPointsY[anchorPointsCounter++] = _y;
		}
	}
	public void addControlPoint (int _x, int _y) {
		if (controlPointsCounter < MAX_POINTS) {
			controlPointsX[controlPointsCounter] = _x;
			controlPointsY[controlPointsCounter++] = _y;
		}
	}
	
	public void draw (PApplet _mainApplet) {
		if (transparency > 0f && anchorPointsCounter > 1 && controlPointsCounter > 0) {
			_mainApplet.stroke(color,transparency);
			_mainApplet.noFill();
			//_mainApplet.strokeWeight(strokeWeight*=GROWTH_FACTOR);
			// use multiple lines instead of high stroke weight. thick lines produce artefacts
			for (int j=-linesCount; j<=linesCount;j+=GROWTH_STEPS) {
				_mainApplet.beginShape();
				for (int i=0; i<anchorPointsCounter-1; i++) {
					if (i==0) _mainApplet.vertex(anchorPointsX[i],anchorPointsY[i]+j);
					_mainApplet.bezierVertex(controlPointsX[i],controlPointsY[i]+j,controlPointsX[i],controlPointsY[i]+j,anchorPointsX[i+1],anchorPointsY[i+1]+j);
				}
				_mainApplet.endShape();
			}
			linesCount = PApplet.round(linesCount*GROWTH_FACTOR);
			transparency -= MAX_TRANSPARENCY/(FADE_OUT_SECONDS*_mainApplet.frameRate);
		}
	}
}
