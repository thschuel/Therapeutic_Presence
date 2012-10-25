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


// M_1_6_02_TOOL.pde
// Agent.pde, GUI.pde, Ribbon3d.pde, TileSaver.pde
// 
// Generative Gestaltung, ISBN: 978-3-87439-759-9
// First Edition, Hermann Schmidt, Mainz, 2009
// Hartmut Bohnacker, Benedikt Gross, Julia Laub, Claudius Lazzeroni
// Copyright 2009 Hartmut Bohnacker, Benedikt Gross, Julia Laub, Claudius Lazzeroni
//
// http://www.generative-gestaltung.de
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.package utils;

public class Agent {

	private boolean isOutside = false;
	private PVector p;
	private float stepSize;
	private Ribbon ribbon;
	private int col;
	private float strokeW;
	private float minStroke = 8;
	private float maxStroke = 14;
	private float startX, startY, startAngle;
	private PApplet mainApplet;

	public Agent(PApplet _mainApplet, float _startX, float _startY, float _startAngle) {
	    mainApplet=_mainApplet;
	    startX=_startX;
	    startY=_startY;
	    startAngle=_startAngle;
		p = new PVector(0, 0, 0);
	    setToStartPostition();
	    stepSize = mainApplet.random(5, 20);
	    // how many points has the ribbon
	    ribbon = new Ribbon(p, (int)mainApplet.random(50, 150));
	    mainApplet.colorMode(PConstants.HSB,360,100,100,100);
	    float r = mainApplet.random(1.0f);
	    if (r < 0.4) col = mainApplet.color(mainApplet.random(190,200),mainApplet.random(80,100),mainApplet.random(50,70));
	    else if (r < 0.5) col = mainApplet.color(52,100,mainApplet.random(50,80));
	    else col = mainApplet.color(273,mainApplet.random(50,80),mainApplet.random(40,60));
	
	    strokeW = mainApplet.random(1.0f);
	}

	public void update1(float angleLeftToY, float angleRightToY, float angleLeftToZ, float angleRightToZ, float width, float height){ 
	    /* convert polar to cartesian coordinates
	     stepSize is distance of the point to the last point
	     angleY is the angle for rotation around y-axis
	     angleZ is the angle for rotation around z-axis
	     */
	    p.x += PApplet.cos(startAngle) * PApplet.cos(angleRightToY) */* PApplet.cos(angleY) */ stepSize;
	    p.y += PApplet.sin(startAngle) * PApplet.sin(angleRightToY) */* PApplet.sin(angleY) */ stepSize;
	    p.z += PApplet.cos(angleLeftToZ) * PApplet.sin(angleRightToZ) * stepSize;

    	// boundingbox
    	if (p.x<-width/2 || p.x>width/2 ||
    		p.y<-height/2 || p.y>height/2 ||
      		p.z<-400 || p.z>400) {
    		setToStartPostition();
      		isOutside = true;
    	}

    	// create ribbons
    	ribbon.update(p,isOutside);
    	isOutside = false;
  	}

  	public void draw() {
  		//ribbon.drawMeshRibbon(mainApplet,col,PApplet.map(strokeW,0,1,1,12));
  		ribbon.drawMeshRibbon(mainApplet,col,PApplet.map(strokeW,0,1,minStroke,maxStroke));
  	}

	private void setToStartPostition() {
	    p.x=startX;
	    p.y=startY;
	    p.z=0;
	}
}
