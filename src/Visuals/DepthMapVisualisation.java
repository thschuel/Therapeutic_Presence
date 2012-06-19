package Visuals;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.*;

public class DepthMapVisualisation extends Visualisation {

	protected SimpleOpenNI kinect;
	
	public DepthMapVisualisation (PApplet _mainApplet, SimpleOpenNI _kinect) {
		super(_mainApplet);
		kinect = _kinect;
	}

	public void setup() {
	}

	public void reset() {
		// reset the scene
		mainApplet.background(backgroundColor);
		// set the camera to the position of the kinect, facing towards the scene
		mainApplet.camera(0,0,0,0,0,1,0,1,0);
	}

	public void draw() {
		// rotate the scene: kinect data comes upside down!
		mainApplet.pushMatrix();
		mainApplet.rotateX(rotX);
		mainApplet.rotateY(rotY);
		mainApplet.rotateZ(rotZ);
		mainApplet.translate(translateX,translateY,translateZ);
		
		int[] depthMap = kinect.depthMap();
		PVector[] realWorldDepthMap = kinect.depthMapRealWorld();
		int h = kinect.depthHeight();
		int w = kinect.depthWidth();
		int stepSize = 3;  // to speed up the drawing, draw every third point
		int index;

		mainApplet.stroke(70,70,70); 
		for(int y=0; y<h; y+=stepSize) {
			for(int x=0; x<w; x+=stepSize) {
				index=x+y*w;
				if(depthMap[index] > 0) { 
					// draw the projected point
					mainApplet.point(realWorldDepthMap[index].x,realWorldDepthMap[index].y,realWorldDepthMap[index].z);
				}
			} 
		}

		mainApplet.popMatrix();
	}

}
