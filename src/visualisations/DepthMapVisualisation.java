package visualisations;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.*;
import therapeuticpresence.TherapeuticPresence;

public class DepthMapVisualisation extends AbstractVisualisation {

	protected SimpleOpenNI kinect;
	
	public DepthMapVisualisation (TherapeuticPresence _mainApplet, SimpleOpenNI _kinect) {
		super(_mainApplet);
		mainApplet.setMirrorKinect(true);
		kinect = _kinect;
	}

	public void setup() {
	}

	public void draw() {
		
		int[] depthMap = kinect.depthMap();
		PVector[] realWorldDepthMap = kinect.depthMapRealWorld();
		int h = kinect.depthHeight();
		int w = kinect.depthWidth();
		int stepSize = 3;  // to speed up the drawing, draw every third point
		int index;

		mainApplet.colorMode(PConstants.RGB,255,255,255,100);
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
	}

	public short getVisualisationType() {
		return TherapeuticPresence.DEPTHMAP_VISUALISATION;
	}
}
