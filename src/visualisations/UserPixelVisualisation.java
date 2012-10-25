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

package visualisations;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.*;
import therapeuticpresence.TherapeuticPresence;

public class UserPixelVisualisation extends DepthMapVisualisation {
	protected int userId;
	protected int color;
	
	public UserPixelVisualisation (TherapeuticPresence _mainApplet, SimpleOpenNI _kinect, int _userId) {
		super(_mainApplet,_kinect);
		userId = _userId;
	}

	public void setup() {
		mainApplet.colorMode(PConstants.HSB,360,100,100,100);
		color=mainApplet.color(mainApplet.random(360),100,100,100);
	}
	
	public int getColor () {
		return color;
	}

	public void draw() {

		int[] userPixels=kinect.getUsersPixels(SimpleOpenNI.USERS_ALL);
		if(userPixels != null) {
			PVector[] realWorldDepthMap = kinect.depthMapRealWorld();
			int h = kinect.depthHeight();
			int w = kinect.depthWidth();
			int stepSize = 3;  // to speed up the drawing, draw every third point
			int index;
			
			mainApplet.stroke(color); 
			for(int y=0; y<h; y+=stepSize) {
				for(int x=0; x<w; x+=stepSize) {
					index=x+y*w;
					if(userPixels[index] == userId) { 
						// draw the projected point
						mainApplet.point(realWorldDepthMap[index].x,realWorldDepthMap[index].y,realWorldDepthMap[index].z);
					}
				} 
			}
		}
	}

	public short getVisualisationType() {
		return TherapeuticPresence.USER_PIXEL_VISUALISATION;
	}
}
