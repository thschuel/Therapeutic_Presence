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
import therapeuticpresence.PostureProcessing;
import therapeuticpresence.TherapeuticPresence;

public class BasicScene2D extends AbstractScene {
	protected int alertColor;
	protected float activeTime = 0f;
	protected final float alertFadeOutTime = 1f; // seconds to fade out
	protected final float timeToFullAlert = PostureProcessing.timeHoldShapeToTrigger; // seconds to fully display alert
	
	public BasicScene2D (TherapeuticPresence _mainApplet, int _backgroundColor) {
		super(_mainApplet,_backgroundColor);
		mainApplet.colorMode(PConstants.RGB,255,255,255,100);
		alertColor = mainApplet.color(255,0,0,100);
	}

	public void reset() {
		// apply alert
		if (activeTime > timeToFullAlert) activeTime = timeToFullAlert;
		int newAlpha = PApplet.round(100f*activeTime/timeToFullAlert);
		alertColor = (alertColor&0xffffff) | newAlpha<<24;
		backgroundColor=PApplet.blendColor(defaultBackgroundColor,alertColor,PConstants.BLEND);
		// reset the scene
		mainApplet.background(backgroundColor);
		mainApplet.camera(); // reset the camera for 2d drawing
		// fade out alert
		activeTime -= alertFadeOutTime/mainApplet.frameRate;
		if (activeTime<0f) activeTime=0f;
	}

	public short getSceneType() {
		return TherapeuticPresence.BASIC_SCENE2D;
	}

	public boolean sceneIs3D() {
		return false;
	}
	
	public void shapeActiveAlert (float _activeTime) {
		activeTime = _activeTime;
	}
}
