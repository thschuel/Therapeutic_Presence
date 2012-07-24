package utils;
import processing.core.PApplet;
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
	private Ribbon3D ribbon;
	private int col;
	private float strokeW;
	private float minStroke = 1;
	private float maxStroke = 3;
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
	    ribbon = new Ribbon3D(p, (int)mainApplet.random(50, 150));
	
	    float r = mainApplet.random(1.0f);
	    if (r < 0.4) col = mainApplet.color(mainApplet.random(190,200),mainApplet.random(80,100),mainApplet.random(50,70));
	    else if (r < 0.5) col = mainApplet.color(52,100,mainApplet.random(50,80));
	    else col = mainApplet.color(273,mainApplet.random(50,80),mainApplet.random(40,60));
	
	    strokeW = mainApplet.random(1.0f);
	}

	public void update1(float angleY, float angleZ, float width, float height){ 
	    /* convert polar to cartesian coordinates
	     stepSize is distance of the point to the last point
	     angleY is the angle for rotation around y-axis
	     angleZ is the angle for rotation around z-axis
	     */
	    p.x += PApplet.cos(startAngle+angleZ) * PApplet.cos(startAngle+angleY) * stepSize;
	    p.y += PApplet.sin(startAngle+angleZ) * stepSize;
	    p.z += PApplet.cos(startAngle+angleZ) * PApplet.sin(startAngle+angleY) * stepSize;

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
  		ribbon.drawLineRibbon(mainApplet,col,PApplet.map(strokeW,0,1,minStroke,maxStroke));
  	}

	private void setToStartPostition() {
	    p.x=startX;
	    p.y=startY;
	    p.z=0;
	}
}
