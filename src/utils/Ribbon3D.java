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

//M_1_6_02_TOOL.pde
//Agent.pde, GUI.pde, Ribbon3d.pde, TileSaver.pde
//
//Generative Gestaltung, ISBN: 978-3-87439-759-9
//First Edition, Hermann Schmidt, Mainz, 2009
//Hartmut Bohnacker, Benedikt Gross, Julia Laub, Claudius Lazzeroni
//Copyright 2009 Hartmut Bohnacker, Benedikt Gross, Julia Laub, Claudius Lazzeroni
//
//http://www.generative-gestaltung.de
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package utils;

import processing.core.*;


public class Ribbon3D {
	private int count; // how many points has the ribbon
	private PVector[] p;
	private boolean[] isGap;
	
	public Ribbon3D (PVector theP, int theCount) {
		count = theCount; 
		p = new PVector[count];
		isGap = new boolean[count];
		for(int i=0; i<count; i++) {
			p[i] = new PVector(theP.x,theP.y,theP.z);
			isGap[i] = false;
		}
	}
	
	public void update(PVector theP, boolean theIsWraped){
		// shift the values to the right side
		for(int i=count-1; i>0; i--) {
			p[i].set(p[i-1]);
			isGap[i] = isGap[i-1];
		}
		p[0].set(theP);
		isGap[0] = theIsWraped;
	}
	
	public void drawMeshRibbon(PApplet _mainApplet, int theMeshCol, float theWidth) {
		_mainApplet.pushStyle();
		// draw the ribbons with meshes
		_mainApplet.fill(theMeshCol);
		_mainApplet.noStroke();
	
		_mainApplet.beginShape(PConstants.QUAD_STRIP);
		for(int i=0; i<count-1; i++) {
			// if the point was wraped -> finish the mesh an start a new one
			if (isGap[i] == true) {
				//_mainApplet.vertex(p[i].x, p[i].y, p[i].z);
				_mainApplet.vertex(p[i].x, p[i].y, p[i].z);
				_mainApplet.endShape();
				_mainApplet.beginShape(PConstants.QUAD_STRIP);
			} else {        
				PVector v1 = PVector.sub(p[i],p[i+1]);
				PVector v2 = PVector.add(p[i+1],p[i]);
				PVector v3 = v1.cross(v2);      
				v2 = v1.cross(v3);
				v2.normalize();
				v2.mult(theWidth);
				_mainApplet.vertex(p[i].x+v2.x,p[i].y+v2.y,p[i].z+v2.z);
				_mainApplet.vertex(p[i].x-v2.x,p[i].y-v2.y,p[i].z-v2.z);
			}

		}
		_mainApplet.endShape();
		_mainApplet.popStyle();
	}
	
	
	public void drawLineRibbon(PApplet _mainApplet, int theStrokeCol, float theWidth) {
		_mainApplet.pushStyle();
		// draw the ribbons with lines
		_mainApplet.noFill();
		_mainApplet.strokeWeight(theWidth);
		_mainApplet.stroke(theStrokeCol);
		_mainApplet.beginShape();
		for(int i=0; i<count; i++) {
			_mainApplet.vertex(p[i].x, p[i].y, p[i].z);
			// if the point was wraped -> finish the line an start a new one
			if (isGap[i] == true) {
				_mainApplet.endShape();
				_mainApplet.beginShape();
			} 
		}
		_mainApplet.endShape();
		_mainApplet.popStyle();
	}
}

