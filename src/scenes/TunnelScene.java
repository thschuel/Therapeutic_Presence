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
import shapes3d.*;

public class TunnelScene extends BasicScene {
	
	protected Tube tunnelTube = null;
	public static float tunnelWidth = 4000f;
	public static float tunnelHeight = 3000f;
	public static float tunnelLength = 5000f;
	public static float tunnelShrink = 10f;
	public static float tunnelEntryZ = tunnelLength;
	
	// animation of texture, background colors are controlled by audio stream
	protected PGraphics textureWalls = null;
	protected PImage textureImg = null;
	protected float offsetTunnelEffect = 0;
	protected float offsetTunnelEffectMax = 0;
	protected final int horizontalLines = 2;
	protected final int verticalLines = 10;
	protected final int linesHeight = 256;
	protected final int linesWidth = 64;
	protected int odd=0;
	
	// colors
	protected int backgroundTintColor = 0;
	protected int backgroundTintHueMax = 360;
	protected int backgroundTintHue = 207;
	protected int defaultBackgroundHighlightColor = 0x10ffffff;
	
	public TunnelScene (TherapeuticPresence _mainApplet, int _backgroundColor, AudioManager _audioManager) {
		super (_mainApplet,_backgroundColor,_audioManager);
		// rotate and set up for third person view
		cameraZ = tunnelLength; // negative translation, because translation will be applied after rotation around Y
		tunnelTube = new Tube(mainApplet,8,60);
		// set radii of tube
		tunnelTube.setSize(tunnelWidth/2f,tunnelHeight/2f,(tunnelWidth/2f)/tunnelShrink,(tunnelWidth/2f)/tunnelShrink,tunnelLength);
		tunnelTube.rotateBy(-PConstants.PI/2,0,0);
		tunnelTube.z(tunnelLength/2f-1f);
		tunnelTube.visible(false,Tube.BOTH_CAP);
		textureWalls = mainApplet.createGraphics(verticalLines*linesWidth,horizontalLines*linesHeight,PConstants.P2D);
		textureImg = mainApplet.loadImage("../data/texture.png");
		offsetTunnelEffectMax = linesHeight; 
		
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
		updateTexture();
		
		mainApplet.lights();
		mainApplet.ambientLight(backgroundTintHue,92,fftDCValueDelayed);
		mainApplet.lightSpecular(backgroundTintHue,92,fftDCValueDelayed);
		mainApplet.specular(backgroundTintColor);
		mainApplet.shininess(5.0f);
		//tunnelTube.setTexture("../data/smoketex.jpg");
		tunnelTube.setTexture(textureWalls.get(),1,1);
		tunnelTube.drawMode(Shape3D.SOLID|Shape3D.TEXTURE);
		tunnelTube.draw();
		mainApplet.popStyle();
	}
	
	public static float getTunnelWidthAt (float _z) {
		if (tunnelEntryZ-_z < 0 || tunnelEntryZ-_z > tunnelLength) {
			return 0;
		} 
		return tunnelWidth/tunnelShrink + _z/tunnelLength * (tunnelWidth-tunnelWidth/tunnelShrink);
	}
	
	public static float getTunnelHeightAt (float _z) {
		if (tunnelEntryZ-_z < 0 || tunnelEntryZ-_z > tunnelLength) {
			return 0;
		} 
		return tunnelHeight/tunnelShrink + _z/tunnelLength * (tunnelHeight-tunnelHeight/tunnelShrink);
	}
	
	private void updateTexture () {
		// animation
		if (offsetTunnelEffect>0) offsetTunnelEffect-=linesHeight/(mainApplet.frameRate*AbstractScene.animationSpeed);
		else { 
			offsetTunnelEffect = linesHeight-1;
			odd=(odd+1)%2;
		}
		// assemble texture
		textureWalls.colorMode(PConstants.HSB,backgroundTintHueMax,100,audioManager.getMaxFFT(),100);
		textureWalls.noStroke();
		textureWalls.beginDraw();
//		textureWalls.tint(255f,(fftDCValueDelayed/audioManager.getMaxFFT())*255f);
//		textureWalls.image(textureImg,0,0,textureWalls.width,textureWalls.height);
		int colorCode=0;
		for (int i=0;i<verticalLines;i++) {
			if (i>=verticalLines/2) {
				colorCode=207;
			} else {
				colorCode=20;
			}
			for (int j=0; j<horizontalLines;j++) {
				backgroundTintColor = textureWalls.color(colorCode,80f+((i+j+odd)%2)*20f,fftDCValueDelayed,100);
				backgroundTintColor = PApplet.blendColor(backgroundTintColor,alertColor,PConstants.BLEND);
				textureWalls.fill(backgroundTintColor,50f);
				textureWalls.rect(i*linesWidth,j*linesHeight+PApplet.round(offsetTunnelEffect),linesWidth,linesHeight);
				
				if (j==0) {
					backgroundTintColor = textureWalls.color(colorCode,80f+((i+j+1+odd)%2)*20f,fftDCValueDelayed,100);
					backgroundTintColor = PApplet.blendColor(backgroundTintColor,alertColor,PConstants.BLEND);
					textureWalls.fill(backgroundTintColor,50f);
					textureWalls.rect(i*linesWidth,0,linesWidth,PApplet.round(offsetTunnelEffect));
				}
			}
		}
		textureWalls.endDraw();
	}
	
}
