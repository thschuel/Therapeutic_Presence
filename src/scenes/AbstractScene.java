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

import therapeuticpresence.TherapeuticPresence;

public abstract class AbstractScene {
	protected TherapeuticPresence mainApplet = null;
	protected int backgroundColor = 0;
	protected int defaultBackgroundColor;

	public static float animationSpeed = 6f; // seconds
	
	public AbstractScene (TherapeuticPresence _mainApplet, int _backgroundColor) {
		mainApplet = _mainApplet;
		backgroundColor = _backgroundColor;
		defaultBackgroundColor = _backgroundColor;
	}
	
	public abstract void reset ();
	public abstract short getSceneType ();
	public abstract boolean sceneIs3D ();
	public abstract void shapeActiveAlert (float _time);
	
	public void setBackgroundColor (int _backgroundColor) {
		backgroundColor = _backgroundColor;
	}
	public int getBackgroundColor () {
		return backgroundColor;
	}
}
