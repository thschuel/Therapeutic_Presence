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

package utils;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import scenes.TunnelScene;
import therapeuticpresence.TherapeuticPresence;

public class BezierCurve {
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
	private int drawMethod = 0;
	private float strokeOffset = 0f; 
	private float strokeOffsetGrowth = 1.2f;
	private float strokeWeight = 1f;
	private final float strokeWeightGrowth = 1.2f;
	
	public float zOffset = 0f;
	
	private int color;
	public float transparency = MAX_TRANSPARENCY;
	private short regressionMode = QUADRATIC_REGRESSION;
	private int framesAlive = 0;
	private float fadeOutFrames;
	
	public BezierCurve (float _initialStrokeShape, int _color) {
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
			
			
			if (drawMethod == 0) 
				drawUsingStrokeWeight(_mainApplet);
			else if (drawMethod == 1)
				drawUsingStrokeOffset(_mainApplet);
			else
				drawUsingPolygons(_mainApplet);
			
			transparency -= regression(fadeOutFrames);
			if (++framesAlive >= fadeOutFrames) transparency = 0f;
			zOffset += TunnelScene.tunnelLength/_mainApplet.frameRate*FADE_OUT_SECONDS;
			strokeWeight *= strokeWeightGrowth;
			strokeOffset *= strokeOffsetGrowth;
		}
	}
	
	private void drawUsingStrokeWeight (TherapeuticPresence _mainApplet) {
		_mainApplet.stroke(color,transparency);
		_mainApplet.noFill();
		_mainApplet.strokeWeight(strokeWeight);
		_mainApplet.beginShape();
		for (int i=0; i<anchorPointsCounter-1; i++) {
			if (i==0) { 
				_mainApplet.vertex(anchorPoints[i].x,anchorPoints[i].y,anchorPoints[i].z-zOffset);
			}
			_mainApplet.bezierVertex(controlPoints[i].x,controlPoints[i].y,controlPoints[i].z-zOffset,controlPoints[i].x,controlPoints[i].y,controlPoints[i].z-zOffset,anchorPoints[i+1].x,anchorPoints[i+1].y,anchorPoints[i+1].z-zOffset);
		}
		_mainApplet.endShape();
	}
	
	private void drawUsingPolygons (TherapeuticPresence _mainApplet) {
		_mainApplet.fill(color,transparency);
		_mainApplet.noStroke();
		_mainApplet.beginShape(PConstants.POLYGON);
		for (int i=0; i<anchorPointsCounter-1; i++) {
			if (i==0) { 
				_mainApplet.vertex(anchorPoints[i].x,anchorPoints[i].y-strokeWeight,anchorPoints[i].z-zOffset);
			}
			_mainApplet.bezierVertex(controlPoints[i].x,controlPoints[i].y-strokeWeight,controlPoints[i].z-zOffset,controlPoints[i].x,controlPoints[i].y-strokeWeight,controlPoints[i].z-zOffset,anchorPoints[i+1].x,anchorPoints[i+1].y-strokeWeight,anchorPoints[i+1].z-zOffset);
		}
		for (int i=anchorPointsCounter-2; i>=0; i--) {
			if (i==anchorPointsCounter-2) { 
				_mainApplet.vertex(anchorPoints[i+1].x,anchorPoints[i+1].y+strokeWeight,anchorPoints[i+1].z-zOffset);
			}
			_mainApplet.bezierVertex(controlPoints[i].x,controlPoints[i].y+strokeWeight,controlPoints[i].z-zOffset,controlPoints[i].x,controlPoints[i].y+strokeWeight,controlPoints[i].z-zOffset,anchorPoints[i].x,anchorPoints[i].y+strokeWeight,anchorPoints[i].z-zOffset);
		}
		_mainApplet.endShape(PConstants.CLOSE);
	}
	
	private void drawUsingStrokeOffset (TherapeuticPresence _mainApplet) {
		_mainApplet.stroke(color,transparency);
		_mainApplet.noFill();
		for (float j=-strokeOffset; j<=strokeOffset;j+=strokeOffset) {
			_mainApplet.beginShape();
			for (int i=0; i<anchorPointsCounter-1; i++) {
				if (i==0) _mainApplet.vertex(anchorPoints[i].x,anchorPoints[i].y+j,anchorPoints[i].z-zOffset);
				_mainApplet.bezierVertex(controlPoints[i].x,controlPoints[i].y+j,controlPoints[i].z-zOffset,controlPoints[i].x,controlPoints[i].y+j,controlPoints[i].z-zOffset,anchorPoints[i+1].x,anchorPoints[i+1].y+j,anchorPoints[i+1].z-zOffset);
			}
			_mainApplet.endShape();
			if (strokeOffset == 0) break;
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
