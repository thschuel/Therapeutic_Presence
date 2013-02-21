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

package scenes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import therapeuticpresence.*;
import shapes3d.*;

public class HorizonScene extends BasicScene {
	
	// colors
	protected int backgroundTintColor = 0;
	protected int backgroundTintHueMax = 360;
	protected int backgroundTintHue = 207;
	protected int defaultBackgroundHighlightColor = 0x10ffffff;
	
	// birds in the sky
	public static final short NUMBER_BIRDS = 16; 
	protected Bird[] birds = new Bird[NUMBER_BIRDS];
	
	public HorizonScene (TherapeuticPresence _mainApplet, int _backgroundColor, AudioManager _audioManager) {
		super (_mainApplet,_backgroundColor,_audioManager);
		//makeBirds();
		animationSpeed=6f;
	}
	
	public void reset () {
		mainApplet.pushStyle();
		// change colors based on audio stream
		fftDCValue = audioManager.getMeanFFT(0);
		fftDCValueDelayed += (fftDCValue-fftDCValueDelayed)/audioReactionDelay;
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,100,audioManager.getMaxFFT(),100);
		backgroundTintColor = mainApplet.color(backgroundTintHue,100,fftDCValueDelayed,100);
		defaultBackgroundColor = PApplet.blendColor(backgroundTintColor,defaultBackgroundHighlightColor,PConstants.BLEND);
		backgroundTintColor = PApplet.blendColor(backgroundTintColor,alertColor,PConstants.BLEND);
		super.reset();
		// update texture and draw background
		//updateTexture();
		
		mainApplet.lights();
		mainApplet.ambientLight(backgroundTintHue,92,fftDCValueDelayed);
		mainApplet.lightSpecular(backgroundTintHue,92,fftDCValueDelayed);
		mainApplet.specular(backgroundTintColor);
		mainApplet.shininess(5.0f);
		mainApplet.colorMode(PApplet.RGB,255,255,255,100);
		mainApplet.noStroke();
		mainApplet.pushMatrix();
		mainApplet.fill(0,80,0,100);
		mainApplet.rect(-20000,-20000,40000,20000);
		mainApplet.translate(0f,0f,-20f);
		mainApplet.fill(0,100,200,100);
		mainApplet.rect(-20000,-100,40000,20100);
		mainApplet.popMatrix();
		for (int i=0; i<NUMBER_BIRDS; i++) {
			if (birds[i] != null)
				birds[i].draw(mainApplet);
		}
		mainApplet.popStyle();
	}
	
	private void makeBirds () {
		mainApplet.pushStyle();
		mainApplet.colorMode(PConstants.RGB,255,255,255,100);
		for (int i=0; i<NUMBER_BIRDS; i++) {
			float randX = 0f;
			float randY = 0f;
			do {
				randX = mainApplet.random(-3500f,3500f);
				randY = mainApplet.random(100f,3000f);
			} while (!checkCenterCoordinates(randX,randY));
			PVector center = new PVector(randX,randY,200f);
			float width = mainApplet.random(100f,500f);
			float height = mainApplet.random(width/3,width/3+width/7);
			float strokeWeight = mainApplet.random(3f,5f);
			int color = mainApplet.color(255,255,255,100);
			birds[i] = new Bird(center,width,height,strokeWeight,color);
		}
		mainApplet.popStyle();
	}
	
	private boolean checkCenterCoordinates (float _x, float _y) {
		boolean returnValue = true;
		for (int i=0; i<NUMBER_BIRDS; i++) {
			if (birds[i] != null) {
				float centerX = birds[i].center.x;
				float centerY = birds[i].center.y;
				if (((centerX < _x && _x-centerX < 200f) ||
					(centerX >= _x && centerX-_x < 200f)) &&
					((centerY < _y && _y-centerY < 100f) ||
					(centerY >= _y && centerY-_y < 100f))) {
					returnValue = false;
				}
			}
		}
		return returnValue;
	}
	
}

class Bird {
	public PVector center;
	private PVector[] anchorPoints = new PVector[3];
	private PVector[] controlPoints = new PVector[2];
	private float strokeWeight;
	private int color;
	
	public Bird (PVector _center, float _width, float _height, float _strokeWeight, int _color) {
		center = _center;
		strokeWeight = _strokeWeight;
		color = _color;

		anchorPoints[0] = new PVector (_center.x-_width/2,_center.y,_center.z);
		anchorPoints[1] = new PVector (_center.x,_center.y,_center.z);
		anchorPoints[2] = new PVector (_center.x+_width/2,_center.y,_center.z);
		controlPoints[0] = new PVector (_center.x-_width/4,_center.y+_height,_center.z);
		controlPoints[1] = new PVector (_center.x+_width/4,_center.y+_height,_center.z);
	}
	
	public void draw (PApplet _mainApplet) {
		_mainApplet.pushStyle();
		_mainApplet.noFill();
		_mainApplet.stroke(color);
		_mainApplet.strokeWeight(strokeWeight);
		_mainApplet.beginShape();
		_mainApplet.vertex(anchorPoints[0].x,anchorPoints[0].y,anchorPoints[0].z);
		_mainApplet.bezierVertex(controlPoints[0].x,controlPoints[0].y,controlPoints[0].z,
				 				 controlPoints[0].x,controlPoints[0].y,controlPoints[0].z,
				 				 anchorPoints[1].x,anchorPoints[1].y,anchorPoints[1].z);
		_mainApplet.bezierVertex(controlPoints[1].x,controlPoints[1].y,controlPoints[1].z,
				 				 controlPoints[1].x,controlPoints[1].y,controlPoints[1].z,
				 				 anchorPoints[2].x,anchorPoints[2].y,anchorPoints[2].z);
		_mainApplet.endShape();
		_mainApplet.popStyle();
	}
}
