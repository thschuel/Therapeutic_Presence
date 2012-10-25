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
import therapeuticpresence.*;
import utils.GridSolver;
import shapes3d.*;

public class LiquidScene extends BasicScene {
	
	protected int backgroundTintColor = 0;
	protected int backgroundTintHueMax = 360;
	protected int backgroundTintHue = 207;
	protected int defaultBackgroundHighlightColor = 0x10ffffff;

	private float angle=0;
	private float angleThreshold = 45f;
	private float lightPosX=0;
	private float lightPosY=0;
	private Ellipsoid ellipsoid;
	
	public LiquidScene (TherapeuticPresence _mainApplet, int _backgroundColor, AudioManager _audioManager) {
		super (_mainApplet,_backgroundColor,_audioManager);
		// set up for third person view
		cameraZ = TherapeuticPresence.cameraEyeZ;

		ellipsoid = new Ellipsoid(mainApplet,20,20);
		ellipsoid.setTexture("../data/smoketex.jpg");
		ellipsoid.setRadius(10000f,10000f,1000f);
		ellipsoid.moveTo(0,0,-1000f);
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,100,100,100);
		ellipsoid.fill(mainApplet.color(207,92,100,100));
		ellipsoid.drawMode(Shape3D.SOLID|Shape3D.TEXTURE);
		
		animationSpeed = 2f;
	}
	
	public void reset () {
		mainApplet.pushStyle();
		// change colors based on audio stream
		fftDCValue = audioManager.getMeanFFT(0);
		fftDCValueDelayed += (fftDCValue-fftDCValueDelayed)/audioReactionDelay;
		mainApplet.colorMode(PApplet.HSB,backgroundTintHueMax,100,audioManager.getMaxFFT()/3f,100);
		backgroundTintColor = mainApplet.color(backgroundTintHue,100,fftDCValueDelayed,100);
		defaultBackgroundColor = PApplet.blendColor(backgroundTintColor,defaultBackgroundHighlightColor,PConstants.BLEND);
		backgroundTintColor = PApplet.blendColor(backgroundTintColor,alertColor,PConstants.BLEND);
		super.reset();
  
		// animation of lighting to elicit symmetric movements
		lightPosX = PApplet.cos(PApplet.radians(angle)) * 6000f;
		lightPosY = PApplet.sin(PApplet.radians(angle)) * 6000f;
		if (angle < angleThreshold) {
			angle+=angleThreshold/(mainApplet.frameRate*animationSpeed);
			if (angle >= angleThreshold) angleThreshold=-angleThreshold;
		}
		if (angle > angleThreshold) {
			angle-=angleThreshold/(mainApplet.frameRate*animationSpeed);
			if (angle <= angleThreshold) angleThreshold=-angleThreshold;
		}


		mainApplet.lights();
		mainApplet.directionalLight(backgroundTintHue,92,fftDCValueDelayed,lightPosX,lightPosY,6000f);
		mainApplet.spotLight(backgroundTintHue,92,5f,0f,0f,0f,lightPosX,lightPosY,2000f,2f,10f);
		mainApplet.directionalLight(backgroundTintHue,92,fftDCValueDelayed,-lightPosX,lightPosY,6000f);
		mainApplet.spotLight(backgroundTintHue,92,5f,0f,0f,0f,-lightPosX,lightPosY,2000f,2f,10f);
		//mainApplet.directionalLight(backgroundTintHue,92,fftDCValueDelayed,-lightPosX,-lightPosY,6000f);
		mainApplet.lightSpecular(backgroundTintHue,92,fftDCValueDelayed);
		mainApplet.specular(backgroundTintColor);
		mainApplet.shininess(5.0f);
		ellipsoid.draw();
		mainApplet.popStyle();
	}

	public short getSceneType() {
		return TherapeuticPresence.LIQUID_SCENE;
	}
}
